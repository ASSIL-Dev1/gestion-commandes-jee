-- MySQL 8.0.16+ : importer dans une base vide gestion_commandes.
-- Aucune instruction DROP : les données existantes ne sont pas effacées.
CREATE TABLE client (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  nom VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL,
  telephone VARCHAR(30) NOT NULL DEFAULT '',
  adresse VARCHAR(255) NOT NULL DEFAULT '',
  CONSTRAINT uk_client_email UNIQUE (email)
);
CREATE TABLE produit (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  reference VARCHAR(40) NOT NULL,
  designation VARCHAR(180) NOT NULL,
  prix DECIMAL(12,2) NOT NULL,
  CONSTRAINT uk_produit_reference UNIQUE (reference),
  CONSTRAINT ck_produit_prix CHECK (prix >= 0 AND prix <= 99999999.99)
);
CREATE TABLE commande (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  client_id BIGINT NOT NULL,
  date_commande DATE NOT NULL,
  statut VARCHAR(20) NOT NULL DEFAULT 'EN_COURS',
  CONSTRAINT fk_commande_client FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE RESTRICT,
  CONSTRAINT ck_commande_statut CHECK (statut IN ('EN_COURS','VALIDEE','LIVREE','ANNULEE'))
);
CREATE TABLE ligne_commande (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  commande_id BIGINT NOT NULL,
  produit_id BIGINT NOT NULL,
  quantite INT NOT NULL,
  prix_unitaire DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_ligne_commande FOREIGN KEY (commande_id) REFERENCES commande(id) ON DELETE CASCADE,
  CONSTRAINT fk_ligne_produit FOREIGN KEY (produit_id) REFERENCES produit(id) ON DELETE RESTRICT,
  CONSTRAINT uk_commande_produit UNIQUE (commande_id,produit_id),
  CONSTRAINT ck_ligne_quantite CHECK (quantite BETWEEN 1 AND 100000),
  CONSTRAINT ck_ligne_prix CHECK (prix_unitaire >= 0)
);
CREATE INDEX idx_commande_client ON commande(client_id);
CREATE INDEX idx_ligne_produit ON ligne_commande(produit_id);
