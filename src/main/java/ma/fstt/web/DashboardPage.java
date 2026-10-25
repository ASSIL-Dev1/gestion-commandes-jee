package ma.fstt.web;

import ma.fstt.dao.*;
import ma.fstt.entities.Commande;
import javax.servlet.http.*;
import java.sql.SQLException;
import java.math.BigDecimal;
public class DashboardPage {
    public void handle(HttpServletRequest r,HttpServletResponse s) {
        r.setAttribute("active","dashboard");r.setAttribute("title","Vue d'ensemble");
        try {
            r.setAttribute("clientCount",new ClientDAO().findAll().size());r.setAttribute("produitCount",new ProduitDAO().findAll().size());
            var orders=new CommandeDAO().findAll();r.setAttribute("commandeCount",orders.size());r.setAttribute("recent",orders.stream().limit(5).toList());
            r.setAttribute("total",orders.stream().filter(o->!"ANNULEE".equals(o.getStatut())).map(Commande::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add));
        } catch(SQLException e){WebSupport.error(r,s,e);}
    }
}
