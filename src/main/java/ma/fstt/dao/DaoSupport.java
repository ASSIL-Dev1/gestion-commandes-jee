package ma.fstt.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
final class DaoSupport {
    private DaoSupport() {}
    static long generatedId(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) return keys.getLong(1);
            throw new SQLException("Identifiant non généré");
        }
    }
    static void requireChanged(int count) {
        if (count == 0) throw new IllegalArgumentException("Cet enregistrement n'existe plus. Rechargez la liste.");
    }
}
