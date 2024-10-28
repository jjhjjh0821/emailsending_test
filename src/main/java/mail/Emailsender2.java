package mail;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Emailsender2 {
    public static void main(String[] args) {
        String password = "1234"; // 해시할 비밀번호

        try {
            // MessageDigest 인스턴스를 SHA-256 알고리즘으로 가져오기
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            // 비밀번호를 바이트 배열로 변환 후 해시 계산
            byte[] hash = md.digest(password.getBytes());
            
            // 바이트 배열을 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            // 해시 값 출력
            System.out.println("SHA-256 해시: " + hexString.toString());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
