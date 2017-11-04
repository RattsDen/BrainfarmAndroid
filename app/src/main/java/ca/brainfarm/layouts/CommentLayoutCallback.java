package ca.brainfarm.layouts;

import android.widget.PopupMenu;

/**
 * This interface is used to define a method that should be called when a button
 * (such as reply or bookmark) is pressed on a comment view.
 *
 * ProjectActivity implements this interface
 */

public interface CommentLayoutCallback {

    void replyPressed(CommentLayout commentView);

    void editPressed(CommentLayout commentView);

    void deletePressed(CommentLayout commentView);

    void bookmarkPressed(CommentLayout commentView);

    void synthesizePressed(CommentLayout commentView);

    void createCommentOptionsPopupMenu(PopupMenu popup, CommentLayout commentView);

}
