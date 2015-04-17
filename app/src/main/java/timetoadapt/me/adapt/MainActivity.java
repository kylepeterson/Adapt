package timetoadapt.me.adapt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;


public class MainActivity extends Activity {
    protected AdaptApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (AdaptApp) getApplication();
        AdaptApp instance = app.getInstance();

        ParseObject analObject = new ParseObject("Analytics");
        analObject.put("action", "app_open");
        analObject.saveInBackground();

        TextView welcomeText = (TextView) findViewById(R.id.welcome_text_view);
        welcomeText.setText("Welcome " + ParseUser.getCurrentUser().getUsername() + "!");

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
                startActivity(new Intent(MainActivity.this, UserCreationActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
