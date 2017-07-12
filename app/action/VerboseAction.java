package action;


import play.Logger;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

/**
 * Created by dsenanayaka on 7/11/2017.
 */
public class VerboseAction  extends play.mvc.Action.Simple {

    public CompletionStage<Result> call(Http.Context ctx) {
        System.out.println("VerboseAction is called");
        Logger.info("Calling action for {}", ctx);
        //delegating the action is a must delegate.call(...).
        return delegate.call(ctx);
    }
}
