package ma.fstt;
import org.junit.jupiter.api.*;
import org.apache.catalina.startup.Tomcat;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import ma.fstt.dao.*;
import static org.junit.jupiter.api.Assertions.*;
class WebIntegrationTest {
    static Tomcat server; static HttpClient browser; static String base;
    @BeforeAll static void start() throws Exception {TestDatabase.reset("web",true);server=LocalServer.start(0);base="http://localhost:"+server.getConnector().getLocalPort();browser=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).followRedirects(HttpClient.Redirect.NEVER).build();}
    @AfterAll static void stop() throws Exception {if(server!=null){server.stop();server.destroy();}}
    HttpResponse<String> get(String path) throws Exception {return browser.send(HttpRequest.newBuilder(URI.create(base+path)).GET().build(),HttpResponse.BodyHandlers.ofString());}
    String token(String path) throws Exception {var r=get(path);assertEquals(200,r.statusCode(),r.body());Matcher m=Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(r.body());assertTrue(m.find(),r.body());return m.group(1);}
    HttpResponse<String> post(String path,String body) throws Exception {return browser.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());}
    @Test void allPagesCompileAndRender() throws Exception {for(String path:List.of("/","/clients","/produits","/commandes","/clients?action=new","/clients?action=edit&id=1","/produits?action=new","/produits?action=edit&id=1","/commandes?action=new","/commandes?action=edit&id=1","/commandes?action=detail&id=1")){var r=get(path);assertEquals(200,r.statusCode(),path+" "+r.body());assertTrue(r.body().contains("Commandes"));assertTrue(r.body().contains("Servlet · JSP · JavaBean"));assertTrue(r.body().contains("◫"));}}
    @Test void csrfAndGetDeletionBlocked() throws Exception {assertEquals(403,post("/clients","action=delete&id=1").statusCode());assertEquals(400,get("/clients?action=delete&id=1").statusCode());assertTrue(new ClientDAO().findById(1).isPresent());}
    @Test void clientFormEscapesAndPersists() throws Exception {
        String csrf=token("/clients?action=new");
        var r=post("/clients","action=save&csrf="+csrf+"&nom="+URLEncoder.encode("<script>alert(1)</script>",StandardCharsets.UTF_8)+"&email=web%40example.test&telephone=0600&adresse=Tanger");assertEquals(302,r.statusCode(),r.body());
        String html=get("/clients").body();assertTrue(html.contains("&lt;script&gt;"));assertFalse(html.contains("<script>alert(1)</script>"));
        long id=new ClientDAO().findAll().stream().filter(c->c.getEmail().equals("web@example.test")).findFirst().orElseThrow().getId();
        assertEquals(302,post("/clients","action=delete&csrf="+csrf+"&id="+id).statusCode());
    }
    @Test void invalidInputAndLinkedDeletionShown() throws Exception {String csrf=token("/produits?action=new");var invalid=post("/produits","action=save&csrf="+csrf+"&reference=X&designation=X&prix=-2");assertEquals(400,invalid.statusCode());assertTrue(invalid.body().contains("Prix invalide"));var linked=post("/produits","action=delete&csrf="+csrf+"&id=1");assertEquals(409,linked.statusCode());assertTrue(linked.body().contains("Opération impossible"));}
    @Test void orderHttpCreateUpdateDelete() throws Exception {
        String csrf=token("/commandes?action=new");String common="csrf="+csrf+"&clientId=1&dateCommande=2026-10-24&statut=EN_COURS&produitId=1&quantite=2";
        var created=post("/commandes","action=save&"+common);assertEquals(302,created.statusCode(),created.body());
        var dao=new CommandeDAO();var o=dao.findAll().get(0);assertEquals(new java.math.BigDecimal("498.00"),o.getTotal());
        var updated=post("/commandes","action=save&id="+o.getId()+"&"+common.replace("quantite=2","quantite=3"));assertEquals(302,updated.statusCode(),updated.body());assertEquals(new java.math.BigDecimal("747.00"),dao.findById(o.getId()).orElseThrow().getTotal());
        assertEquals(302,post("/commandes","action=delete&csrf="+csrf+"&id="+o.getId()).statusCode());assertTrue(dao.findById(o.getId()).isEmpty());
    }
}
