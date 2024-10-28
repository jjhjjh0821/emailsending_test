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
 
	static Properties mailServerProperties;
	static Session getMailSession;
	static MimeMessage generateMailMessage;
  
	public static String generateAndSendEmail(String mail) throws AddressException, MessagingException {
 
		// Step1
		System.out.println("\n 1st ===> setup Mail Server Properties..");
		mailServerProperties = System.getProperties();
		mailServerProperties.put("mail.smtp.host", "smtp.gmail.com");
		mailServerProperties.put("mail.smtp.port", "587");
		mailServerProperties.put("mail.smtp.connectiontimeout", "5000");
		mailServerProperties.put("mail.smtp.auth", "true");
		mailServerProperties.put("mail.smtp.starttls.enable", "true");
		mailServerProperties.put("mail.smtp.starttls.required", "true");
		mailServerProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
		mailServerProperties.put("mail.smtp.ssl.protocols","TLSv1.2");
		System.out.println("Mail Server Properties have been setup successfully..");
 
		// Step2
		System.out.println("\n\n 2nd ===> get Mail Session..");
		getMailSession = Session.getDefaultInstance(mailServerProperties, null);
		generateMailMessage = new MimeMessage(getMailSession);
		generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(mail));
		generateMailMessage.setSubject("Email Checking");
		
		String otp = null;
		try {
            otp = OTPGenerator.create(); // Generate OTP
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

            generateMailMessage.setContent(emailBody, "text/html; charset=UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception appropriately, perhaps log it
            System.out.println("OTPGenerator Error..");
        }
		System.out.println("Mail Session has been created successfully..");
 
		// Step3
		System.out.println("\n\n 3rd ===> Get Session and Send mail");
		Transport transport = getMailSession.getTransport("smtp");
 
		// Enter your correct gmail UserID and Password
		transport.connect("smtp.gmail.com", "whdtjftest@gmail.com", "ylbiaugrvdiwojec");
		transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
		transport.close();
		
		return otp;
	}
}