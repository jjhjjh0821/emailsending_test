package login;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.PrintWriter;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
import javax.servlet.annotation.WebServlet;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	// JWT 비밀키 정의 (수정할것)
	private static final String SECRET_KEY = "your-secret-key"; 
	
	// POST 요청을 처리
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	// CORS 설정 (모든 도메인에서 접근 가능하게 허용)
    	response.setHeader("Access-Control-Allow-Origin", "*"); // CORS 허용
        response.setHeader("Access-Control-Allow-Methods", "POST");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    	
        // 클라이언트로부터 전달된 이메일과 해시된 비밀번호를 가져옴
    	String email = request.getParameter("email");
        String hashedPassword = request.getParameter("hashedpassword");
        
        // DB에서 이메일과 비밀번호 일치 확인
        boolean isValidUser = validateUser(email, hashedPassword);
        
        // 응답 설정
        PrintWriter out = response.getWriter(); // 응답을 작성하기 위한 PrintWriter 객체 생성
        response.setContentType("text/plain"); // 응답 유형을 설정 (plain text)
        
        // 사용자 인증에 성공하면 JWT 토큰을 생성
        if (isValidUser) {
            // JWT 토큰 생성
            String token = Jwts.builder()
                .setSubject(email) // 토큰의 주체 = 이매일 주소
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1시간 후 만료
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 비밀키로 서명
                .compact();
            
            response.setStatus(HttpServletResponse.SC_OK); // 응답 상태를 200 OK로 설정
            out.write(token); // 생성된 토큰을 응답으로 전송
        } else {
            // 로그인 실패
        	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 에러
            out.write("Invalid email or password.");
        	}
        out.flush(); // 응답을 클라이언트로 전송
    }
    
    // 사용자 인증
    private boolean validateUser(String email, String hashedPassword) {
    	
    	// 데이터베이스 연결 정보 설정
    	String dburl = "jdbc:postgresql://localhost:5432/postgres"; 
        String dbusername = "postgres"; 
        String dbpassword = "1234";
        
        boolean isValid = false;
        try {
        	
            Class.forName("org.postgresql.Driver"); // PostgreSQL JDBC 드라이버를 로드
            
            Connection connection = DriverManager.getConnection(dburl, dbusername, dbpassword); // 데이터베이스에 연결
            
            // 이메일과 해시된 비밀번호가 일치하는지 조회하는 쿼리
            String query = "SELECT * FROM userdata WHERE email=? AND password=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, hashedPassword);
            ResultSet resultSet = statement.executeQuery(); // 쿼리 실행
            isValid = resultSet.next(); // 결과가 존재하는 경우 true 반환
            connection.close(); // 데이터베이스 연결 종료
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); // 드라이버 로드 실패 시 오류 출력
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path.");
        } catch (SQLException e) {
            e.printStackTrace(); // 자세한 SQL 오류 확인
        }
        
        return isValid; // 사용자 인증 결과 반환
    }
    
    
}