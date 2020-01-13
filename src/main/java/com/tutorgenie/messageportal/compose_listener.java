package com.tutorgenie.messageportal;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class compose_listener implements View.OnClickListener
{
    private data_type_message Message;
    private AlertDialog prevDialog;
    private Context context;
    private String to_field=null;
    compose_listener(data_type_message Message, AlertDialog prevDialog, Context context, String... params)
    {
        this.Message = Message;
        this.prevDialog = prevDialog;
        this.context=context;
        if(params!=null&&params.length>0)
        {
            to_field = params[0];
        }
    }
    @SuppressLint("SimpleDateFormat")
    @Override
    public void onClick(View v)
    {
        Util.composeMessage(Message, prevDialog, context, to_field);
    }
}
