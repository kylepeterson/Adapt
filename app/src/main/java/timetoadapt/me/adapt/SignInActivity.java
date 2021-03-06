package timetoadapt.me.adapt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by ravnon on 4/14/15.
 */
public class SignInActivity extends Activity {
    protected static AdaptApp instance;

    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        AdaptApp app = (AdaptApp) getApplication();
        instance = app.getInstance();

        ScrollView layout = (ScrollView) findViewById(R.id.signin_form);
        layout.setBackgroundColor(getResources().getColor(R.color.adapt_dark_grey));
        layout.setBackgroundResource(R.drawable.mountain);
        layout.getBackground().setAlpha(50);

        ParseObject analObject = new ParseObject("Analytics");
        analObject.put("action", "user_sign_in");
        analObject.saveInBackground();

        // Set up the login form.
        usernameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);

        // Set up the submit button click handler
        Button actionButton = (Button) findViewById(R.id.sign_in_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                login();
            }
        });

        TextView signUp = (TextView) findViewById(R.id.sign_up_text_prompt);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });


    }

    private void login() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate the log in data
        boolean validationError = false;
        StringBuilder validationErrorMessage = new StringBuilder(getString(R.string.error_intro));
        if (username.length() == 0) {
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_blank_username));
        }
        if (password.length() == 0) {
            if (validationError) {
                validationErrorMessage.append(getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_blank_password));
        }
        validationErrorMessage.append(getString(R.string.error_end));

        // If there is a validation error, display the error
        if (validationError) {
            Crouton.makeText(SignInActivity.this, validationErrorMessage.toString(), Style.ALERT).show();
            return;
        }

        // Set up a progress dialog
        final ProgressDialog dialog = new ProgressDialog(SignInActivity.this);
        dialog.setMessage(getString(R.string.progress_login));
        dialog.show();

        // Call the Parse login method
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                dialog.dismiss();
                if (e != null) {
                    // Show the error message
                    Crouton.makeText(SignInActivity.this, e.getMessage(), Style.ALERT).show();
                } else {
                    // Start an intent for the dispatch activity
                    instance.updateCurrentUser();
                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
    }
}