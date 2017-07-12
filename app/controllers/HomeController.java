package controllers;

import action.VerboseAction;
import akka.dispatch.forkjoin.ForkJoinPool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import executionContexts.JDBCExecutionContext;
import play.libs.concurrent.Futures;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.*;

import javax.inject.Inject;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.CompletableFuture.supplyAsync;

//Controller is a collective set of actions
public class HomeController extends Controller {

    ObjectMapper mapper = new ObjectMapper();
    HttpExecutionContext httpExecutionContext;
    //akka forkJoinPool
    private final Executor customExecutor = ForkJoinPool.commonPool();

    private JDBCExecutionContext jdbcExecutionContext;
    private Futures futures;

    @Inject
    public HomeController(HttpExecutionContext ec, JDBCExecutionContext jdbcExecutionContext, Futures futures) {
        this.httpExecutionContext = ec;
        this.jdbcExecutionContext = jdbcExecutionContext;
        this.futures = futures;
    }

    //an action which consume input parameters and Return a Result
    public Result index() {
        return ok("Got Request :" + request() + " !");
    }


    //reading url/query params
    public Result indexWithParam(String name) {
        return ok("Hello :" + name);
    }

    //json output
    //http://localhost:9000/json/name/dimuthu
    public Result indexWithParamJson(String name) throws Exception {

        //set headers
        response().setHeader("testHeader", "testHeader");

        //set cookies
        response().setCookie(Http.Cookie.builder("testCookie", "testCookie").build());

        //set cookie with more details
        response().setCookie(
                Http.Cookie.builder("theme", "blue")
                        .withMaxAge(Duration.ofSeconds(3600))
                        .withPath("/some/path")
                        .withDomain(".example.com")
                        .withSecure(false)
                        .withHttpOnly(true)
                        .withSameSite(Http.Cookie.SameSite.STRICT)
                        .build()
        );

        //discard a cookie
        response().discardCookie("theme");

        //reading session variable
        String userEmail = session("userEmail");

        if (userEmail == null) {
            //set session details
            session("userEmail", "dim912@gmail.com");
            System.out.println("set user session to :" + session("userEmail"));
        } else {
            System.out.println("user is :" + session("userEmail"));
        }

        //remove session Details
        session().remove("connected");

        //json output
        JsonNode json = mapper.readTree("{\"name\": \"Dimuthu\"}");
        Result jsonResult = ok(json);
        return jsonResult;
    }

    //clear session
    public Result logout() throws Exception {
        session().clear();
        return ok(mapper.readTree("{\"status:\":\"ok\"}"));
    }

    //flash mesasges ( only used to show success/error messages)
    public Result save() {
        flash("success", "The item has been created");
        return redirect("/home");
    }


    //using a custom Action class instead of default action class
    @With(VerboseAction.class)
    public Result verboseIndex() {
        return ok("It works!");
    }

    //async calculation , sync
    //within the same thread
    //http://localhost:9000/json/sync/info
    public CompletionStage<Result> syncInfo() throws Exception {
        return calcInfo().thenApply(info -> {
            try {
                return ok(((ObjectNode) info).put("type", "sync"));
                //return ok(((ObjectNode)info).set("type",mapper.readTree("{\"type\" : \"async\"}")));
            } catch (Exception e) {
                return null;
            }
        });
    }

    //async calculation , async
    //within the same thread
    //http://localhost:9000/json/async/info
    //when using completionState inside an Action => its a must giving HttpContext
    // else => "There is no HTTP Context available from here" error raised
    public CompletionStage<Result> asyncInfo() throws Exception {
        return calcInfo().thenApplyAsync(info -> {
            try {
                return ok(((ObjectNode) info).put("type", "async"));
                //return ok(((ObjectNode)info).set("type",mapper.readTree("{\"type\" : \"async\"}")));
            } catch (Exception e) {
                return null;
            }
        }, httpExecutionContext.current());
    }

    public CompletionStage<Result> blockingCallInfo() throws Exception {

        Executor jdbcExec = HttpExecution.fromThread((Executor) jdbcExecutionContext);
        return supplyAsync(() -> blockingAPICall(), jdbcExec)
                .thenApplyAsync(info -> ok(((ObjectNode) info).put("type", "callBlockingAPI")), jdbcExec);
    }


    public CompletionStage<JsonNode> calcInfo() throws Exception {
        JsonNode json = mapper.readTree("{\"name\": \"Dimuthu\" ,  \"age\" : \"20\"}");
        return CompletableFuture.completedFuture(json);
    }

    //timeout defined action
    public CompletionStage<String> delayedResult() {
        long start = System.currentTimeMillis();
        return futures.delayed(() -> CompletableFuture.supplyAsync(() -> {
            long end = System.currentTimeMillis();
            long seconds = end - start;
            return "rendered after " + seconds + " seconds";
        }, customExecutor), Duration.of(3, SECONDS));
    }


    //since this is a blocking call. Should be run on a separate execution context.(here jdbcExecutionContext)
    //it should be configured with enough thread, to handle concurrency
    public JsonNode blockingAPICall() {
        try {
            return mapper.readTree("{\"name\": \"Dimuthu\" ,  \"age\" : \"20\"}");
        } catch (Exception e) {
            return null;
        }
    }
}
