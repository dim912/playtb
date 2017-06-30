package controllers;

import play.mvc.*;

//Controller is a collective set of actions
public class HomeController extends Controller {

    //an action which consume input parameters and Return a Result
    public Result index() {
        return ok("Got Request :" + request() + " !");
    }


    public Result indexWithParam(String name ) {
        return ok("Hello :" +  name );
    }

}
