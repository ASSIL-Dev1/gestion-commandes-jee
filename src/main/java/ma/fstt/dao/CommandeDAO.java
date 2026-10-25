package ma.fstt.dao;

import ma.fstt.config.Database;
import ma.fstt.entities.*;
import ma.fstt.service.CommandeService;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
public class CommandeDAO implements CommandeService {
    private final LigneDeCommandeDAO lignesDAO = new LigneDeCommandeDAO();
    private Commande map(Connection c, ResultSet rs) throws SQLException {
        Commande e = new Commande(); e.setId(rs.getLong("id")); e.setDateCommande(rs.getDate("date_commande").toLocalDate()); e.setStatut(rs.getString("statut"));
        Client client = new Client(); client.setId(rs.getLong("client_id")); client.setNom(rs.getString("client_nom")); e.setClient(client);
        e.setLignes(lignesDAO.findByCommande(c,e.getId())); return e;
    }
    public List<Commande> findAll() throws SQLException {
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement("SELECT co.*,cl.nom client_nom FROM commande co JOIN client cl ON cl.id=co.client_id ORDER BY co.id DESC"); ResultSet rs = ps.executeQuery()) {
            List<Commande> result = new ArrayList<>(); while (rs.next()) result.add(map(c,rs)); return result;
        }
    }
    public Optional<Commande> findById(long id) throws SQLException {
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement("SELECT co.*,cl.nom client_nom FROM commande co JOIN client cl ON cl.id=co.client_id WHERE co.id=?")) {
            ps.setLong(1,id); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(c,rs)) : Optional.empty(); }
        }
    }
    public void save(Commande e) throws SQLException {
        if (e.getClient()==null || e.getClient().getId()==null || e.getDateCommande()==null || !Set.of("EN_COURS","VALIDEE","LIVREE","ANNULEE").contains(e.getStatut())) throw new IllegalArgumentException("Commande incomplète.");
        if (e.getLignes()==null || e.getLignes().isEmpty() || e.getLignes().size()>50) throw new IllegalArgumentException("Une commande doit contenir de 1 à 50 lignes.");
        Set<Long> ids = new HashSet<>();
        for (LigneDeCommande l:e.getLignes()) if(l.getProduit()==null || l.getProduit().getId()==null || !ids.add(l.getProduit().getId()) || l.getQuantite()<1 || l.getQuantite()>100000) throw new IllegalArgumentException("Produits distincts et quantités de 1 à 100000 requis.");
        Long originalId = e.getId();
        try (Connection c = Database.open()) {
            c.setAutoCommit(false);
            try {
                Map<Long,BigDecimal> historical = new HashMap<>();
                if (originalId != null) {
                    try (PreparedStatement lock = c.prepareStatement("SELECT id FROM commande WHERE id=? FOR UPDATE")) {
                        lock.setLong(1,originalId); try (ResultSet rs=lock.executeQuery()) { if(!rs.next()) throw new IllegalArgumentException("Commande introuvable."); }
                    }
                    for (LigneDeCommande l:lignesDAO.findByCommande(c,originalId)) historical.put(l.getProduit().getId(),l.getPrixUnitaire());
                }
                // Le prix transmis par le navigateur n'est jamais accepté.
                try (PreparedStatement product = c.prepareStatement("SELECT prix FROM produit WHERE id=?")) {
                    for (LigneDeCommande l:e.getLignes()) {
                        product.setLong(1,l.getProduit().getId());
                        try (ResultSet rs=product.executeQuery()) {
                            if(!rs.next()) throw new IllegalArgumentException("Un produit n'existe plus.");
                            l.setPrixUnitaire(historical.getOrDefault(l.getProduit().getId(),rs.getBigDecimal("prix")));
                        }
                    }
                }
                String sql=originalId==null ? "INSERT INTO commande(client_id,date_commande,statut) VALUES(?,?,?)" : "UPDATE commande SET client_id=?,date_commande=?,statut=? WHERE id=?";
                try (PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1,e.getClient().getId()); ps.setDate(2,java.sql.Date.valueOf(e.getDateCommande())); ps.setString(3,e.getStatut());
                    if(originalId!=null) ps.setLong(4,originalId); DaoSupport.requireChanged(ps.executeUpdate());
                    if(originalId==null) e.setId(DaoSupport.generatedId(ps));
                }
                lignesDAO.replace(c,e.getId(),e.getLignes()); c.commit();
            } catch (SQLException | RuntimeException ex) {
                try { c.rollback(); } catch(SQLException rollback) { ex.addSuppressed(rollback); }
                e.setId(originalId); throw ex;
            }
        }
    }
    public void delete(long id) throws SQLException {
        // La FK ON DELETE CASCADE supprime les lignes dans la même instruction atomique.
        try (Connection c=Database.open(); PreparedStatement ps=c.prepareStatement("DELETE FROM commande WHERE id=?")) { ps.setLong(1,id); DaoSupport.requireChanged(ps.executeUpdate()); }
    }
}
