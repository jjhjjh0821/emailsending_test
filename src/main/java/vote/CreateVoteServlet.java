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
import java.util.ArrayList;
import java.util.List;

import user.JWTUtils; 

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.Part; 
import java.io.InputStreamReader; 
import java.nio.charset.StandardCharsets;

@WebServlet("/createvote")
@MultipartConfig
public class CreateVoteServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8"); // 요청 인코딩을 UTF-8로 설정

        String title = null;
        String start = null;
        String end = null;
        Part voterCsvPart = null; // 유권자 CSV 파일 파트

        try {
            // Part로부터 데이터 읽기
            title = request.getParameter("title");
            start = request.getParameter("start");
            end = request.getParameter("end");

            // CSV 파일 파트 가져오기
            voterCsvPart = request.getPart("voterCsv"); // 업로드된 유권자 CSV 파일

            // CSV 파일 형식 확인
            if (!voterCsvPart.getContentType().equals("text/csv")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
                response.getWriter().write("Invalid file format. Please upload a CSV file.");
                return;
            }

            String authHeader = request.getHeader("Authorization"); // Authorization 헤더 추출
            if (authHeader != null && authHeader.startsWith("Bearer ")) { // Bearer 토큰이 있는지 확인
                String token = authHeader.substring(7);
                String email = JWTUtils.getEmailFromToken(token); // JWT에서 이메일 추출

                // 시작 및 종료 시간 포맷 변경
                String startFormatted = start.replace('T', ' ') + ":00";
                String endFormatted = end.replace('T', ' ') + ":00";

                // Timestamp 객체로 변환
                Timestamp startTimestamp = Timestamp.valueOf(startFormatted);
                Timestamp endTimestamp = Timestamp.valueOf(endFormatted);

                // 유권자 CSV 파일을 읽어서 데이터베이스에 저장
                List<String> voters = readVotersFromCsv(voterCsvPart); // CSV에서 유권자 읽기
                int voterCount = voters.size()-1; // 유권자 수 계산
                
                // 데이터베이스에 저장하면서 vote_id를 받아옴
                int voteId = VoteListdb.savevotedb(title, startTimestamp, endTimestamp, email, voterCount);
      
                VoteListdb.saveVoters(voteId, voters); // 유권자 저장

                response.setContentType("application/json"); // 응답 타입을 JSON으로 설정
                response.setCharacterEncoding("UTF-8"); // 문자 인코딩을 UTF-8로 설정

                // JSON 형태로 vote_id 반환
                JSONObject responseJson = new JSONObject();
                responseJson.put("id", voteId); // vote_id를 JSON으로 설정

                response.setStatus(HttpServletResponse.SC_OK); // 응답 상태 코드 200 OK 설정
                response.getWriter().write(responseJson.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 토큰이 없거나 유효하지 않은 경우 401
                response.getWriter().write("Unauthorized: Token missing or invalid.");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 예외 발생 시 500 Internal Server Error 
            response.getWriter().write("Failed to save data.");
            e.printStackTrace();
        }
    }
	
	// CSV 파일에서 유권자 이메일을 읽어오는 메서드
    private List<String> readVotersFromCsv(Part voterCsvPart) throws IOException {
        List<String> voters = new ArrayList<>(); // 유권자 리스트

        try (BufferedReader br = new BufferedReader(new InputStreamReader(voterCsvPart.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                voters.add(line.trim()); // 각 줄을 유권자 리스트에 추가
            }
        }

        return voters; // 유권자 리스트 반환
    }
	
}
