package ca.brainfarm.data;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

/**
 * Created by Eric Thompson on 2017-12-06.
 */

public class Rating {

    @SerializedName("CommentID")
    public int commentID;
    @SerializedName("UserID")
    public int userID;
    @SerializedName("Weight")
    public int weight;
    @SerializedName("CreationDate")
    public Calendar creationDate;

}
