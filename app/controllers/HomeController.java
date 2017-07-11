package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.mvc.*;

import java.time.Duration;

//Controller is a collective set of actions
public class HomeController extends Controller {

    ObjectMapper mapper = new ObjectMapper();


    //an action which consume input parameters and Return a Result
    public Result index() {
        return ok("Got Request :" + request() + " !");
    }


    //reading url/query params
    public Result indexWithParam(String name) {
        return ok("Hello :" + name);
    }

    //json output
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


}
