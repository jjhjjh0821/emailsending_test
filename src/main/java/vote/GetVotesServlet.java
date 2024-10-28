package vote;

import user.User;

import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/getvotes")
public class GetVotesServlet extends HttpServlet {
    
    // 데이터베이스 연결 정보를 설정합니다.
	private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres"; // DB URL
    private static final String DB_USER = "postgres"; // DB 사용자명
    private static final String DB_PASSWORD = "1234"; // DB 비밀번호

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        List<Vote> votes = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT vote_id, vote_title, start_datetime, end_datetime, creator_email FROM vote_list";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String id = resultSet.getString("vote_id");
                    String title = resultSet.getString("vote_title");
                    String start = resultSet.getString("start_datetime");
                    String end = resultSet.getString("end_datetime");
                    String creatorEmail = resultSet.getString("creator_email");

                    // 이메일로부터 사용자 이름 가져오기
                    String creatorName = getUserNameByEmail(creatorEmail);

                    votes.add(new Vote(id, title, start, end, creatorName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Gson 라이브러리를 사용해 자바 객체를 JSON으로 변환
        String json = new Gson().toJson(votes);
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    private String getUserNameByEmail(String email) {
        String name = "Unknown";
        String sql = "SELECT name FROM userdata WHERE email = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    name = resultSet.getString("name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    // 내부 클래스로 Vote 객체 정의
    private static class Vote {
    	private String id;
        private String title;
        private String start;
        private String end;
        private String creatorName;

        public Vote(String id, String title, String start, String end, String creatorName) {
            this.id = id;
            this.title = title;
            this.start = start;
            this.end = end;
            this.creatorName = creatorName; // 필드 초기화
        }

        // Getter 메서드
        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getStart() {
            return start;
        }

        public String getEnd() {
            return end;
        }
        
        public String getCreatorName() {
            return creatorName;
        }
    }
}
