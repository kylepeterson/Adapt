package timetoadapt.me.adapt;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by ravnon on 5/19/15.
 */
public class AskQuestionActivity extends Activity {

    private AdaptApp instance;
    private HypothesisListItem hypothesisData;
    private boolean shouldRate;
    private int timesAnswered;
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
        actionBar.setDisplayHomeAsUpEnabled(true);

        AdaptApp app = (AdaptApp) getApplication();
        instance = app.getInstance();

        Intent intent = getIntent();
        hypothesisData = intent.getParcelableExtra("hypothesisData");
        timeToAsk = intent.getIntExtra("timeToAsk", -1);
        timesAnswered = intent.getIntExtra("timesAnswered", 0);

        Log.d("askQuestion", "Asking hypothesis id = " + hypothesisData.objectID + " in category = " + hypothesisData.categoryName + " with timeToAsk = " + timeToAsk);

        if (timeToAsk == -1) {
            Crouton.makeText(AskQuestionActivity.this, "We're sorry, there was a problem getting data. Please try again.", Style.ALERT).show();
        } else {
            questionText = (TextView) findViewById(R.id.question_text);

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Question");
            final ParseObject obj = ParseObject.createWithoutData("Hypothesis", hypothesisData.objectID);
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
                            String toAskText = getHypothesisQuestion(hypothesisData, timeToAsk);
                            newQuestion.put("questionText", toAskText);
                            newQuestion.put("timeToAsk", Math.min(timeToAsk, 1));
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

            shouldRate = false;

            if (timesAnswered >= 5) { // user answered over 5 times
                ParseQuery<ParseObject> ratingQuery = new ParseQuery<>("HypothesisRating");
                ratingQuery.whereEqualTo("user", instance.getCurrentUser());
                ratingQuery.whereEqualTo("hypothesis", ParseObject.createWithoutData("Hypothesis", hypothesisData.objectID));
                ratingQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if (e == null) {
                            shouldRate = list.isEmpty(); // user hasn't rated before
                        } else {
                            Crouton.makeText(AskQuestionActivity.this, e.getMessage(), Style.ALERT).show();
                        }
                    }
                });
            }
        }
    }

    private void displayData() {
        questionText.setText(question.getString("questionText"));

        SeekBar sb = (SeekBar) findViewById(R.id.ratingSeekBar);
        sb.setVisibility(View.VISIBLE);

        final TextView ratingCaption = (TextView) findViewById(R.id.seekBarCaption);
        ratingCaption.setVisibility(View.VISIBLE);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ratingCaption.setText(progress + "/10");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (shouldRate) {
            findViewById(R.id.hypothesis_rating_question_text).setVisibility(View.VISIBLE);
            findViewById(R.id.ratingStarBar).setVisibility(View.VISIBLE);
        }

        Button submit = (Button) findViewById(R.id.submit_data_button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dialog = new ProgressDialog(AskQuestionActivity.this);
                dialog.setMessage("Submitting data...");
                dialog.show();

                int ratingSeek = ((SeekBar) findViewById(R.id.ratingSeekBar)).getProgress();


                if (shouldRate) {
                    float ratingStars = ((RatingBar) findViewById(R.id.ratingStarBar)).getRating();
                    ParseObject rating = new ParseObject("HypothesisRating");
                    rating.put("hypothesis", ParseObject.createWithoutData("Hypothesis", hypothesisData.objectID));
                    rating.put("user", instance.getCurrentUser());
                    rating.put("rating", ratingStars);
                    rating.saveEventually();
                }

                ParseObject answer = new ParseObject("Answer");
                answer.put("question", question);
                answer.put("user", instance.getCurrentUser());
                answer.put("submittedAt", Calendar.getInstance().getTime());
                answer.put("answerContent", ratingSeek);

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

    public String getHypothesisQuestion(HypothesisListItem hypothesis, int timeToAsk) {
        String questionText;
        if (hypothesis.categoryID.equals(getResources().getString(R.string.focus_object_id))) {
            if (timeToAsk == 0) {
                questionText = getResources().getString(R.string.focus_before_question);
            } else {
                questionText = getResources().getString(R.string.focus_after_question);
            }
        } else if (hypothesis.categoryID.equals(getResources().getString(R.string.sleep_object_id))) {
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
            }
        }
        return questionText;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, ListActivity.class)));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        // log in vs log out
        if (instance.getCurrentUser() == null) {
            // not logged in
            menu.findItem(R.id.action_log_in).setVisible(true);
            menu.findItem(R.id.action_log_out).setVisible(false);
        } else {
            // logged in
            menu.findItem(R.id.action_log_in).setVisible(false);
            menu.findItem(R.id.action_log_out).setVisible(true);
        }
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
                final Intent nextActivity = new Intent(AskQuestionActivity.this, UserSettingActivity.class);
                Log.d("actionbar", "settings clicked");
                startActivity(nextActivity);
                return true;
            case R.id.action_log_out:
                instance.logoutCurrentUser();
                startActivity(new Intent(AskQuestionActivity.this, MainActivity.class));
                Log.d("actionbar", "logout clicked");
                return true;
            case R.id.action_log_in:
                final Intent signInActivity = new Intent(AskQuestionActivity.this, SignInActivity.class);
                startActivity(signInActivity);
                return true;
            case android.R.id.home:
                final Intent mainActivity = new Intent(AskQuestionActivity.this, MainActivity.class);
                startActivity(mainActivity);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
