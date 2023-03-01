package top.tanmw.oracle2dm.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 18926
 *
 * @author TMW
 * @since 2023/2/25 12:25
 */
@SpringBootTest
class TransformServiceTest {
    @Autowired
    private TransformService transformService;

    @Test
    void queryAll() throws Exception {
        System.out.println(transformService.queryAll());
    }

}