package ma.fstt.web;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
@WebFilter("/*")
public class SecurityFilter implements Filter {
    private final SecureRandom random=new SecureRandom();
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws IOException,ServletException {
        HttpServletRequest r=(HttpServletRequest)request;HttpServletResponse s=(HttpServletResponse)response;
        r.setCharacterEncoding("UTF-8");s.setCharacterEncoding("UTF-8");
        s.setHeader("X-Content-Type-Options","nosniff");s.setHeader("X-Frame-Options","DENY");s.setHeader("Referrer-Policy","same-origin");
        if(!r.getRequestURI().startsWith(r.getContextPath()+"/assets/")) {
            s.setHeader("Cache-Control","no-store");HttpSession session=r.getSession();
            synchronized(session) { if(session.getAttribute("csrf")==null){byte[] b=new byte[32];random.nextBytes(b);session.setAttribute("csrf",Base64.getUrlEncoder().withoutPadding().encodeToString(b));} }
            if("POST".equals(r.getMethod()) && !session.getAttribute("csrf").equals(r.getParameter("csrf"))) {s.sendError(403,"Formulaire expiré ou jeton invalide. Rechargez la page.");return;}
        }
        chain.doFilter(request,response);
    }
}
