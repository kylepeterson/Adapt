package timetoadapt.me.adapt;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private HypothesisListItem hypothesisData;
    private LinearLayout experiences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hypothesis_profile);
        // Bring things to the front of the view
        RelativeLayout titleBox = (RelativeLayout) findViewById(R.id.title_box);
        titleBox.bringToFront();

        // Hide name of activity in actionbar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        // make action bar home button work
        actionBar.setDisplayHomeAsUpEnabled(true);
        AdaptApp app = (AdaptApp) getApplication();
        instance = app.getInstance();

        Intent intent = getIntent();
        hypothesisData = intent.getParcelableExtra("hypothesisData");

        // get parameters
        final String hypId = hypothesisData.objectID;

        TextView tryThis = (TextView) findViewById(R.id.hypothesis_try_this);
        TextView toAccomplish = (TextView) findViewById(R.id.hypothesis_to_accomplish);
        TextView description = (TextView) findViewById(R.id.hypothesis_description);

        tryThis.setText(hypothesisData.tryThis);
        toAccomplish.setText(hypothesisData.toAccomplish);
        description.setText(hypothesisData.description);


        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery("Image");

        //ParseQuery<ParseObject> hypQuery = ParseQuery.getQuery("Hypothesis");
        //hypQuery.whereEqualTo("objectId", hypId);

        imageQuery.whereEqualTo("hypId", hypId);
        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (list != null && !list.isEmpty()) {
                    ParseObject imageObject = list.get(0);
                    ParseFile file = imageObject.getParseFile("image");
                    final ParseImageView imageView = (ParseImageView) findViewById(R.id.imageView);
                    imageView.setPlaceholder(getResources().getDrawable(R.color.adapt_dark_grey));
                    imageView.setParseFile(file);
                    Log.d("images", "about to set loadIB on " + imageView);
                    imageView.loadInBackground(new GetDataCallback() {
                        public void done(byte[] data, ParseException e) {
                            if (e == null) {
                                Log.d("images", "loaded image");
                            } else {
                                Log.d("images", "error with image: " + e.getMessage());
                            }
                            // The image is loaded and displayed!

                        }
                    });
                } else {
                    Log.d("images", "no image found for " + hypId + ". List of POs: " + list);
                }
            }
        });
        join = (Button) findViewById(R.id.hypothesis_join_button);
        updateJoinButton();
        join.bringToFront();

        final EditText experience = (EditText) findViewById(R.id.experience_edit_text);
        Button submitExperience = (Button) findViewById(R.id.experience_submit);
        submitExperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = experience.getText().toString();
                if (!text.isEmpty()) {
                    ParseObject comment = new ParseObject("Comment");
                    comment.put("hypothesis", ParseObject.createWithoutData("Hypothesis", hypothesisData.objectID));
                    comment.put("user", instance.getCurrentUser());
                    comment.put("votes", 1);
                    comment.put("content", text);
                    comment.saveInBackground();

                    experience.getText().clear();
                }
            }
        });

        final WebView dataWebView = (WebView) findViewById(R.id.data_web_view);
        // enable javascript

        if (instance.getCurrentUser() != null) {
            dataWebView.getSettings().setJavaScriptEnabled(true);

            // disable scroll on touch
            dataWebView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return (event.getAction() == MotionEvent.ACTION_MOVE);
                }
            });

            //disable scrolling
            dataWebView.setVerticalScrollBarEnabled(false);
            dataWebView.setHorizontalScrollBarEnabled(false);

            // Enable html5 features in webview
            WebSettings ws = dataWebView.getSettings();
            ws.setAllowFileAccess(true);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                try {
                    Log.d("webview", "Enabling HTML5-Features");
                    Method m1 = WebSettings.class.getMethod("setDomStorageEnabled", new Class[]{Boolean.TYPE});
                    m1.invoke(ws, Boolean.TRUE);

                    Method m2 = WebSettings.class.getMethod("setDatabaseEnabled", new Class[]{Boolean.TYPE});
                    m2.invoke(ws, Boolean.TRUE);

                    Method m3 = WebSettings.class.getMethod("setDatabasePath", new Class[]{String.class});
                    m3.invoke(ws, "/data/data/" + getPackageName() + "/databases/");

                    Method m4 = WebSettings.class.getMethod("setAppCacheMaxSize", new Class[]{Long.TYPE});
                    m4.invoke(ws, 1024 * 1024 * 8);

                    Method m5 = WebSettings.class.getMethod("setAppCachePath", new Class[]{String.class});
                    m5.invoke(ws, "/data/data/" + getPackageName() + "/cache/");

                    Method m6 = WebSettings.class.getMethod("setAppCacheEnabled", new Class[]{Boolean.TYPE});
                    m6.invoke(ws, Boolean.TRUE);

                    Log.d("webview", "Enabled HTML5-Features");
                } catch (NoSuchMethodException e) {
                    Log.e("webview", "Reflection fail", e);
                } catch (InvocationTargetException e) {
                    Log.e("webview", "Reflection fail", e);
                } catch (IllegalAccessException e) {
                    Log.e("webview", "Reflection fail", e);
                }
            }

            ParseUser currentUser = instance.getCurrentUser();
            String chartUrl = "";
            if (currentUser != null) {
                // get chart with personal and aggregate
                String userId = instance.getCurrentUser().getObjectId();
                Log.d("params", "current hyp: " + hypId + ". current user: " + userId);
                // load chart
                chartUrl = "http://adapt.parseapp.com/chart?user=" + userId + "&hypothesis=" + hypId;
            } else {
                // get chart with just aggregate
                chartUrl = "http://adapt.parseapp.com/chart?hypothesis=" + hypId;
            }

            // Loading bar display while webview is loading
            ProgressBar spinner;
            spinner = (ProgressBar) findViewById(R.id.loading);
            dataWebView.setWebViewClient(new AppWebViewClients(spinner));
            dataWebView.setVisibility(View.GONE);
            dataWebView.loadUrl(chartUrl);
            dataWebView.reload();
            dataWebView.setVisibility(View.VISIBLE);

        }

        experiences = (LinearLayout) findViewById(R.id.experience_layout);
        getExperiences();

        // bring focus to top of scrollview not to top of webview
        final ScrollView main = (ScrollView) findViewById(R.id.scrollWrapper);
        main.post(new Runnable() {
            public void run() {
                main.scrollTo(0, 0);
            }
        });
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
        } else {
            join.setText(getResources().getText(R.string.hypothesis_join_text));
            join.setBackgroundColor(getResources().getColor(R.color.adapt_blue));
            join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (instance.getCurrentUser() != null) {
                        subscribeUser();
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
        }
    }

    public void subscribeUser() {
        final ProgressDialog dialog = new ProgressDialog(HypothesisProfileActivity.this);
        dialog.setMessage("Joining you...");
        dialog.show();

        ParseObject hypo = ParseObject.createWithoutData("Hypothesis", hypothesisData.objectID);
        hypo.increment("usersJoined");
        hypo.increment("totalUsers");
        hypo.saveEventually();

        instance.getCurrentUser().addUnique("joined", hypothesisData.objectID);
        instance.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                if (e == null) {
                    instance.updateCurrentUser();
                    //    updateJoinButton();
                } else {
                    Crouton.makeText(HypothesisProfileActivity.this, e.getMessage(), Style.ALERT).show();
                }
            }
        });

        Intent questionPage = new Intent(HypothesisProfileActivity.this, AskQuestionActivity.class);
        // Add any extras here for data that needs to be passed to the QuestionActivity
        questionPage.putExtra("hypothesisData", hypothesisData);
        questionPage.putExtra("timeToAsk", 0);
        startActivity(questionPage);
    }

    private void unsubscribeUser(String hypothesisID) {
        final ProgressDialog dialog = new ProgressDialog(HypothesisProfileActivity.this);
        dialog.setMessage("Unsubscribing you...");
        dialog.show();

        ParseObject hypo = ParseObject.createWithoutData("Hypothesis", hypothesisData.objectID);
        hypo.increment("usersJoined", -1);
        hypo.saveEventually();

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

    public void getExperiences() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Comment");
        query.whereEqualTo("hypothesis", ParseObject.createWithoutData("Hypothesis", hypothesisData.objectID));
        query.addDescendingOrder("votes");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                for (ParseObject comment : list) {
                    final String id = comment.getObjectId();
                    String content = comment.getString("content");
                    int votes = comment.getInt("votes");
                    String author = comment.getParseUser("user").getUsername();

                    View commentRow = getLayoutInflater().inflate(R.layout.comment_row, null);
                    ((TextView) commentRow.findViewById(R.id.votes)).setText(Integer.toString(votes));
                    ((TextView) commentRow.findViewById(R.id.comment_text)).setText(content);

                    commentRow.findViewById(R.id.upvote).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ParseObject comm = ParseObject.createWithoutData("Comment", id);
                            comm.increment("votes", 1);
                            comm.saveInBackground();
                        }
                    });

                    commentRow.findViewById(R.id.downvote).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ParseObject comm = ParseObject.createWithoutData("Comment", id);
                            comm.increment("votes", -1);
                            comm.saveInBackground();
                        }
                    });

                    experiences.addView(commentRow);
                }
            }
        });
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
        if (instance.hasUserJoinedHypothesis(hypothesisData.objectID)) {
            menu.findItem(R.id.action_unsubscribe).setVisible(true);
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
                final Intent nextActivity = new Intent(HypothesisProfileActivity.this, UserSettingActivity.class);
                Log.d("actionbar", "settings clicked");
                startActivity(nextActivity);
                return true;
            case R.id.action_log_out:
                instance.logoutCurrentUser();
                startActivity(new Intent(HypothesisProfileActivity.this, MainActivity.class));
                Log.d("actionbar", "logout clicked");
                return true;
            case R.id.action_log_in:
                final Intent signInActivity = new Intent(HypothesisProfileActivity.this, SignInActivity.class);
                startActivity(signInActivity);
                return true;
            case android.R.id.home:
                final Intent mainActivity = new Intent(HypothesisProfileActivity.this, MainActivity.class);
                startActivity(mainActivity);
                return true;
            case R.id.action_unsubscribe:
                unsubscribeUser(hypothesisData.objectID);
                item.setVisible(false);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class AppWebViewClients extends WebViewClient {
        private ProgressBar progressBar;

        public AppWebViewClients(ProgressBar progressBar) {
            this.progressBar = progressBar;
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }
    }
}


