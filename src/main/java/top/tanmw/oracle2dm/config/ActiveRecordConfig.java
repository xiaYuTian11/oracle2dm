package top.tanmw.oracle2dm.config;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.OrderedFieldContainerFactory;
import com.jfinal.plugin.activerecord.dialect.OracleDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.hikaricp.HikariCpPlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import top.tanmw.oracle2dm.constants.DbConstant;

/**
 * @author TMW
 * @since 2023/2/25 17:38
 */
@Configuration
@Order(1)
public class ActiveRecordConfig {
    @Value("${spring.datasource.dynamic.datasource.master.url}")
    private String url;

    @Value("${spring.datasource.dynamic.datasource.master.driver-class-name}")
    private String driverClass;

    @Value("${spring.datasource.dynamic.datasource.master.username}")
    private String username;

    @Value("${spring.datasource.dynamic.datasource.master.password}")
    private String password;

    @Bean
    public ActiveRecordPlugin initActiveRecordPlugin() {
        //sqlserver数据库
        // DruidPlugin druidPlugin = new DruidPlugin(url, username, password);
        HikariCpPlugin druidPlugin = new HikariCpPlugin(url, username, password);
        druidPlugin.setDriverClass(driverClass);
        // 配置ActiveRecord插件
        ActiveRecordPlugin arp = new ActiveRecordPlugin(DbConstant.DM, druidPlugin);
        // 配置Sqlserver方言
        arp.setDialect(new OracleDialect());
        arp.setShowSql(true);
        // 配置属性名(字段名)大小写不敏感容器工厂
        arp.setContainerFactory(new CaseInsensitiveContainerFactory());
        arp.setContainerFactory(new OrderedFieldContainerFactory());
        druidPlugin.start();
        arp.start();
        return arp;
    }

}
