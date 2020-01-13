package com.tutorgenie.messageportal;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class cache
{
    private ArrayList<data_type_message> inbox_messages = new ArrayList<>();
    private ArrayList<data_type_message> sent_messages = new ArrayList<>();
    private ArrayList<data_type_student> contact_list = new ArrayList<>();

    public void SortContactList()
    {
        contact_list.sort(new Comparator<data_type_student>()
        {
            @Override
            public int compare(data_type_student o1, data_type_student o2)
            {
                return o1.getLastname().compareTo(o2.getLastname());
            }
        });
    }


    public ArrayList<data_type_student> getContact_list()
    {
        return contact_list;
    }

    //two hashmaps are same except one uses string as key, and the other one uses long
    private HashMap<String, ArrayList<data_type_tutor_schedule_item>> schedule_map =
            new HashMap<>();

    private HashMap<Long, ArrayList<data_type_tutor_schedule_item>> schedule_map_date =
            new HashMap<>();


    private ArrayList<data_type_tutor_schedule_item> pendingList = new ArrayList<>();

    private ArrayList<profile_entry> profileData = new ArrayList<>();
    private ArrayList<String> complete_subject_list = new ArrayList<>();
    private BiMap<String, String> nationality_map = HashBiMap.create();
    private ArrayList<String> nationality_list = new ArrayList<>();
    private HashMap<String, String> login_info = new HashMap<>();
    final static String [] gender_list =
            {
                    "Male",
                    "Female",
                    "Other"
            };

    HashMap<String, String> getLogin_info()
    {
        return login_info;
    }

    ArrayList<data_type_tutor_schedule_item> getPendingList()
    {
        return pendingList;
    }

    public void setLogin_info(HashMap<String, String> login_info)
    {
        this.login_info = login_info;
    }

    private Bitmap profileImg;

    HashMap<Long, ArrayList<data_type_tutor_schedule_item>> getSchedule_map_date()
    {
        return schedule_map_date;
    }

    public void setSchedule_map_date(HashMap<Long,
                                     ArrayList<data_type_tutor_schedule_item>> schedule_map_date)
    {
        this.schedule_map_date = schedule_map_date;
    }

    Bitmap getProfileImg()
    {
        return profileImg;
    }

    void setProfileImg(Bitmap profileImg)
    {
        this.profileImg = profileImg;
    }

    ArrayList<String> getNationality_list()
    {
        return nationality_list;
    }

    public void setNationality_list(ArrayList<String> nationality_list)
    {
        this.nationality_list = nationality_list;
    }

    BiMap<String, String> getNationality_map()
    {
        return nationality_map;
    }

    public void setNationality_map(BiMap<String, String> nationality_map)
    {
        this.nationality_map = nationality_map;
    }

    ArrayList<profile_entry> getProfileData()
    {
        return profileData;
    }


    ArrayList<data_type_message> getInbox_messages()
    {
        return inbox_messages;
    }


    ArrayList<data_type_message> getSent_messages()
    {
        return sent_messages;
    }
    HashMap<String, ArrayList<data_type_tutor_schedule_item>> getSchedule_map()
    {
        return schedule_map;
    }


    ArrayList<String> getComplete_subject_list()
    {
        return complete_subject_list;
    }

}
