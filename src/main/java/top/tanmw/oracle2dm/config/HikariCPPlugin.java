// package top.tanmw.oracle2dm.config;
//
// import com.jfinal.plugin.IPlugin;
// import com.jfinal.plugin.activerecord.IDataSourceProvider;
// import com.zaxxer.hikari.HikariDataSource;
//
// import javax.sql.DataSource;
//
// /**
//  * @author TMW
//  * @since 2023/2/25 19:58
//  */
// public class HikariCPPlugin implements IPlugin, IDataSourceProvider {
//     // 数据库连接字符串
//     private String jdbcUrl;
//     // 数据库用户名
//     private String username;
//     // 数据库连接密码
//     private String password;
//     // 数据库驱动类名
//     private String driverClassName = null;
//
//     // 连接池中允许的最大连接数。缺省值：10；推荐的公式：((core_count * 2) + effective_spindle_count)
//     private int maxPoolSize = 10;
//     // 一个连接 idle 状态的最大时长（毫秒），超时则被释放（retired），缺省:10分钟
//     private long idleTimeoutMs = 600000;
//     // 最小空闲数 默认 10
//     private int minIdle = 10;
//     // 一个连接的生命时长（毫秒），超时且没被使用则被释放（retired），缺省:30分钟，建议设置比数据库超时时长少30秒，参考 MySQL
//     // wait_timeout 参数（show variables like '%timeout%';）
//     private long maxLifetimeMs = 1800000;
//     // 等待连接池分配连接的最大时长（毫秒），超过这个时长还没可用的连接则发生 SQLException， 缺省:30秒
//     private long connectionTimeoutMs = 30000;
//
//     /**
//      * hsqldb:"select 1 from INFORMATION_SCHEMA.SYSTEM_USERS" Oracle:"select 1
//      * from dual" DB2:"select 1 from sysibm.sysdummy1" mysql:"select 1"
//      */
//     private String connectionTestQuery = "select 1";
//
//     private HikariDataSource ds;
//     private boolean isStarted = false;
//
//     /**
//      * 实例化
//      *
//      * @param jdbcUrl
//      *            数据库连接字符串
//      *            如：jdbcUrl=jdbc:mysql://localhost:3306/dbname?useUnicode=true&autoReconnect=true&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull&useSSL=false
//      * @param username
//      *            数据库连接用户名
//      * @param password
//      *            数据库连接密码
//      */
//     public HikariCPPlugin(String jdbcUrl, String username, String password) {
//         this.jdbcUrl = jdbcUrl;
//         this.username = username;
//         this.password = password;
//     }
//
//     @Override
//     public boolean start() {
//         if (isStarted) {
//             return true;
//         }
//
//         ds = new HikariDataSource();
//
//         ds.setJdbcUrl(jdbcUrl);
//         ds.setUsername(username);
//         ds.setPassword(password);
//
//         ds.setDriverClassName(driverClassName);
//
//         ds.setConnectionTestQuery(connectionTestQuery);
//         ds.setConnectionTimeout(connectionTimeoutMs);
//
//         ds.setIdleTimeout(idleTimeoutMs);
//         ds.setMaxLifetime(maxLifetimeMs);
//         ds.setMaximumPoolSize(maxPoolSize);
//         ds.setMinimumIdle(minIdle);
//
//         isStarted = true;
//         return true;
//     }
//
//     @Override
//     public boolean stop() {
//         if (ds != null) {
//             ds.close();
//         }
//
//         ds = null;
//         isStarted = false;
//         return true;
//     }
//
//     @Override
//     public DataSource getDataSource() {
//         return ds;
//     }
//
//     /**
//      * 设置额外的关键参数
//      *
//      * @param idleTimeoutMs
//      *            单位毫秒 默认 600000 毫秒
//      * @param maxLifetimeMs
//      *            单位毫秒 默认 1800000 毫秒
//      * @param maxPoolSize
//      *            最大连接池数 默认 10 个
//      * @param minIdle
//      *            最小空闲数 默认 10 个
//      * @return
//      */
//     public HikariCPPlugin set(long idleTimeoutMs, int maxLifetimeMs, int maxPoolSize, int minIdle) {
//         this.idleTimeoutMs = idleTimeoutMs;
//         this.maxLifetimeMs = maxLifetimeMs;
//         this.maxPoolSize = maxPoolSize;
//         this.minIdle = minIdle;
//         return this;
//     }
//
//     /**
//      * 设置数据库连接驱动类名
//      *
//      * @param driverClassName
//      * @return
//      */
//     public HikariCPPlugin setDriverClass(String driverClassName) {
//         this.driverClassName = driverClassName;
//         return this;
//     }
//
//     /**
//      * 设置一个连接 idle 状态的最大时长（毫秒），超时则被释放（retired），缺省:10分钟
//      *
//      * @param idleTimeoutMs
//      * @return
//      */
//     public HikariCPPlugin setIdleTimeoutMs(long idleTimeoutMs) {
//         this.idleTimeoutMs = idleTimeoutMs;
//         return this;
//     }
//
//     /**
//      * 设置一个连接的生命时长（毫秒），超时且没被使用则被释放（retired），缺省:30分钟，建议设置比数据库超时时长少30秒，参考 MySQL
//      * wait_timeout 参数（show variables like '%timeout%';）
//      *
//      * @param maxLifetimeMs
//      * @return
//      */
//     public HikariCPPlugin setMaxLifetimeMs(int maxLifetimeMs) {
//         this.maxLifetimeMs = maxLifetimeMs;
//         return this;
//     }
//
//     /**
//      * 设置连接池中允许的最大连接数。缺省值：10；推荐的公式：((core_count * 2) + effective_spindle_count)
//      *
//      * @param maxPoolSize
//      * @return
//      */
//     public HikariCPPlugin setMaxPoolSize(int maxPoolSize) {
//         this.maxPoolSize = maxPoolSize;
//         return this;
//     }
//
//     /**
//      * 设置最小空闲数 默认 10
//      *
//      * @param minIdle
//      * @return
//      */
//     public HikariCPPlugin setMinIdle(int minIdle) {
//         this.minIdle = minIdle;
//         return this;
//     }
//
//     /**
//      * 设置连接测试查询
//      *
//      * @param connectionTestQuery
//      * @return
//      */
//     public HikariCPPlugin setConnectionTestQuery(String connectionTestQuery) {
//         this.connectionTestQuery = connectionTestQuery;
//         return this;
//     }
// }
