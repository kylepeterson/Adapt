package timetoadapt.me.adapt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.android.iconify.Iconify;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by kylepeterson on 4/24/15.
 *
 */
public class HypothesisAdapter extends ArrayAdapter<HypothesisListItem> {
    private AdaptApp instance;
    protected Context context;
    protected int layoutResourceId;
    protected List<HypothesisListItem> data;

    // Creates a new adapter used to populate the hypothesis list
    public HypothesisAdapter(Context context, int layoutResourceId, List<HypothesisListItem> data) {
        super(context, layoutResourceId, data);

        instance = AdaptApp.getInstance();

        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        HypothesisHolder holder = null;

        if (row == null) {
            // Get Empty layout fragment
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new HypothesisHolder();
            holder.hypothesisText = (TextView) row.findViewById(R.id.hypothesis_text);
            holder.usersJoinedView = (TextView) row.findViewById(R.id.usersCount);
            holder.ratingView = (TextView) row.findViewById(R.id.rating);

            row.setTag(holder);
        } else {
            // If row already exists dont inflate a new one
            holder = (HypothesisHolder) row.getTag();
        }

        final HypothesisListItem listItem = data.get(position);
        // Set items to current rows contents

        // create formatted hypothesis string
        String toAccomplish = listItem.toAccomplish.trim();
        String tryThis = listItem.tryThis.trim();

        holder.hypothesisText.setText(formatHypothesisText(toAccomplish, tryThis));
        holder.usersJoinedView.setText(formatJoinedNumber(listItem.usersJoined));
        formatRatingNumber(holder.ratingView, listItem.rating);

        // set zebra stripes of rows
        if (position % 2 == 0) {
            row.setBackgroundColor(context.getResources().getColor(R.color.adapt_white));
        } else {
            row.setBackgroundColor(context.getResources().getColor(R.color.adapt_zebra_list_grey));
        }
        Button report = (Button) row.findViewById(R.id.report_data);
        if (report != null) { // this is a repoting row
            report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Log.d("SelfReport", "clicked on self report button");
                    ParseQuery<ParseObject> innerQuestionQuery = ParseQuery.getQuery("Question");
                    innerQuestionQuery.whereEqualTo("hypothesis", ParseObject.createWithoutData("Hypothesis", listItem.objectID));
                    innerQuestionQuery.whereEqualTo("timeToAsk", 1);
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Answer");
                    query.whereMatchesQuery("question", innerQuestionQuery);
                    query.whereEqualTo("user", instance.getCurrentUser());
                    query.orderByDescending("submittedAt");
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> list, ParseException e) {
                            Log.d("SelfReport", "found " + list.size() + " answers");
                            if (e != null) {
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                boolean canReport = true;
                                if (!list.isEmpty()) {
                                    Date d = list.get(0).getCreatedAt(); //.getDate("submittedAt");
                                    Log.d("SelfReport", "Last answer = " + d);
                                    Calendar rightNow = Calendar.getInstance();
                                    // set the calendar to start of today
                                    rightNow.set(Calendar.HOUR, 0);
                                    rightNow.set(Calendar.MINUTE, 0);
                                    rightNow.set(Calendar.SECOND, 0);

                                    Date today = rightNow.getTime();
                                    Log.d("SelfReport", "today = " + today);

                                    if (d.after(today)) {
                                        canReport = false;
                                        Log.d("SelfReport", "last answer is after today");
                                        Toast.makeText(getContext(), "You can only report data once a day", Toast.LENGTH_LONG).show();
                                    }
                                }
                                if (canReport) {
                                    Log.d("SelfReport", "let them report");
                                    Intent reportDataActivity = new Intent(v.getContext(), AskQuestionActivity.class);
                                    // Add any extras here for data that needs to be passed to the ListActivity
                                    reportDataActivity.putExtra("hypothesisData", listItem);
                                    // if they have reported more than 5 times we can ask them to rate the hypothesis
                                    //int timeToAsk = list.size() < 5 ? 1 : 2;
                                    reportDataActivity.putExtra("timeToAsk", 1);
                                    reportDataActivity.putExtra("timesAnswered", list.size());
                                    v.getContext().startActivity(reportDataActivity);
                                }

                            }
                        }
                    });
                }
            });
        }

        return row;
    }

    // returns a string that formats the given "toAccomplish" in bold and adds a colon
    // between the two strings
    public static SpannableString formatHypothesisText(String toAccomplish, String tryThis) {
        SpannableString spanString = new SpannableString(toAccomplish + ": " + tryThis);
        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, toAccomplish.length() + 1, 0);

        return spanString;
    }

    // returns a string that puts a comma between every 3 digits of the number
    // 6969 -> "6,969 joined"
    public static String formatJoinedNumber(int num) {
        String result = "";

        int count = 0;
        do {
            if (count == 3) {
                result = "," + result;
                count = 0;
            }

            int lastDigit = num % 10;
            result = lastDigit + result;
            count++;

            num = num / 10;
        } while (num > 0);

        return result + " joined";
    }

    // formats the rating number to add a star icon to its right in the given TextView
    public static void formatRatingNumber(TextView view, double rating) {
        view.setText(rating + " " + Iconify.IconValue.fa_star.formattedName());
        Iconify.addIcons(view);
    }

    public HypothesisListItem getItemAtPosition(int position) {
        return data.get(position);
    }

    // Holder used so that we dont have to call by findViewById for every new row
    static class HypothesisHolder {
        TextView hypothesisText;
        TextView usersJoinedView;
        TextView ratingView;
    }
}
