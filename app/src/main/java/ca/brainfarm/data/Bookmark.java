package ca.brainfarm.data;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

/**
 * Created by Eric Thompson on 2017-04-29.
 */

public class Bookmark {

    // Database fields
    @SerializedName("UserID")
    public int userID;
    @SerializedName("CommentID")
    public int commentID;
    @SerializedName("CreatedDate")
    public Calendar createdDate;
    
}
