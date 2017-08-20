package ca.brainfarm.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Eric Thompson on 2017-04-29.
 */

public class ContributionFile {

    @SerializedName("ContributionFileID")
    public int contributionFileID;
    @SerializedName("CommentID")
    public int commentID;
    @SerializedName("Filename")
    public String filename;

}
