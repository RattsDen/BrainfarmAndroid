package ca.brainfarm.data;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

/**
 * Created by Eric Thompson on 2017-04-29.
 */

public class Project {

    // Database fields
    @SerializedName("ProjectID")
    public int projectID;
    @SerializedName("UserID")
    public int userID;
    @SerializedName("Title")
    public String title;
    @SerializedName("CreationDate")
    public Calendar creationDate;

    // Extra fields
    @SerializedName("Username")
    public String username;
    @SerializedName("Tags")
    public String[] tags;

}
