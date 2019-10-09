package com.tutorgenie.messageportal;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;

public class data_type_message
{
    private String date = "";
    private String from = "";
    private String subject = "";
    private boolean isRead = false;
    private int MessageID = -1;
    private CharSequence message;
    private String to = "";
    private boolean toDelete = false;
    private String time = "";
    private String from_name;
    private String to_name;
    private String label="";
    private Date full_date = new Date();

    public data_type_message()
    {
    }

    public void setFull_date(String date, String time)
    {
        try
        {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            full_date = format.parse(date+" "+time);
        }catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public Date getFull_date()
    {
        return full_date;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public String getFrom_name()
    {
        return from_name;
    }

    public void setFrom_name(String from_name)
    {
        this.from_name = from_name;
    }

    public String getTo_name()
    {
        return to_name;
    }

    public void setTo_name(String to_name)
    {
        this.to_name = to_name;
    }

    public boolean isToDelete()
    {
        return toDelete;
    }

    public void setToDelete(boolean toDelete)
    {
        this.toDelete = toDelete;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public boolean isRead()
    {
        return isRead;
    }

    public void setRead(boolean read)
    {
        isRead = read;
    }

    public int getMessageID()
    {
        return MessageID;
    }

    public void setMessageID(int messageID)
    {
        MessageID = messageID;
    }

    public CharSequence getMessage()
    {
        return message;
    }

    public void setMessage(CharSequence message)
    {
        this.message = message;
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(String to)
    {
        this.to = to;
    }
}
