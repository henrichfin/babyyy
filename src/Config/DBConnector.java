package Config;

import com.mysql.jdbc.Connection;
import java.sql.*;

public class DBConnector {

    final private Connection cn;

    public DBConnector() throws SQLException {
        cn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/inventory_system", "root", "");
    }

    public ResultSet getData(String sql) throws SQLException {
        return cn.createStatement().executeQuery(sql);
    }

    public Connection getConnection() throws SQLException {
        return cn;
    }

    public int insertData(String sql) throws SQLException {
        return cn.prepareStatement(sql).executeUpdate();
    }

    public int updateData(String sql) throws SQLException {
        return cn.prepareStatement(sql).executeUpdate();
    }
}
