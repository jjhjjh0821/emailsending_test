package user;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

public class JWTUtils {
    private static final String SECRET_KEY = "your-secret-key"; // JWT 비밀키 (수정할것)

    public static String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject(); // 일반적으로 subject에 이메일이 저장
        } catch (SignatureException e) {
            throw new RuntimeException("Invalid JWT signature");
        }
    }
}

