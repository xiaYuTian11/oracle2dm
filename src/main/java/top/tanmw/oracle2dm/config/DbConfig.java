package top.tanmw.oracle2dm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Not registered via @EnableConfigurationProperties, marked as Spring component, or scanned via @ConfigurationPropertiesScan
 *
 * @author TMW
 * @since 2023/2/28 13:46
 */
@ConfigurationProperties(value = "com.efficient")
@EnableConfigurationProperties(DbConfig.class)
@Data
@Component
public class DbConfig {

    private Map<String, String> updateFiledLengthMap;
    private List<String> identityList;
    private List<String> skipList;

}
