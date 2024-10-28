package login;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

//@WebFilter("/*") // 모든 요청에 대해 필터 적용
@WebFilter("/login") // 로그인 요청에 대해 필터 적용
public class JwtFilter implements Filter {
    private static final String SECRET_KEY = "your-secret-key"; // JWT 비밀키 (수정할것)

    /*@Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 필터 초기화 코드 (필요시)
    }*/

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request; // 요청을 HttpServletRequest로 변환
        HttpServletResponse httpResponse = (HttpServletResponse) response; // 응답을 HttpServletResponse로 변환
        
        String path = httpRequest.getRequestURI(); // 요청 URI 가져오기

        // 로그인 요청을 필터 제외 
        if (path.equals("/emailsending/login") || path.equals("/Loggingin.html")|| path.equals("/Chekmail.html") ) {
            chain.doFilter(request, response); // 필터 없이 요청을 처리하도록 다음 필터로 넘김
            return;
        }
        
        String authHeader = httpRequest.getHeader("Authorization"); // Authorization 헤더 가져오기

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
        	
            String token = authHeader.substring(7); // "Bearer " 이후의 토큰 부분 추출
            
            try {
            	// 토큰을 파싱하고 유효성 검사
                Claims claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY) // 비밀키를 이용해 토큰을 파싱
                        .parseClaimsJws(token)
                        .getBody(); // 토큰의 페이로드(토큰의 실제 정보가 들어있음) 부분을 가져옴

                // 토큰이 유효하면 요청을 계속 진행
                chain.doFilter(request, response);
            } catch (Exception e) {
            	// 토큰이 유효하지 않은 경우
            	e.printStackTrace();
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token"); // 401 에러 반환
            }
        } else {
        	// 인증 헤더가 없거나 잘못된 경우
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header missing or invalid"); // 401 에러 반환
        }
    }

    /*@Override
    public void destroy() {
        // 필터 종료 코드 (필요시)
    }*/
}
