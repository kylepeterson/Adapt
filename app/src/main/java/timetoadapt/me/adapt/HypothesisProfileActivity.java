package timetoadapt.me.adapt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.SaveCallback;

/**
 * Created by ravnon on 5/1/15.
 */
public class HypothesisProfileActivity extends Activity {

    private AdaptApp instance;
    private Button join;
    private HypothesisListItem hypothesisData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hypothesis_profile);

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
        updateJoinButton();
    }

    public void updateJoinButton() {
        if (instance.hasUserJoinedHypothesis(hypothesisData.objectID)) {
            join.setText("Joined");
            join.setBackgroundColor(getResources().getColor(R.color.adapt_green));
            join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(HypothesisProfileActivity.this, "You already joined this hypothesis", Toast.LENGTH_LONG).show();
                }
            });
        } else {
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
                                Intent intent = new Intent(HypothesisProfileActivity.this, UserCreationActivity.class);
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
                    Toast.makeText(HypothesisProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
