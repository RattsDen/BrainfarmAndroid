package ca.brainfarm.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Eric Thompson on 2017-09-23.
 */

public class SynthesisRequest {

    @SerializedName("LinkedCommentID")
    public int linkedCommentID;
    @SerializedName("Subject")
    public String subject;

}
