package top.tanmw.oracle2dm.service;

import org.junit.jupiter.api.Test;
import top.tanmw.oracle2dm.utils.ThreadUtil;

import java.util.List;
import java.util.Map;

/**
 * @author TMW
 * @since 2023/2/27 16:40
 */
public class OracleTest {
    @Test
    public void test(){
        int total = 2787090;
        int PAGE_SIZE = 10000;
        int page = ((total - 1) / PAGE_SIZE) + 1;
        for (int i = 0; i < page; i++) {
            int finalI = i;
            // String finalConstraint = constraint;
            // ThreadUtil.EXECUTOR_SERVICE.execute(()->{
            //     log.info("分页查询{}数据:{}", tableName, (finalI - 1) * PAGE_SIZE + "--" + finalI * PAGE_SIZE);
            //     List<Map<String, Object>> mapList = oracleDao.queryByTableNameOrderBy(tableName, finalConstraint, (finalI - 1) * PAGE_SIZE, finalI * PAGE_SIZE);
            //     mapListAll.addAll(mapList);
            //     cd.countDown();
            // });
        }
    }
}
