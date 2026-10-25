package ma.fstt;
import ma.fstt.config.Database;
import ma.fstt.dao.*;
import ma.fstt.entities.*;
import ma.fstt.web.WebSupport;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class DaoTest {
    ClientDAO clients=new ClientDAO(); ProduitDAO produits=new ProduitDAO(); CommandeDAO orders=new CommandeDAO();
    @BeforeEach void setup() throws Exception {TestDatabase.reset("dao",true);}
    private LigneDeCommande line(long id,int quantity){LigneDeCommande l=new LigneDeCommande();Produit p=new Produit();p.setId(id);l.setProduit(p);l.setQuantite(quantity);l.setPrixUnitaire(new BigDecimal("0.01"));return l;}
    private Commande order(){Commande o=new Commande();Client c=new Client();c.setId(1L);o.setClient(c);o.setDateCommande(LocalDate.of(2026,10,24));o.getLignes().add(line(1,2));return o;}
    @Test void clientCrud() throws Exception {Client c=new Client();c.setNom("Élodie");c.setEmail("elodie@example.test");c.setAdresse("");c.setTelephone("");clients.save(c);assertNotNull(c.getId());c.setNom("Élodie Martin");clients.save(c);assertEquals(c.getNom(),clients.findById(c.getId()).orElseThrow().getNom());clients.delete(c.getId());assertTrue(clients.findById(c.getId()).isEmpty());}
    @Test void productCrud() throws Exception {Produit p=new Produit();p.setReference("TEST");p.setDesignation("Produit test");p.setPrix(new BigDecimal("12.30"));produits.save(p);p.setPrix(new BigDecimal("15.45"));produits.save(p);assertEquals(p.getPrix(),produits.findById(p.getId()).orElseThrow().getPrix());produits.delete(p.getId());assertTrue(produits.findById(p.getId()).isEmpty());}
    @Test void orderCrudAndServerPrice() throws Exception {Commande o=order();orders.save(o);assertEquals(new BigDecimal("498.00"),orders.findById(o.getId()).orElseThrow().getTotal());o.setStatut("VALIDEE");o.getLignes().add(line(4,1));orders.save(o);assertEquals(new BigDecimal("818.00"),orders.findById(o.getId()).orElseThrow().getTotal());orders.delete(o.getId());assertTrue(orders.findById(o.getId()).isEmpty());try(Connection c=Database.open();var ps=c.prepareStatement("SELECT COUNT(*) FROM ligne_commande WHERE commande_id=?")){ps.setLong(1,o.getId());try(var r=ps.executeQuery()){r.next();assertEquals(0,r.getInt(1));}}}
    @Test void historicalPricePreserved() throws Exception {Commande o=orders.findById(1).orElseThrow();Produit p=produits.findById(1).orElseThrow();p.setPrix(new BigDecimal("999.00"));produits.save(p);o.getLignes().get(0).setQuantite(3);orders.save(o);assertEquals(new BigDecimal("1105.00"),orders.findById(1).orElseThrow().getTotal());}
    @Test void linesCanBeAddedAndRemoved() throws Exception {Commande o=orders.findById(1).orElseThrow();o.setLignes(new ArrayList<>(List.of(line(3,2))));orders.save(o);var actual=orders.findById(1).orElseThrow();assertEquals(1,actual.getLignes().size());assertEquals(new BigDecimal("2980.00"),actual.getTotal());}
    @Test void duplicateProductRejected() {Commande o=order();o.getLignes().add(line(1,1));assertThrows(IllegalArgumentException.class,()->orders.save(o));}
    @Test void emptyAndInvalidQuantityRejected() {Commande o=order();o.getLignes().get(0).setQuantite(0);assertThrows(IllegalArgumentException.class,()->orders.save(o));o.getLignes().clear();assertThrows(IllegalArgumentException.class,()->orders.save(o));}
    @Test void foreignKeysProtectHistory() {assertThrows(SQLException.class,()->clients.delete(1));assertThrows(SQLException.class,()->produits.delete(1));}
    @Test void rollbackRestoresHeaderAndLines() throws Exception {
        Commande o=orders.findById(1).orElseThrow();BigDecimal before=o.getTotal();o.setStatut("ANNULEE");o.getLignes().get(0).setQuantite(13);
        try(Connection c=Database.open()){c.createStatement().execute("ALTER TABLE ligne_commande ADD CONSTRAINT test_failure CHECK(quantite<>13)");}
        assertThrows(SQLException.class,()->orders.save(o));Commande restored=orders.findById(1).orElseThrow();assertEquals("LIVREE",restored.getStatut());assertEquals(before,restored.getTotal());
    }
    @Test void invalidProductLeavesNoPartialOrder() throws Exception {Commande o=order();o.getLignes().add(line(9999,1));int before=orders.findAll().size();assertThrows(IllegalArgumentException.class,()->orders.save(o));assertNull(o.getId());assertEquals(before,orders.findAll().size());}
    @Test void duplicateEmailRejected() throws Exception {Client c=clients.findById(1).orElseThrow();c.setId(null);assertThrows(SQLException.class,()->clients.save(c));}
    @Test void decimalValidation() {assertEquals(new BigDecimal("12.30"),WebSupport.price("12,30"));assertThrows(IllegalArgumentException.class,()->WebSupport.price("-1"));assertThrows(IllegalArgumentException.class,()->WebSupport.price("2.001"));assertThrows(IllegalArgumentException.class,()->WebSupport.price("NaN"));}
}
