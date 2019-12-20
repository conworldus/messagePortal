package com.tutorgenie.messageportal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class message_update_receiver extends BroadcastReceiver
{
    interface action
    {
        void execute();
    }
    private action Action = null;
    public message_update_receiver()
    {

    }
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(Action!=null)
            Action.execute();
    }

    public void setAction (action Action)
    {
        this.Action = Action;
    }
}
