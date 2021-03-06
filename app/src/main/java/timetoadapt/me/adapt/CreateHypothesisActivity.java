package timetoadapt.me.adapt;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by ravnon on 4/16/15.
 */
public class CreateHypothesisActivity extends Activity implements OnAddQuestionListener {
    protected static HypothesisRepo hypothesisRepo;
    private AdaptApp instance;


    // fragment that allows user to add self report questions for hypothesis
    private QuestionsFragment questionsFragment;

    // stores user entered questions and the possible answers for each
    private Map<String, Map<Integer, List<String>>> hypothesisQuestions;

    private String tryThis;
    private String toAccomplish;
    private String description;
    private ParseObject category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hypothesis);

        // Hide name of activity in actionbar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        AdaptApp app = (AdaptApp) getApplication();
        instance = app.getInstance();
        hypothesisRepo = instance.hypothesisRepo;

        hypothesisQuestions = new HashMap<>();

        ParseObject analObject = new ParseObject("Analytics");
        analObject.put("action", "create_hypothesis");
        analObject.saveInBackground();

        // inflate question fragment
        QuestionOverviewFragment overviewFragment = new QuestionOverviewFragment();
        getFragmentManager().beginTransaction().replace(R.id.creation_container, overviewFragment).commit();

    }

    @Override
    // interface for child fragments to report a question has been added by the user
    // assumes the text and options have been checked for validity
    public void onAddQuestion(String text, List<String> options) {
        Map<Integer, List<String>> toPut = new HashMap<>();
        toPut.put(2, new ArrayList<String>());

        hypothesisQuestions.put(text, toPut); // add to list of questions
        questionsFragment.addQuestionToDisplay(text); // display on screen
    }

    @Override
    public void onAddQuestion(String text, int timeToAsk) {
        Map<Integer, List<String>> toPut = new HashMap<>();
        toPut.put(timeToAsk, new ArrayList<String>());

        hypothesisQuestions.put(text, toPut);
    }

    @Override
    public void startQuestionCreation(String tryThis, String toAccomplish, String description, ParseObject category) {
        this.tryThis = tryThis;
        this.toAccomplish = toAccomplish;
        this.description = description;
        this.category = category;

        questionsFragment = new QuestionsFragment();
        getFragmentManager().beginTransaction().replace(R.id.creation_container, questionsFragment).commit();
    }

    @Override
    public void finishQuestionCreation() {
        boolean validationError = false;
        StringBuilder validationErrorMessage = new StringBuilder(getString(R.string.error_intro));

        if (hypothesisQuestions.isEmpty()) {
            validationError = true;
            validationErrorMessage.append("include at least one question");
        }

        validationErrorMessage.append(getString(R.string.error_end));

        // If there is a validation error, display the error
        if (validationError) {
            Crouton.makeText(CreateHypothesisActivity.this, validationErrorMessage.toString(), Style.ALERT).show();
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(CreateHypothesisActivity.this);
        dialog.setMessage("Creating Hypothesis...");
        dialog.show();

        final ParseObject hypothesis = new ParseObject("Hypothesis");
        hypothesis.put("author", instance.getCurrentUser());
        hypothesis.put("ifDescription", this.tryThis);
        hypothesis.put("ifLowerCase", this.tryThis.toLowerCase());
        hypothesis.put("thenDescription", this.toAccomplish);
        hypothesis.put("thenLowerCase", this.toAccomplish.toLowerCase());
        hypothesis.put("description", this.description);
        hypothesis.put("parentCategory", this.category);
        hypothesis.put("usersJoined", 0);


        // submit each user entered question to database
        for (String questionText : hypothesisQuestions.keySet()) {
            Map<Integer, List<String>> timeToAskMap = hypothesisQuestions.get(questionText);
            List<String> options = new ArrayList<>();
            int time = 0;
            for (int timeToAsk : timeToAskMap.keySet()) {
                options = timeToAskMap.get(timeToAsk);
                time = timeToAsk;
            }

            ParseObject question = new ParseObject("Question");

            question.put("questionType", 1);
            question.put("questionText", questionText);
            question.put("hypothesis", hypothesis);
            question.put("timeToAsk", time);

            question.addAllUnique("questionOptions", options);


            question.saveInBackground();
        }

        // save the actual hypothesis.
        // we do this last in order to let the spinner dialog spin until the last thing is complete
        hypothesis.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                if (e == null) {
                    instance.getCurrentUser().add("created", hypothesis.getObjectId());
                    instance.getCurrentUser().saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Intent intent = new Intent(CreateHypothesisActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                Crouton.makeText(CreateHypothesisActivity.this, e.getMessage(), Style.ALERT).show();
                            }
                        }
                    });

                } else {
                    Crouton.makeText(CreateHypothesisActivity.this, e.getMessage(), Style.ALERT).show();
                }
            }
        });
    }

    @Override
    public ParseObject getSelectedCategory() {
        return category;
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
        if(instance.getCurrentUser() == null) {
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
            case R.id.action_log_out:
                instance.logoutCurrentUser();
                startActivity(new Intent(CreateHypothesisActivity.this, MainActivity.class));
                Log.d("actionbar", "logout clicked");
                return true;
            case R.id.action_log_in:
                final Intent signInActivity = new Intent(CreateHypothesisActivity.this, SignInActivity.class);
                startActivity(signInActivity);
                return true;
            case android.R.id.home:
                final Intent mainActivity = new Intent(CreateHypothesisActivity.this, MainActivity.class);
                startActivity(mainActivity);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class QuestionOverviewFragment extends Fragment implements AdapterView.OnItemSelectedListener {
        private EditText tryThis;
        private EditText toAccomplish;
        private Spinner categorySelector;
        private int selectedCategory;
        private EditText description;
        // stores user entered questions and the possible answers for each

        OnAddQuestionListener mListener;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            try {
                mListener = (OnAddQuestionListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.question_overview_fragment, container, false);

            tryThis = (EditText) rootView.findViewById(R.id.try_this_text);
            toAccomplish = (EditText) rootView.findViewById(R.id.to_accomplish_text);
            description = (EditText) rootView.findViewById(R.id.description_text);

            categorySelector = (Spinner) rootView.findViewById(R.id.categories_spinner);
            populateCategorySpinner(categorySelector);
            categorySelector.setOnItemSelectedListener(this);

            // inflate hypothesis preview layout
            LinearLayout previewLayout = (LinearLayout) rootView.findViewById(R.id.hypothesis_preview);
            View hypothesisPreview = getActivity().getLayoutInflater().inflate(R.layout.hypothesis_row, previewLayout, false);
            hypothesisPreview.setBackgroundColor(getResources().getColor(R.color.adapt_zebra_list_grey));

            // set fake number in the rating and users joined boxes
            ((TextView) hypothesisPreview.findViewById(R.id.usersCount)).setText(HypothesisAdapter.formatJoinedNumber(9999));
            HypothesisAdapter.formatRatingNumber((TextView) hypothesisPreview.findViewById(R.id.rating), 4.2);

            // set listeners to update the preview every time the user changes the input
            final TextView hypothesisTextView = (TextView) hypothesisPreview.findViewById(R.id.hypothesis_text);

            hypothesisTextView.setText(HypothesisAdapter.formatHypothesisText("To accomplish this", "Try this"));

            tryThis.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String toAccomplishText = toAccomplish.getText().toString().trim();
                    String tryThisText = tryThis.getText().toString().trim();

                    if (toAccomplishText.isEmpty()) {
                        toAccomplishText = "To accomplish this";
                    }
                    if (tryThisText.isEmpty()) {
                        tryThisText = "Try this";
                    }

                    hypothesisTextView.setText(HypothesisAdapter.formatHypothesisText(toAccomplishText, tryThisText));
                }
            });

            toAccomplish.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String toAccomplishText = toAccomplish.getText().toString().trim();
                    String tryThisText = tryThis.getText().toString().trim();

                    if (toAccomplishText.isEmpty()) {
                        toAccomplishText = "To accomplish this";
                    }
                    if (tryThisText.isEmpty()) {
                        tryThisText = "Try this";
                    }

                    hypothesisTextView.setText(HypothesisAdapter.formatHypothesisText(toAccomplishText, tryThisText));                }
            });

            // add the preview to the layout
            previewLayout.addView(hypothesisPreview);

            Button nextButton = (Button) rootView.findViewById(R.id.next_button);
            nextButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    startQuestionCreation();
                }
            });
            return rootView;
        }

        // adds the possible hypothesis categories to the spinner from the hypothesisrepo
        private void populateCategorySpinner(Spinner categorySpinner) {
            // create list of all categories
            List<String> categoryTitles = new ArrayList<String>();
            categoryTitles.add("Select Category"); // hint to user
            if (hypothesisRepo.categoryList != null) {
                for (ParseObject categoryObject : hypothesisRepo.categoryList) {
                    categoryTitles.add(categoryObject.getString("categoryName"));
                }
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item, categoryTitles);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(dataAdapter);
            categorySpinner.setSelection(0); // prompt is shown by default
        }

        // stores the selected hypothesis from the spinner in a field
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            selectedCategory = pos;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

        private void startQuestionCreation() {
            String tryText = tryThis.getText().toString().trim();
            String accomplishText = toAccomplish.getText().toString().trim();
            String descriptionText = description.getText().toString().trim();

            boolean validationError = false;
            StringBuilder validationErrorMessage = new StringBuilder(getString(R.string.error_intro));
            if (tryText.isEmpty()) {
                validationError = true;
                validationErrorMessage.append(getString(R.string.create_error_try_empty));
            }
            if (accomplishText.isEmpty()) {
                if (validationError) {
                    validationErrorMessage.append(" and ");
                }
                validationError = true;
                validationErrorMessage.append(getString(R.string.create_error_accomplish_empty));
            }
            if (selectedCategory == 0) {
                if (validationError) {
                    validationErrorMessage.append(" and ");
                }
                validationError = true;
                validationErrorMessage.append(getString(R.string.create_error_category_empty));
            }
            if (descriptionText.isEmpty()) {
                if (validationError) {
                    validationErrorMessage.append(" and ");
                }
                validationError = true;
                validationErrorMessage.append(getString(R.string.create_error_description_empty));
            }

            // If there is a validation error, display the error
            if (validationError) {
                validationErrorMessage.append(getString(R.string.error_end));
                Crouton.makeText(getActivity(), validationErrorMessage.toString(), Style.ALERT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("ifDescription", tryText);
            bundle.putString("thenDescription", accomplishText);
            bundle.putString("description", descriptionText);
            bundle.putString("parentCategory", hypothesisRepo.categoryList.get(selectedCategory - 1).getObjectId());

            mListener.startQuestionCreation(tryText, accomplishText, descriptionText, hypothesisRepo.categoryList.get(selectedCategory - 1));
        }
    }

    // The questions fragment maintains a space for the user to add questions
    // to each hypothesis and to see and control what previous questions have
    // been added.
    public static class QuestionsFragment extends Fragment {

        private OnAddQuestionListener mListener;

        private LinearLayout questionList; // where entered questions are shown
        private Button addButton; // click to add a question
        private Button doneButton; // click to submit a question

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            try {
                mListener = (OnAddQuestionListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.add_questions_fragment, container, false);

            ParseObject category = mListener.getSelectedCategory();
            String categoryName = category.getString("categoryName").toLowerCase();

            TextView beforeText = (TextView) rootView.findViewById(R.id.before_question_text);
            TextView afterText = (TextView) rootView.findViewById(R.id.after_question_text);

            String beforeString;
            String afterString;

            if (categoryName.equals("sleep")) {
                beforeString = getResources().getString(R.string.sleep_before_question);
                afterString = getResources().getString(R.string.sleep_after_question);
            } else if (categoryName.equals("focus")) {
                beforeString = getResources().getString(R.string.focus_before_question);
                afterString = getResources().getString(R.string.focus_after_question);
            } else {
                beforeString = getResources().getString(R.string.nutrition_before_question);
                afterString = getResources().getString(R.string.nutrition_after_question);
            }

            beforeText.setText(beforeString);
            afterText.setText(afterString);
            mListener.onAddQuestion(beforeString, 0);
            mListener.onAddQuestion(afterString, 1);

            questionList = (LinearLayout) rootView.findViewById(R.id.question_list);

            addButton = (Button) rootView.findViewById(R.id.add_question_button);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addButton.setVisibility(View.GONE);
                    AddQuestionFragment addQuestion = new AddQuestionFragment();
                    getFragmentManager().beginTransaction().replace(R.id.question_container, addQuestion).commit();

                }
            });

            doneButton = (Button) rootView.findViewById(R.id.done_creating_button);
            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishQuestionCreation();
                }
            });

            return rootView;
        }

        // adds the given text to be displayed as a question the user added
        public void addQuestionToDisplay(String text) {
            TextView tv = new TextView(getActivity());
            tv.setText(text);
            tv.setTextSize(25);
            tv.setTextColor(Color.parseColor("#ff0000"));
            questionList.addView(tv);
            addButton.setVisibility(View.VISIBLE);
        }

        public void finishQuestionCreation() {
            mListener.finishQuestionCreation();
        }
    }

    // The add question fragment allows the user to create one question and
    // all of its corresponding answers to an hypothesis. It then communicates
    // to the parent activity (which must implement the OnAddQuestionListener interface)
    // that the user has added a question
    public static class AddQuestionFragment extends Fragment {

        private OnAddQuestionListener mListener;

        private EditText questionText;
        private List<EditText> optionsList;
        private LinearLayout questionList;
        private int optionCounter = 3;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            try {
                mListener = (OnAddQuestionListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            // Set layout to category fragment
            View rootView = inflater.inflate(R.layout.hypothesis_question_creation_fragment, container, false);
            // Grab category buttons from layout

            questionText = (EditText) rootView.findViewById(R.id.question_text);

            optionsList = new ArrayList<>();
            optionsList.add((EditText) rootView.findViewById(R.id.option1));
            optionsList.add((EditText) rootView.findViewById(R.id.option2));

            optionCounter = 3;

            questionList = (LinearLayout) rootView.findViewById(R.id.options_list);

            Button addOptions = (Button) rootView.findViewById(R.id.add_option_button);
            addOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText temp = new EditText(getActivity());
                    temp.setHint("option " + optionCounter);
                    optionsList.add(temp);

                    questionList.addView(temp);

                    optionCounter++;
                }
            });

            (rootView.findViewById(R.id.complete_question_button)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addQuestion();
                }
            });

            return rootView;
        }

        public void addQuestion() {
            String questionString = questionText.getText().toString().trim();

            int enteredQuestionsCount = 0;
            List<String> options = new ArrayList<>();
            for (EditText et : optionsList) {
                String optionText = et.getText().toString().trim();
                if (!optionText.isEmpty()) {
                    enteredQuestionsCount++;
                    options.add(optionText);
                }
            }

            boolean validationError = false;
            StringBuilder validationErrorMessage = new StringBuilder(getString(R.string.error_intro));

            if (questionString.isEmpty()) {
                validationError = true;
                validationErrorMessage.append("enter a question text");
            }
            if (enteredQuestionsCount < 2) {
                if (validationError) {
                    validationErrorMessage.append(" and ");
                }
                validationError = true;
                validationErrorMessage.append("enter at least 2 options");
            }

            validationErrorMessage.append(getString(R.string.error_end));

            // If there is a validation error, display the error
            if (validationError) {
                Crouton.makeText(getActivity(), validationErrorMessage.toString(), Style.ALERT).show();
                return;
            }

            mListener.onAddQuestion(questionString, options);

            getActivity().getFragmentManager().beginTransaction().remove(this).commit();
        }
    }
}

