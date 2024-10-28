package vote;

import java.sql.*;

public class VoteListdb {
    public static int savevotedb(String title, Timestamp startTimestamp, Timestamp endTimestamp, String email) throws Exception {
        String dburl = "jdbc:postgresql://localhost:5432/postgres"; // 데이터베이스 URL
        String dbusername = "postgres"; // 데이터베이스 접속할 계정의 사용자 이름 (id)
        String dbpassword = "1234"; // 데이터베이스 비밀번호

        int voteId = -1; // -1을 기본값으로 설정하여 오류 확인 가능하게 함
        Connection connection = null; 
        try {
            Class.forName("org.postgresql.Driver"); // 드라이버 로드
            connection = DriverManager.getConnection(dburl, dbusername, dbpassword); // 연결 생성

            // 테이블이 없으면 생성
            String createTableSQL = "CREATE TABLE IF NOT EXISTS vote_list ("
                + "vote_id SERIAL PRIMARY KEY, "  // 자동 증가하는 ID 필드 추가
                + "vote_title VARCHAR(255) NOT NULL, "
                + "start_datetime TIMESTAMP NOT NULL, "
                + "end_datetime TIMESTAMP NOT NULL, "
                + "creator_email VARCHAR(255) NOT NULL)";
            Statement createTableStmt = connection.createStatement();
            createTableStmt.execute(createTableSQL);

            // 데이터 삽입 및 ID 반환
            String insertSQL = "INSERT INTO vote_list (vote_title, start_datetime, end_datetime, creator_email) VALUES (?, ?, ?, ?) RETURNING vote_id";
            PreparedStatement insertStmt = connection.prepareStatement(insertSQL);
            insertStmt.setString(1, title);
            insertStmt.setTimestamp(2, startTimestamp);
            insertStmt.setTimestamp(3, endTimestamp);
            insertStmt.setString(4, email);

            ResultSet rs = insertStmt.executeQuery();
            if (rs.next()) {
                voteId = rs.getInt("vote_id");
            }

        } catch (SQLException e) {
            System.out.println("Failed to insert vote into the database.");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Database connection closed successfully.");
                } catch (SQLException e) {
                    System.out.println("Failed to close the database connection.");
                    e.printStackTrace();
                }
            }
        }
        return voteId;
    }
    
    // 투표 삭제
    public static boolean deleteVoteById(int voteId) throws SQLException {
    	String dburl = "jdbc:postgresql://localhost:5432/postgres"; // 데이터베이스 URL
        String dbusername = "postgres"; // 데이터베이스 접속할 계정의 사용자 이름 (id)
        String dbpassword = "1234"; // 데이터베이스 비밀번호
        
        Connection connection = null;
        boolean isDeleted = false;
        
        try {
            connection = DriverManager.getConnection(dburl, dbusername, dbpassword);
            String deleteSQL = "DELETE FROM vote_list WHERE vote_id = ?";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteSQL);
            deleteStmt.setInt(1, voteId);
            int rowsAffected = deleteStmt.executeUpdate();
            isDeleted = rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        
        return isDeleted;
    }
}
