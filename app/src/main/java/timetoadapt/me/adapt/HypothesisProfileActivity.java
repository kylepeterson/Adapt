package timetoadapt.me.adapt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by ravnon on 5/1/15.
 */
public class HypothesisProfileActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hypothesis_profile);

        Intent intent = getIntent();
        HypothesisListItem hypothesisData = (HypothesisListItem) intent.getParcelableExtra("hypothesisData");

        TextView tryThis = (TextView) findViewById(R.id.hypothesis_try_this);
        TextView toAccomplish = (TextView) findViewById(R.id.hypothesis_to_accomplish);
        TextView description = (TextView) findViewById(R.id.hypothesis_description);

        tryThis.setText(hypothesisData.tryThis);
        toAccomplish.setText(hypothesisData.toAccomplish);
        description.setText(hypothesisData.description);


    }
}
