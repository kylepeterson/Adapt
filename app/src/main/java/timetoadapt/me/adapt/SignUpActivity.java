package timetoadapt.me.adapt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by ravnon on 4/14/15.
 */
public class SignUpActivity extends Activity {
    protected static AdaptApp instance;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText passwordConfirmationEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        AdaptApp app = (AdaptApp) getApplication();
        instance = app.getInstance();

        ScrollView layout = (ScrollView) findViewById(R.id.signup_form);
        layout.setBackgroundColor(getResources().getColor(R.color.adapt_dark_grey));
        layout.setBackgroundResource(R.drawable.mountain2);
        layout.getBackground().setAlpha(50);

        ParseObject analObject = new ParseObject("Analytics");
        analObject.put("action", "user_sign_up");
        analObject.saveInBackground();

        usernameEditText = (EditText) findViewById(R.id.username_edit_text);

        passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        passwordConfirmationEditText = (EditText) findViewById(R.id.password_confirmation_edit_text);


        // Set up the submit button click handler
        Button mActionButton = (Button) findViewById(R.id.sign_up_button);
        mActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                signup();
            }
        });
    }

    private void signup() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String passwordAgain = passwordConfirmationEditText.getText().toString().trim();

        // Validate the sign up data
        boolean validationError = false;
        StringBuilder validationErrorMessage = new StringBuilder(getString(R.string.error_intro));
        if (username.length() == 0) {
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_blank_username));
        } else if (username.contains("@")) {
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_bad_username));
        }
        if (password.length() == 0) {
            if (validationError) {
                validationErrorMessage.append(getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_blank_password));
        }
        if (!password.equals(passwordAgain)) {
            if (validationError) {
                validationErrorMessage.append(getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_mismatched_passwords));
        }
        validationErrorMessage.append(getString(R.string.error_end));

        // If there is a validation error, display the error
        if (validationError) {
            Crouton.makeText(SignUpActivity.this, validationErrorMessage.toString(), Style.ALERT, (ViewGroup) findViewById(R.id.crouton_error)).show();
            return;
        }

        // Set up a progress dialog
        final ProgressDialog dialog = new ProgressDialog(SignUpActivity.this);
        dialog.setMessage(getString(R.string.progress_signup));
        dialog.show();

        // Set up a new Parse user
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);

        // Call the Parse signup method
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                if (e != null) {
                    // Show the error message
                    Crouton.makeText(SignUpActivity.this, e.getMessage(), Style.ALERT, (ViewGroup) findViewById(R.id.crouton_error)).show();
                } else {
                    // Start an intent for the dispatch activity
                    instance.updateCurrentUser();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
    }
}
