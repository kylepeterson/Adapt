package timetoadapt.me.adapt;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        HypothesisHolder holder = null;

        if (row == null) {
            // Get Empty layout fragment
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new HypothesisHolder();
            holder.tryThisView = (TextView) row.findViewById(R.id.tryThis);
            holder.toAccomplishView = (TextView) row.findViewById(R.id.goal);
            holder.usersJoinedView = (TextView) row.findViewById(R.id.usersCount);
            holder.ratingView = (TextView) row.findViewById(R.id.rating);

            row.setTag(holder);
        } else {
            // If row already exists dont inflate a new one
            holder = (HypothesisHolder) row.getTag();
        }

        HypothesisListItem listItem = data.get(position);
        // Set items to current rows contents
        holder.tryThisView.setText(listItem.tryThis);
        holder.toAccomplishView.setText(listItem.toAccomplish);
        holder.usersJoinedView.setText(listItem.usersJoined + "");
        holder.ratingView.setText(listItem.rating + "");

        return row;
    }

    public HypothesisListItem getItemAtPosition(int position) {
        return data.get(position);
    }

    // Holder used so that we dont have to call by findViewById for every new row
    static class HypothesisHolder {
        TextView tryThisView;
        TextView toAccomplishView;
        TextView usersJoinedView;
        TextView ratingView;
    }
}
