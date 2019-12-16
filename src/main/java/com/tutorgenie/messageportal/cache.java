package com.tutorgenie.messageportal;

import android.graphics.Bitmap;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class cache
{
    private ArrayList<data_type_message> inbox_messages = new ArrayList<>();
    private ArrayList<data_type_message> sent_messages = new ArrayList<>();
    private HashMap<String, ArrayList<data_type_tutor_schedule_item>> schedule_map =
            new HashMap<>();
    private HashMap<Long, ArrayList<data_type_tutor_schedule_item>> schedule_map_date =
            new HashMap<>();

    private ArrayList<profile_entry> profileData = new ArrayList<>();
    private ArrayList<String> complete_subject_list = new ArrayList<>();
    private BiMap<String, String> nationality_map = HashBiMap.create();
    private ArrayList<String> nationality_list = new ArrayList<>();
    private HashMap<String, String> login_info = new HashMap<>();

    public HashMap<String, String> getLogin_info()
    {
        return login_info;
    }

    public void setLogin_info(HashMap<String, String> login_info)
    {
        this.login_info = login_info;
    }

    private Bitmap profileImg;

    public HashMap<Long, ArrayList<data_type_tutor_schedule_item>> getSchedule_map_date()
    {
        return schedule_map_date;
    }

    public void setSchedule_map_date(HashMap<Long,
                                     ArrayList<data_type_tutor_schedule_item>> schedule_map_date)
    {
        this.schedule_map_date = schedule_map_date;
    }

    public Bitmap getProfileImg()
    {
        return profileImg;
    }

    public void setProfileImg(Bitmap profileImg)
    {
        this.profileImg = profileImg;
    }

    public ArrayList<String> getNationality_list()
    {
        return nationality_list;
    }

    public void setNationality_list(ArrayList<String> nationality_list)
    {
        this.nationality_list = nationality_list;
    }

    public BiMap<String, String> getNationality_map()
    {
        return nationality_map;
    }

    public void setNationality_map(BiMap<String, String> nationality_map)
    {
        this.nationality_map = nationality_map;
    }

    public ArrayList<profile_entry> getProfileData()
    {
        return profileData;
    }

    public void setProfileData(ArrayList<profile_entry> profileData)
    {
        profileData = profileData;
    }

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

    public ArrayList<String> getComplete_subject_list()
    {
        return complete_subject_list;
    }

    public void setComplete_subject_list(ArrayList<String> complete_subject_list)
    {
        this.complete_subject_list = complete_subject_list;
    }
}
