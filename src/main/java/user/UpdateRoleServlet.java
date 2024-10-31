package user;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebServlet("/updateRole")
public class UpdateRoleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private login.db db = new login.db();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // JSON 데이터를 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
        UpdateRoleRequest updateRoleRequest = objectMapper.readValue(jsonData, UpdateRoleRequest.class);

        String email = updateRoleRequest.getEmail();

        // 데이터베이스에 사용자 권한 업데이트
        boolean success = db.updateUserRole(email);

        if (success) {
            // 역할 요청 삭제
            db.deleteRoleRequest(email);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"message\":\"권한이 변경되었습니다.\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"권한 변경에 실패했습니다.\"}");
        }
    }

    private static class UpdateRoleRequest {
        private String email; // 이메일 하나만 받음

        public String getEmail() {
            return email;
        }
    }
}
