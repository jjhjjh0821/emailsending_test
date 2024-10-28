package vote;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/getresults")
public class GetVoteResultsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
    	
    	String voteId = request.getParameter("voteId");

        if (voteId == null || voteId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "투표 ID가 제공되지 않았습니다.");
            return;
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            JSONObject result = Candidatesdb.getVoteResults(voteId);
            out.print(result.toString());
            out.flush();
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버에서 결과를 가져오는 데 실패했습니다.");
            e.printStackTrace();
        }
    }
}

