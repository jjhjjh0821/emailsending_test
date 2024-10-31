package login;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {
	
	public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); // MessageDigest 객체 생성, SHA-256 이용 
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder(); // 해시값을 16진수 문자열로 변환
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString(); 
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // SHA-256 알고리즘을 찾을 수 없는 경우 RuntimeException 발생
        }
    }

}
