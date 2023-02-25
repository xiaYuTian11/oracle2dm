package top.tanmw.oracle2dm;

import java.sql.*;

/**
 * @author TMW
 * @since 2023/2/25 11:03
 */
public class OracleDataSource {

    public static void main(String[] argv) throws SQLException {

        System.out.println("-------- Oracle JDBC Connection Testing ------");

        try {

            Class.forName("oracle.jdbc.driver.OracleDriver");

        } catch (ClassNotFoundException e) {

            System.out.println("Where is your Oracle JDBC Driver?");
            e.printStackTrace();
            return;

        }

        System.out.println("Oracle JDBC Driver Registered!");

        Connection connection = null;

        try {

            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@172.18.40.200:1521:smz", "zbbsmz", "123456");

        } catch (SQLException e) {

            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;

        }

        if (connection != null) {
            System.out.println("You made it, take control your database now!");
        } else {
            System.out.println("Failed to make connection!");
        }

        final Statement statement = connection.createStatement();
        String sql = "select * from gbp_user";
        final ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            System.out.println(resultSet.getString(2));
        }

    }

}
