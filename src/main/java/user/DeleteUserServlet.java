package user;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import org.json.JSONObject;

@WebServlet("/deleteUser")
public class DeleteUserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    String dburl = "jdbc:postgresql://localhost:5432/postgres"; // 데이터베이스 URL
    String dbusername = "postgres"; // 데이터베이스 접속할 계정의 사용자 이름 (id)
    String dbpassword = "1234"; // 데이터베이스 비밀번호

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // JSON 형태로 받은 데이터 읽기
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        }
        
        JSONObject jsonObject = new JSONObject(jsonBuilder.toString());
        String email = jsonObject.optString("email");

        if (email == null || email.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Email is required");
            return;
        }

        try {
            deleteUser(email);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("User deleted successfully");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error deleting user");
            e.printStackTrace();
        }
    }
    // 이메일로 유저를 삭제하는 메서드
    private void deleteUser(String email) throws SQLException {
        Connection connection = null;
        PreparedStatement deleteStmt = null;
        try {
            connection = DriverManager.getConnection(dburl, dbusername, dbpassword);
            String deleteUserSQL = "DELETE FROM userdata WHERE email = ?"; //삭제 SQL 문
            deleteStmt = connection.prepareStatement(deleteUserSQL); //삭제 실행
            deleteStmt.setString(1, email);

            int rowsDeleted = deleteStmt.executeUpdate();
            if (rowsDeleted > 0) { //삭제 성공
                System.out.println("User deleted successfully!");
            } else { //삭제 실패
                System.out.println("Failed to delete user.");
                throw new SQLException("Failed to delete user with email: " + email);
            }
        } finally {
            if (deleteStmt != null) {
                deleteStmt.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
