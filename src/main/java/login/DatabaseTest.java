package login;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/postgres"; // 데이터베이스 URL
        String user = "postgres"; // 데이터베이스 사용자
        String password = "1234"; // 데이터베이스 비밀번호

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM userdata")) {

            // 결과 출력
            while (resultSet.next()) {
                String email = resultSet.getString("email");
                String passwordFromDb = resultSet.getString("password");
                System.out.println("Email: " + email + ", Password: " + passwordFromDb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
