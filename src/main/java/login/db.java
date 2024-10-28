package login;

import java.sql.*;
import java.util.List;

public class db {
	
    public static void savedb(String name, String dob, String email, String hashedPassword) throws Exception {
    	
    	//데이터베이스 서버 주소 : 로컬호스트 + 연결하려는 데이터베이스 이름 + postgresql 서버의 포트번호(5432)
    	String dburl = "jdbc:postgresql://localhost:5432/postgres";
        String dbusername = "postgres";
        String dbpassword = "1234";

        try {
            Class.forName("org.postgresql.Driver"); //Class.forName() 을 이용해서 드라이버 로드
        } catch (ClassNotFoundException e) {
            System.out.println("Failed to load PostgreSQL driver");
            e.printStackTrace();
            return;
        } // if you have a error, check jdbc driver(jar file)
        System.out.println("Successfully loaded PostgreSQL driver");

        Connection connection = null; 

        try {
            connection = DriverManager.getConnection(dburl, dbusername, dbpassword);
            //DriverManager.getConnection() 으로 연결 후 connection 인스턴스에 객체 저장
                    
        } catch (SQLException e) {
            System.out.println("Failed to connect to the DB");
            e.printStackTrace();
            return;
        } /// if you have a error, check DB information (db_name, user name, password)

        if (connection != null) {
            System.out.println(connection);
            System.out.println("Successfully connected to the DB");
        } else {
            System.out.println("Failed to connect to the DB");
        }

        try {
        	
            String createTableSQL = "CREATE TABLE IF NOT EXISTS userdata ("
                + "email VARCHAR(100) PRIMARY KEY, " // 이메일을 기본키로 설정
                + "name VARCHAR(100) NOT NULL, "
                + "dob DATE NOT NULL, "
                + "password VARCHAR(64) NOT NULL, "
                + "user_type VARCHAR(20) DEFAULT 'user'" // 사용자 유형: 기본값 'user'
                + ")";
            Statement createTableStmt = connection.createStatement();
            createTableStmt.execute(createTableSQL);
            
            String insertSQL = "INSERT INTO userdata (name, dob, email, password, user_type) VALUES (?, ?, ?, ?, 'user')";
            PreparedStatement insertStmt = connection.prepareStatement(insertSQL);
            insertStmt.setString(1, name);
            insertStmt.setDate(2, java.sql.Date.valueOf(dob));
            insertStmt.setString(3, email);
            insertStmt.setString(4, hashedPassword);
            int rowsInserted = insertStmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("A new user was inserted successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Failed to insert user into the database.");
            e.printStackTrace();
        } finally {
            // Close the connection
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Failed to close the database connection.");
                e.printStackTrace();
            }
        }
        
    }
    
    // 이메일 목록에 따라 사용자 역할 업데이트하는 메서드 - 아직 작동 안함
    public boolean updateUserRoles(List<String> emails) {
    	String dburl = "jdbc:postgresql://localhost:5432/postgres"; // 데이터베이스 URL
        String dbusername = "postgres"; // 데이터베이스 접속할 계정의 사용자 이름 (id)
        String dbpassword = ""; // 데이터베이스 비밀번호
        
        try (Connection conn = DriverManager.getConnection(dburl, dbusername, dbpassword)) {
            String sql = "UPDATE users SET user_type = 'ec' WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (String email : emails) {
                    pstmt.setString(1, email);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 이메일 목록에 따라 역할 요청 삭제하는 메서드 - 아직 작동 안함
    public boolean deleteRoleRequests(List<String> emails) {
    	String dburl = "jdbc:postgresql://localhost:5432/postgres"; // 데이터베이스 URL
        String dbusername = "postgres"; // 데이터베이스 접속할 계정의 사용자 이름 (id)
        String dbpassword = ""; // 데이터베이스 비밀번호
        
        try (Connection conn = DriverManager.getConnection(dburl, dbusername, dbpassword)) {
            String sql = "DELETE FROM role_requests WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (String email : emails) {
                    pstmt.setString(1, email);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}