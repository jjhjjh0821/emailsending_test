package vote;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONArray;

@WebServlet("/getVoteTimes")
public class GetVoteTimeServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String voteId = request.getParameter("voteId");

        if (voteId == null || voteId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid vote ID");
            return;
        }

        PrintWriter out = response.getWriter();  // 응답을 작성하기 위한 PrintWriter 객체

        try {
            JSONArray voteTimes = Candidatesdb.getVoteTimes(voteId); // 투표 시간 목록을 JSONArray로 가져옴
            
            // JSONArray를 콘솔에 출력
            //System.out.println("Vote Times: " + voteTimes.toString());
            
            out.print(voteTimes.toString()); // JSONArray를 문자열로 변환하여 출력
            out.flush(); // 출력 버퍼 비우기
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}
