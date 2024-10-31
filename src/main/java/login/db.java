package login;

import java.sql.*;
import java.util.List;

public class db {
	
	// 사용자 데이터를 데이터베이스에 저장
    public static void savedb(String name, String dob, String email, String hashedPassword) throws Exception {
    	
    	// 데이터베이스 서버 주소 : 로컬호스트 + 연결하려는 데이터베이스 이름 + postgresql 서버의 포트번호(5432)
    	String dburl = "jdbc:postgresql://localhost:5432/postgres"; 
        String dbusername = "postgres"; 
        String dbpassword = "1234";

        try {
            Class.forName("org.postgresql.Driver"); // PostgreSQL JDBC 드라이버 로드
        } catch (ClassNotFoundException e) {
            System.out.println("Failed to load PostgreSQL driver");
            e.printStackTrace();
            return; // 드라이버 로드 실패 시 종료. jdbc driver(jar file) 확인하기
        }
        System.out.println("Successfully loaded PostgreSQL driver");

        Connection connection = null; 

        try {
            connection = DriverManager.getConnection(dburl, dbusername, dbpassword);
            //DriverManager.getConnection() 으로 연결 후 connection 인스턴스에 객체 저장
                    
        } catch (SQLException e) {
            System.out.println("Failed to connect to the DB");
            e.printStackTrace();
            return; // 연결 실패 시 종료. db_name, user name, password 확인하기
        }

        if (connection != null) {
            System.out.println(connection); // 연결 정보 출력
            System.out.println("Successfully connected to the DB");
        } else {
            System.out.println("Failed to connect to the DB");
        }

        try {
        	
        	// userdata 테이블을 생성 (존재하지 않을 경우)
            String createTableSQL = "CREATE TABLE IF NOT EXISTS userdata ("
                + "email VARCHAR(100) PRIMARY KEY, " // 이메일을 기본키로 설정
                + "name VARCHAR(100) NOT NULL, "
                + "dob DATE NOT NULL, "
                + "password VARCHAR(64) NOT NULL, "
                + "user_type VARCHAR(20) DEFAULT 'user'" // 사용자 유형: 기본값 'user'
                + ")";
            Statement createTableStmt = connection.createStatement();
            createTableStmt.execute(createTableSQL); // 테이블 생성 실행
            
            // 사용자 정보를 데이터베이스에 삽입
            String insertSQL = "INSERT INTO userdata (name, dob, email, password, user_type) VALUES (?, ?, ?, ?, 'user')";
            PreparedStatement insertStmt = connection.prepareStatement(insertSQL);
            insertStmt.setString(1, name);
            insertStmt.setDate(2, java.sql.Date.valueOf(dob)); // 문자열을 SQL 날짜로 변환
            insertStmt.setString(3, email);
            insertStmt.setString(4, hashedPassword);
            int rowsInserted = insertStmt.executeUpdate(); // 삽입 실행
            if (rowsInserted > 0) {
                System.out.println("A new user was inserted successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Failed to insert user into the database.");
            e.printStackTrace();
        } finally {
            // Close the connection
            try {
                connection.close(); // 데이터베이스 연결 종료
            } catch (SQLException e) {
                System.out.println("Failed to close the database connection.");
                e.printStackTrace();
            }
        }
        
    }
    
    // 기존 선관위 권한 박탈 메서드
    public boolean EctoUser(String email) {
    	String dburl = "jdbc:postgresql://localhost:5432/postgres"; 
        String dbusername = "postgres"; 
        String dbpassword = "1234";
        
        try (Connection conn = DriverManager.getConnection(dburl, dbusername, dbpassword)) {
            // 사용자 권한을 'user'로 변경
            String updateSql = "UPDATE userdata SET user_type = 'user' WHERE email = ?";
            try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                updatePstmt.setString(1, email);
                int rowsUpdated = updatePstmt.executeUpdate();
                
                return rowsUpdated > 0; // 성공적으로 업데이트되었는지 여부 반환
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // 예외 발생 시 false 반환
        }
    }
    
    // 사용자 권한 업데이트
    public boolean updateUserRole(String email) {
    	String dburl = "jdbc:postgresql://localhost:5432/postgres"; 
        String dbusername = "postgres"; 
        String dbpassword = "1234";

        try (Connection conn = DriverManager.getConnection(dburl, dbusername, dbpassword)) {
            String sql = "UPDATE userdata SET user_type = 'ec' WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 선관위 권한 요청 삭제
    public boolean deleteRoleRequest(String email) {
    	String dburl = "jdbc:postgresql://localhost:5432/postgres"; 
        String dbusername = "postgres"; 
        String dbpassword = "1234";

        try (Connection conn = DriverManager.getConnection(dburl, dbusername, dbpassword)) {
            String sql = "DELETE FROM role_requests WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}