package timetoadapt.me.adapt;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

/**
 * Created by kylepeterson on 4/24/15.
 * This is the object used to populate the hypothesis list page
 */
public class HypothesisListItem implements Parcelable {
    public String tryThis;
    public String toAccomplish;
    public String description;
    public String categoryID;
    public String categoryName;
    public int usersJoined;
    public double rating;
    public String objectID;

    public HypothesisListItem(String tryThis, String toAccomplish, int usersJoined, double rating, String description, String objectID, String categoryID, String categoryName) {
        this.tryThis = tryThis;
        this.toAccomplish = toAccomplish;
        this.usersJoined = usersJoined;
        this.rating = rating;
        this.description = description;
        this.objectID = objectID;
        this.categoryID = categoryID;
        this.categoryName = categoryName;
    }

    // Creates a listItem out of a parseObject
    public HypothesisListItem(ParseObject parseObject) {
        parseObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                tryThis = object.getString("ifDescription");
                toAccomplish = object.getString("thenDescription");
                usersJoined = object.getInt("usersJoined");
                rating = object.getDouble("rating");
                description = object.getString("description");
                categoryID = object.getParseObject("parentCategory").getObjectId();
                categoryName = object.getParseObject("parentCategory").getString("categoryName");
                objectID = object.getObjectId();            }
        });
//        this.tryThis = parseObject.getString("ifDescription");
//        this.toAccomplish = parseObject.getString("thenDescription");
//        this.usersJoined = parseObject.getInt("usersJoined");
//        this.rating = parseObject.getDouble("rating");
//        this.description = parseObject.getString("description");
//        this.categoryID = parseObject.getParseObject("parentCategory").getObjectId();
//        this.categoryName = parseObject.getParseObject("parentCategory").getString("categoryName");
//        this.objectID = parseObject.getObjectId();
    }

    public HypothesisListItem(Parcel in) {
        this.tryThis = in.readString();
        this.toAccomplish = in.readString();
        this.usersJoined = in.readInt();
        this.rating = in.readDouble();
        this.description = in.readString();
        this.categoryID = in.readString();
        this.categoryName = in.readString();
        this.objectID = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tryThis);
        dest.writeString(toAccomplish);
        dest.writeInt(usersJoined);
        dest.writeDouble(rating);
        dest.writeString(description);
        dest.writeString(categoryID);
        dest.writeString(categoryName);
        dest.writeString(objectID);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<HypothesisListItem> CREATOR = new Parcelable.Creator<HypothesisListItem>() {
        public HypothesisListItem createFromParcel(Parcel in) {
            return new HypothesisListItem(in);
        }

        public HypothesisListItem[] newArray(int size) {
            return new HypothesisListItem[size];
        }
    };
}
