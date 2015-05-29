package timetoadapt.me.adapt;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by ravnon on 5/1/15.
 */
public class HypothesisProfileActivity extends Activity {

    private AdaptApp instance;
    private Button join;
    private TextView ubsubscribe;
    private HypothesisListItem hypothesisData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hypothesis_profile);

        // Hide name of activity in actionbar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        AdaptApp app = (AdaptApp) getApplication();
        instance = app.getInstance();

        Intent intent = getIntent();
        hypothesisData = intent.getParcelableExtra("hypothesisData");

        TextView tryThis = (TextView) findViewById(R.id.hypothesis_try_this);
        TextView toAccomplish = (TextView) findViewById(R.id.hypothesis_to_accomplish);
        TextView description = (TextView) findViewById(R.id.hypothesis_description);

        tryThis.setText(hypothesisData.tryThis);
        toAccomplish.setText(hypothesisData.toAccomplish);
        description.setText(hypothesisData.description);

        join = (Button) findViewById(R.id.hypothesis_join_button);
        ubsubscribe = (TextView) findViewById(R.id.unsubscribe_button);
        updateJoinButton();

        WebView dataWebView = (WebView) findViewById(R.id.data_web_view);
        dataWebView.getSettings().setJavaScriptEnabled(true);
        dataWebView.loadUrl("http://bud.haus/~pi/dangus_cam/");

    }

    public void updateJoinButton() {
        if (instance.hasUserJoinedHypothesis(hypothesisData.objectID)) {
            join.setText(getResources().getText(R.string.hypothesis_joined_text));
            join.setBackgroundColor(getResources().getColor(R.color.adapt_green));
            join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Crouton.makeText(HypothesisProfileActivity.this, "You already joined this hypothesis", Style.INFO).show();

                }
            });
            ubsubscribe.setVisibility(View.VISIBLE);
            ubsubscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unsubscribeUser(hypothesisData.objectID);
                }
            });
        } else {
            join.setText(getResources().getText(R.string.hypothesis_join_text));
            join.setBackgroundColor(getResources().getColor(R.color.adapt_blue));
            join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (instance.getCurrentUser() != null) {
                        subscribeUser(hypothesisData.objectID);
                    } else { // not signed in
                        AlertDialog.Builder builder = new AlertDialog.Builder(HypothesisProfileActivity.this);
                        builder.setMessage(R.string.user_required_subscribe_dialog_message);

                        builder.setPositiveButton(R.string.user_required_dialog_positive, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(HypothesisProfileActivity.this, SignInActivity.class);
                                startActivity(intent);
                            }
                        });

                        builder.setNegativeButton(R.string.user_required_dialog_negative, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // negative click does nothing, just dismisses dialog
                            }
                        });
                        builder.create().show();
                    }
                }
            });
            ubsubscribe.setVisibility(View.GONE);
        }
    }


    public void subscribeUser(String hypothesisID) {
        final ProgressDialog dialog = new ProgressDialog(HypothesisProfileActivity.this);
        dialog.setMessage("Joining you...");
        dialog.show();

        instance.getCurrentUser().add("joined", hypothesisID);
        instance.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                if (e == null) {
                    instance.updateCurrentUser();
                    updateJoinButton();
                } else {
                    Crouton.makeText(HypothesisProfileActivity.this, e.getMessage(), Style.ALERT).show();
                }
            }
        });

        Intent questionPage = new Intent(HypothesisProfileActivity.this, AskQuestionActivity.class);
        // Add any extras here for data that needs to be passed to the QuestionActivity
        questionPage.putExtra("hypothesisID", hypothesisID);
        startActivity(questionPage);
    }

    private void unsubscribeUser(String hypothesisID) {
        final ProgressDialog dialog = new ProgressDialog(HypothesisProfileActivity.this);
        dialog.setMessage("Unsubscribing you...");
        dialog.show();

        List<String> toRemove = new ArrayList<>();
        toRemove.add(hypothesisID);

        instance.getCurrentUser().removeAll("joined", toRemove);
        instance.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                if (e == null) {
                    instance.updateCurrentUser();
                    updateJoinButton();
                } else {
                    Crouton.makeText(HypothesisProfileActivity.this, e.getMessage(), Style.ALERT).show();
                }
            }
        });
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
            case R.id.action_settings:
                final Intent nextActivity = new Intent(HypothesisProfileActivity.this, UserSettingActivity.class);
                Log.d("actionbar", "settings clicked");
                startActivity(nextActivity);
                return true;
            case R.id.action_log_out:
                instance.logoutCurrentUser();
                startActivity(new Intent(HypothesisProfileActivity.this, MainActivity.class));
                Log.d("actionbar", "logout clicked");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
