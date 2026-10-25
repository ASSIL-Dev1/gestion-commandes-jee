# Gestion des commandes — atelier JEE MVC1

Application Java web en français : CRUD clients, produits, commandes et lignes, calcul des totaux, tableau de bord et recherche dans les listes. Projet Maven WAR, importable dans Eclipse. Données fictives facultatives incluses.

## Prérequis

- JDK 17 ou supérieur, Maven 3.9+, MySQL 8.0.16+ / 8.4, Apache Tomcat **9.0.x**.
- Eclipse IDE for Enterprise Java and Web Developers pour travailler avec l'IDE.
- Le code utilise `javax.servlet` (Java EE 8), Servlet **4.0**, JSP 2.3 et JSTL 1.2. Tomcat 10/11 utilise `jakarta.servlet` et requiert une migration.

## 1. Créer la base MySQL

Se connecter avec un compte administrateur, puis exécuter (remplacer le mot de passe) :

```sql
CREATE DATABASE gestion_commandes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'commandes_app'@'localhost' IDENTIFIED BY 'REMPLACER_PAR_UN_MOT_DE_PASSE';
GRANT SELECT, INSERT, UPDATE, DELETE ON gestion_commandes.* TO 'commandes_app'@'localhost';
USE gestion_commandes;
SOURCE C:/chemin/gestion-commandes-jee/sql/01-schema.sql;
SOURCE C:/chemin/gestion-commandes-jee/sql/02-demo.sql;
```

`SOURCE` est une commande du client `mysql`, pas de JDBC. Dans MySQL Workbench, ouvrir et exécuter les scripts dans cet ordre après avoir sélectionné la base. Le script de démonstration est facultatif ; il est prévu pour une base vide. Les scripts ne suppriment aucune table existante.

## 2. Configurer la connexion

Deux possibilités, au choix :

- Variables d'environnement `DB_URL`, `DB_USER`, `DB_PASSWORD` dans le processus qui lance Tomcat.
- Copier `src/main/resources/db.properties.example` vers `src/main/resources/db.properties`, puis adapter les trois valeurs **avant** de compiler le WAR.

