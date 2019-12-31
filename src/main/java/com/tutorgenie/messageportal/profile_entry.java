package com.tutorgenie.messageportal;

public class profile_entry
{
    private String title;
    private Object content;
    enum ENTRY_TYPE
    {
        TEXT,
        NUMBER,
        CURRENCY,
        DATE,
        MULTISELECT
    }

    private ENTRY_TYPE type = ENTRY_TYPE.MULTISELECT;

    public ENTRY_TYPE getType()
    {
        return type;
    }

    public void setType(ENTRY_TYPE type)
    {
        this.type = type;
    }

    String getTitle()
    {
        return title;
    }

    void setTitle(String title)
    {
        this.title = title;
    }

    public Object getContent()
    {
        return content;
    }

    public void setContent(Object content)
    {
        this.content = content;
    }
}
