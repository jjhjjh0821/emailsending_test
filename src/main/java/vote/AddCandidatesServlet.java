package vote;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/addcandidates")
public class AddCandidatesServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    request.setCharacterEncoding("UTF-8");

	    // 클라이언트로부터 전송된 JSON 데이터를 받아옴
	    StringBuilder jsonBuilder = new StringBuilder();
	    BufferedReader reader = request.getReader();
	    String line;
	    while ((line = reader.readLine()) != null) {
	        jsonBuilder.append(line);
	    }

	    try {
	        // JSON 데이터 파싱
	        JSONObject json = new JSONObject(jsonBuilder.toString());
	        JSONArray candidates = json.getJSONArray("candidates");

	        // 헤더에서 vote-id 추출
	        String voteId = request.getHeader("vote-id");
	        if (voteId == null || voteId.isEmpty()) {
	            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	            response.getWriter().write("{\"message\": \"Vote ID is required.\"}");
	            return;
	        }

	        // 데이터베이스에 저장
	        Candidatesdb.saveCandidates(voteId, candidates);

	        // 성공적으로 데이터베이스에 저장되었음을 클라이언트에 응답
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.getWriter().write("{\"message\": \"Candidates added successfully.\"}");

	    } catch (Exception e) {
	        // 저장 중 오류 발생 시 클라이언트에 응답
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        response.getWriter().write("{\"message\": \"Failed to save candidates.\"}");
	        e.printStackTrace(); // 오류 로그 출력
	    }
	}

}