package timetoadapt.me.adapt;

import com.parse.ParseObject;

import java.util.List;

public interface OnAddQuestionListener {
    public void onAddQuestion(String text, List<String> options);
    public void onAddQuestion(String text, int timeToAsk);
    public void startQuestionCreation(String tryThis, String toAccomplish, String description, ParseObject category);
    public void finishQuestionCreation();

    public ParseObject getSelectedCategory();
}
