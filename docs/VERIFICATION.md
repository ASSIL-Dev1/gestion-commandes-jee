# Vérification du livrable

Date : 24 octobre 2026.

## Résultat automatisé

Commande : `mvn verify` avec Maven 3.9.9 et JDK 25.0.2, compilation ciblant Java 17.

| Suite | Tests | Échecs | Erreurs |
| --- | ---: | ---: | ---: |
| DaoTest | 12 | 0 | 0 |
| WebIntegrationTest | 5 | 0 | 0 |
| Total | 17 | 0 | 0 |

Résultat final : **BUILD SUCCESS**. WAR produit : `gestion-commandes.war`.

Les tests DAO utilisent H2 2.3.232 en mode MySQL. Les tests HTTP utilisent Tomcat 9.0.123 embarqué, compilent les JSP et soumettent les formulaires réels avec une session et un jeton CSRF. Le test de rollback ajoute temporairement une contrainte qui force une erreur lors du remplacement des lignes, puis vérifie que le statut et les anciennes lignes ont été restaurés. Le refus attendu d'une suppression liée produit une exception SQL journalisée ; ce cas est un test réussi.

L'encodage des fragments JSP a été corrigé après le contrôle visuel ; les tests HTTP vérifient aussi les symboles de navigation et les séparateurs UTF-8.

## Vérification dans le navigateur

Sur Tomcat local et une base H2 temporaire avec les données fictives :

1. Tableau de bord : 3 clients, 4 produits, 3 commandes et un montant hors annulations de 3203,00 MAD.
2. Nouvelle commande : sélection d'Atlas Digital, 2 claviers à 249,00 MAD et 1 souris à 179,00 MAD.
3. Ajout dynamique d'une seconde ligne : total estimé affiché de 677,00 MAD.
4. Soumission : redirection vers la liste avec message de succès.
5. Détail enregistré : deux lignes, sous-totaux de 498,00 et 179,00 MAD, total serveur de 677,00 MAD.

Captures conservées dans `docs/apercu-tableau-de-bord.png`, `docs/apercu-formulaire-commande.png` et `docs/apercu-commande-enregistree.png`. La commande créée pour cette vérification existe seulement dans la base éphémère du serveur de test ; elle n'est pas ajoutée au script SQL de démonstration.

Le mini rapport PDF a été rendu puis ses quatre pages ont été examinées : texte, tableaux, diagramme, liens et pagination.

## Recette à faire sur votre MySQL

Aucun serveur MySQL réel n'a été utilisé dans cette session. H2 en mode MySQL ne garantit pas toutes les particularités du serveur MySQL. Après installation selon le README :

1. Importer le schéma et les données facultatives dans une base vide, en utilisant InnoDB.
2. Configurer la connexion puis déployer le WAR dans Tomcat 9.
3. Vérifier les 3 clients, 4 produits, 3 commandes et le total de 3203,00 MAD si les données de démonstration ont été importées.
4. Créer, modifier et supprimer un client et un produit inutilisés.
5. Créer une commande de plusieurs produits ; modifier le statut, les quantités, ajouter et retirer une ligne.
6. Modifier le prix catalogue d'un produit commandé puis vérifier que la commande conserve son prix historique, y compris après enregistrement d'une modification.
7. Vérifier le refus de suppression du client ou produit utilisé ; supprimer ensuite la commande et vérifier la cascade de ses lignes.
8. Redémarrer Tomcat et vérifier la persistance des données dans MySQL.

## Publication

Les sources sont prêtes pour un dépôt GitHub avec un `.gitignore`. Aucun dépôt distant n'a été créé et aucun code n'a été publié.
