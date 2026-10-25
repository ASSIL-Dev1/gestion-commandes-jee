package ma.fstt.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LigneDeCommande implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long commandeId;
    private Produit produit;
    private int quantite;
    private BigDecimal prixUnitaire;
    public LigneDeCommande() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCommandeId() { return commandeId; }
    public void setCommandeId(Long commandeId) { this.commandeId = commandeId; }
    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public BigDecimal getSousTotal() { return prixUnitaire == null ? BigDecimal.ZERO : prixUnitaire.multiply(BigDecimal.valueOf(quantite)); }
}
