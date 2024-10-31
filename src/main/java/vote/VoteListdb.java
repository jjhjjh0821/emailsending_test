package vote;

import java.sql.*;
import java.util.List;

public class VoteListdb {
	
	// 투표 정보를 데이터베이스에 저장
    public static int savevotedb(String title, Timestamp startTimestamp, Timestamp endTimestamp, String email,  int voterCount) throws Exception {
    	String dburl = "jdbc:postgresql://localhost:5432/postgres"; 
        String dbusername = "postgres"; 
        String dbpassword = "1234";

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
                + "voter_count INT NOT NULL DEFAULT 0, "
                + "creator_email VARCHAR(255) NOT NULL)";
            Statement createTableStmt = connection.createStatement();
            createTableStmt.execute(createTableSQL); // 테이블 생성 실행

            // 데이터 삽입 및 ID 반환
            String insertSQL = "INSERT INTO vote_list (vote_title, start_datetime, end_datetime, voter_count, creator_email) VALUES (?, ?, ?, ?, ?) RETURNING vote_id";
            PreparedStatement insertStmt = connection.prepareStatement(insertSQL);
            insertStmt.setString(1, title);
            insertStmt.setTimestamp(2, startTimestamp);
            insertStmt.setTimestamp(3, endTimestamp);
            insertStmt.setInt(4, voterCount);
            insertStmt.setString(5, email.trim());

            ResultSet rs = insertStmt.executeQuery(); // 쿼리 실행
            if (rs.next()) {
                voteId = rs.getInt("vote_id"); // 생성된 vote_id 가져오기
            }

        } catch (SQLException e) {
            System.out.println("Failed to insert vote into the database.");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    //System.out.println("Database connection closed successfully.");
                } catch (SQLException e) {
                    System.out.println("Failed to close the database connection.");
                    e.printStackTrace();
                }
            }
        }
        return voteId; // 생성된 vote_id 반환
    }
    
    // 유권자를 데이터베이스에 저장
    public static void saveVoters(int voteId, List<String> voters) throws SQLException {
    	String dburl = "jdbc:postgresql://localhost:5432/postgres"; 
        String dbusername = "postgres"; 
        String dbpassword = "1234";

        String tableName = "vote_" + voteId + "_voters"; // 유권자 테이블 이름 생성

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dburl, dbusername, dbpassword); // 연결 생성

            // 유권자 테이블이 없으면 생성
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "id SERIAL PRIMARY KEY, "  // 자동 증가하는 ID 필드 추가
                + "email VARCHAR(255) NOT NULL UNIQUE)"; // 유권자 이메일 필드
            Statement createTableStmt = connection.createStatement();
            createTableStmt.execute(createTableSQL); // 테이블 생성 실행

            // 유권자 이메일 삽입
            String insertSQL = "INSERT INTO " + tableName + " (email) VALUES (?)";
            PreparedStatement insertStmt = connection.prepareStatement(insertSQL);

            for (String voter : voters) {
                insertStmt.setString(1, voter.trim());
                insertStmt.addBatch(); // 배치 처리
            }
            insertStmt.executeBatch(); // 배치 실행

        } catch (SQLException e) {
            System.out.println("Failed to save voters to the database.");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    //System.out.println("Database connection closed successfully.");
                } catch (SQLException e) {
                    System.out.println("Failed to close the database connection.");
                    e.printStackTrace();
                }
            }
        }
    }
    
    // 유권자 여부 확인 - 문제점: csv의 첫행 못읽고있음. 첫행만 뺴면 작동해서 버리는 행으로 놔뒀음
    public static boolean isVoter(int voteId, String email) throws SQLException {
    	String dburl = "jdbc:postgresql://localhost:5432/postgres"; 
        String dbusername = "postgres"; 
        String dbpassword = "1234";

        String tableName = "vote_" + voteId + "_voters"; // 유권자 테이블 이름 생성
        boolean isVoter = false; // 유권자 여부를 나타내는 변수

        Connection connection = null;
        try {
            // 드라이버 로드
            Class.forName("org.postgresql.Driver"); // PostgreSQL 드라이버 로드

            connection = DriverManager.getConnection(dburl, dbusername, dbpassword); // 연결 생성

            // 이메일 존재 여부 확인 쿼리
            String checkVoterSQL = "SELECT COUNT(*) FROM " + tableName + " WHERE email = ?";
            PreparedStatement checkVoterStmt = connection.prepareStatement(checkVoterSQL);
            checkVoterStmt.setString(1, email.trim());

            ResultSet rs = checkVoterStmt.executeQuery(); // 쿼리 실행
            if (rs.next()) {
                isVoter = rs.getInt(1) > 0; // 유권자가 존재하는지 확인
            }

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver not found.");
            e.printStackTrace();
            return false; // 드라이버가 없으면 false 반환
        } catch (SQLException e) {
            System.out.println("Failed to check if the user is a voter.");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    //System.out.println("Database connection closed successfully.");
                } catch (SQLException e) {
                    System.out.println("Failed to close the database connection.");
                    e.printStackTrace();
                }
            }
        }
        return isVoter; // 유권자 여부 반환
    }
    
    // 투표 삭제
    public static boolean deleteVoteById(int voteId) throws SQLException {
    	String dburl = "jdbc:postgresql://localhost:5432/postgres"; 
        String dbusername = "postgres"; 
        String dbpassword = "1234";
        
        Connection connection = null;
        boolean isDeleted = false; // 삭제 여부를 나타내는 변수
        
        try {
            connection = DriverManager.getConnection(dburl, dbusername, dbpassword);
            
            // 투표 리스트에서 삭제
            String deleteSQL = "DELETE FROM vote_list WHERE vote_id = ?";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteSQL);
            deleteStmt.setInt(1, voteId);
            int rowsAffected = deleteStmt.executeUpdate();
            isDeleted = rowsAffected > 0; // 삭제 성공 여부 확인
            
            // 투표 삭제 됐으면 유권자 목록도 삭제
            if (isDeleted) {
                String deleteVoterTableSQL = "DROP TABLE IF EXISTS vote_" + voteId + "_voters";
                Statement dropTableStmt = connection.createStatement();
                dropTableStmt.executeUpdate(deleteVoterTableSQL); // 유권자 테이블 삭제 실행
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        
        return isDeleted; // 삭제 여부 반환
    }
}
