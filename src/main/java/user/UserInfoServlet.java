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

@WebServlet("/userinfo")
public class UserInfoServlet extends HttpServlet {

	private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres"; // DB URL
    private static final String DB_USER = "postgres"; // DB 사용자명
    private static final String DB_PASSWORD = "1234"; // DB 비밀번호

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer " 이후의 토큰 부분 추출

            try {
                String email = JWTUtils.getEmailFromToken(token); // 토큰에서 이메일 추출

                if (email != null) {
                    User user = getUserByEmail(email);

                    if (user != null) {
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        PrintWriter out = response.getWriter();

                     // 사용자 정보를 JSON 형식으로 응답
                        out.print("{\"email\":\"" + user.getEmail() + "\","
                                + "\"name\":\"" + user.getName() + "\","
                                + "\"dob\":\"" + user.getDob().toString() + "\","
                                + "\"user_type\":\"" + user.getuser_type() + "\"}");
                        out.flush();
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private User getUserByEmail(String email) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;

        try {
            // DB 연결
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // SQL 쿼리 작성
            String sql = "SELECT email, name, dob, user_type FROM userdata WHERE email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);

            // 쿼리 실행
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // 사용자 정보 생성
                user = new User(
                        rs.getString("email"),
                        rs.getString("name"),
                        rs.getDate("dob"),
                        rs.getString("user_type")
                );
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return user;
    }
}
