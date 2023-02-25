package top.tanmw.oracle2dm.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.tanmw.oracle2dm.constants.DbConstant;
import top.tanmw.oracle2dm.dao.DmDao;
import top.tanmw.oracle2dm.dao.OracleDao;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

    private static final List<String> SKIP_LIST = new ArrayList<String>() {{
        add("DICTTREE_LIB");
        add("DICTT_LIBMAPPING");
        add("DICT_CODE_LIB");
        add("LIB_MANAGER");
    }};

    private static final List<String> IDENTITY_LIST = new ArrayList<String>() {{
        add("GBP_USER");
    }};

    private static final List<String> UPDATE_FILED_LENGTH = new ArrayList<String>() {{
        add("alter table \"DAIMA\" modify \"DWMC\" VARCHAR2(2000)");
        add("alter table \"DAIMA\" modify \"YDBM\" VARCHAR2(2000)");
        add("alter table \"ZJ_WJYS\" modify \"WJMC\" VARCHAR2(2000)");
        add("alter table \"DFPBZLX\" modify \"DFPBZLX_DESP\" VARCHAR2(2000)");
        add("alter table \"GBP_USER_BEFORMD5\" modify \"REALNAME\" VARCHAR2(2000)");
        add("alter table \"JDXZ_BD\" modify \"BEIZHU\" VARCHAR2(2000)");
        add("alter table \"JJJ\" modify \"YDBM\" VARCHAR2(2000)");
        add("alter table \"LOCAL_BD\" modify \"BEIZHU\" VARCHAR2(2000)");
        add("alter table \"LXFS\" modify \"LXFS_LXR\" VARCHAR2(2000)");
        add("alter table \"REPORT_JGSY_COMMON\" modify \"JGSY_NAME\" VARCHAR2(2000)");
        add("alter table \"REPORT_SY\" modify \"INCORPORATOR\" VARCHAR2(2000)");
        add("alter table \"REPORT_T_BWRY_JBXX\" modify \"XM\" VARCHAR2(2000)");
        add("alter table \"SY\" modify \"INCORPORATOR\" VARCHAR2(2000)");
        add("alter table \"ZHXSH\" modify \"DESP\" VARCHAR2(2000)");
    }};

    @Autowired
    private OracleDao oracleDao;
    @Autowired
    private DmDao dmDao;

    public List<Map<String, Object>> queryAll() throws Exception {
        DbPro db = Db.use(DbConstant.DM);

        List<String> oracleAllTable = oracleDao.findAllTable();
        List<String> dmAllTable = dmDao.findAllTable();
        Collection<String> intersection = CollUtil.intersection(oracleAllTable, dmAllTable);
        log.info("交集：{}", String.join(",", intersection));
        intersection = intersection.stream().filter(tableName -> !SKIP_LIST.contains(tableName.toUpperCase()))
                .sorted().collect(Collectors.toList());

        Collection<String> subtract = CollUtil.subtract(oracleAllTable, dmAllTable);
        log.info("oracle 中有但是 dm 中没有的表：{}", String.join(",", subtract));

        Collection<String> subtractDm = CollUtil.subtract(dmAllTable, oracleAllTable);
        log.info("dm 中有但是 oracle 中没有的表：{}", String.join(",", subtractDm));

        // 修改数据库长度
        UPDATE_FILED_LENGTH.forEach(sql -> {
            try {
                db.update(sql);
            } catch (Exception e) {
                log.error("修改字符串长度失败：{}", sql);
            }
        });

        long startTime = System.currentTimeMillis();
        AtomicBoolean flag = new AtomicBoolean(false);
        this.executor(intersection, (tableName) -> {
            if (!flag.get()) {
                if (!StrUtil.equalsIgnoreCase("GBP_MENU_SANYUAN", tableName)) {
                    return;
                } else {
                    flag.set(true);
                }
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
                db.update("SET IDENTITY_INSERT " + tableName + " ON");
                db.batchSave(tableName, recordList, 10000);
                db.update("SET IDENTITY_INSERT " + tableName + " OFF");
            } else {
                db.batchSave(tableName, recordList, 10000);
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
