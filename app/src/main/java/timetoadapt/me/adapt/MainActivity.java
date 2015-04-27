package timetoadapt.me.adapt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    protected AdaptApp app;
    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (AdaptApp) getApplication();
        AdaptApp instance = app.getInstance();

        ParseObject analObject = new ParseObject("Analytics");
        analObject.put("action", "app_open");
        analObject.saveInBackground();

        currentUser = ParseUser.getCurrentUser();

        // Set up click handlers on navigation buttons
        Button browseButton = (Button) findViewById(R.id.browse);
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the list activity
                Intent browseIntent = new Intent(MainActivity.this, ListActivity.class);
                // Add any extras here for data that needs to be passed to the ListActivity
                startActivity(browseIntent);
            }
        });

        final Button createButton = (Button) findViewById(R.id.create);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // Navigate to the create screen activity
            if (currentUser != null) { // user is signed in, can create hypothesis
                Intent createIntent = new Intent(MainActivity.this, CreateHypothesisActivity.class);
                startActivity(createIntent);
            } else { // not signed in
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.user_required_dialog_message);

                builder.setPositiveButton(R.string.user_required_dialog_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(MainActivity.this, UserCreationActivity.class));
                    }
                });

                builder.setNegativeButton(R.string.user_required_dialog_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                builder.create().show();
            }
            }
        });

        // choose which fragment to inflate
        if (ParseUser.getCurrentUser() == null) { // user not signed in
            // user creation fragment
            UserCreationFragment topic = new UserCreationFragment();
            topic.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().replace(R.id.subscriptions_container, topic).commit();
        } else { // user totally signed in
            // current subscriptions fragment
            HypothesisListFragment list = new HypothesisListFragment();
            list.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().replace(R.id.subscriptions_container, list).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_log_out:
                ParseUser.logOut();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public static class UserCreationFragment extends Fragment {

        public UserCreationFragment() {

        }

        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            // Set layout to category fragment
            View rootView = inflater.inflate(R.layout.user_creation_fragment, container, false);

            // Grab category buttons from layout
            final Button signinButton = (Button) rootView.findViewById(R.id.signin_button);
            signinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), SignInActivity.class));
                }
            });

            final Button signupButton = (Button) rootView.findViewById(R.id.signup_button);
            signupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), SignUpActivity.class));
                }
            });

            ((TextView) rootView.findViewById(R.id.user_creation_title)).setText(R.string.create_user_welcome);
            ((TextView) rootView.findViewById(R.id.user_creation_explanation_text)).setText(R.string.hypothesis_subscription_explanation);

            return rootView;
        }
    }

    public  class HypothesisListFragment extends Fragment {

        public HypothesisListFragment() {

        }

        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.hypothesis_list_fragment, container, false);
            ListView list = (ListView) rootView.findViewById(R.id.hypList);
            // put code here to append rows to the list view for each hypothesis
            // needs to use ArrayAdapter and a custom layout for each row, found in hypothesis_row.xml
            List<String> joinedIds = currentUser.getList("joined");
            String userName = currentUser.getUsername();
            String email = currentUser.getEmail();
            Log.d("joined", "user: " + userName + ", email: " + email + " ... joined hypotheses: " + joinedIds);

            final List<ParseObject> hypothesesParseObjects = new ArrayList<ParseObject>();
            List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
            for (String id : joinedIds) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Hypothesis");
                query.whereMatches("objectId", id);
                queries.add(query);
            }
            // Get all hypotheses the user is signed up for
            ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);

            mainQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null) {
                        HypothesisListItem[] listData = new HypothesisListItem[parseObjects.size()];
                        for (int i = 0; i < parseObjects.size(); i++) {
                            // add returned hypotheses to the array of list items
                            listData[i] = new HypothesisListItem(parseObjects.get(i));
                        }
                        // Adapter to create listView rows
                        HypothesisAdapter adapter = new HypothesisAdapter(MainActivity.this, R.layout.hypothesis_row, listData);
                        ListView listView = (ListView) rootView.findViewById(R.id.hypList);
                        // set adapter to the list view
                        listView.setAdapter(adapter);
                        Log.i("userHypotheses", "user hypotheses retrieved" + parseObjects);
                    } else {
                        Log.d("parseError", "error retrieving hypothesis " + e.getMessage());
                        Log.i("application", "error retrieving hypothesis " + e.getMessage());
                    }
                }
            });
            return rootView;
        }
    }
}
