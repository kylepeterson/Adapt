package timetoadapt.me.adapt;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by ravnon on 5/19/15.
 */
public class AskQuestionActivity extends Activity {

    private AdaptApp instance;
    private String hypothesisID;
    private String hypothesisCategory;
    private int timeToAsk;
    private RatingBar rating;
    private TextView questionText;
    private ParseObject question;

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
        hypothesisCategory = intent.getStringExtra("hypothesisCategory");
        timeToAsk = intent.getIntExtra("timeToAsk", 0);


        questionText = (TextView) findViewById(R.id.question_text);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Question");
        final ParseObject obj = ParseObject.createWithoutData("Hypothesis", hypothesisID);
        query.whereEqualTo("hypothesis", obj);

        query.whereEqualTo("timeToAsk", timeToAsk);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    if (!list.isEmpty()) {
                        question = list.get(0);
                        displayData();
                    } else {
                        final ParseObject newQuestion = new ParseObject("Question");
                        newQuestion.put("hypothesis", obj);
                        newQuestion.put("questionType", 1);
                        String questionText;
                        if (hypothesisCategory.equals(getResources().getString(R.string.focus_object_id))) {
                            if (timeToAsk == 0) {
                                questionText = getResources().getString(R.string.focus_before_question);
                            } else {
                                questionText = getResources().getString(R.string.focus_after_question);
                            }
                        } else if (hypothesisCategory.equals(getResources().getString(R.string.sleep_object_id))) {
                            if (timeToAsk == 0) {
                                questionText = getResources().getString(R.string.sleep_before_question);
                            } else {
                                questionText = getResources().getString(R.string.sleep_after_question);
                            }
                        } else {
                            if (timeToAsk == 0) {
                                questionText = getResources().getString(R.string.nutrition_before_question);
                            } else {
                                questionText = getResources().getString(R.string.nutrition_after_question);
                            }                        }
                        newQuestion.put("questionText", questionText);
                        newQuestion.put("timeToAsk", timeToAsk);
                        newQuestion.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    question = newQuestion;
                                    displayData();
                                } else {
                                    Crouton.makeText(AskQuestionActivity.this, e.getMessage(), Style.ALERT).show();
                                }
                            }
                        });
                    }
                } else {
                    Crouton.makeText(AskQuestionActivity.this, e.getMessage(), Style.ALERT).show();
                }
            }
        });
    }

    private void displayData() {
        questionText.setText(question.getString("questionText"));

        rating = (RatingBar) findViewById(R.id.ratingBar);

        Button submit = (Button) findViewById(R.id.submit_data_button);
        submit.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                final ProgressDialog dialog = new ProgressDialog(AskQuestionActivity.this);
                dialog.setMessage("Submitting data...");
                dialog.show();

                float numStars = rating.getRating();

                ParseObject answer = new ParseObject("Answer");
                answer.put("question", question);
                answer.put("user", instance.getCurrentUser());
                answer.put("answerContent", numStars);

                answer.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            dialog.dismiss();
                            Intent intent = new Intent(AskQuestionActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            dialog.dismiss();
                            Crouton.makeText(AskQuestionActivity.this, e.getMessage(), Style.ALERT).show();
                        }
                    }
                });
            }
        });

    }
}
