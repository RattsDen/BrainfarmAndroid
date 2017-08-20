package ca.brainfarm.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Eric Thompson on 2017-04-26.
 */

public class SynthesisJunction {

    @SerializedName("SynthesisCommentID")
    public int synthesisCommentID;
    @SerializedName("LinkedCommentID")
    public int linkedCommentID;
    @SerializedName("SummaryBlurb")
    public String summaryBlurb;

}
