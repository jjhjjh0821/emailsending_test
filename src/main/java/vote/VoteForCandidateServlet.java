package vote;

import user.JWTUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONArray;

@WebServlet("/voteForCandidate")
public class VoteForCandidateServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("application/json; charset=UTF-8");
    	response.setCharacterEncoding("UTF-8");

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Authorization token is missing.\"}");
            return;
        }

        try {
            String voteId = request.getParameter("voteId");
            int symbol = Integer.parseInt(request.getParameter("symbol"));

            Candidatesdb.voteForCandidate(voteId, token.replace("Bearer ", ""), symbol);

            // 투표 후 결과 반환
            JSONArray results = Candidatesdb.getCandidates(voteId);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(results.toString());

        } catch (SQLException e) {
            if (e.getMessage().contains("이미 투표 하셨습니다.")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"message\": \"이미 투표 하셨습니다.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"message\": \"Failed to cast vote.\"}");
            }
            e.printStackTrace();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\": \"Failed to cast vote.\"}");
            e.printStackTrace();
        }
    }
}
