package timetoadapt.me.adapt;

import android.app.Activity;
import android.app.Fragment;
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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;


public class ListActivity extends Activity {
    protected static HypothesisRepo hypothesisRepo;
    private static AdaptApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        app = (AdaptApp) getApplication();
        AdaptApp instance = app.getInstance();
        hypothesisRepo = instance.hypothesisRepo;

        if(savedInstanceState == null) {
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Fragment representing the overview of the categories of hypotheses
    // For now this is just sleep, focus and nutrition
    public  class CategoriesFragment extends Fragment {

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
            // Generalized click listener for all three buttons
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create new list fragment
                    HypothesisListFragment list = new HypothesisListFragment();
                    // Attach name of category to fragment
                    Bundle nextArgs = new Bundle();
                    Button currentButton = (Button) v;
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
    public  class HypothesisListFragment extends Fragment {

        public HypothesisListFragment() {

        }

        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.hypothesis_list_fragment, container, false);
            Bundle arguments = getArguments();
            String category = arguments.getString("category");
            // put code here to query parse for all hypothesis mapping to the category
            ListView list = (ListView) rootView.findViewById(R.id.hypList);
            // put code here to append rows to the list view for each hypothesis
            // needs to use ArrayAdapter and a custom layout for each row, found in hypothesis_row.xml
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Hypothesis");
            // Set appropriate category
            if(category.equals("Sleep")) {
                Log.d("list", "sleep category chosen : " + R.string.sleep_object_id);
                //query.whereEqualTo("categoryName", "Sleep");
                ParseObject obj = ParseObject.createWithoutData("Category", getString(R.string.sleep_object_id));
                query.whereEqualTo("parentCategory", obj);

            } else if(category.equals("Focus")) {
                Log.d("list", "focus category chosen : " + R.string.focus_object_id);
                //query.whereEqualTo("categoryName", "Focus");
                ParseObject obj = ParseObject.createWithoutData("Category", getString(R.string.focus_object_id ));
                query.whereEqualTo("parentCategory", obj);
            } else if(category.equals("Nutrition")) {
                Log.d("list", "nutrition category chosen: " + R.string.nutrition_object_id);
                //query.whereEqualTo("categoryName", "Nutrition");
                ParseObject obj = ParseObject.createWithoutData("Category", getString(R.string.nutrition_object_id));
                query.whereEqualTo("parentCategory", obj);
            } else {
                Log.d("list", "no specific category chosen, all hypothesis queried");
            }
            query.orderByDescending("usersJoined");
            // execute query sorted by userJoined for now
            query.findInBackground( new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if(e == null) {
                        // populate list with returned hypotheses
                        populateHypothesesList(parseObjects, rootView);
                        Log.i("application", "Hypotheses retrieved " + parseObjects);
                    } else {
                        Log.d("parseError", "error retrieving hypotheses " + e.getMessage());
                        Log.i("application", "error retrieving hypotheses " + e.getMessage());
                    }
                }
            });
            return rootView;
        }

        // Given a list of parseObjects containing hypotheses populate the list fragment
        public void populateHypothesesList(List<ParseObject> parseObjects, View rootView) {
            // Convert list of parseobjects to array of hypothesisListItems
            HypothesisListItem[] listData = new HypothesisListItem[parseObjects.size()];
            for(int i = 0; i < parseObjects.size(); i++) {
                listData[i] = new HypothesisListItem(parseObjects.get(i));
            }
            // Get Adapter
            final HypothesisAdapter adapter = new HypothesisAdapter(ListActivity.this , R.layout.hypothesis_row, listData);
            ListView listView = (ListView)rootView.findViewById(R.id.hypList);

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

}
