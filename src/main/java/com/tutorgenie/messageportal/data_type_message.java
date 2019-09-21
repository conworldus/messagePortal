package com.tutorgenie.messageportal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private String label;

    public data_type_message()
    {
    }

    public data_type_message(String date, String from, String subject, boolean isRead)
    {
        this.date = date;
        this.from = from;
        this.subject = subject;
        this.isRead = isRead;
    }

    public static boolean getMessageFromJSON(JSONArray messages, data_type_message m) throws JSONException
    {
        if (messages == null) throw new JSONException("JSON ARRAY IS EMPTY!");
        JSONObject temp;
        for (int i = 0; i < messages.length(); i++)
        {
            m = new data_type_message();
            temp = messages.getJSONObject(i);
            m.setDate(temp.getString("date"));
            m.setMessage(temp.getString("message"));
            m.setFrom(temp.getString("from_id"));
            m.setTo(temp.getString("to_id"));
            m.setMessageID(temp.getInt("message_id"));
            m.setSubject(temp.getString("subject"));
            m.setFrom_name(temp.getString("from_name"));
            m.setTo_name(temp.getString("to_name"));
            m.setTime(temp.getString("time"));
            int r = temp.getInt("is_read");
            if (r == 0)
                m.setRead(false);
            else m.setRead(true);
        }
        return true;
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
