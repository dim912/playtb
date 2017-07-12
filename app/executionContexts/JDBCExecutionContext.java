package executionContexts;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;

import javax.inject.Inject;

/**
 * Created by dsenanayaka on 7/12/2017.
 */
public class JDBCExecutionContext extends CustomExecutionContext {
    @Inject
    public JDBCExecutionContext(ActorSystem actorSystem, String name) {
        //use a custom thead pool defined in application.conf
        //(done through akka dispatcher configuration)
        super(actorSystem, "app.jdbcDispatcher");
    }
}
