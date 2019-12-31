package com.tutorgenie.messageportal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.TypedValue;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

class Util
{
    static String CapsFirst(String str) {
        String[] words = str.split(" ");
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < words.length; i++) {
            ret.append(Character.toUpperCase(words[i].charAt(0)));
            ret.append(words[i].substring(1));
            if(i < words.length - 1) {
                ret.append(' ');
            }
        }
        return ret.toString();
    }

    static boolean updateProfile(Context context, String field, String value)
    {
        boolean result=false;
        try
        {
            JSONObject queryData =
                    new JSONObject();
            queryData.put("username",
                    context.getString(R.string.test_username));
            queryData.put("password",
                    context.getString(R.string.test_password));
            queryData.put("query_type",
                    "UPDATE_PROFILE");
            queryData.put("field",
                    field);
            queryData.put("value", value);
            String response = (new CONNECTOR()).execute(queryData).get();
            if(response.trim().equals("OK")) //update success
            {
                result = true;
            }
        }catch (JSONException | ExecutionException |InterruptedException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    static String changeDateStringLocale(String inDate)
    {
        String format = "yyyy-MM-dd";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sFormat = new SimpleDateFormat(format);
        DateFormat outFormat = SimpleDateFormat.getDateInstance();
        try
        {
            Date date = sFormat.parse(inDate);
            return outFormat.format(date);
        }catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static JSONObject connectionObject(Context context) throws JSONException
    {
        JSONObject queryData = new JSONObject();
        queryData.put("username", GlobalData.username);
        queryData.put("password", GlobalData.password);
        return queryData;
    }

    static int getActionBarHeight(Context context)
    {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            return TypedValue.complexToDimensionPixelSize(tv.data,
                context.getResources().getDisplayMetrics());
        }
        return 0;
    }

    static void sendDataToOtherApps(Context context, String data)
    {
        String dummySubject = "From Tutor-Genie";
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, dummySubject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, data.trim());
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_via)));
    }

    static void checkPermissions(Context context, int callbackId, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions =
                    permissions && ContextCompat.checkSelfPermission(context, p) == PERMISSION_GRANTED;
        }

        if (!permissions)
            ActivityCompat.requestPermissions((Activity)context, permissionsId, callbackId);
    }

    static void removePending(data_type_tutor_schedule_item item)
    {
        GlobalData.DataCache.getPendingList().remove(item);
        MainActivity.pendingCount.setValue(GlobalData.DataCache.getPendingList().size());
    }
}
