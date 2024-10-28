package login;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionTest {

    public static void main(String[] args) {
        // JDBC 드라이버 로드
        try {
            Class.forName("org.postgresql.Driver");
            // 데이터베이스 연결
            String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
            String dbUsername = "postgres";
            String dbPassword = "1234";

            Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            if (connection != null) {
                System.out.println("Connected to the database!");
                // 연결이 성공하면 여기서 추가 작업을 수행할 수 있습니다.
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the database. Please check your connection details.");
        }
    }
}
