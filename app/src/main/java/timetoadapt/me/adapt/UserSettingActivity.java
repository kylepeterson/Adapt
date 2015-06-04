package timetoadapt.me.adapt;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by kylepeterson on 5/16/15.
 */
public class UserSettingActivity extends PreferenceActivity {
    protected static AdaptApp instance;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.user_settings);

        AdaptApp app = (AdaptApp) getApplication();
        instance = app.getInstance();
    }

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
            case R.id.action_log_out:
                instance.logoutCurrentUser();
                startActivity(new Intent(UserSettingActivity.this, MainActivity.class));
                Log.d("actionbar", "logout clicked");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
