package com.tutorgenie.messageportal;

public class data_type_tutor_schedule_item
{
    private String timeStamp;
    private String comment;
    private String status;
    private String subject;
    private String tutorname;
    private String studentname;
    private String duration;
    private String DateStr;
    private String StudentNote;
    private int DateInt;
    private int HourInt;
    private boolean owned = false;
    private int ScheduleID;
    private String location;
    private boolean booked = false;
    private boolean bookable = false;
    private String TutorNote;
    private double lat=0;
    private double lng=0;

    public double getLat()
    {
        return lat;
    }

    public String getTutorNote()
    {
        return TutorNote;
    }

    public void setTutorNote(String tutorNote)
    {
        TutorNote = tutorNote;
    }

    public void setLat(double lat)
    {
        this.lat = lat;
    }

    public double getLng()
    {
        return lng;
    }

    public void setLng(double lng)
    {
        this.lng = lng;
    }

    public String getStudentNote()
    {
        return StudentNote;
    }

    public void setStudentNote(String studentNote)
    {
        StudentNote = studentNote;
    }

    public data_type_tutor_schedule_item() {}

    public data_type_tutor_schedule_item(String timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public int getScheduleID()
    {
        return ScheduleID;
    }

    public void setScheduleID(int scheduleID)
    {
        ScheduleID = scheduleID;
    }

    public int getHourInt()
    {
        return HourInt;
    }

    public void setHourInt(int hourInt)
    {
        HourInt = hourInt;
    }

    public int getDateInt()
    {
        return DateInt;
    }

    public void setDateInt(int dateInt)
    {
        DateInt = dateInt;
    }

    public boolean isOwned()
    {
        return owned;
    }

    public void setOwned(boolean owned)
    {
        this.owned = owned;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public String getTutorname()
    {
        return tutorname;
    }

    public void setTutorname(String tutorname)
    {
        this.tutorname = tutorname;
    }

    public String getStudentname()
    {
        return studentname;
    }

    public void setStudentname(String studentname)
    {
        this.studentname = studentname;
    }

    public String getDuration()
    {
        return duration;
    }

    public void setDuration(String duration)
    {
        this.duration = duration;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public boolean isBookable()
    {
        return bookable;
    }

    public void setBookable(boolean bookable)
    {
        this.bookable = bookable;
    }

    public String getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public boolean isBooked()
    {
        return booked;
    }

    public void setBooked(boolean booked)
    {
        this.booked = booked;
    }

    public String getDateStr()
    {
        return DateStr;
    }

    public void setDateStr(String dateStr)
    {
        DateStr = dateStr;
    }
}
