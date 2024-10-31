package vote;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/deletevote")
public class DeleteVoteServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String voteId = request.getParameter("id");
        String token = request.getHeader("Authorization");
        
        // 토큰이 없거나 비어 있는 경우
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"Authorization token is missing.\"}");
            return;
        }

        try {
        	// 투표 리스트에서 투표 삭제
            boolean isDeleted = VoteListdb.deleteVoteById(Integer.parseInt(voteId));
            if (isDeleted) {
            	// 후보자 및 투표 기록 테이블 삭제
                Candidatesdb.deleteVoteTables(voteId);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}

