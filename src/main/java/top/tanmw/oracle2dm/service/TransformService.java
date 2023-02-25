package top.tanmw.oracle2dm.service;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.tanmw.oracle2dm.dao.DmDao;
import top.tanmw.oracle2dm.dao.OracleDao;
import top.tanmw.oracle2dm.utils.ThreadUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * @author TMW
 * @since 2023/2/25 12:23
 */
@Service
@Slf4j
public class TransformService {

    @Autowired
    private OracleDao oracleDao;
    @Autowired
    private DmDao dmDao;

    public List<Map<String, Object>> queryAll() throws Exception {
        List<String> oracleAllTable = oracleDao.findAllTable();
        List<String> dmAllTable = dmDao.findAllTable();
        Collection<String> intersection = CollUtil.intersection(oracleAllTable, dmAllTable);
        log.info("交集：{}", String.join(",", intersection));

        Collection<String> subtract = CollUtil.subtract(oracleAllTable, dmAllTable);
        log.info("oracle 中有但是 dm 中没有的表：{}", String.join(",", subtract));

        Collection<String> subtractDm = CollUtil.subtract(dmAllTable, oracleAllTable);
        log.info("dm 中有但是 oracle 中没有的表：{}", String.join(",", subtractDm));

        long startTime = System.currentTimeMillis();
        this.executor(intersection, (tableName) -> {
            Integer count = oracleDao.countByTableName(tableName);
            log.info(String.format("查询到%s中数据%s条", tableName, count));
            // List<Map<String, Object>> tableNameList = oracleDao.queryByTableName(tableName);
            // log.info(String.format("查询到%s中数据%s条", tableName, tableNameList.size()));
        });
        // CountDownLatch cd = new CountDownLatch(intersection.size());
        // for (String tableName : intersection) {
        //     ThreadUtil.EXECUTOR_SERVICE.execute(() -> {
        //         Integer count = oracleDao.countByTableName(tableName);
        //         log.info(String.format("查询到%s中数据%s条", tableName, count));
        //         // List<Map<String, Object>> tableNameList = oracleDao.queryByTableName(tableName);
        //         // log.info(String.format("查询到%s中数据%s条", tableName, tableNameList.size()));
        //         cd.countDown();
        //     });
        // }
        // cd.await();
        long endTime = System.currentTimeMillis();
        log.info("总计耗时：{}", endTime - startTime);
        return null;
    }

    private void executor(Collection<String> list, Consumer<String> consumer) throws Exception {
        CountDownLatch cd = new CountDownLatch(list.size());
        for (String tableName : list) {
            ThreadUtil.EXECUTOR_SERVICE.execute(() -> {
                consumer.accept(tableName);
                cd.countDown();
            });
        }
        cd.await();
    }
}
