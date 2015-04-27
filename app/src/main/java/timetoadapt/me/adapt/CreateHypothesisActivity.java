package timetoadapt.me.adapt;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ravnon on 4/16/15.
 */
public class CreateHypothesisActivity extends Activity implements AdapterView.OnItemSelectedListener, OnAddQuestionListener {

    protected static HypothesisRepo hypothesisRepo;

    private EditText tryThis;
    private EditText toAccomplish;
    private Spinner categorySelector;
    private int selectedCategory;
    private EditText description;

    private QuestionsFragment questionsFragment;

    private Map<String, List<String>> hypothesisQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hypothesis);

        AdaptApp app = (AdaptApp) getApplication();
        AdaptApp instance = app.getInstance();
        hypothesisRepo = instance.hypothesisRepo;

        hypothesisQuestions = new HashMap<String, List<String>>();

        ParseObject analObject = new ParseObject("Analytics");
        analObject.put("action", "create_hypothesis");
        analObject.saveInBackground();

        tryThis = (EditText) findViewById(R.id.try_this_text);
        toAccomplish = (EditText) findViewById(R.id.to_accomplish_text);
        description = (EditText) findViewById(R.id.description_text);

        categorySelector = (Spinner) findViewById(R.id.categories_spinner);
        populateCategorySpinner(categorySelector);
        categorySelector.setOnItemSelectedListener(this);

        questionsFragment = new QuestionsFragment();
        //questions.setArguments(getIntent().getExtras());
        // Inflate categories overview fragment
        getFragmentManager().beginTransaction().replace(R.id.questions_container, questionsFragment).commit();

        Button nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startHypothesisCreation();
            }
        });
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        selectedCategory = pos;
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    private void populateCategorySpinner(Spinner categorySpinner) {
        List<String> categoryTitles = new ArrayList<String>();
        categoryTitles.add("Select Category");
        if (hypothesisRepo.categoryList != null) {
            for (ParseObject categoryObject : hypothesisRepo.categoryList) {
                categoryTitles.add(categoryObject.getString("categoryName"));
            }
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categoryTitles);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(dataAdapter);
        categorySpinner.setSelection(0); // prompt is shown by default
    }

    private void startHypothesisCreation() {
        String tryText = tryThis.getText().toString().trim();
        String accomplishText = toAccomplish.getText().toString().trim();
        String descriptionText = description.getText().toString().trim();
        String selectedCategoryText = categorySelector.getItemAtPosition(selectedCategory).toString().trim();

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
        if (hypothesisQuestions.isEmpty()) {
            if (validationError) {
                validationErrorMessage.append(" and ");
            }
            validationError = true;
            validationErrorMessage.append("you must include at least one question");
        }

        validationErrorMessage.append(getString(R.string.error_end));

        // If there is a validation error, display the error
        if (validationError) {
            Toast.makeText(CreateHypothesisActivity.this, validationErrorMessage.toString(), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        ParseObject hypothesis = new ParseObject("Hypothesis");

        hypothesis.put("ifDescription", tryText);
        hypothesis.put("thenDescription", accomplishText);
        hypothesis.put("description", descriptionText);
        hypothesis.put("categoryName", selectedCategoryText);

        hypothesis.saveInBackground();

        for (String questionText : hypothesisQuestions.keySet()) {
            List<String> options = hypothesisQuestions.get(questionText);

            ParseObject question = new ParseObject("Question");

            question.put("questionType", 1);
            question.put("questionText", questionText);
            question.put("hypothesis", hypothesis);

            question.addAllUnique("questionOptions", options);

            question.saveInBackground();
        }
    }

    @Override
    public void onAddQuestion(String text, List<String> options) {
        hypothesisQuestions.put(text, options);

        questionsFragment.addQuestionToDisplay(text);
    }

    public static class QuestionsFragment extends Fragment {

        LinearLayout questionList;

        public QuestionsFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            // Set layout to category fragment
            View rootView = inflater.inflate(R.layout.add_questions_fragment, container, false);
            // Grab category buttons from layout

            questionList = (LinearLayout) rootView.findViewById(R.id.question_list);

            Button addButton = (Button) rootView.findViewById(R.id.add_question_button);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddQuestionFragment addQuestion = new AddQuestionFragment();
                    //questions.setArguments(getIntent().getExtras());
                    // Inflate categories overview fragment
                    getFragmentManager().beginTransaction().replace(R.id.question_container, addQuestion).commit();

                }
            });

            return rootView;
        }

        public void addQuestionToDisplay(String text) {
            TextView tv = new TextView(getActivity());

            tv.setText(text);

            questionList.addView(tv);
        }
    }

    public static class AddQuestionFragment extends Fragment {

        OnAddQuestionListener mListener;

        EditText questionText;
        List<EditText> optionsList;
        LinearLayout questionList;
        int optionCounter = 3;

        public AddQuestionFragment() {

        }

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

            optionsList = new ArrayList<EditText>();
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
                    optionCounter++;
                    //temp.setLayoutParams(new AbsoluteLayout.LayoutParams(WRAP_CONTENT,WRAP_CONTENT));
                    optionsList.add(temp);

                    questionList.addView(temp);
                }
            });

            ((Button) rootView.findViewById(R.id.complete_question_button)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addQuestion();
                }
            });

            return rootView;
        }

        public void addQuestion() {
            String text = questionText.getText().toString().trim();

            List<String> options = new ArrayList<String>();
            for (EditText et : optionsList) {
                options.add(et.getText().toString().trim());
            }

            mListener.onAddQuestion(text, options);

            getActivity().getFragmentManager().beginTransaction().remove(this).commit();
        }

    }
}

