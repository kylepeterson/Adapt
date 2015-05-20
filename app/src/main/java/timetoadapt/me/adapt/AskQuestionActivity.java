package timetoadapt.me.adapt;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by ravnon on 5/19/15.
 */
public class AskQuestionActivity extends Activity {

    private AdaptApp instance;
    private String hypothesisID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_question);

        // Hide name of activity in actionbar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        AdaptApp app = (AdaptApp) getApplication();
        instance = app.getInstance();

        Intent intent = getIntent();
        hypothesisID = intent.getStringExtra("hypothesisID");

        final TextView questionText = (TextView) findViewById(R.id.question_text);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Question");
        ParseObject obj = ParseObject.createWithoutData("Hypothesis", hypothesisID);
        query.whereEqualTo("hypothesis", obj);

        query.whereEqualTo("timeToAsk", 0);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    questionText.setText(list.get(0).getString("questionText"));
                } else {
                    Crouton.makeText(AskQuestionActivity.this, e.getMessage(), Style.ALERT).show();
                }
            }
        });


    }
}
