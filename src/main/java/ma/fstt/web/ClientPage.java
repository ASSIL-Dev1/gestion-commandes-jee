package ma.fstt.web;

import ma.fstt.dao.ClientDAO;
import ma.fstt.entities.Client;
import ma.fstt.service.ClientService;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
public class ClientPage {
    private final ClientService dao=new ClientDAO();
    public boolean handle(HttpServletRequest r,HttpServletResponse s) throws IOException {
        r.setAttribute("active","clients"); r.setAttribute("title","Clients"); WebSupport.flash(r);
        String action=r.getParameter("action"); action=action==null?"list":action;
        r.setAttribute("form",action.equals("new")||action.equals("edit"));
        try {
            if("POST".equals(r.getMethod())) {
                if("delete".equals(action)) dao.delete(WebSupport.id(r));
                else if("save".equals(action)) {
                    r.setAttribute("form",true); Client e=new Client(); e.setId(WebSupport.optionalId(r.getParameter("id")));
                    e.setNom(WebSupport.text(r,"nom","Nom",120,true));
                    e.setEmail(WebSupport.text(r,"email","Email",180,true));
                    e.setTelephone(WebSupport.text(r,"telephone","Téléphone",30,false));
                    e.setAdresse(WebSupport.text(r,"adresse","Adresse",255,false));

                    if (!e.getEmail().matches("^[^\\s@]+@[^\\s@]+[.][^\\s@]+$")) throw new IllegalArgumentException("Adresse email invalide.");

                    dao.save(e);
                } else throw new IllegalArgumentException("Action inconnue.");
                WebSupport.redirect(r,s,"/clients"); return true;
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
