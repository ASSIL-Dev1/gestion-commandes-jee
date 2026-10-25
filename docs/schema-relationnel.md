# Schéma relationnel

| Table | Colonnes | Contraintes principales |
| --- | --- | --- |
| client | **id**, nom, email, telephone, adresse | PK id, UNIQUE email |
| produit | **id**, reference, designation, prix | PK id, UNIQUE reference, prix positif ou nul |
| commande | **id**, client_id, date_commande, statut | PK id, FK client_id → client.id, statut contrôlé |
| ligne_commande | **id**, commande_id, produit_id, quantite, prix_unitaire | PK id, deux FK, UNIQUE(commande_id, produit_id), quantité > 0 |

Les identifiants sont BIGINT AUTO_INCREMENT ; les prix sont DECIMAL(12,2). Toutes les colonnes sont NOT NULL ; le téléphone et l'adresse peuvent être des chaînes vides. Les dates sont des DATE.

Client 1 — 0..* Commande ; Commande 1 — 1..* LigneDeCommande ; Produit 1 — 0..* LigneDeCommande.

La contrainte d'au moins une ligne et la limite de 50 lignes sont assurées par la couche Java lors de l'enregistrement de l'agrégat commande. Les clés étrangères SQL assurent les références, mais ne peuvent pas garantir à elles seules cette cardinalité minimale.

`ON DELETE RESTRICT` protège les clients et les produits utilisés ; `ON DELETE CASCADE` supprime les lignes d'une commande supprimée. La contrainte UNIQUE empêche la répétition d'un produit dans une même commande.

Le prix de ligne est un instantané du prix de vente. Il évite de recalculer les anciennes commandes avec le prix actuel du catalogue. Le total est dérivé, donc aucune colonne total ne peut devenir incohérente.

Le script définit les index de recherche par client et par produit. Utiliser InnoDB (moteur par défaut de MySQL 8) pour les transactions et les clés étrangères.
