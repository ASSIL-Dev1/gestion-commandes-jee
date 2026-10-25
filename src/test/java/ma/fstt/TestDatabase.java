package ma.fstt;
import ma.fstt.config.Database;
import java.nio.file.*;
import java.sql.*;
import java.io.StringReader;
import org.h2.tools.RunScript;
public final class TestDatabase {
    public static void reset(String name,boolean demo) throws Exception {
        System.setProperty("db.url","jdbc:h2:mem:"+name+";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        System.setProperty("db.user","sa");System.setProperty("db.password","");
        try(Connection c=Database.open()) {
            c.createStatement().execute("DROP ALL OBJECTS");
            RunScript.execute(c,new StringReader(Files.readString(Path.of("sql/01-schema.sql"))));
            if(demo) RunScript.execute(c,new StringReader(Files.readString(Path.of("sql/02-demo.sql"))));
        }
    }
}
