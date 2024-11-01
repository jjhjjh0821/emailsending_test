package mail;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Emailsender {
 
	static Properties mailServerProperties; // 메일 서버 속성을 저장할 변수
	static Session getMailSession; // 메일 세션을 설정할 변수
	static MimeMessage generateMailMessage; // 이메일 메시지를 생성할 변수
  
	// 이메일을 생성하고 전송
	public static String generateAndSendEmail(String mail) throws AddressException, MessagingException {
 
		// Step1: 메일 서버 속성 설정
		System.out.println("\n 1st ===> setup Mail Server Properties");
		mailServerProperties = System.getProperties(); // 기본 시스템 속성 가져오기
		// SMTP 서버 설정 (Gmail 사용)
		mailServerProperties.put("mail.smtp.host", "smtp.gmail.com"); // SMTP 서버 주소를 지정 (Gmail)
		mailServerProperties.put("mail.smtp.port", "587"); // SMTP 서버의 포트를 지정 (587번 포트는 TLS를 위한 포트)
		mailServerProperties.put("mail.smtp.connectiontimeout", "10000"); // 연결 시간 제한 설정 (5초 동안 서버와 연결이 안 될 경우 연결 시도를 중단)
		mailServerProperties.put("mail.smtp.auth", "true"); // SMTP 서버 연결 시 인증이 필요함을 명시
		mailServerProperties.put("mail.smtp.starttls.enable", "true"); // SSL/TLS 암호화를 활성화. 암호화되지 않은 SMTP 연결을 TLS로 업그레이드하는 것을 허용 (보안 강화)
		mailServerProperties.put("mail.smtp.starttls.required", "true"); // STARTTLS가 반드시 필요함을 명시. 서버가 TLS를 지원하지 않으면 연결이 중단
		mailServerProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com"); // SSL 인증서를 신뢰하도록 설정, Gmail 서버 인증서를 자동으로 신뢰 (보안 경고 방지)
		mailServerProperties.put("mail.smtp.ssl.protocols","TLSv1.2"); // SSL 통신에 사용할 프로토콜을 설정. Gmail에서는 TLSv1.2를 사용
		System.out.println("Mail Server Properties have been setup successfully");
 
		// Step2: 메일 세션 생성
		System.out.println("\n\n 2nd ===> get Mail Session");
		getMailSession = Session.getDefaultInstance(mailServerProperties, null);
		generateMailMessage = new MimeMessage(getMailSession); // 새로운 이메일 메시지 객체 생성
		generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(mail)); // 수신자 설정
		//generateMailMessage.addRecipient(Message.RecipientType.CC, new InternetAddress("")); // 참조 수신자 설정 가능
		generateMailMessage.setSubject("Email Checking"); // 이메일 제목 설정
		
		// OTP 생성 및 이메일 본문 작성
		String otp = null;
		try {
            otp = OTPGenerator.create(); // OTP 생성
            
            // 이메일 본문에 HTML 형식으로 OTP를 포함
            String emailBody = "<html>" +
            	    "<head>" +
            	    "<meta charset='UTF-8'>" +
            	    "<style>" +
            	    "body { font-family: Arial, sans-serif; background-color: #e9ecef; margin: 0; padding: 0; }" +
            	    ".container { width: 100%; max-width: 600px; margin: 30px auto; background: #ffffff; border-radius: 15px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); padding: 20px; }" +
            	    ".header { background-color: #007bff; color: #ffffff; padding: 10px; border-radius: 15px 15px 0 0; text-align: center; }" +
            	    ".content { padding: 20px; text-align: center; }" +
            	    ".otp { font-size: 28px; font-weight: bold; color: #007bff; }" +
            	    ".footer { font-size: 14px; color: #6c757d; text-align: center; margin-top: 20px; }" +
            	    "</style>" +
            	    "</head>" +
            	    "<body>" +
            	    "<div class='container'>" +
            	    "<div class='header'>" +
            	    "<h1>이메일 인증</h1>" +
            	    "</div>" +
            	    "<div class='content'>" +
            	    "<p>안녕하세요,</p>" +
            	    "<p>저희 서비스를 이용해 주셔서 감사합니다.</p>" +
            	    "<p>이메일 인증을 위한 일회성 비밀번호(OTP)는 다음과 같습니다:</p>" +
            	    "<p class='otp'>" + otp + "</p>" +
            	    "<p>이 OTP를 사용하여 등록 과정을 완료해 주세요.</p>" +
            	    "<p>만약 이 OTP를 요청하지 않으셨다면 이 이메일을 무시하셔도 됩니다.</p>" +
            	    "</div>" +
            	    "<div class='footer'>" +
            	    "<p>감사합니다,<br>JY</p>" +
            	    "</div>" +
            	    "</div>" +
            	    "</body>" +
            	    "</html>";

            generateMailMessage.setContent(emailBody, "text/html; charset=UTF-8"); // 이메일 본문을 HTML로 설정
        } catch (Exception e) {
            e.printStackTrace(); // 오류가 발생하면 로그를 출력
            System.out.println("OTPGenerator Error");
        }
		System.out.println("Mail Session has been created successfully");
 
		// Step3: 메일 전송 설정 및 전송
		System.out.println("\n\n 3rd ===> Get Session and Send mail");
		Transport transport = getMailSession.getTransport("smtp");  // SMTP 프로토콜 사용
 
		// Gmail 인증을 위해 발신자 이메일과 비밀번호를 입력
		// 2단계 인증을 사용 중인 경우 앱 비밀번호를 입력해야 함
		transport.connect("smtp.gmail.com", "whdtjftest@gmail.com", "ylbiaugrvdiwojec");
	      transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
	      transport.close();
		
		return otp; // 생성된 OTP 반환
	}
}