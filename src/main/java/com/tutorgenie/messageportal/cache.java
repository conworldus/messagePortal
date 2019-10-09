package com.tutorgenie.messageportal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class cache
{
    private ArrayList<data_type_message> inbox_messages = new ArrayList<>();
    private ArrayList<data_type_message> sent_messages = new ArrayList<>();
    private HashMap<String, ArrayList<data_type_tutor_schedule_item>> schedule_map =
            new HashMap<>();

    public ArrayList<data_type_message> getInbox_messages()
    {
        return inbox_messages;
    }

    public void setInbox_messages(ArrayList<data_type_message> inbox_messages)
    {
        this.inbox_messages = inbox_messages;
    }

    public ArrayList<data_type_message> getSent_messages()
    {
        return sent_messages;
    }

    public void setSent_messages(ArrayList<data_type_message> sent_messages)
    {
        this.sent_messages = sent_messages;
    }

    public HashMap<String, ArrayList<data_type_tutor_schedule_item>> getSchedule_map()
    {
        return schedule_map;
    }

    public void setSchedule_map(HashMap<String, ArrayList<data_type_tutor_schedule_item>> schedule_map)
    {
        this.schedule_map = schedule_map;
    }
}
