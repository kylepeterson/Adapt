package timetoadapt.me.adapt;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kylepeterson on 4/16/15.
 */
public class HypothesisRepo {
    public List<ParseObject> categoryList;

    public HypothesisRepo(List<ParseObject> categoryList) {
        this.categoryList = categoryList;
    }

    public HypothesisRepo() {
        this(null);
    }

    // sets the category list to the given list of parse objects
    public void setCategoryList(List<ParseObject> categoryList) {
        this.categoryList = new ArrayList<ParseObject>(categoryList);
    }
}
