package ma.fstt.web;

import ma.fstt.dao.ProduitDAO;
import ma.fstt.entities.Produit;
import ma.fstt.service.ProduitService;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
public class ProduitPage {
    private final ProduitService dao=new ProduitDAO();
    public boolean handle(HttpServletRequest r,HttpServletResponse s) throws IOException {
        r.setAttribute("active","produits"); r.setAttribute("title","Produits"); WebSupport.flash(r);
        String action=r.getParameter("action"); action=action==null?"list":action;
        r.setAttribute("form",action.equals("new")||action.equals("edit"));
        try {
            if("POST".equals(r.getMethod())) {
                if("delete".equals(action)) dao.delete(WebSupport.id(r));
                else if("save".equals(action)) {
                    r.setAttribute("form",true); Produit e=new Produit(); e.setId(WebSupport.optionalId(r.getParameter("id")));
                    e.setReference(WebSupport.text(r,"reference","Référence",40,true));
                    e.setDesignation(WebSupport.text(r,"designation","Désignation",180,true));
                    e.setPrix(WebSupport.price(r.getParameter("prix")));

                    dao.save(e);
                } else throw new IllegalArgumentException("Action inconnue.");
                WebSupport.redirect(r,s,"/produits"); return true;
            }
            if(!java.util.Set.of("list","new","edit").contains(action)) throw new IllegalArgumentException("Action inconnue.");
            if("edit".equals(action)) r.setAttribute("item",dao.findById(WebSupport.id(r)).orElseThrow(()->new IllegalArgumentException("Enregistrement introuvable.")));
            r.setAttribute("items",dao.findAll());
        } catch(SQLException|IllegalArgumentException e) {
            WebSupport.error(r,s,e);
            if(!Boolean.TRUE.equals(r.getAttribute("form"))) try { r.setAttribute("items",dao.findAll()); } catch(SQLException ignored) {}
        }
        return false;
    }
}
