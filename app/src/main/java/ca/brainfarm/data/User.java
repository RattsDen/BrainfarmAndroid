package ca.brainfarm.data;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

/**
 * Created by Eric Thompson on 2017-04-26.
 */

public class User {

    @SerializedName("UserID")
    public int userID;
    @SerializedName("Username")
    public String username;
    @SerializedName("CreationDate")
    public Calendar creationDate;
    @SerializedName("Email")
    public String email;

}
