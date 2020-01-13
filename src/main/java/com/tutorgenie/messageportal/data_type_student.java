package com.tutorgenie.messageportal;

import android.graphics.Bitmap;

public class data_type_student
{
    private String username;
    private String firstname;
    private String lastname;
    private String gender;
    private String dateofbirth;
    private String phone_number;
    private String self_intro, webtoken;
    private int contact_id;
    private Bitmap thumb;

    public Bitmap getThumb()
    {
        return thumb;
    }

    public int getContact_id()
    {
        return contact_id;
    }

    public void setContact_id(int contact_id)
    {
        this.contact_id = contact_id;
    }

    public void setThumb(Bitmap thumb)
    {
        this.thumb = thumb;
    }

    public data_type_student()
    {
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getFirstname()
    {
        return firstname;
    }

    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    public String getLastname()
    {
        return lastname;
    }

    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getDateofbirth()
    {
        return dateofbirth;
    }

    public void setDateofbirth(String dateofbirth)
    {
        this.dateofbirth = dateofbirth;
    }

    public String getPhone_number()
    {
        return phone_number;
    }

    public void setPhone_number(String phone_number)
    {
        this.phone_number = phone_number;
    }

    public String getSelf_intro()
    {
        return self_intro;
    }

    public void setSelf_intro(String self_intro)
    {
        this.self_intro = self_intro;
    }

    public String getWebtoken()
    {
        return webtoken;
    }

    public void setWebtoken(String webtoken)
    {
        this.webtoken = webtoken;
    }
}
