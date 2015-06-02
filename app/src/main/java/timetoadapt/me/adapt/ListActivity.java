package timetoadapt.me.adapt;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ListActivity extends Activity {
    protected static HypothesisRepo hypothesisRepo;
    private static AdaptApp app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Hide name of activity in actionbar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        // make action bar home button work
        actionBar.setDisplayHomeAsUpEnabled(true);

        app = (AdaptApp) getApplication();
        AdaptApp instance = app.getInstance();
        hypothesisRepo = instance.hypothesisRepo;
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        } else if (savedInstanceState == null) {
            // When entered through browse button
            // Create new categories fragment
            CategoriesFragment topic = new CategoriesFragment();
            topic.setArguments(getIntent().getExtras());
            // Inflate categories overview fragment
            getFragmentManager().beginTransaction().replace(R.id.container, topic).commit();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, ListActivity.class)));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        // log in vs log out
        if(app.getCurrentUser() == null) {
            // not logged in
            menu.findItem(R.id.action_log_in).setVisible(true);
            menu.findItem(R.id.action_log_out).setVisible(false);
        } else {
            // logged in
            menu.findItem(R.id.action_log_in).setVisible(false);
            menu.findItem(R.id.action_log_out).setVisible(true);
        }
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
                final Intent nextActivity = new Intent(ListActivity.this, UserSettingActivity.class);
                Log.d("actionbar", "settings clicked");
                startActivity(nextActivity);
                return true;
            case R.id.action_log_out:
                app.logoutCurrentUser();
                startActivity(new Intent(ListActivity.this, MainActivity.class));
                Log.d("actionbar", "logout clicked");
                return true;
            case R.id.action_log_in:
                final Intent signInActivity = new Intent(ListActivity.this, SignInActivity.class);
                startActivity(signInActivity);
                return true;
            case android.R.id.home:
                final Intent mainActivity = new Intent(ListActivity.this, MainActivity.class);
                startActivity(mainActivity);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Fragment representing the overview of the categories of hypotheses
    // For now this is just sleep, focus and nutrition
    public class CategoriesFragment extends Fragment {

        public CategoriesFragment() {

        }

        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            // Set layout to category fragment
            View rootView = inflater.inflate(R.layout.categories_fragment, container, false);
            // Grab category buttons from layout
            final Button category1 = (Button) rootView.findViewById(R.id.cat1);
            final Button category2 = (Button) rootView.findViewById(R.id.cat2);
            final Button category3 = (Button) rootView.findViewById(R.id.cat3);
            // Set button text content to categories found in application object
            List<ParseObject> categoryList = hypothesisRepo.categoryList;
            Log.i("application", "categoryList in ListActivity set to " + hypothesisRepo.categoryList);
            category1.setText(categoryList.get(0).getString("categoryName"));
            Log.i("application", "category should be set to set to " + hypothesisRepo.categoryList.get(0).getString("categoryName"));

            category2.setText(categoryList.get(1).getString("categoryName"));
            category3.setText(categoryList.get(2).getString("categoryName"));

            category1.setTextAppearance(getApplicationContext(), R.style.TextShadow);
            category2.setTextAppearance(getApplicationContext(), R.style.TextShadow);
            category3.setTextAppearance(getApplicationContext(), R.style.TextShadow);

            // set background images of buttons
            category1.setBackgroundResource(R.drawable.category_sleep);
            category2.setBackgroundResource(R.drawable.category_focus);
            category3.setBackgroundResource(R.drawable.category_nutrition);

            category1.getBackground().setAlpha(150);
            category2.getBackground().setAlpha(150);
            category3.getBackground().setAlpha(150);

            // Generalized click listener for all three buttons
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create new list fragment
                    HypothesisListFragment list = new HypothesisListFragment();
                    // Attach name of category to fragment
                    Bundle nextArgs = new Bundle();
                    Button currentButton = (Button) v;
                    nextArgs.putBoolean("search", false);
                    nextArgs.putString("category", currentButton.getText().toString());
                    list.setArguments(nextArgs);
                    // Inflate list fragment
                    getFragmentManager().beginTransaction().addToBackStack("List").replace(R.id.container, list).commit();
                }
            };
            // Set click listeners to all three buttons
            category1.setOnClickListener(clickListener);
            category2.setOnClickListener(clickListener);
            category3.setOnClickListener(clickListener);
            return rootView;
        }
    }

    // Fragment for the list of Hypothesis reached when a category is clicked
    // or a hypothesis is searched for
    public class HypothesisListFragment extends Fragment {

        public HypothesisListFragment() {

        }

        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.hypothesis_list_fragment, container, false);
            Bundle arguments = getArguments();
            boolean search = arguments.getBoolean("search");
            if(!search) {

                // Display entire category case
                String category = arguments.getString("category");

                // needs to use ArrayAdapter and a custom layout for each row, found in hypothesis_row.xml
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Hypothesis");

                // Set appropriate category
                if (!category.isEmpty()) {
                    Log.d("list", category + " category chosen");
                    String categoryString = category.toLowerCase() + "_object_id";
                    String categoryID = getString(getResources().getIdentifier(categoryString, "string", getPackageName()));

                    ParseObject obj = ParseObject.createWithoutData("Category", categoryID);
                    query.whereEqualTo("parentCategory", obj);
                } else {
                    Log.d("list", "no specific category chosen, all hypothesis queried");
                }

                query.orderByDescending("usersJoined");
                // execute query sorted by userJoined for now
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if (e == null) {
                            // populate list with returned hypotheses
                            populateHypothesesList(parseObjects, rootView);
                            Log.i("application", "Hypotheses retrieved " + parseObjects);
                        } else {
                            Log.d("parseError", "error retrieving hypotheses " + e.getMessage());
                            Log.i("application", "error retrieving hypotheses " + e.getMessage());
                        }
                    }
                });
            } else {
                Log.d("search", "entered search case in OnCreateView");
                // Display search results
                String searchQuery = arguments.getString("query");
                // needs to use ArrayAdapter and a custom layout for each row, found in hypothesis_row.xml

                // split keywords on spaces
                String[] queryTerms = searchQuery.split(" ");
                List<String> queryList = Arrays.asList(queryTerms);

                // Here is where we need the complex query. We have n search terms in the above array.
                // We need all hypotheses whose ifDescription (or thenDescription) contain at least one of these keywords
                // Can use another search strategy, but this seems like a valid and basic strategy.

                // I gave it a shot but this is a more garvage version which is not very useful
                // if a keyword matches a title exactly
                ParseQuery<ParseObject> ifQuery = ParseQuery.getQuery("Hypothesis");
                ifQuery.whereContainedIn("ifDescription", queryList);
                ParseQuery<ParseObject> thenQuery = ParseQuery.getQuery("Hypothesis");
                thenQuery.whereContainedIn("thenDescription", queryList);

                // If a full query is the beginning of a title
                List<ParseQuery<ParseObject>> compoundQuery = new ArrayList<ParseQuery<ParseObject>>();
                ParseQuery<ParseObject> ifStartsQuery = ParseQuery.getQuery("Hypothesis");
                ifStartsQuery.whereStartsWith("ifDescription", searchQuery);
                ParseQuery<ParseObject> thenStartsQuery = ParseQuery.getQuery("Hypothesis");
                thenStartsQuery.whereStartsWith("thenDescription", searchQuery);
                compoundQuery.add(thenStartsQuery);

                compoundQuery.add(ifQuery);
                compoundQuery.add(thenQuery);
                ParseQuery<ParseObject> finalQuery = ParseQuery.or(compoundQuery);

                Log.d("search", "termsList: " + queryTerms);

                // Sort by users
                finalQuery.addDescendingOrder("usersJoined");

                finalQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if (e == null) {
                            // populate list with returned hypotheses
                            // Once the query works this method call will populate the listview with all rturned queries
                            populateHypothesesList(parseObjects, rootView);
                            Log.i("application", "Hypotheses retrieved " + parseObjects);
                        } else {
                            Log.d("parseError", "error retrieving hypotheses " + e.getMessage());
                            Log.i("application", "error retrieving hypotheses " + e.getMessage());
                        }
                    }
                });
            }
            return rootView;
        }

        // Given a list of parseObjects containing hypotheses populate the list fragment
        public void populateHypothesesList(List<ParseObject> parseObjects, View rootView) {
            // Convert list of parseobjects to array of hypothesisListItems
            List<HypothesisListItem> listData = new ArrayList<>();
            for (int i = 0; i < parseObjects.size(); i++) {
                listData.add(new HypothesisListItem(parseObjects.get(i)));
            }
            // Get Adapter
            final HypothesisAdapter adapter = new HypothesisAdapter(ListActivity.this, R.layout.hypothesis_row, listData);
            ListView listView = (ListView) rootView.findViewById(R.id.hypList);

            // set adapter to the list view
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent profilePage = new Intent(ListActivity.this, HypothesisProfileActivity.class);
                    // Add any extras here for data that needs to be passed to the ListActivity
                    profilePage.putExtra("hypothesisData", adapter.getItemAtPosition(position));
                    startActivity(profilePage);
                }
            });
        }
    }

    public void doSearch(String query) {
        Log.d("search", "received query of " + query);
        HypothesisListFragment list = new HypothesisListFragment();
        // Attach name of category to fragment
        Bundle nextArgs = new Bundle();
        // create new hypothesis list fragment with a flag for search and pass along the query
        nextArgs.putBoolean("search", true);
        nextArgs.putString("query", query);
        list.setArguments(nextArgs);
        // Inflate list fragment
        getFragmentManager().beginTransaction().addToBackStack("List").replace(R.id.container, list).commit();
    }

}
