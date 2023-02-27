package top.tanmw.oracle2dm.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author TMW
 * @since 2023/2/25 12:15
 */
@DS("oracle")
@Mapper
public interface OracleDao {

    Integer countByTableName(String tableName);

    List<Map<String, Object>> queryByTableName(String tableName);

    List<Map<String, Object>> queryByTableNameOrderBy(String tableName, String orderByFiled, Integer page, Integer size);

    List<String> findAllTable();

    String findConstraintByP(String tableName);

    String findConstraintByOther(String tableName);
}
