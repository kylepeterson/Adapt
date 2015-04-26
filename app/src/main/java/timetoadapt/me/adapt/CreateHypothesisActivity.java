package timetoadapt.me.adapt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ravnon on 4/16/15.
 */
public class CreateHypothesisActivity extends Activity implements AdapterView.OnItemSelectedListener {

    protected static HypothesisRepo hypothesisRepo;

    private EditText tryThis;
    private EditText toAccomplish;
    private Spinner categorySelector;
    private int selectedCategory;
    private EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hypothesis);

        AdaptApp app = (AdaptApp) getApplication();
        AdaptApp instance = app.getInstance();
        hypothesisRepo = instance.hypothesisRepo;

        ParseObject analObject = new ParseObject("Analytics");
        analObject.put("action", "create_hypothesis");
        analObject.saveInBackground();

        tryThis = (EditText) findViewById(R.id.try_this_text);
        toAccomplish = (EditText) findViewById(R.id.to_accomplish_text);
        description = (EditText) findViewById(R.id.description_text);

        categorySelector = (Spinner) findViewById(R.id.categories_spinner);
        populateCategorySpinner(categorySelector);
        categorySelector.setOnItemSelectedListener(this);

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

        validationErrorMessage.append(getString(R.string.error_end));

        // If there is a validation error, display the error
        if (validationError) {
            Toast.makeText(CreateHypothesisActivity.this, validationErrorMessage.toString(), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        Intent questionIntent = new Intent(this, MainActivity.class);
        Bundle hypothesisInfo = new Bundle();
        hypothesisInfo.putString("try", tryText);
        hypothesisInfo.putString("accomplish", accomplishText);
        hypothesisInfo.putString("category", categorySelector.getItemAtPosition(selectedCategory).toString());
        hypothesisInfo.putString("description", descriptionText);
        questionIntent.putExtras(hypothesisInfo);

        //startActivity(questionIntent);

    }
}
