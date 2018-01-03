package ca.brainfarm.data;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

/**
 * Created by Eric Thompson on 2017-04-29.
 */

public class Comment {

    // Database fields
    @SerializedName("CommentID")
    public int commentID;
    @SerializedName("UserID")
    public int userID;
    @SerializedName("ParentCommentID")
    public Integer parentCommentID;
    @SerializedName("ProjectID")
    public int projectID;
    @SerializedName("CreationDate")
    public Calendar creationDate;
    @SerializedName("EditedDate")
    public Calendar editedDate;
    @SerializedName("BodyText")
    public String bodyText;
    @SerializedName("IsSynthesis")
    public boolean isSynthesis;
    @SerializedName("IsContribution")
    public boolean isContribution;
    @SerializedName("IsSpecification")
    public boolean isSpecification;

    // Extra fields
    @SerializedName("Username")
    public String username;
    @SerializedName("IsBookmarked")
    public boolean isBookmarked;
    @SerializedName("IsRemoved")
    public boolean isRemoved;
    @SerializedName("Children")
    public Comment[] children;
    @SerializedName("LinkingCommentIDs")
    public int[] linkingCommentIDs; // Synthesis comments that link to this
    @SerializedName("Syntheses")
    public SynthesisJunction[] syntheses; // Comments that this synthesis links to
    @SerializedName("ContributionFiles")
    public ContributionFile[] contributionFiles;

}
