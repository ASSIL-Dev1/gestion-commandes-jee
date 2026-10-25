-- Données fictives. Importer une seule fois, après 01-schema.sql dans une base vide.
INSERT INTO client(id,nom,email,telephone,adresse) VALUES
 (1,'Atlas Digital','contact@atlas.example','0600000001','Tanger'),
 (2,'Studio Rif','bonjour@rif.example','0600000002','Tétouan'),
 (3,'Nour Services','contact@nour.example','0600000003','Rabat');
INSERT INTO produit(id,reference,designation,prix) VALUES
 (1,'PRD-001','Clavier sans fil',249.00),
 (2,'PRD-002','Souris ergonomique',179.00),
 (3,'PRD-003','Écran 24 pouces',1490.00),
 (4,'PRD-004','Support ordinateur',320.00);
INSERT INTO commande(id,client_id,date_commande,statut) VALUES
 (1,1,'2026-10-20','LIVREE'),(2,2,'2026-10-22','VALIDEE'),(3,3,'2026-10-24','EN_COURS');
INSERT INTO ligne_commande(commande_id,produit_id,quantite,prix_unitaire) VALUES
 (1,1,2,249.00),(1,2,2,179.00),(2,3,1,1490.00),(2,4,1,320.00),(3,2,3,179.00);
