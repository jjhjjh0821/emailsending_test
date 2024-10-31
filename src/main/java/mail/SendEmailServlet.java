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
        
        // 전송된 JSON 데이터에서 이메일 주소 추출
        String email = jsonData.replace("\"", ""); // JSON 문자열에서 따옴표 제거
        
        try {
        	// 이메일 발송 메서드 호출하여 이메일로 OTP 발송
        	String otp = Emailsender.generateAndSendEmail(email);
        	
        	// OTP 생성 및 메일 발송이 성공했는지 여부 확인
            if (otp != null) {
                // OTP를 클라이언트에게 반환하여 성공 응답 전송
            	response.getWriter().write(otp);
            } else {
            	// OTP 생성에 실패한 경우 실패 메시지 전송
                response.getWriter().write("Failed to generate OTP.");
            }
           
        } catch (Exception e) {
        	// 메일 발송 중 오류 발생 시 클라이언트에게 오류 메시지 응답
            response.getWriter().write("Failed to send mail.");
            e.printStackTrace(); // 오류 로그 출력
        }
    }
	
}