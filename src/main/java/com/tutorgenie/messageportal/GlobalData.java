package com.tutorgenie.messageportal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import java.lang.reflect.Array;
import java.util.ArrayList;

//handle all the program constants
public class GlobalData
{
    static cache DataCache = new cache();
    public static CONNECTOR connector = new CONNECTOR();
    static ViewGroup.LayoutParams params=null;
    private static int screenWidth;

    static int getScreenWidth()
    {
        return screenWidth;
    }

    public static int getScreenHeight()
    {
        return screenHeight;
    }

    private static int screenHeight;

    @SuppressLint("StaticFieldLeak")
    /*This is not a memory leak because MainActivity must always be alive*/
    static MainActivity mainActivityAccess;

    static void initializeScreenSize(Activity activity)
    {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenHeight = point.y;
        screenWidth = point.x;
    }

    static String username="", password="";
}
