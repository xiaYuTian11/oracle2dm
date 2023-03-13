package top.tanmw.oracle2dm.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.tanmw.oracle2dm.config.DbConfig;
import top.tanmw.oracle2dm.constants.DbConstant;
import top.tanmw.oracle2dm.dao.DmDao;
import top.tanmw.oracle2dm.dao.OracleDao;
import top.tanmw.oracle2dm.utils.ThreadUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author TMW
 * @since 2023/2/25 12:23
 */
@Service
@Slf4j
public class TransformService {

    public static Integer BATCH_SAVE_SIZE;
    private static Integer PAGE_SIZE;
    private static Integer NEED_PAGE;
    private static boolean CHANGE_FIELD_LENGTH;
    @Value("${com.efficient.transform-file}")
    private String transformFilePath;
    @Value("${com.efficient.transform-skip:true}")
    private boolean transformSkip;

    @Value("${com.efficient.batch-save-size}")
    public void setBatchSaveSize(Integer batchSaveSize) {
        TransformService.BATCH_SAVE_SIZE = batchSaveSize;
    }

    @Value("${com.efficient.page-size}")
    public void setPageSize(Integer pageSize) {
        TransformService.PAGE_SIZE = pageSize;
    }

    @Value("${com.efficient.change-field-length:true}")
    public void setChangeFieldLength(boolean changeFieldLength) {
        TransformService.CHANGE_FIELD_LENGTH = changeFieldLength;
    }

    @Value("${com.efficient.need-page}")
    public void setNeedPage(Integer needPage) {
        TransformService.NEED_PAGE = needPage;
    }

    @Autowired
    private DbConfig dbConfig;
    @Autowired
    private OracleDao oracleDao;
    @Autowired
    private DmDao dmDao;

    private DbPro db;

