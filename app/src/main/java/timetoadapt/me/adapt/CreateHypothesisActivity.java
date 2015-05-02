package timetoadapt.me.adapt;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ravnon on 4/16/15.
 */
public class CreateHypothesisActivity extends Activity implements AdapterView.OnItemSelectedListener, OnAddQuestionListener {
    private EditText tryThis;
    private EditText toAccomplish;
    private Spinner categorySelector;
    private int selectedCategory;
    private EditText description;
    protected static HypothesisRepo hypothesisRepo;


    // fragment that allows user to add self report questions for hypothesis
    private QuestionsFragment questionsFragment;

    // stores user entered questions and the possible answers for each
    private HashMap<String, List<String>> hypothesisQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hypothesis);

        AdaptApp app = (AdaptApp) getApplication();
        AdaptApp instance = app.getInstance();
        hypothesisRepo = instance.hypothesisRepo;

        hypothesisQuestions = new HashMap<>();

        ParseObject analObject = new ParseObject("Analytics");
        analObject.put("action", "create_hypothesis");
        analObject.saveInBackground();

        // inflate question fragment
        QuestionOverviewFragment overviewFragment = new QuestionOverviewFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable("hypothesisQuestions", hypothesisQuestions);
        overviewFragment.setArguments(arguments);
        getFragmentManager().beginTransaction().replace(R.id.creation_container, overviewFragment).commit();

    }

    // stores the selected hypothesis from the spinner in a field
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        selectedCategory = pos;
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    // interface for child fragments to report a question has been added by the user
    // assumes the text and options have been checked for validity
    public void onAddQuestion(String text, List<String> options) {
        hypothesisQuestions.put(text, options); // add to list of questions
        questionsFragment.addQuestionToDisplay(text); // display on screen
    }




    // get user information from all fields and attempts to create a full hypothesis
    // alerts the user if something is missing/wrong from one of the fields
    private void startHypothesisCreation() {
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
        if (hypothesisQuestions.isEmpty()) {
            if (validationError) {
                validationErrorMessage.append(" and ");
            }
            validationError = true;
            validationErrorMessage.append("include at least one question");
        }

        validationErrorMessage.append(getString(R.string.error_end));

        // If there is a validation error, display the error
        if (validationError) {
            Toast.makeText(CreateHypothesisActivity.this, validationErrorMessage.toString(), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(CreateHypothesisActivity.this);
        dialog.setMessage("Creating Hypothesis...");
        dialog.show();

        ParseObject hypothesis = new ParseObject("Hypothesis");

        hypothesis.put("ifDescription", tryText);
        hypothesis.put("thenDescription", accomplishText);
        hypothesis.put("description", descriptionText);
        // 0 category is not a real one (part of spinner)
        hypothesis.put("parentCategory", hypothesisRepo.categoryList.get(selectedCategory - 1));

        // submit each user entered question to database
        for (String questionText : hypothesisQuestions.keySet()) {
            List<String> options = hypothesisQuestions.get(questionText);

            ParseObject question = new ParseObject("Question");

            question.put("questionType", 1);
            question.put("questionText", questionText);
            question.put("hypothesis", hypothesis);

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
                Intent intent = new Intent(CreateHypothesisActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(CreateHypothesisActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            }
        });
    }



    // The questions fragment maintains a space for the user to add questions
    // to each hypothesis and to see and control what previous questions have
    // been added.
    public static class QuestionsFragment extends Fragment {

        private LinearLayout questionList; // where entered questions are shown
        private Button addButton; // click to add a question

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            // Set layout to category fragment
            View rootView = inflater.inflate(R.layout.add_questions_fragment, container, false);
            // Grab category buttons from layout

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
                Toast.makeText(getActivity(), validationErrorMessage.toString(), Toast.LENGTH_LONG)
                        .show();
                return;
            }

            mListener.onAddQuestion(questionString, options);

            getActivity().getFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    public static class QuestionOverviewFragment extends Fragment implements AdapterView.OnItemSelectedListener, OnAddQuestionListener {
        private EditText tryThis;
        private EditText toAccomplish;
        private Spinner categorySelector;
        private int selectedCategory;
        private EditText description;
        private QuestionsFragment questionsFragment;
        // stores user entered questions and the possible answers for each
        private HashMap<String, List<String>> hypothesisQuestions;

        public QuestionOverviewFragment() {

        }

        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.question_overview_fragment, container, false);
            tryThis = (EditText) rootView.findViewById(R.id.try_this_text);
            toAccomplish = (EditText) rootView.findViewById(R.id.to_accomplish_text);
            description = (EditText) rootView.findViewById(R.id.description_text);

            Bundle args = getArguments();
            hypothesisQuestions = (HashMap<String, List<String>>) args.getSerializable("hypothesisQuestions");
            categorySelector = (Spinner) rootView.findViewById(R.id.categories_spinner);
            populateCategorySpinner(categorySelector);
            categorySelector.setOnItemSelectedListener(this);

            // inflate question add fragment
            Button addQuestion = (Button) rootView.findViewById(R.id.add_question_button_1);
            addQuestion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    questionsFragment = new QuestionsFragment();
                    getFragmentManager().beginTransaction().replace(R.id.creation_container, questionsFragment).commit();
                }
            });

            Button nextButton = (Button) rootView.findViewById(R.id.next_button);
            nextButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    startHypothesisCreation();
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

        @Override
        // interface for child fragments to report a question has been added by the user
        // assumes the text and options have been checked for validity
        public void onAddQuestion(String text, List<String> options) {
            hypothesisQuestions.put(text, options); // add to list of questions
            questionsFragment.addQuestionToDisplay(text); // display on screen
        }

        private void startHypothesisCreation() {
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
            if (hypothesisQuestions.isEmpty()) {
                if (validationError) {
                    validationErrorMessage.append(" and ");
                }
                validationError = true;
                validationErrorMessage.append("include at least one question");
            }

            validationErrorMessage.append(getString(R.string.error_end));

            // If there is a validation error, display the error
            if (validationError) {
                Toast.makeText(getActivity(), validationErrorMessage.toString(), Toast.LENGTH_LONG)
                        .show();
                return;
            }

            final ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Creating Hypothesis...");
            dialog.show();

            ParseObject hypothesis = new ParseObject("Hypothesis");

            hypothesis.put("ifDescription", tryText);
            hypothesis.put("thenDescription", accomplishText);
            hypothesis.put("description", descriptionText);
            // 0 category is not a real one (part of spinner)
            hypothesis.put("parentCategory", hypothesisRepo.categoryList.get(selectedCategory - 1));

            // submit each user entered question to database
            for (String questionText : hypothesisQuestions.keySet()) {
                List<String> options = hypothesisQuestions.get(questionText);

                ParseObject question = new ParseObject("Question");

                question.put("questionType", 1);
                question.put("questionText", questionText);
                question.put("hypothesis", hypothesis);

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
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}

