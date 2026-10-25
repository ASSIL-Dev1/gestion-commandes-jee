package ma.fstt.web;

import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
public final class WebSupport {
    private WebSupport() {}
    public static Long optionalId(String value) {
        if(value==null || value.isBlank()) return null;
        try { long n=Long.parseLong(value); if(n>0) return n; } catch(NumberFormatException ignored) {}
        throw new IllegalArgumentException("Identifiant invalide.");
    }
    public static long id(HttpServletRequest r) { Long id=optionalId(r.getParameter("id")); if(id==null) throw new IllegalArgumentException("Identifiant manquant."); return id; }
    public static String text(HttpServletRequest r,String key,String label,int max,boolean required) {
        String value=r.getParameter(key); value=value==null?"":value.trim();
        if((required && value.isEmpty()) || value.length()>max) throw new IllegalArgumentException(label+" : valeur obligatoire ou longueur maximale de "+max+" caractères.");
        return value;
    }
    public static BigDecimal price(String input) {
        try { BigDecimal p=new BigDecimal(input.replace(',','.')).setScale(2,RoundingMode.UNNECESSARY);
            if(p.signum()<0 || p.compareTo(new BigDecimal("99999999.99"))>0) throw new NumberFormatException(); return p;
        } catch(Exception e) { throw new IllegalArgumentException("Prix invalide : de 0 à 99999999,99, avec deux décimales au maximum."); }
    }
    public static void flash(HttpServletRequest r) { Object m=r.getSession().getAttribute("flash"); if(m!=null){r.setAttribute("success",m);r.getSession().removeAttribute("flash");} }
    public static void redirect(HttpServletRequest r,HttpServletResponse s,String route) throws IOException { r.getSession().setAttribute("flash","Opération effectuée avec succès."); s.sendRedirect(r.getContextPath()+route); }
    public static void error(HttpServletRequest r,HttpServletResponse s,Exception e) {
        if(e instanceof IllegalArgumentException) { r.setAttribute("error",e.getMessage()); s.setStatus(400); }
        else { r.getServletContext().log("Erreur de persistance",e);
            String state=e instanceof SQLException ? ((SQLException)e).getSQLState() : null;
            r.setAttribute("error",state!=null && state.startsWith("23") ? "Opération impossible : référence déjà utilisée, doublon ou données liées à une commande." : "Base de données indisponible. Vérifiez MySQL et la configuration DB_URL / DB_USER / DB_PASSWORD."); s.setStatus(state!=null && state.startsWith("23")?409:503);
        }
    }
}
