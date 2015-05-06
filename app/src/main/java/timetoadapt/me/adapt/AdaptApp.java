package timetoadapt.me.adapt;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by kylepeterson on 4/16/15.
 */
public class AdaptApp extends Application {
    private static AdaptApp instance;
    public HypothesisRepo hypothesisRepo;
    public static ParseUser currentUser;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getResources().getString(R.string.parse_application_id), getResources().getString(R.string.parse_client_key));
        HypothesisRepo repo = createHypothesisRepo();
        initInstance(repo);
        currentUser = ParseUser.getCurrentUser();
        context = getApplicationContext();
    }

    public void initInstance(HypothesisRepo repo) {
        if (instance == null) {
            instance = new AdaptApp(repo);
            Log.i("application", "new repo instance set to " + repo.categoryList);
        }
    }

    public static AdaptApp getInstance() {
        return instance;
    }

    public AdaptApp() {}

    private AdaptApp(HypothesisRepo repo) {
        hypothesisRepo = repo;
        Log.i("application", "Repo built in singleton: " + repo.categoryList);
    }

    public HypothesisRepo getHypothesisRepo() {
        return hypothesisRepo;
    }

    public HypothesisRepo createHypothesisRepo() {
        // Query server to fill in appropriate categories
        final HypothesisRepo repo = new HypothesisRepo();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Category");
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e == null) {
                    repo.setCategoryList(parseObjects);
                    Log.i("application", "Repo built in singleton: " + repo.categoryList);
                } else {
                    Log.d("parseError", "error retrieving categories " + e.getMessage());
                    Log.i("application", "error retrieving categories " + e.getMessage());
                }
            }
        });
        return repo;
    }

    public ParseUser getCurrentUser() {
        return currentUser;
    }

    public void updateCurrentUser() {
        if (currentUser == null) {
            currentUser = ParseUser.getCurrentUser();
        }
        currentUser.fetchInBackground();
    }

    public void logoutCurrentUser() {
        currentUser.logOut();
    }

    public static Context getAppContext(){
        return context;
    }
}
