package timetoadapt.me.adapt;

import com.parse.ParseObject;

/**
 * Created by kylepeterson on 4/24/15.
 * This is the object used to populate the hypothesis list page
 */
public class HypothesisListItem {
    public String tryThis;
    public String toAccomplish;
    public int usersJoined;
    public double rating;

    public HypothesisListItem(String tryThis, String toAccomplish, int usersJoined, double rating) {
        this.tryThis = tryThis;
        this.toAccomplish = toAccomplish;
        this.usersJoined = usersJoined;
        this.rating = rating;
    }

    public HypothesisListItem() {
        this("", "", 0, 0.0);
    }

    // Creates a listItem out of a parseObject
    public HypothesisListItem(ParseObject parseObject) {
        this.tryThis = parseObject.getString("ifDescription");
        this.toAccomplish = parseObject.getString("thenDescription");
        this.usersJoined = parseObject.getInt("usersJoined");
        this.rating = parseObject.getDouble("rating");
    }

}
