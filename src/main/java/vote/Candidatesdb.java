package vote;

import org.json.JSONArray;
import org.json.JSONObject;

import user.JWTUtils;

import java.sql.*;

public class Candidatesdb {

    public static void saveCandidates(String voteId, JSONArray candidates) throws Exception {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres?characterEncoding=UTF-8";
        String dbUsername = "postgres";
        String dbPassword = "1234";
        

        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            String tableName = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_") + "_candidates";
            String voteRecordTable = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_") + "_record";
            
            // 후보자 테이블 생성
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" 
                    + "symbol INT NOT NULL, "
                    + "name VARCHAR(255) NOT NULL, "
                    + "votes_cnt INT DEFAULT 0, " // 득표수
                    + "UNIQUE(symbol))";
            Statement createTableStmt = connection.createStatement();
            createTableStmt.executeUpdate(createTableSQL);
            
            // 투표 기록 테이블 생성 (투표 유무 확인용으로 미리 만듦)
            String createRecordTableSQL = "CREATE TABLE IF NOT EXISTS " + voteRecordTable + " (" +
                                          "id SERIAL PRIMARY KEY, " + 
                                          "vote_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + 
                                          "email VARCHAR(255) NOT NULL, " +
                                          "symbol INT NOT NULL, " +
                                          "UNIQUE (email))";  // 이메일을 UNIQUE로 설정하여 중복 방지
            try (Statement createRecordStmt = connection.createStatement()) {
                createRecordStmt.executeUpdate(createRecordTableSQL);
            }

            String insertDefaultQuery = "INSERT INTO " + tableName + " (symbol, name) VALUES (0, '기권') ON CONFLICT (symbol) DO NOTHING";
            try (PreparedStatement pstmt = connection.prepareStatement(insertDefaultQuery)) {
                pstmt.executeUpdate();
            }

            String insertCandidatesQuery = "INSERT INTO " + tableName + " (symbol, name) VALUES (?, ?) ON CONFLICT (symbol) DO NOTHING";
            try (PreparedStatement pstmt = connection.prepareStatement(insertCandidatesQuery)) {
                for (int i = 0; i < candidates.length(); i++) {
                    pstmt.setInt(1, i + 1);
                    pstmt.setString(2, candidates.getString(i));
                    pstmt.addBatch();
                }
                int[] rowsInserted = pstmt.executeBatch();
                int successfulInserts = 0;
                for (int count : rowsInserted) {
                    if (count > 0) {
                        successfulInserts++;
                    }
                }
                System.out.println(successfulInserts + " candidates were inserted successfully!");
            }

        } catch (SQLException e) {
            System.out.println("Failed to manage candidates in the database.");
            e.printStackTrace();
            throw e;
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
    }
    
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
                                          "symbol INT NOT NULL, " +
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
                pstmt.executeUpdate();
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
    
    // 후보자 반환
    public static JSONArray getCandidates(String voteId) throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres?characterEncoding=UTF-8";
        String dbUsername = "postgres";
        String dbPassword = "1234";

        Connection connection = null;
        JSONArray candidates = new JSONArray();
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            String tableName = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_") + "_candidates";

            String selectCandidatesSQL = "SELECT symbol, name, votes_cnt FROM " + tableName + " ORDER BY symbol";
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
        return candidates;
    }
    
    // 결과 반환 
    public static JSONObject getVoteResults(String voteId) throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres?characterEncoding=UTF-8";
        String dbUsername = "postgres";
        String dbPassword = "1234";

        Connection connection = null;
        JSONObject result = new JSONObject();
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
                    totalVotes = rs.getInt(1);
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

            result.put("totalVotes", totalVotes);
            result.put("candidates", candidates);

        } catch (SQLException e) {
            System.out.println("Failed to retrieve vote results.");
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
        return result;
    }
    
    // 투표 여부 확인
    public static boolean hasUserVoted(String voteId, String token) throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres?characterEncoding=UTF-8";
        String dbUsername = "postgres";
        String dbPassword = "1234";

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            String email = JWTUtils.getEmailFromToken(token);

            String voteRecordTable = "vote_" + voteId.replaceAll("[^a-zA-Z0-9]", "_") + "_record";

            String checkVoteSQL = "SELECT COUNT(*) FROM " + voteRecordTable + " WHERE email = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkVoteSQL)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
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

