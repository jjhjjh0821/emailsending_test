package user;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

// 권한 거절 서블릿
@WebServlet("/rejectRole")
public class RejectRoleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private login.db db = new login.db();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // JSON 데이터를 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
        RejectRoleRequest rejectRoleRequest = objectMapper.readValue(jsonData, RejectRoleRequest.class);

        String email = rejectRoleRequest.getEmail();

        // 역할 요청 삭제
        boolean success = db.deleteRoleRequest(email);

        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"message\":\"권한 신청이 거절되었습니다.\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"거절 처리에 실패했습니다.\"}");
        }
    }

    private static class RejectRoleRequest {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
