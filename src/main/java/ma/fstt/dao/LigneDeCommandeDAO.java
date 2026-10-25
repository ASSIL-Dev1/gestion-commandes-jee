package ma.fstt.dao;

import ma.fstt.entities.*;
import ma.fstt.service.LigneDeCommandeService;
import java.sql.*;
import java.util.*;
public class LigneDeCommandeDAO implements LigneDeCommandeService {
    public List<LigneDeCommande> findByCommande(Connection c, long id) throws SQLException {
        String sql = "SELECT l.id ligne_id,l.commande_id,l.quantite,l.prix_unitaire,p.* FROM ligne_commande l JOIN produit p ON p.id=l.produit_id WHERE l.commande_id=? ORDER BY l.id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1,id); List<LigneDeCommande> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) {
                LigneDeCommande l = new LigneDeCommande(); l.setId(rs.getLong("ligne_id")); l.setCommandeId(id);
                l.setProduit(ProduitDAO.map(rs)); l.setQuantite(rs.getInt("quantite")); l.setPrixUnitaire(rs.getBigDecimal("prix_unitaire")); result.add(l);
            }} return result;
        }
    }
    public void replace(Connection c, long id, List<LigneDeCommande> lignes) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM ligne_commande WHERE commande_id=?")) { ps.setLong(1,id); ps.executeUpdate(); }
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO ligne_commande(commande_id,produit_id,quantite,prix_unitaire) VALUES(?,?,?,?)")) {
            for (LigneDeCommande l : lignes) { ps.setLong(1,id); ps.setLong(2,l.getProduit().getId()); ps.setInt(3,l.getQuantite()); ps.setBigDecimal(4,l.getPrixUnitaire()); ps.addBatch(); }
            ps.executeBatch();
        }
    }
}
