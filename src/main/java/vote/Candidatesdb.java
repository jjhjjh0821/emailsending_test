package vote;

import org.json.JSONArray;
import org.json.JSONObject;

import user.JWTUtils;

import java.sql.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Candidatesdb {
	
	// 후보자를 데이터베이스에 저장
    public static void saveCandidates(String voteId, JSONArray candidates) throws Exception {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres?characterEncoding=UTF-8";
        String dbUsername = "postgres";
        String dbPassword = "1234";

        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver"); // PostgreSQL JDBC 드라이버 로드
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword); // 데이터베이스 연결

            String tableName = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_") + "_candidates"; // 후보자 테이블 이름
            String voteRecordTable = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_") + "_record"; // 투표 기록 테이블 이름
            
            // 후보자 테이블 생성
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" 
                    + "symbol INT NOT NULL, "
                    + "name VARCHAR(255) NOT NULL, "
                    + "votes_cnt INT DEFAULT 0, " // 득표수
                    + "UNIQUE(symbol))";
            Statement createTableStmt = connection.createStatement();
            createTableStmt.executeUpdate(createTableSQL); // 테이블 생성 쿼리 실행
            
            // 투표 기록 테이블 생성 (투표 유무 확인용으로 미리 만듦)
            String createRecordTableSQL = "CREATE TABLE IF NOT EXISTS " + voteRecordTable + " (" +
                                          "id SERIAL PRIMARY KEY, " + 
                                          "vote_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + 
                                          "email VARCHAR(255) NOT NULL, " +
                                          "symbol INT NOT NULL, " +
                                          "UNIQUE (email))";  // 이메일을 UNIQUE로 설정하여 중복 방지
            try (Statement createRecordStmt = connection.createStatement()) {
                createRecordStmt.executeUpdate(createRecordTableSQL); // 테이블 생성 쿼리 실행
            }

            // 기권 옵션 추가
            String insertDefaultQuery = "INSERT INTO " + tableName + " (symbol, name) VALUES (0, '기권') ON CONFLICT (symbol) DO NOTHING";
            try (PreparedStatement pstmt = connection.prepareStatement(insertDefaultQuery)) {
                pstmt.executeUpdate();
            }

            // 후보자 추가 쿼리
            String insertCandidatesQuery = "INSERT INTO " + tableName + " (symbol, name) VALUES (?, ?) ON CONFLICT (symbol) DO NOTHING";
            try (PreparedStatement pstmt = connection.prepareStatement(insertCandidatesQuery)) {
                for (int i = 0; i < candidates.length(); i++) {
                    pstmt.setInt(1, i + 1); // 후보자의 symbol
                    pstmt.setString(2, candidates.getString(i)); // 후보자의 이름
                    pstmt.addBatch(); // 배치에 추가
                }
                int[] rowsInserted = pstmt.executeBatch(); // 배치 실행
                int successfulInserts = 0; // 성공적으로 삽입된 후보자 수
                for (int count : rowsInserted) {
                    if (count > 0) {
                        successfulInserts++; // 성공적으로 삽입된 경우 카운트
                    }
                }
                System.out.println(successfulInserts + " candidates were inserted successfully!"); // 성공 메시지 출력
            }

        } catch (SQLException e) {
            System.out.println("Failed to manage candidates in the database.");
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.close(); // 데이터베이스 연결 종료
                    //System.out.println("Database connection closed successfully.");
                } catch (SQLException e) {
                    System.out.println("Failed to close the database connection.");
                    e.printStackTrace();
                }
            }
        }
    }
    
    // 후보자에게 투표
    public static void voteForCandidate(String voteId, String token, int symbol) throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres?characterEncoding=UTF-8";
        String dbUsername = "postgres";
        String dbPassword = "1234";

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            // JWT 토큰에서 이메일 추출
            String email = JWTUtils.getEmailFromToken(token);

            String tableName = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_") + "_candidates";

            // 투표 기록 테이블
            String voteRecordTable = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_") + "_record";

            // 투표 기록 테이블이 존재하는지 확인하고 없으면 생성
            String createRecordTableSQL = "CREATE TABLE IF NOT EXISTS " + voteRecordTable + " (" +
            							  "id SERIAL PRIMARY KEY, " + 
            							  "vote_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + 
                                          "email VARCHAR(255) NOT NULL, " +
                                          "symbol INT NOT NULL, " + // 테스트용. 실제로는 누구에게 투표했는지 알면 안됨.
                                          "UNIQUE (email))";  // 이메일을 UNIQUE로 설정하여 중복 방지
            try (Statement createRecordStmt = connection.createStatement()) {
                createRecordStmt.executeUpdate(createRecordTableSQL);
            }

            // 이미 투표한 사용자인지 확인
            String checkVoteSQL = "SELECT COUNT(*) FROM " + voteRecordTable + " WHERE email = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkVoteSQL)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("이미 투표 하셨습니다.");
                    }
                }
            }

            // 투표
            String updateVoteSQL = "UPDATE " + tableName + " SET votes_cnt = votes_cnt + 1 WHERE symbol = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateVoteSQL)) {
                pstmt.setInt(1, symbol);
                pstmt.executeUpdate(); // 득표수 업데이트
            }

            // 투표 기록 저장
            String insertVoteRecordSQL = "INSERT INTO " + voteRecordTable + " (vote_time, email, symbol) VALUES (CURRENT_TIMESTAMP, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertVoteRecordSQL)) {
                pstmt.setString(1, email);
                pstmt.setInt(2, symbol);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println("Failed to cast vote.");
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // 후보자 목록 반환
    public static JSONArray getCandidates(String voteId) throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres?characterEncoding=UTF-8";
        String dbUsername = "postgres";
        String dbPassword = "1234";

        Connection connection = null;
        JSONArray candidates = new JSONArray(); // 후보자 배열
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            String tableName = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_") + "_candidates";

            String selectCandidatesSQL = "SELECT symbol, name, votes_cnt FROM " + tableName + " ORDER BY symbol"; // 후보자 조회
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(selectCandidatesSQL)) {
                while (rs.next()) {
                    candidates.put(new org.json.JSONObject()
                        .put("symbol", rs.getInt("symbol"))
                        .put("name", rs.getString("name"))
                        .put("votes_cnt", rs.getInt("votes_cnt")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve candidates.");
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return candidates; // 후보자 배열 반환
    }
    
    // 득표수 결과 반환 
    public static JSONObject getVoteResults(String voteId) throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres?characterEncoding=UTF-8";
        String dbUsername = "postgres";
        String dbPassword = "1234";

        Connection connection = null;
        JSONObject result = new JSONObject(); // 결과를 담을 JSON 객체
        JSONArray candidates = new JSONArray();
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            String tableName = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_") + "_candidates";
            String voteRecordTable = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_") + "_record";

            // 전체 투표수 계산
            String totalVotesSQL = "SELECT COUNT(*) FROM " + voteRecordTable;
            int totalVotes = 0;
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(totalVotesSQL)) {
                if (rs.next()) {
                    totalVotes = rs.getInt(1); // 전체 투표수 저장
                }
            }

            // 후보자 정보 가져오기
            String selectCandidatesSQL = "SELECT symbol, name, votes_cnt FROM " + tableName + " ORDER BY symbol";
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(selectCandidatesSQL)) {
                while (rs.next()) {
                    candidates.put(new org.json.JSONObject()
                        .put("symbol", rs.getInt("symbol"))
                        .put("name", rs.getString("name"))
                        .put("votes_cnt", rs.getInt("votes_cnt")));
                }
            }

            result.put("totalVotes", totalVotes); // 결과 JSON에 전체 투표수 추가
            result.put("candidates", candidates); // 결과 JSON에 후보자 배열 추가

        } catch (SQLException e) {
            System.out.println("Failed to retrieve vote results.");
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.close(); // 연결 닫기
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return result; // 결과 반환
    }
    
    // 투표 시간 목록 반환
    public static JSONArray getVoteTimes(String voteId) throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres?characterEncoding=UTF-8";
        String dbUsername = "postgres";
        String dbPassword = "1234";

        Connection connection = null;
        JSONArray voteTimes = new JSONArray(); // 투표 시간 배열
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            String voteRecordTable = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_") + "_record";

            String selectVoteTimesSQL = "SELECT vote_time FROM " + voteRecordTable + " ORDER BY vote_time"; // 투표 시간 조회
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(selectVoteTimesSQL)) {
                while (rs.next()) {
                	ZonedDateTime kstDateTime = rs.getTimestamp("vote_time")
                	        .toInstant()
                	        .atZone(ZoneId.of("Asia/Seoul")); // 한국 표준시로 변환
                	    
                	    voteTimes.put(kstDateTime.toString()); // KST로 변환한 시간을 추가
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve vote times.");
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.close(); // 연결 닫기
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return voteTimes; // 투표 시간 배열 반환
    }
    
    // 투표 여부 확인
    public static boolean hasUserVoted(String voteId, String token) throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres?characterEncoding=UTF-8";
        String dbUsername = "postgres";
        String dbPassword = "1234";

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            String email = JWTUtils.getEmailFromToken(token); // JWT 토큰에서 이메일 추출

            String voteRecordTable = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_") + "_record";

            String checkVoteSQL = "SELECT COUNT(*) FROM " + voteRecordTable + " WHERE email = ?";  // 사용자가 투표했는지 확인
            try (PreparedStatement checkStmt = connection.prepareStatement(checkVoteSQL)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0; // 투표 여부 반환
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to check vote status.");
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    // 후보자 및 기록 테이블을 삭제
    public static void deleteVoteTables(String voteId) throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres?characterEncoding=UTF-8";
        String dbUsername = "postgres";
        String dbPassword = "1234";

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            String tableName = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_");
            String candidatesTable = tableName + "_candidates";
            String recordTable = tableName + "_record";

            // 후보자 테이블 삭제
            String dropCandidatesTableSQL = "DROP TABLE IF EXISTS " + candidatesTable;
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(dropCandidatesTableSQL);
            }

            // 투표 기록 테이블 삭제
            String dropRecordTableSQL = "DROP TABLE IF EXISTS " + recordTable;
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(dropRecordTableSQL);
            }

        } catch (SQLException e) {
            System.out.println("Failed to delete vote tables.");
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

