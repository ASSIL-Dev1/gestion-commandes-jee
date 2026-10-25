package ma.fstt.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import ma.fstt.entities.LigneDeCommande;
/** Les lignes sont modifiées uniquement dans la transaction de leur commande. */
public interface LigneDeCommandeService {
    List<LigneDeCommande> findByCommande(Connection connection, long commandeId) throws SQLException;
    void replace(Connection connection, long commandeId, List<LigneDeCommande> lignes) throws SQLException;
}
