package timetoadapt.me.adapt;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;


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
                    LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

                    View popupView = layoutInflater.inflate(R.layout.user_required_popup, null);

                    final PopupWindow popup = new PopupWindow(popupView, 400, 600);

                    TextView dismissButton = (TextView) popupView.findViewById(R.id.dismiss_button);
                    dismissButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            popup.dismiss();
                        }
                    });

                    Button registerButton = (Button) popupView.findViewById(R.id.register_button);
                    registerButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent createIntent = new Intent(MainActivity.this, UserCreationActivity.class);
                            startActivity(createIntent);
                        }
                    });

                    popup.showAsDropDown(createButton, 0, 0);



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
}
