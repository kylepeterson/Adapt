package timetoadapt.me.adapt;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.parse.ParseObject;

import java.util.List;


public class ListActivity extends Activity {
    protected static HypothesisRepo hypothesisRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        AdaptApp app = (AdaptApp) getApplication();
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
    public static class CategoriesFragment extends Fragment {

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
                    getFragmentManager().beginTransaction().replace(R.id.container, list).commit();
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
    public static class HypothesisListFragment extends Fragment {

        public HypothesisListFragment() {

        }

        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.hypothesis_list_fragment, container, false);
            Bundle arguments = getArguments();
            String category = arguments.getString("category");
            // put code here to query parse for all hypothesis mapping to the category
            ListView list = (ListView) rootView.findViewById(R.id.hypList);
            // put code here to append rows to the list view for each hypothesis
            // needs to use ArrayAdapter and a custom layout for each row, found in hypothesis_row.xml
            return rootView;
        }
    }

}
