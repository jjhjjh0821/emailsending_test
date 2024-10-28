package mail;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;

@WebServlet("/emailsend")
public class SendEmailServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // 클라이언트로부터 전송된 데이터를 읽어옴
        String jsonData = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        
        // 전송된 JSON 데이터를 이메일 주소로 추출
        String email = jsonData.replace("\"", ""); // JSON 문자열에서 따옴표 제거
        
        try {
            // DB 클래스의 savedb 메서드 호출하여 데이터베이스에 저장
        	String otp = Emailsender.generateAndSendEmail(email);
        	
            // 클라이언트에게 OTP를 반환
            if (otp != null) {
                // otp와 함께 성공적으로 메일을 보냈음을 클라이언트에 응답
            	response.getWriter().write(otp);
            } else {
                response.getWriter().write("Failed to generate OTP.");
            }
           
        } catch (Exception e) {
            // 저장 중 오류 발생 시 클라이언트에 응답
            response.getWriter().write("Failed to send mail.");
            e.printStackTrace(); // 오류 로그 출력
        }
    }
	
}