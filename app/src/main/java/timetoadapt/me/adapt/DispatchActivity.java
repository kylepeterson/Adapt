package timetoadapt.me.adapt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.parse.ParseUser;

/**
 * Created by ravnon on 4/14/15.
 */
public class DispatchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(DispatchActivity.this, UserCreationActivity.class));
        } else {
            startActivity(new Intent(DispatchActivity.this, MainActivity.class));
        }
    }
}