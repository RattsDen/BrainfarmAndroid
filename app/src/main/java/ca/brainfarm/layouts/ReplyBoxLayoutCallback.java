package ca.brainfarm.layouts;

/**
 * Created by Eric Thompson on 2017-09-23.
 */

public interface ReplyBoxLayoutCallback {

    void cancelReplyPressed(ReplyBoxLayout replyBoxLayout);

    void submitReplyPressed(ReplyBoxLayout replyBoxLayout);

    void submitEditReplyPressed(ReplyBoxLayout replyBoxLayout);

    void chooseFilePressed(ReplyBoxLayout replyBoxLayout);

}
