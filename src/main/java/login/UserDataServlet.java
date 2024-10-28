package login;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;

@WebServlet("/dbsave")
public class UserDataServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 클라이언트로부터 전송된 데이터를 받아옴
        String name = request.getParameter("name");
        String dob = request.getParameter("dob");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // 비밀번호 해시화
        String hashedPassword = PasswordUtils.hashPassword(password);
        
        try {
            // DB 클래스의 savedb 메서드 호출하여 데이터베이스에 저장
            db.savedb(name, dob, email, hashedPassword);
            
            // 성공적으로 데이터베이스에 저장되었음을 클라이언트에 응답
            response.getWriter().write("Data saved successfully!");
        } catch (Exception e) {
            // 저장 중 오류 발생 시 클라이언트에 응답
            response.getWriter().write("Failed to save data.");
            e.printStackTrace(); // 오류 로그 출력
        }
    }
}

