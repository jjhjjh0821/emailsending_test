package vote;

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
	
	private static final long serialVersionUID = 1L;
    
    // 데이터베이스 연결 정보 설정
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "1234";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        List<Vote> votes = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) { // 데이터베이스 연결
            String sql = "SELECT vote_id, vote_title, start_datetime, end_datetime, voter_count, creator_email FROM vote_list";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) { // 결과 집합 순회
                    String id = resultSet.getString("vote_id");
                    String title = resultSet.getString("vote_title");
                    String start = resultSet.getString("start_datetime");
                    String end = resultSet.getString("end_datetime");
                    int voterCount = resultSet.getInt("voter_count"); 
                    String creatorEmail = resultSet.getString("creator_email");

                    // 이메일로부터 사용자 이름 가져옴
                    String creatorName = getUserNameByEmail(creatorEmail);

                    votes.add(new Vote(id, title, start, end, voterCount, creatorName)); // Vote 객체 추가
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Gson로 자바 객체를 JSON으로 변환
        String json = new Gson().toJson(votes);
        PrintWriter out = response.getWriter();
        out.print(json); // JSON 출력
        out.flush(); // 출력 버퍼 비우기
    }

    // 이메일로부터 사용자 이름을 가져옴
    private String getUserNameByEmail(String email) {
        String name = "Unknown"; // 기본 이름 설정
        String sql = "SELECT name FROM userdata WHERE email = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) { // 결과가 존재하면
                    name = resultSet.getString("name"); // 이름 가져오기
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name; // 사용자 이름 반환
    }

    // 내부 클래스로 Vote 객체 정의
    private static class Vote {
    	private String id;
        private String title;
        private String start;
        private String end;
        private int voterCount;
        private String creatorName;

        // 생성자
        public Vote(String id, String title, String start, String end, int voterCount, String creatorName) {
            this.id = id;
            this.title = title;
            this.start = start;
            this.end = end;
            this.voterCount = voterCount;
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
        
        public int getVoterCount() {
            return voterCount;
        }
        
        public String getCreatorName() {
            return creatorName;
        }
    }
}
