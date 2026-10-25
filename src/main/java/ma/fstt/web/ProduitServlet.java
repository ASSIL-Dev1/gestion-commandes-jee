package ma.fstt.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
/** Alias d'URL uniquement : le traitement MVC1 appartient à la JSP et à son bean de page. */
@WebServlet("/produits")
public class ProduitServlet extends HttpServlet {
    protected void service(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        if(!"GET".equals(request.getMethod()) && !"POST".equals(request.getMethod())) { response.sendError(405);return; }
        request.getRequestDispatcher("/WEB-INF/pages/produits.jsp").forward(request,response);
    }
}
