package com.tutorgenie.messageportal;

import androidx.appcompat.app.AppCompatActivity;

public class CONST
{
    static final String status_cancelled = "cancelled";
    static final String status_declined = "declined";
    static final String status_accepted = "accepted";
    static final String status_pending = "pending";

    static final int acceptBtn = 100;
    static final int declineBtn = 101;
    static final int cancelBtn = 102;

    static final String label_inbox = "Inbox";
    static final String label_marked="Marked";

    public static final double golden_ratio = 1.61803398875;

    public static final String mailUpdateAction = "com.tutorgenie.messageportal.mailnotification";
    public static final String QUERY_TYPE = "query_type";
    public static final String data = "data";
    public static final String SUCCESS = "success";
    public static final String ERROR = "ERROR";
    public static final String NO_MESSAGE = "NO MESSAGES";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String CONTACT_LIST_EMPTY = "CONTACT_LIST_EMPTY";
    public static final String OK = "OK";

    public static final int PREVIEW_MESSAGE_LENGTH = 25;
    public static final String PREVIEW_ENDING="...";

    //below are server query types
    public static final String QUERY_MARK_READ = "MARK_READ";
    public static final String QUERY_SINGLE_STUDENT="GET_STUDENT_INFO";

}
