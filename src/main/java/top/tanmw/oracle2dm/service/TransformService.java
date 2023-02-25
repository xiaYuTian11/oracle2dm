package top.tanmw.oracle2dm.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.tanmw.oracle2dm.constants.DbConstant;
import top.tanmw.oracle2dm.dao.DmDao;
import top.tanmw.oracle2dm.dao.OracleDao;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author TMW
 * @since 2023/2/25 12:23
 */
@Service
@Slf4j
public class TransformService {

    private static final Integer BATCH_SAVE_SIZE = 10000;
    private static final Integer PAGE_SIZE = 500000;

    private static final List<String> IDENTITY_LIST = new ArrayList<String>() {{
        add("GBP_USER");
    }};

    @Autowired
    private OracleDao oracleDao;
    @Autowired
    private DmDao dmDao;

    public List<Map<String, Object>> queryAll() throws Exception {
        List<String> oracleAllTable = oracleDao.findAllTable();
        List<String> dmAllTable = dmDao.findAllTable();
        Collection<String> intersection = CollUtil.intersection(oracleAllTable, dmAllTable);
        log.info("交集：{}", String.join(",", intersection));
        intersection = intersection.stream().sorted().collect(Collectors.toList());

        Collection<String> subtract = CollUtil.subtract(oracleAllTable, dmAllTable);
        log.info("oracle 中有但是 dm 中没有的表：{}", String.join(",", subtract));

        Collection<String> subtractDm = CollUtil.subtract(dmAllTable, oracleAllTable);
        log.info("dm 中有但是 oracle 中没有的表：{}", String.join(",", subtractDm));

        long startTime = System.currentTimeMillis();
        this.executor(intersection, (tableName) -> {
            if (!StrUtil.equalsIgnoreCase("GBP_USER", tableName)) {
                return;
            }
            Integer delete = dmDao.delete(tableName);
            log.info("删除Dm数据库中{}数据:{}条", tableName, delete);

            Integer count = oracleDao.countByTableName(tableName);
            log.info(String.format("查询到%s中数据:%s条", tableName, count));
            if (count <= 0) {
                return;
            }
            List<Map<String, Object>> mapList = oracleDao.queryByTableName(tableName);
            List<Record> recordList = new ArrayList<>(mapList.size());
            log.info("开始转换模型：{}", tableName);
            mapList.forEach(map -> {
                Record record = new Record();
                record.setColumns(map);
                recordList.add(record);
            });
            if (IDENTITY_LIST.contains(tableName.toUpperCase())) {
                Db.use(DbConstant.DM).update("SET IDENTITY_INSERT " + tableName + " ON");
                Db.use(DbConstant.DM).batchSave(tableName, recordList, 10000);
                Db.use(DbConstant.DM).update("SET IDENTITY_INSERT " + tableName + " OFF");
            } else {
                Db.use(DbConstant.DM).batchSave(tableName, recordList, 10000);
            }
            log.info("往Dm数据库中{}插入数据：{}", tableName, recordList.size());
        });

        long endTime = System.currentTimeMillis();
        log.info("总计耗时：{}", endTime - startTime);
        return null;
    }

    private void executor(Collection<String> list, Consumer<String> consumer) throws Exception {
        // CountDownLatch cd = new CountDownLatch(list.size());
        for (String tableName : list) {
            // ThreadUtil.EXECUTOR_SERVICE.execute(() -> {
            consumer.accept(tableName);
            // cd.countDown();
            // });
        }
        // cd.await();
    }
}
