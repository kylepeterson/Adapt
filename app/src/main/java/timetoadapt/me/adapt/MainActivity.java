package timetoadapt.me.adapt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
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
    protected static AdaptApp instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AdaptApp app = (AdaptApp) getApplication();
        instance = app.getInstance();

        ParseObject analObject = new ParseObject("Analytics");
        analObject.put("action", "app_open");
        analObject.saveInBackground();

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
                if (instance.getCurrentUser() != null) { // user is signed in, can create hypothesis
                    Intent createIntent = new Intent(MainActivity.this, CreateHypothesisActivity.class);
                    startActivity(createIntent);
                } else { // not signed in
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.user_required_create_dialog_message);

                    builder.setPositiveButton(R.string.user_required_dialog_positive, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(MainActivity.this, SignInActivity.class));
                        }
                    });

                    builder.setNegativeButton(R.string.user_required_dialog_negative, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // negative click does nothing, just dismisses dialog
                        }
                    });

                    builder.create().show();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // choose which fragment to inflate
        if (ParseUser.getCurrentUser() == null) { // user not signed in
            // user creation fragment
            QuoteFragment quotes = new QuoteFragment();
            quotes.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().replace(R.id.subscriptions_container, quotes).commit();
        } else { // user totally signed in
            // current subscriptions fragment
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            // HypothesisListFragment list = new HypothesisListFragment();
            // Next three commented lines are for inflating created list
            // CreatedListFragment createdList = new CreatedListFragment();
/*
            list.setArguments(getIntent().getExtras());
            createdList.setArguments(getIntent().getExtras());

            ft.replace(R.id.subscriptions_container, list);

            ft.replace(R.id.created_container, createdList);*/
            CombinedFragment combinedFragment = new CombinedFragment();
            combinedFragment.setArguments(getIntent().getExtras());
            ft.replace(R.id.subscriptions_container, combinedFragment);
            ft.commit();

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
                final Intent nextActivity = new Intent(MainActivity.this, UserSettingActivity.class);
                Log.d("actionbar", "settings clicked");
                startActivity(nextActivity);
                return true;
            case R.id.action_log_out:
                instance.logoutCurrentUser();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                Log.d("actionbar", "logout clicked");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class HypothesisListFragment extends Fragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            // find all the hypotheses the user subscribed to.
            List<String> joinedIds = instance.getCurrentUser().getList("joined");

            // if the user has some subscriptions, display them. Otherwise, prompt user to subscribe
            if (joinedIds != null) {
                final View rootView = inflater.inflate(R.layout.hypothesis_list_fragment, container, false);
                // put code here to append rows to the list view for each hypothesis
                // needs to use ArrayAdapter and a custom layout for each row, found in hypothesis_row.xml
                String userName = instance.getCurrentUser().getUsername();
                Log.d("joined", "user: " + userName + " joined hypotheses: " + joinedIds);

                // we find all objectIDs contained in the list stored in the user table
                ParseQuery<ParseObject> mainQuery = ParseQuery.getQuery("Hypothesis");
                mainQuery.whereContainedIn("objectId", joinedIds);
                
                mainQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if (e != null) {
                            Log.d("parseError", "error retrieving hypothesis " + e.getMessage());
                            Log.i("application", "error retrieving hypothesis " + e.getMessage());
                        } else {
                            List<HypothesisListItem> listData = new ArrayList<>();
                            for (int i = 0; i < parseObjects.size(); i++) {
                                // add returned hypotheses to the array of list items
                                listData.add(new HypothesisListItem(parseObjects.get(i)));
                            }

                            // Adapter to create listView rows
                            final HypothesisAdapter adapter = new HypothesisAdapter(getActivity(), R.layout.report_row, listData);
                            ListView listView = (ListView) rootView.findViewById(R.id.hypList);
                            // set adapter to the list view
                            listView.setAdapter(adapter);

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent profilePage = new Intent(MainActivity.this, HypothesisProfileActivity.class);
                                    // Add any extras here for data that needs to be passed to the ListActivity
                                    profilePage.putExtra("hypothesisData", adapter.getItemAtPosition(position));
                                    startActivity(profilePage);
                                }
                            });

                        }
                    }
                });
                return rootView;
            } else {
                TextView tv = new TextView(getActivity());
                tv.setText("Welcome " + instance.getCurrentUser().getUsername() + "!\nYour subscribed hypothesis will show up here. Hit Browse to find hypotheses that work for you!");
                tv.setTextColor(Color.parseColor("#1DAD74"));
                tv.setTop(20);
                tv.setTextSize(25);
                return tv;
            }
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }

    public class CreatedListFragment extends Fragment {


        public CreatedListFragment() {

        }

        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {

            List<String> createdIds = instance.getCurrentUser().getList("created");
            Log.d("mainpage", "createdIds: " + createdIds);
            if(createdIds != null) {
                final LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.hypothesis_list_fragment, container, false);
                Log.d("mainpage", "rootView: " + rootView);
                ParseQuery<ParseObject> mainQuery = ParseQuery.getQuery("Hypothesis");
                mainQuery.whereContainedIn("objectId", createdIds);
                mainQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if(e != null) {
                            Log.d("parseError", "error retrieving hypothesis " + e.getMessage());
                            Log.i("application", "error retrieving hypothesis " + e.getMessage());;
                        } else {
                            Log.d("mainpage", "parseObjets: " + parseObjects);
                            List<HypothesisListItem> listData = new ArrayList<>();
                            for (int i = 0; i < parseObjects.size(); i++) {
                                // add returned hypotheses to the array of list items
                                listData.add(new HypothesisListItem(parseObjects.get(i)));
                            }
                            Log.d("mainpage", "listdata: " + listData);
                            Log.d("mainpage", "first hypothesis" + listData.get(0).tryThis);
                            // Adapter to create listView rows
                            ListView listView = (ListView) rootView.getChildAt(0);
                            final HypothesisAdapter adapter = new HypothesisAdapter(getActivity(), R.layout.hypothesis_row, listData);
                            // set adapter to the list view
                            listView.setAdapter(adapter);
                            Log.d("mainpage", "listview: " + listView);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent profilePage = new Intent(MainActivity.this, HypothesisProfileActivity.class);
                                    // Add any extras here for data that needs to be passed to the ListActivity
                                    profilePage.putExtra("hypothesisData", adapter.getItemAtPosition(position));
                                    startActivity(profilePage);
                                }
                            });
                        }
                    }
                });
                return rootView;
            } else {
                // display message if empty?
                return new TextView(getActivity());
            }

        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }

    public class QuoteFragment extends Fragment {

        public QuoteFragment() {

        }

        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            // Set layout to quote fragment
            View rootView = inflater.inflate(R.layout.quote_fragment, container, false);

            TypedArray quotes = getResources().obtainTypedArray(R.array.inspirational_quotes);
            TypedArray authors = getResources().obtainTypedArray(R.array.authors);

            int choice = (int) (Math.random() * quotes.length());
            String quote = quotes.getString(choice);
            String author = authors.getString(choice);

            TextView quoteView = (TextView) rootView.findViewById(R.id.quote_view);
            TextView authorView = (TextView) rootView.findViewById(R.id.author_view);

            quoteView.setText("\"" + quote + "\"");
            authorView.setText(" - " + author);

            return rootView;
        }
    }

    public class CombinedFragment extends Fragment {

        public CombinedFragment() {

        }

        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {

            // find all the hypotheses the user subscribed to.
            List<String> joinedIds = instance.getCurrentUser().getList("joined");
            List<String> createdIds = instance.getCurrentUser().getList("created");

            // if the user has some subscriptions, display them. Otherwise, prompt user to subscribe
            if (joinedIds != null || createdIds != null) {
                final View rootView = inflater.inflate(R.layout.combined_list_fragment, container, false);
                if(joinedIds != null) {
                    // put code here to append rows to the list view for each hypothesis
                    // needs to use ArrayAdapter and a custom layout for each row, found in hypothesis_row.xml
                    String userName = instance.getCurrentUser().getUsername();
                    Log.d("joined", "user: " + userName + " joined hypotheses: " + joinedIds);

                    // Make header visible
                    rootView.findViewById(R.id.hypTitle).setVisibility(View.VISIBLE);
                    // we find all objectIDs contained in the list stored in the user table
                    ParseQuery<ParseObject> mainQuery = ParseQuery.getQuery("Hypothesis");
                    mainQuery.whereContainedIn("objectId", joinedIds);

                    mainQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            if (e != null) {
                                Log.d("parseError", "error retrieving hypothesis " + e.getMessage());
                                Log.i("application", "error retrieving hypothesis " + e.getMessage());
                            } else {
                                List<HypothesisListItem> listData = new ArrayList<>();
                                for (int i = 0; i < parseObjects.size(); i++) {
                                    // add returned hypotheses to the array of list items
                                    listData.add(new HypothesisListItem(parseObjects.get(i)));
                                }

                                // Adapter to create listView rows
                                final HypothesisAdapter adapter = new HypothesisAdapter(getActivity(), R.layout.report_row, listData);
                                ListView listView = (ListView) rootView.findViewById(R.id.hypList);
                                // set adapter to the list view
                                listView.setAdapter(adapter);

                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent profilePage = new Intent(MainActivity.this, HypothesisProfileActivity.class);
                                        // Add any extras here for data that needs to be passed to the ListActivity
                                        profilePage.putExtra("hypothesisData", adapter.getItemAtPosition(position));
                                        startActivity(profilePage);
                                    }
                                });

                            }
                        }
                    });
                } else {
                    // No joined hypotheses, hide title
                    rootView.findViewById(R.id.hypTitle).setVisibility(View.GONE);
                }
                if(createdIds != null) {
                    Log.d("mainpage", "rootView: " + rootView);
                    ParseQuery<ParseObject> mainQuery = ParseQuery.getQuery("Hypothesis");
                    // show title
                    rootView.findViewById(R.id.createdTitle).setVisibility(View.VISIBLE);
                    mainQuery.whereContainedIn("objectId", createdIds);
                    mainQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            if (e != null) {
                                Log.d("parseError", "error retrieving hypothesis " + e.getMessage());
                                Log.i("application", "error retrieving hypothesis " + e.getMessage());
                                ;
                            } else {
                                Log.d("mainpage", "parseObjets: " + parseObjects);
                                List<HypothesisListItem> listData = new ArrayList<>();
                                for (int i = 0; i < parseObjects.size(); i++) {
                                    // add returned hypotheses to the array of list items
                                    listData.add(new HypothesisListItem(parseObjects.get(i)));
                                }
                                Log.d("mainpage", "listdata: " + listData);
                                Log.d("mainpage", "first hypothesis" + listData.get(0).tryThis);
                                // Adapter to create listView rows
                                ListView listView = (ListView) rootView.findViewById(R.id.createdList);
                                final HypothesisAdapter adapter = new HypothesisAdapter(getActivity(), R.layout.hypothesis_row, listData);
                                // set adapter to the list view
                                listView.setAdapter(adapter);
                                Log.d("mainpage", "listview: " + listView);
                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent profilePage = new Intent(MainActivity.this, HypothesisProfileActivity.class);
                                        // Add any extras here for data that needs to be passed to the ListActivity
                                        profilePage.putExtra("hypothesisData", adapter.getItemAtPosition(position));
                                        startActivity(profilePage);
                                    }
                                });
                            }
                        }
                    });
                } else {
                    // No created hypotheses, hide title
                    rootView.findViewById(R.id.createdTitle).setVisibility(View.GONE);
                }
                return rootView;
            } else {
                // No joined or created hypotheses
                TextView tv = new TextView(getActivity());
                tv.setText("Welcome " + instance.getCurrentUser().getUsername() + "!\nYour subscribed hypothesis will show up here. Hit Browse to find hypotheses that work for you!");
                tv.setTextColor(Color.parseColor("#1DAD74"));
                tv.setTop(20);
                tv.setTextSize(25);
                return tv;
            }
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }


}
