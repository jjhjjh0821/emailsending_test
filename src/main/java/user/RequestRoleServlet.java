package user;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Statement;
import java.io.BufferedReader;

@WebServlet("/requestRole")
public class RequestRoleServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres"; // DB URL
    private static final String DB_USER = "postgres"; // DB 사용자명
    private static final String DB_PASSWORD = "1234"; // DB 비밀번호
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        // JSON 데이터 파싱
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(new JSONObject().put("message", "잘못된 요청입니다.").toString());
            return;
        }
        
        // JSON 데이터 검증
        String jsonString = sb.toString();
        JSONObject jsonRequest;
        try {
            jsonRequest = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(new JSONObject().put("message", "유효하지 않은 JSON 형식입니다.").toString());
            return;
        }

        // 이메일, 이름, 생년월일 추출
        String email = jsonRequest.optString("email", null);
        String name = jsonRequest.optString("name", null);
        String dob = jsonRequest.optString("dob", null);
        
        // 필수 데이터 누락 시 에러 반환
        if (email == null || name == null || dob == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(new JSONObject().put("message", "필수 데이터가 누락되었습니다.").toString());
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // role_requests 테이블이 없으면 생성
            String createTableSQL = "CREATE TABLE IF NOT EXISTS role_requests ("
                + "name VARCHAR(100) NOT NULL, "
                + "email VARCHAR(100) NOT NULL, "
                + "dob DATE NOT NULL, "
                + "PRIMARY KEY (email))";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
            }

            // 신청 상태 확인
            String checkSql = "SELECT * FROM role_requests WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    // 이미 신청한 경우
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write(new JSONObject().put("message", "이미 권한 신청을 하셨습니다.").toString());
                } else {
                    // 새로운 신청
                    String sql = "INSERT INTO role_requests (name, email, dob) VALUES (?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, name);
                        pstmt.setString(2, email);
                        pstmt.setDate(3, java.sql.Date.valueOf(dob));
                        pstmt.executeUpdate();
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write(new JSONObject().put("message", "권한 신청이 완료되었습니다.").toString());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(new JSONObject().put("message", "권한 신청에 실패했습니다.").toString());
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // JWT 토큰에서 사용자 이메일 추출
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String email = JWTUtils.getEmailFromToken(token);
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // role_requests 테이블이 없으면 생성
            String createTableSQL = "CREATE TABLE IF NOT EXISTS role_requests ("
                + "name VARCHAR(100) NOT NULL, "
                + "email VARCHAR(100) NOT NULL, "
                + "dob DATE NOT NULL, "
                + "PRIMARY KEY (email))";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
            }
            
            // 역할 신청 여부 확인
            String query = "SELECT * FROM role_requests WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();
                
                JSONObject jsonResponse = new JSONObject();
                
                if (rs.next()) {
                    // 이미 역할 신청이 되어 있는 경우
                    jsonResponse.put("message", "이미 권한 신청을 하셨습니다.");
                } else {
                    // 역할 신청이 되어 있지 않은 경우
                    jsonResponse.put("message", "역할 신청이 가능합니다.");
                }
                
                PrintWriter out = response.getWriter();
                out.print(jsonResponse.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류");
        }
    }
}
