package ma.fstt.web;

import ma.fstt.dao.*;
import ma.fstt.entities.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
public class CommandePage {
    private final CommandeDAO dao=new CommandeDAO();
    public boolean handle(HttpServletRequest r,HttpServletResponse s) throws IOException {
        r.setAttribute("active","commandes"); r.setAttribute("title","Commandes"); WebSupport.flash(r);
        String action=r.getParameter("action"); action=action==null?"list":action;
        boolean post="POST".equals(r.getMethod());
        r.setAttribute("form",Set.of("new","edit","save").contains(action));
        r.setAttribute("detail","detail".equals(action));
        try {
            r.setAttribute("clients",new ClientDAO().findAll()); r.setAttribute("produits",new ProduitDAO().findAll());
            if(post) {
                if("delete".equals(action)) dao.delete(WebSupport.id(r));
                else if("save".equals(action)) {
                    Commande e=new Commande(); e.setId(WebSupport.optionalId(r.getParameter("id")));
                    Client client=new Client(); client.setId(WebSupport.optionalId(r.getParameter("clientId"))); e.setClient(client);
                    try { e.setDateCommande(LocalDate.parse(r.getParameter("dateCommande"))); if(e.getDateCommande().getYear()<1900 || e.getDateCommande().getYear()>2100) throw new IllegalArgumentException(); }
                    catch(Exception ex) { throw new IllegalArgumentException("Date invalide : année comprise entre 1900 et 2100."); }
                    e.setStatut(r.getParameter("statut"));
                    String[] products=r.getParameterValues("produitId"), quantities=r.getParameterValues("quantite");
                    if(products==null || quantities==null || products.length!=quantities.length || products.length<1 || products.length>50) throw new IllegalArgumentException("Ajoutez entre 1 et 50 lignes complètes.");
                    for(int i=0;i<products.length;i++) {
                        LigneDeCommande l=new LigneDeCommande(); Produit p=new Produit(); p.setId(WebSupport.optionalId(products[i])); l.setProduit(p);
                        try {l.setQuantite(Integer.parseInt(quantities[i]));} catch(Exception ex){throw new IllegalArgumentException("Quantité entière requise.");}
                        e.getLignes().add(l);
                    }
                    dao.save(e);
                } else throw new IllegalArgumentException("Action inconnue.");
                WebSupport.redirect(r,s,"/commandes"); return true;
            }
            if(!Set.of("list","new","edit","detail").contains(action)) throw new IllegalArgumentException("Action inconnue.");
            if(Set.of("edit","detail").contains(action)) r.setAttribute("item",dao.findById(WebSupport.id(r)).orElseThrow(()->new IllegalArgumentException("Commande introuvable.")));
            r.setAttribute("today",LocalDate.now()); r.setAttribute("items",dao.findAll());
        } catch(SQLException|IllegalArgumentException ex) {
            WebSupport.error(r,s,ex);
            if(!Boolean.TRUE.equals(r.getAttribute("form"))) try {r.setAttribute("items",dao.findAll());} catch(SQLException ignored) {}
        }
        // Reconstituer les lignes après validation refusée, sans perdre les saisies.
        if(post && "save".equals(action)) {
            List<LigneDeCommande> submitted=new ArrayList<>();
            String[] p=r.getParameterValues("produitId"), q=r.getParameterValues("quantite");
            if(p!=null && q!=null && p.length==q.length) for(int i=0;i<Math.min(50,p.length);i++) {
                LigneDeCommande l=new LigneDeCommande(); Produit product=new Produit();
                try {product.setId(WebSupport.optionalId(p[i]));l.setQuantite(Integer.parseInt(q[i]));} catch(IllegalArgumentException ignored) {l.setQuantite(1);}
                l.setProduit(product); submitted.add(l);
            }
            r.setAttribute("submittedLines",submitted);
        }
        return false;
    }
}
