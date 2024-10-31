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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/getECusers")
public class GetECUsersServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

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
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    PrintWriter out = response.getWriter();
                    
                    // 사용자 목록을 가져오는 메서드 호출
                    List<User> users = getECUsers();

                    // 사용자 목록을 JSON 형식으로 응답
                    out.print("{\"users\":[");
                    for (int i = 0; i < users.size(); i++) {
                        User user = users.get(i);
                        out.print("{\"email\":\"" + user.getEmail() + "\","
                                + "\"name\":\"" + user.getName() + "\","
                                + "\"dob\":\"" + user.getDob().toString() + "\","
                                + "\"user_type\":\"" + user.getuser_type() + "\"}");
                        if (i < users.size() - 1) {
                            out.print(",");
                        }
                    }
                    out.print("]}");
                    out.flush();
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

    private List<User> getECUsers() throws SQLException {
        String sql = "SELECT email, name, dob, user_type FROM userdata WHERE user_type = 'ec'";
        List<User> users = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                        rs.getString("email"),
                        rs.getString("name"),
                        rs.getDate("dob"),
                        rs.getString("user_type")
                );
                users.add(user);
            }
        }
        return users;
    }
}