    public boolean queryAll() throws Exception {
        db = Db.use(DbConstant.DM);

        // 初始化文件
        File file = new File(transformFilePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }

        List<String> readLines = FileUtil.readLines(file, StandardCharsets.UTF_8);
        List<String> successTable = new ArrayList<>();
        readLines.forEach(readLine -> successTable.add(readLine.split(":")[1]));
        List<String> skipList = dbConfig.getSkipList();
        if (transformSkip) {
            skipList.addAll(successTable);
        }

        List<String> oracleAllTable = oracleDao.findAllTable();
        List<String> dmAllTable = dmDao.findAllTable();
        List<String> intersection = CollUtil.intersection(oracleAllTable, dmAllTable)
                .stream().sorted().collect(Collectors.toList());
        intersection = intersection.stream().filter(tableName -> !skipList.contains(tableName.toUpperCase()))
                .sorted().collect(Collectors.toList());
        log.info("交集：{}", String.join(",", intersection));

        Collection<String> subtract = CollUtil.subtract(oracleAllTable, dmAllTable);
        log.info("oracle 中有但是 dm 中没有的表：{}", String.join(",", subtract));

        Collection<String> subtractDm = CollUtil.subtract(dmAllTable, oracleAllTable);
        log.info("dm 中有但是 oracle 中没有的表：{}", String.join(",", subtractDm));

        intersection.forEach(tableName -> {
            Integer delete = dmDao.delete(tableName);
            log.info("删除Dm数据库中{}数据:{}条", tableName, delete);
        });

        // 修改数据库长度
        if (CHANGE_FIELD_LENGTH) {
            dbConfig.getUpdateFiledLengthMap().forEach((k, v) -> {
                String[] split = v.split(",");
                for (String str : split) {
                    try {
                        db.update(String.format("alter table %s modify %s VARCHAR2(8000)", k, str));
                    } catch (Exception e) {
                        log.error("修改字符串长度失败：{} - {}", k, str);
                    }
                }
            });
        }

        ReentrantLock lock = new ReentrantLock();

        Date startDate = new Date();
        AtomicBoolean needPage = new AtomicBoolean(false);
        int size = intersection.size();
        List<String> finalIntersection = intersection;
        this.executor(intersection, (tableName) -> {
            int indexOf = finalIntersection.indexOf(tableName);
            log.info("当前查询表：{},进度为：{}", tableName, indexOf + "/" + size);

            Integer count = oracleDao.countByTableName(tableName);
            log.info(String.format("查询到%s中数据:%s条", tableName, count));
            if (count <= 0) {
                return;
            }

            if (count > NEED_PAGE) {
                needPage.set(true);
            }

            AtomicBoolean removeR = new AtomicBoolean(false);

            if (needPage.get()) {
                // 查询数据库表主键
                String constraint = oracleDao.findConstraintByP(String.format("'%s'", tableName));
                if (StrUtil.isBlank(constraint)) {
                    constraint = oracleDao.findConstraintByOther(String.format("'%s'", tableName));
                }

                if (StrUtil.isBlank(constraint)) {
                    List<Map<String, Object>> mapList = oracleDao.queryByTableName(tableName);
                    this.save2Dm(tableName, mapList, removeR.get());
                } else {
                    removeR.set(true);
                    int page = ((count - 1) / PAGE_SIZE) + 1;
                    for (int i = 0; i < page; i++) {
                        // if (i >= 1) {
                        //     continue;
                        // }
                        log.info("分页查询{}数据:{}", tableName, i * PAGE_SIZE + "--" + (i + 1) * PAGE_SIZE);
                        List<Map<String, Object>> mapList = oracleDao.queryByTableNameOrderBy(tableName, constraint, i * PAGE_SIZE, (i + 1) * PAGE_SIZE);
                        this.save2Dm(tableName, mapList, removeR.get());
                    }
                }
            } else {
                List<Map<String, Object>> mapList = oracleDao.queryByTableName(tableName);
                this.save2Dm(tableName, mapList, removeR.get());
            }

            this.writeResult(tableName, file, lock);
        });

        Date endDate = new Date();
        DateUtil.between(startDate, endDate, DateUnit.MINUTE);
        log.info("总计耗时： {}m", DateUtil.between(startDate, endDate, DateUnit.MINUTE));
        return true;
    }

    private void save2Dm(String tableName, List<Map<String, Object>> mapListAll, boolean removeR) {
        List<Record> recordList = new ArrayList<>(mapListAll.size());
        mapListAll.forEach(map -> {
            Record record = new Record();
            record.setColumns(map);
            if (removeR) {
                record.remove("R");
            }
            recordList.add(record);
        });
        if (dbConfig.getIdentityList().contains(tableName.toUpperCase())) {
            db.update("SET IDENTITY_INSERT " + tableName + " ON");
            db.batchSave(tableName, recordList, BATCH_SAVE_SIZE);
            db.update("SET IDENTITY_INSERT " + tableName + " OFF");
        } else {
            db.batchSave(tableName, recordList, BATCH_SAVE_SIZE);
        }
        log.info("往Dm数据库中{}插入数据：{}", tableName, recordList.size());
    }

    private void executor(Collection<String> list, Consumer<String> consumer) throws Exception {
        CountDownLatch cd = new CountDownLatch(list.size());
        for (String tableName : list) {
            CopyOnWriteArrayList<String> errorList = new CopyOnWriteArrayList<>();
            ThreadUtil.EXECUTOR_SERVICE.execute(() -> {
                try {
                    consumer.accept(tableName);
                } catch (Exception e) {
                    log.error("传输异常的表: " + tableName, e);
                    errorList.add(tableName);
                } finally {
                    cd.countDown();
                }
            });
        }
        cd.await();
    }

    public void writeResult(String tableName, File file, ReentrantLock lock) {
        lock.lock();
        try {
            FileUtil.appendUtf8String(String.format("SUCCESS:%s\n", tableName), file);
        } finally {
            lock.unlock();
        }
    }
}
