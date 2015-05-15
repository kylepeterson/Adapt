package timetoadapt.me.adapt;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseObject;

/**
 * Created by ravnon on 4/14/15.
 */
public class UserCreationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_creation_fragment);

        // Hide name of activity in actionbar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        ParseObject analObject = new ParseObject("Analytics");
        analObject.put("action", "user_creation_screen");
        analObject.saveInBackground();

        Button signinButton = (Button) findViewById(R.id.signin_button);
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserCreationActivity.this, SignInActivity.class));
            }
        });

        Button signupButton = (Button) findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserCreationActivity.this, SignUpActivity.class));
            }
        });

        ((TextView) findViewById(R.id.user_creation_title)).setText(R.string.create_user_welcome);
        ((TextView) findViewById(R.id.user_creation_explanation_text)).setText("");
    }
}
