package top.tanmw.oracle2dm.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author TMW
 * @since 2023/2/25 12:16
 */
@DS("master")
@Mapper
public interface DmDao {

    List<String> findAllTable();

    Integer delete(String tableName);
}
