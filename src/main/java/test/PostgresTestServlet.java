package test;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/testPostgres")
public class PostgresTestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres"; // EC2의 공인 IP 주소 입력
        String dbUsername = "postgres"; // PostgreSQL 사용자 이름
        String dbPassword = "1234"; // PostgreSQL 비밀번호

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // PostgreSQL 드라이버 로드
            Class.forName("org.postgresql.Driver");

            // 데이터베이스 연결
            Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            out.println("<h1>데이터베이스에 성공적으로 연결되었습니다.3</h1>");

            // 쿼리 실행
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT tablename FROM pg_tables WHERE schemaname='public'");

            // 테이블 목록 출력
            out.println("<h2>현재 데이터베이스의 테이블 목록3:</h2>");
            out.println("<ul>");
            while (resultSet.next()) {
                out.println("<li>" + resultSet.getString("tablename") + "</li>");
            }
            out.println("</ul>");

            // 리소스 정리
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            // 오류 처리: 상세한 에러 메시지 출력
            out.println("<h2>데이터베이스 연결 실패3:</h2>");
            out.println("<pre>" + e.getClass().getName() + ": " + e.getMessage() + "</pre>");
            out.println("<pre>스택 트레이스:</pre>");
            for (StackTraceElement element : e.getStackTrace()) {
                out.println("<pre>" + element.toString() + "</pre>");
            }
        } finally {
            out.close();
        }
    }
}
