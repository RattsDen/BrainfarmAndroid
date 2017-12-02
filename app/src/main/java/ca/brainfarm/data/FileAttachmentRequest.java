package ca.brainfarm.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Eric Thompson on 2017-11-03.
 */

public class FileAttachmentRequest {

    @SerializedName("ContributionFileID")
    public int contributionFileID;
    @SerializedName("Filename")
    public String filename;

}
