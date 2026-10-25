<%@ page import="ma.fstt.web.DashboardPage" %><% new DashboardPage().handle(request,response); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<p class="subtitle">Vos clients, votre catalogue et vos commandes, au même endroit.</p>
<div class="stats"><article class="stat"><span>Clients enregistrés</span><strong><c:out value="${clientCount}" default="—"/></strong><a href="${pageContext.request.contextPath}/clients">Gérer les clients ↗</a></article>
<article class="stat"><span>Produits au catalogue</span><strong><c:out value="${produitCount}" default="—"/></strong><a href="${pageContext.request.contextPath}/produits">Voir le catalogue ↗</a></article>
<article class="stat"><span>Commandes au total</span><strong><c:out value="${commandeCount}" default="—"/></strong><a href="${pageContext.request.contextPath}/commandes">Suivre les commandes ↗</a></article>
<article class="stat accent"><span>Montant hors annulations</span><strong><fmt:formatNumber value="${empty total ? 0 : total}" minFractionDigits="2" maxFractionDigits="2"/><small> MAD</small></strong><span>Somme des commandes non annulées</span></article></div>
<section class="panel"><div class="panel-heading"><div><h2>Dernières commandes</h2><p>Les cinq commandes les plus récentes par création.</p></div><a class="button" href="${pageContext.request.contextPath}/commandes?action=new">+ Nouvelle commande</a></div>
<div class="table-wrap"><table><thead><tr><th>Commande</th><th>Client</th><th>Date</th><th>Statut</th><th class="number">Montant</th><th></th></tr></thead><tbody>
<c:forEach items="${recent}" var="o"><tr><td class="strong">CMD-<c:out value="${o.id}"/></td><td><c:out value="${o.client.nom}"/></td><td><c:out value="${o.dateCommande}"/></td><td><span class="badge ${o.statut}"><c:out value="${o.statut}"/></span></td><td class="number"><fmt:formatNumber value="${o.total}" minFractionDigits="2"/> MAD</td><td><a href="${pageContext.request.contextPath}/commandes?action=detail&amp;id=${o.id}">Consulter →</a></td></tr></c:forEach>
<c:if test="${empty recent}"><tr><td colspan="6" class="empty">Aucune commande pour le moment. Commencez par ajouter un client et un produit.</td></tr></c:if></tbody></table></div></section>
<section class="intro-card"><span class="intro-icon">↗</span><div><h2>Un circuit de commande simple</h2><p>Créez votre client, ajoutez vos produits puis composez votre commande. Les montants sont calculés automatiquement.</p></div></section>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
