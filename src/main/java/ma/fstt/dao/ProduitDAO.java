package ma.fstt.dao;

import ma.fstt.config.Database;
import ma.fstt.entities.Produit;
import ma.fstt.service.ProduitService;
import java.sql.*;
import java.util.*;
public class ProduitDAO implements ProduitService {
    static Produit map(ResultSet rs) throws SQLException {
        Produit e = new Produit(); e.setId(rs.getLong("id"));
        e.setReference(rs.getString("reference"));
        e.setDesignation(rs.getString("designation"));
        e.setPrix(rs.getBigDecimal("prix"));
        return e;
    }
    public List<Produit> findAll() throws SQLException {
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement("SELECT * FROM produit ORDER BY id DESC"); ResultSet rs = ps.executeQuery()) {
            List<Produit> result = new ArrayList<>(); while (rs.next()) result.add(map(rs)); return result;
        }
    }
    public Optional<Produit> findById(long id) throws SQLException {
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement("SELECT * FROM produit WHERE id=?")) {
            ps.setLong(1, id); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        }
    }
    public void save(Produit e) throws SQLException {
        boolean create = e.getId() == null;
        String sql = create ? "INSERT INTO produit (reference, designation, prix) VALUES (?,?,?)" : "UPDATE produit SET reference=?,designation=?,prix=? WHERE id=?";
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getReference());
            ps.setString(2, e.getDesignation());
            ps.setBigDecimal(3, e.getPrix());
            if (!create) ps.setLong(4, e.getId());
            DaoSupport.requireChanged(ps.executeUpdate()); if (create) e.setId(DaoSupport.generatedId(ps));
        }
    }
    public void delete(long id) throws SQLException {
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement("DELETE FROM produit WHERE id=?")) { ps.setLong(1, id); DaoSupport.requireChanged(ps.executeUpdate()); }
    }
}
