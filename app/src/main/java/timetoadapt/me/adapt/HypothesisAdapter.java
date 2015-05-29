package timetoadapt.me.adapt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.joanzapata.android.iconify.Iconify;

import java.util.List;

/**
 * Created by kylepeterson on 4/24/15.
 *
 */
public class HypothesisAdapter extends ArrayAdapter<HypothesisListItem> {
    protected Context context;
    protected int layoutResourceId;
    protected List<HypothesisListItem> data;

    // Creates a new adapter used to populate the hypothesis list
    public HypothesisAdapter(Context context, int layoutResourceId, List<HypothesisListItem> data) {
        super(context, layoutResourceId, data);

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

        HypothesisListItem listItem = data.get(position);
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
                public void onClick(View v) {
                    Intent reportDataActivity = new Intent(v.getContext(), AskQuestionActivity.class);
                    // Add any extras here for data that needs to be passed to the ListActivity
                    reportDataActivity.putExtra("hypothesisID", data.get(position).objectID);
                    reportDataActivity.putExtra("hypothesisCategory", data.get(position).category);
                    reportDataActivity.putExtra("timeToAsk", 1);
                    v.getContext().startActivity(reportDataActivity);
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
