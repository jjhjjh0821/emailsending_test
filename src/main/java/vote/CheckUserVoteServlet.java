package vote;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import org.json.JSONObject;

@WebServlet("/checkvote")
public class CheckUserVoteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String voteId = request.getParameter("voteId");
        String token = request.getHeader("Authorization").substring(7); // "Bearer "를 제거
        
        try {
            boolean hasVoted = Candidatesdb.hasUserVoted(voteId, token);
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("hasVoted", hasVoted);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse.toString());
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "투표 상태 확인 중 오류가 발생했습니다.");
        }
    }
}
