/* 이제 수정은 되는데 
개선사항1. 토큰을 이메일을 주체로 발급해서 토큰이 회수됨 -> 다시 로그인 해야 됨. 
개선사항2. 투표생성자 이메일 정보는 바뀌지 않음.*/

package user;

import login.PasswordUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/updateUserInfo")
public class UpdateUserInfoServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres"; // DB URL
    private static final String DB_USER = "postgres"; // DB 사용자명
    private static final String DB_PASSWORD = "1234"; // DB 비밀번호

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = JWTUtils.getEmailFromToken(token); // 토큰에서 이메일 추출

            if (email != null) {
                String newEmail = request.getParameter("email");
                String newPassword = request.getParameter("password");

                try {
                    // 이메일이 변경될 경우
                    if (newEmail != null && !newEmail.isEmpty()) {
                        updateEmail(email, newEmail);
                    }

                    // 비밀번호가 변경될 경우
                    if (newPassword != null && !newPassword.isEmpty()) {
                        updatePassword(email, newPassword);
                    }

                    response.setStatus(HttpServletResponse.SC_OK);
                } catch (SQLException e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    // 이메일을 업데이트하는 메소드
    private void updateEmail(String oldEmail, String newEmail) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Updating email from " + oldEmail + " to " + newEmail); // 로그 추가
            String updateEmailSQL = "UPDATE userdata SET email = ? WHERE email = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateEmailSQL)) {
                updateStmt.setString(1, newEmail);
                updateStmt.setString(2, oldEmail);
                int rowsAffected = updateStmt.executeUpdate();
                System.out.println("Rows affected: " + rowsAffected); // 변경된 행 수 출력
            }
        }
    }
    
    // 비밀번호를 업데이트하는 메소드
    private void updatePassword(String email, String newPassword) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String hashedPassword = PasswordUtils.hashPassword(newPassword); // 비밀번호를 해시로 변환
            System.out.println("Updating password for email: " + email); // 로그 추가
            String updatePasswordSQL = "UPDATE userdata SET password = ? WHERE email = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updatePasswordSQL)) {
                updateStmt.setString(1, hashedPassword);
                updateStmt.setString(2, email);
                int rowsAffected = updateStmt.executeUpdate();
                System.out.println("Rows affected: " + rowsAffected); // 변경된 행 수 출력
            }
        }
    }
}