Exemple PowerShell (valeurs valables seulement pour cette session et les processus qu'elle lance) :

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/gestion_commandes?connectionTimeZone=UTC&characterEncoding=UTF-8'
$env:DB_USER='commandes_app'
$env:DB_PASSWORD='VOTRE_MOT_DE_PASSE'
```

Les propriétés Java `-Ddb.url`, `-Ddb.user`, `-Ddb.password` ont priorité, puis viennent les variables d'environnement, puis le fichier. Aucun secret réel n'est livré. Ne pas publier `db.properties` ou un WAR contenant des secrets.

## 3. Compiler et lancer

À la racine du projet :

```text
mvn clean verify
```

Copier `target/gestion-commandes.war` dans le dossier `webapps` de Tomcat 9. Démarrer Tomcat avec les variables configurées, puis ouvrir :

```text
http://localhost:8080/gestion-commandes/
```

Le serveur MySQL doit être initialisé séparément. Le WAR se génère avec Maven et lit les variables d'environnement au lancement.

## 4. Importer dans Eclipse

1. File > Import > Maven > Existing Maven Projects, puis sélectionner ce dossier et son `pom.xml`.
2. Sélectionner le JDK 17+ dans Java > Installed JREs.
3. Ajouter un serveur Apache Tomcat v9.0 dans la vue Servers et sélectionner son installation.
4. Dans les propriétés du projet, vérifier Project Facets : Java 17 et Dynamic Web Module **4.0**. Maven/m2e configure le projet web à l'import ; si nécessaire, Maven > Update Project.
5. Dans la configuration de lancement du serveur, renseigner les trois variables DB_*, puis Run on Server.

Le livrable est directement le résultat de la conversion en Maven demandée par l'atelier. Pour reproduire le parcours pédagogique : créer un Dynamic Web Project avec module 4.0, puis Configure > Convert to Maven Project et reprendre les dépendances de ce `pom.xml`.

## Architecture MVC1

```text
Navigateur -> filtre technique -> Servlet alias d'URL -> JSP
                                                       |
                                                bean XxxPage
                                                       |
                                      interface Service -> DAO -> MySQL
```

Chaque JSP déclenche son bean de page avec une seule instruction Java, puis affiche les résultats avec EL/JSTL. Les servlets ClientServlet, ProduitServlet et CommandeServlet font seulement le transfert vers la JSP ; elles ne pilotent ni les DAO ni les règles CRUD. La JSP reste le point de traitement MVC1. Un servlet contrôleur qui traiterait le CRUD avant de sélectionner une vue correspondrait à MVC2. Ce choix concilie la demande de MVC1 et la présence de servlets dans l'énoncé. `@Inject` et `@ApplicationScoped` ne sont pas employés : l'instanciation explicite évite d'imposer un conteneur CDI à Tomcat. Les dépendances CDI ne sont donc pas nécessaires.

Packages imposés : `ma.fstt.entities`, `ma.fstt.dao`, `ma.fstt.service`. Autres packages : `config` pour JDBC, `web` pour les beans de page, servlets et filtre.

## Règles métier

- Un client possède zéro à plusieurs commandes ; une commande appartient à un client et possède 1 à 50 lignes.
- Chaque ligne référence un produit distinct, une quantité de 1 à 100000 et un prix unitaire historique.
- Les prix utilisent `BigDecimal` / `DECIMAL`, sont compris entre 0 et 99999999,99 et comportent au plus deux décimales.
- Le serveur lit le prix du catalogue pour un nouveau produit et conserve le prix historique d'une ligne existante. Le navigateur affiche une estimation ; le calcul serveur fait foi.
- La modification remplace les lignes dans une transaction JDBC avec rollback. L'en-tête de la commande est verrouillé pendant la modification.
- Le total n'est pas stocké : il est la somme quantité × prix historique. Le montant du tableau de bord exclut les annulations ; ce n'est pas un encaissement comptable.
- Suppression d'une commande : cascade sur ses lignes. Suppression d'un client ou produit encore référencé : refus par clé étrangère.
- Email client et référence produit uniques. Statuts : EN_COURS, VALIDEE, LIVREE, ANNULEE ; aucune règle de transition imposée par le sujet.
- Les écritures se font uniquement par POST, avec jeton CSRF et redirection après succès ; affichage échappé via `c:out`, requêtes préparées JDBC et fermeture des ressources.
- Pas de connexion utilisateur, gestion de stock, TVA ni paiement : ces fonctions ne sont pas demandées. Application destinée à l'atelier/local ; ajouter authentification et autorisations avant exposition publique.
- Sans JavaScript, les CRUD clients/produits et la consultation restent utilisables ; la composition des lignes de commande utilise JavaScript.

## Tests et limites

`mvn verify` exécute les tests DAO sur H2 en mode MySQL et les tests HTTP sur Tomcat 9 embarqué : compilation réelle des JSP, formulaires CRUD, échappement, CSRF, contraintes, prix historiques et rollback. H2 sert seulement aux tests, il n'entre pas dans le WAR. Ces tests ne remplacent pas une recette avec votre serveur MySQL.

Voir `docs/VERIFICATION.md` pour les résultats obtenus et la recette MySQL à reproduire. `src/test/java/ma/fstt/LocalServer.java` est un serveur de démonstration à données éphémères réservé aux vérifications locales.

## Diagrammes et rapport

- `docs/diagramme-classes.svg` : UML prêt à consulter ; source Mermaid dans `docs/diagramme-classes.mmd`.
- `docs/schema-relationnel.md` : tables, clés et cardinalités.
- `docs/rapport-complet-assil-elghrib.pdf` et `docs/rapport-complet-assil-elghrib.docx` : rapport complet personnalisé pour Assil Elghrib, deuxième année LSI.

## Mettre le code sur GitHub

Le dossier est prêt à être versionné. Créer un dépôt vide dans votre compte puis, à la racine :

```text
git init
git add .
git commit -m "Application JEE MVC1 de gestion des commandes"
git branch -M main
git remote add origin https://github.com/VOTRE_COMPTE/gestion-commandes-jee.git
git push -u origin main
```

Remplacer l'URL par celle de votre dépôt.

## Références techniques

- Tomcat 9, Servlet 4.0 et JSP 2.3 : https://tomcat.apache.org/tomcat-9.0-doc/
- Connector/J Maven : https://dev.mysql.com/doc/connector-j/en/connector-j-installing-maven.html
- Maven : https://maven.apache.org/guides/getting-started/
