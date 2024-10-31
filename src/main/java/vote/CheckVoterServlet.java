package vote;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import user.JWTUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/checkVoter")
public class CheckVoterServlet extends HttpServlet {
		
		private static final long serialVersionUID = 1L;
		
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	        response.setContentType("application/json");
	        PrintWriter out = response.getWriter();
	        
	        // 요청 파라미터로부터 투표 ID 가져오기
	        String voteIdStr = request.getParameter("voteId");
	        //System.out.println("Received voteId: " + voteIdStr);
	        
	        // 요청 헤더에서 JWT 토큰 가져오기
	        String token = request.getHeader("Authorization"); // 예: "Bearer <token>"
	        if (token != null && token.startsWith("Bearer ")) {
	            token = token.substring(7); // "Bearer " 부분 제거
	        }

	        // 유효한 토큰인지 확인하고 이메일 추출
	        String email = null;
	        if (token != null) {
	            try {
	                email = JWTUtils.getEmailFromToken(token);
	                //System.out.println("Extracted email: " + email);
	            } catch (RuntimeException e) {
	            	//System.out.println("JWT error: " + e.getMessage());
	                out.print("{\"isVoter\": false, \"error\": \"" + e.getMessage() + "\"}");
	                return;
	            }
	        }
	        
	        // 유효한 투표 ID인지 확인
	        if (voteIdStr == null || email == null) {
	        	//System.out.println("voteIdStr or email is null.");
	            out.print("{\"isVoter\": false}");
	            return;
	        }

	        int voteId;
	        try {
	            voteId = Integer.parseInt(voteIdStr);
	        } catch (NumberFormatException e) {
	        	//System.out.println("Invalid voteId format: " + voteIdStr);
	            out.print("{\"isVoter\": false}");
	            return;
	        }

	        // 유권자 여부 확인
	        try {
	        	boolean isVoter = VoteListdb.isVoter(voteId, email);
	            response.setStatus(HttpServletResponse.SC_OK);
	            //System.out.println("{\"isVoter\": " + isVoter + "}");
	            out.print("{\"isVoter\": " + isVoter + "}");
	        } catch (SQLException e) {
	            e.printStackTrace();
	            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	            out.print("{\"error\": \"Database error\"}");
	            //System.out.println("SQL error: " + e.getMessage());
	        } finally {
	            out.close();
	        }
	    }
}
