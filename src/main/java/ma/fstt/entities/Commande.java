package ma.fstt.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Commande implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Client client;
    private LocalDate dateCommande;
    private String statut = "EN_COURS";
    private List<LigneDeCommande> lignes = new ArrayList<>();
    public Commande() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public LocalDate getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDate dateCommande) { this.dateCommande = dateCommande; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public List<LigneDeCommande> getLignes() { return lignes; }
    public void setLignes(List<LigneDeCommande> lignes) { this.lignes = lignes; }
    public BigDecimal getTotal() { return lignes.stream().map(LigneDeCommande::getSousTotal).reduce(BigDecimal.ZERO, BigDecimal::add); }
}
