package ma.fstt;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.*;
import java.nio.file.*;
/** Serveur de vérification local avec H2 éphémère, hors du WAR de production. */
public final class LocalServer {
    public static Tomcat start(int port) throws Exception {
        Tomcat t=new Tomcat();t.setBaseDir(Path.of("target/tomcat-"+port).toAbsolutePath().toString());t.setPort(port);t.getConnector().setProperty("address","127.0.0.1");
        Context ctx=t.addWebapp("",Path.of("src/main/webapp").toAbsolutePath().toString());
        ctx.setParentClassLoader(LocalServer.class.getClassLoader());
        StandardRoot resources=new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources,"/WEB-INF/classes",Path.of("target/classes").toAbsolutePath().toString(),"/"));
        ctx.setResources(resources);t.start();
        if(!t.getConnector().getState().isAvailable()) throw new IllegalStateException("Connecteur HTTP indisponible");
        return t;
    }
    public static void main(String[] args) throws Exception {
        TestDatabase.reset("preview",true);Tomcat t=start(8087);
        System.out.println("PREVIEW_READY http://localhost:8087");t.getServer().await();
    }
}
