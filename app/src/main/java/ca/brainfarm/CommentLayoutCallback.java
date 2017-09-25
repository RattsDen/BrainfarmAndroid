package ca.brainfarm;

/**
 * This interface is used to define a method that should be called when a button
 * (such as reply or bookmark) is pressed on a comment view.
 *
 * ProjectActivity implements this interface
 */

public interface CommentLayoutCallback {

    void replyPressed(CommentLayout commentView);

    void bookmarkPressed(CommentLayout commentView);

}
