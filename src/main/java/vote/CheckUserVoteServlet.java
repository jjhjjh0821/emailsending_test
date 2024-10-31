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
	
	private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String voteId = request.getParameter("voteId");  // 요청 파라미터에서 voteId 추출
        String token = request.getHeader("Authorization").substring(7); // "Bearer "를 제거
        
        try {
            boolean hasVoted = Candidatesdb.hasUserVoted(voteId, token); // 사용자가 해당 투표에 이미 투표했는지 확인
            JSONObject jsonResponse = new JSONObject(); // JSON 응답 객체 생성
            jsonResponse.put("hasVoted", hasVoted); // 투표 여부 추가
            response.setContentType("application/json"); // 응답 타입을 JSON으로 설정
            response.setCharacterEncoding("UTF-8");  // 문자 인코딩을 UTF-8로 설정
            response.getWriter().write(jsonResponse.toString());  // JSON 응답을 클라이언트에 전송
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "투표 상태 확인 중 오류가 발생했습니다.");
        }
    }
}
