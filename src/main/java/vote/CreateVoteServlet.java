package vote;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.BufferedReader;
import org.json.JSONObject;
import java.sql.*;
import user.JWTUtils; 

@WebServlet("/createvote")
public class CreateVoteServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        StringBuilder jsonBuilder = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        
        try {
            JSONObject json = new JSONObject(jsonBuilder.toString());
            String title = json.optString("title");
            String start = json.optString("start");
            String end = json.optString("end");

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = JWTUtils.getEmailFromToken(token);

                String startFormatted = start.replace('T', ' ') + ":00";
                String endFormatted = end.replace('T', ' ') + ":00";

                Timestamp startTimestamp = Timestamp.valueOf(startFormatted);
                Timestamp endTimestamp = Timestamp.valueOf(endFormatted);

                // 데이터베이스에 저장하면서 vote_id를 받아옴
                int voteId = VoteListdb.savevotedb(title, startTimestamp, endTimestamp, email);
                
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                // JSON 형태로 vote_id 반환
                JSONObject responseJson = new JSONObject();
                responseJson.put("id", voteId);  // vote_id를 JSON으로 설정

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(responseJson.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Token missing or invalid.");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to save data.");
            e.printStackTrace();
        }
    }
}
