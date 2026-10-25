package ma.fstt.dao;

import ma.fstt.config.Database;
import ma.fstt.entities.Client;
import ma.fstt.service.ClientService;
import java.sql.*;
import java.util.*;
public class ClientDAO implements ClientService {
    static Client map(ResultSet rs) throws SQLException {
        Client e = new Client(); e.setId(rs.getLong("id"));
        e.setNom(rs.getString("nom"));
        e.setEmail(rs.getString("email"));
        e.setTelephone(rs.getString("telephone"));
        e.setAdresse(rs.getString("adresse"));
        return e;
    }
    public List<Client> findAll() throws SQLException {
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement("SELECT * FROM client ORDER BY id DESC"); ResultSet rs = ps.executeQuery()) {
            List<Client> result = new ArrayList<>(); while (rs.next()) result.add(map(rs)); return result;
        }
    }
    public Optional<Client> findById(long id) throws SQLException {
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement("SELECT * FROM client WHERE id=?")) {
            ps.setLong(1, id); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        }
    }
    public void save(Client e) throws SQLException {
        boolean create = e.getId() == null;
        String sql = create ? "INSERT INTO client (nom, email, telephone, adresse) VALUES (?,?,?,?)" : "UPDATE client SET nom=?,email=?,telephone=?,adresse=? WHERE id=?";
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getEmail());
            ps.setString(3, e.getTelephone());
            ps.setString(4, e.getAdresse());
            if (!create) ps.setLong(5, e.getId());
            DaoSupport.requireChanged(ps.executeUpdate()); if (create) e.setId(DaoSupport.generatedId(ps));
        }
    }
    public void delete(long id) throws SQLException {
        try (Connection c = Database.open(); PreparedStatement ps = c.prepareStatement("DELETE FROM client WHERE id=?")) { ps.setLong(1, id); DaoSupport.requireChanged(ps.executeUpdate()); }
    }
}
