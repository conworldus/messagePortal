package com.tutorgenie.messageportal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class adapter_calendar implements ExpandableListAdapter
{
    //use data schedule
    ArrayList<data_type_tutor_schedule_item> schedule_list = new ArrayList<>();
    private Context context;
    private final  static int BYDATE=0;
    final static int BYMONTH=2;
    final static int BYWEEK=1;
    final static int BYPENDING=4;
    private final static int ALL=3;
    private LinearLayout response_control;
    private Button cancel;
    private TextView status_final, status_group;
    adapter_calendar(Context context)
    {
        this.context =context;
    }
    private data_type_tutor_schedule_item item = null;
    public boolean calendarPermission = false;

    void setSchedule_list(String dateStr)
    {
        dateStr = dateStr.trim();
        schedule_list.clear();
        if(dateStr.equals("all"))
        {
            for (Map.Entry<String, ArrayList<data_type_tutor_schedule_item>> Entry : GlobalData.DataCache.getSchedule_map().entrySet())
                schedule_list.addAll(Objects.requireNonNull(GlobalData.DataCache.getSchedule_map().get(Entry.getKey())));
        }
        else
        {
            if(GlobalData.DataCache.getSchedule_map().containsKey(dateStr))
            {
                Log.e("has", "schedule");
                schedule_list.addAll(Objects.requireNonNull(GlobalData.DataCache.getSchedule_map().get(dateStr)));
            }
            else
                Log.e("no", "schedule for "+dateStr);
        }
    }

    void setSchedule_list(ArrayList<data_type_tutor_schedule_item> newlist)
    {
        schedule_list = newlist;
    }



    void setScheduleList(int METHOD, Long... params)
    {
        schedule_list.clear();
        switch (METHOD)
        {
            case BYDATE:
                if(GlobalData.DataCache.getSchedule_map_date().containsKey(params[0]))
                    schedule_list.addAll(Objects.requireNonNull(GlobalData.DataCache.getSchedule_map().get(params[0])));
                break;
            case BYMONTH:
            case BYWEEK:
                for(Long key: GlobalData.DataCache.getSchedule_map_date().keySet())
                {
                    if(key>=params[0]&&key<=params[1])
                        schedule_list.addAll((Objects.requireNonNull(GlobalData.DataCache.getSchedule_map_date().get(key))));
                }
                //create a whole week of date Strs;
                break;
            case ALL:
                break;
        }
        //now sort it
        if(Build.VERSION.SDK_INT>=24)
            schedule_list.sort(comp);
    }

    private Comparator<data_type_tutor_schedule_item> comp =
            new Comparator<data_type_tutor_schedule_item>()
    {
        @Override
        public int compare(data_type_tutor_schedule_item o1, data_type_tutor_schedule_item o2)
        {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            try
            {
                int c = format.parse(o1.getDateStr()).compareTo(format.parse(o2.getDateStr()));
                if (c == 0)
                {
                    if(o1.getHourInt()<o2.getHourInt())
                        return 1;
                    else return -1;
                }
                return -c;
            }catch (ParseException e)
            {e.printStackTrace();}
            return 0;
        }
    };

    @Override
    public void registerDataSetObserver(DataSetObserver observer)
    {
        //write a new observer here?
        observer = new DataSetObserver()
        {
            @Override
            public void onChanged()
            {
                super.onChanged();
            }

            @Override
            public void onInvalidated()
            {
                super.onInvalidated();
            }
        };
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer)
    {
            //no need
    }

    @Override
    public int getGroupCount()
    {
        if(schedule_list==null)
            return 0;
        return schedule_list.size();
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        //there shall be only one child each
        return 1;
    }


    @Override
    public Object getGroup(int groupPosition)
    {
        if(schedule_list!=null)
            return schedule_list.get(groupPosition);
        else return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        if(schedule_list!=null)
            return schedule_list.get(groupPosition); //child position doesn't matter
        else return null;
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        Log.d("PARENT", "PARENT created at "+groupPosition);
        View view = convertView;
        if(view==null)
        {
            view = LayoutInflater.from(context).inflate(R.layout.adapter_calendar_group, null);
        }
        item = schedule_list.get(groupPosition); //first time initialization
        TextView date = view.findViewById(R.id.schedule_item_date),
                time = view.findViewById(R.id.timeMarker),
                subject = view.findViewById(R.id.subject),
                studentName = view.findViewById(R.id.student_name);
        ImageView indicator = view.findViewById(R.id.expansion_indicator),
                share = view.findViewById(R.id.calendarShare);
        status_group = view.findViewById(R.id.request_status);
        if(isExpanded)
            indicator.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
        else indicator.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
        Log.e("Err", item.getDateStr());
        date.setText(Util.changeDateStringLocale(item.getDateStr()));
        time.setText(item.getTimeStamp());
        subject.setText(context.getString(R.string.subject_prefix, item.getSubject()));
        studentName.setText(item.getStudentname());
        status_group.setText(item.getStatus());
        switch (item.getStatus())
        {
            case "cancelled":
            case "declined":
                status_group.setTextColor(Color.BLACK);
                break;
            case "accepted":
                status_group.setTextColor(context.getResources().getColor(R.color.green_mild));
                break;
            case "pending":
                status_group.setTextColor(context.getResources().getColor(R.color.yellow_mild));
                break;
        }

        share.setOnClickListener(a->
        {
            //Toast.makeText(context, "Bingo", Toast.LENGTH_SHORT).show();

            PopupMenu popup = new PopupMenu(context, share);
            //Inflating the Popup using xml file
            popup.getMenuInflater()
                    .inflate(R.menu.calendarsharemenu, popup.getMenu());

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(mItem ->
            {
                Log.e("Selected", mItem.getOrder()+"");
                switch (mItem.getOrder())
                {
                    case 1:
                    {

                        Log.e("Calling", mItem.getOrder()+"");
                        if(!calendarPermission)
                        {
                            if(context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR)== PackageManager.PERMISSION_GRANTED
                                    &&context.checkSelfPermission(Manifest.permission.READ_CALENDAR)==PackageManager.PERMISSION_GRANTED)
                                    writeCurrentItemToCalendar(groupPosition);
                        }
                        else
                        {
                            Log.e("Exe", mItem.getOrder()+"");
                            writeCurrentItemToCalendar(groupPosition);
                        }
                        break;
                    }
                    case 2:
                        //showCalendar();
                        //construct the share message

                        StringBuilder shareMessage = new StringBuilder();
                        data_type_tutor_schedule_item cItem = schedule_list.get(groupPosition);
                        for (Method m : cItem.getClass().getMethods())
                            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0)
                            {
                                shareMessage.append(m.getName().substring(3));
                                shareMessage.append(": ");
                                try
                                {
                                    shareMessage.append(m.invoke(cItem));
                                } catch (IllegalAccessException | InvocationTargetException e)
                                {
                                    e.printStackTrace();
                                }
                                shareMessage.append("\n");
                                // do your thing with r
                            }

                        Util.sendDataToOtherApps(context, shareMessage.toString());
                        break;
                }
                return true;
            });

            popup.show(); //showing popup menu

        });
        return view;
    }

   /* private void showCalendar()
    {
        String projection[] = {"_id", "calendar_displayName"};
        Uri calendars;
        calendars = Uri.parse("content://com.android.calendar/calendars");

        ContentResolver contentResolver = context.getContentResolver();
        Cursor managedCursor = contentResolver.query(calendars, projection, null, null, null);

        if (managedCursor.moveToFirst())
        {
            String calName;
            String calID;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);
            do
            {
                calName = managedCursor.getString(nameCol);
                calID = managedCursor.getString(idCol);
                Log.i("CalName", calName+" ID: "+calID);
            } while (managedCursor.moveToNext());
            managedCursor.close();
        }
    }*/

    private void writeCurrentItemToCalendar(int groupPosition)
    {
        Log.e("C", "writeFunc");
        data_type_tutor_schedule_item cItem = schedule_list.get(groupPosition);
        try
        {
            ContentResolver cr = context.getContentResolver();
            ContentValues cv = new ContentValues();
            int duration = Integer.parseInt(cItem.getDuration()) * 60 * 1000;
            @SuppressLint("SimpleDateFormat") Date date =
                    (new SimpleDateFormat("yyyy-MM-dd")).parse(cItem.getDateStr());
            Log.i("Date", cItem.getDateStr());

            int hrInt = cItem.getHourInt();
            Log.i("Hour Int", hrInt+"");
            long startHour = (hrInt-(hrInt%100))*60*60*10+(hrInt%100)*60*1000;
            Log.i("Long Hr", startHour+"");
            cv.put(CalendarContract.Events.TITLE, "Tutoring Sessioins");
            cv.put(CalendarContract.Events.DTSTART, date.getTime()+startHour);
            cv.put(CalendarContract.Events.DTEND, date.getTime()+startHour+duration);
            cv.put(CalendarContract.Events.DESCRIPTION,
                    "Subject: "+cItem.getSubject()+"\nStudent: "+cItem.getStudentname()+
                            "\nLocation: "+cItem.getLocation());
            cv.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Hong" +
                    "_Kong");
            cv.put(CalendarContract.Events.CALENDAR_ID, 1);

            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, cv);
            Log.d("URI", Objects.requireNonNull(uri).getLastPathSegment());
        }catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressLint("InflateParams")
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        View view = convertView;
        if(view==null)
        {
            view = LayoutInflater.from(context).inflate(R.layout.adapter_calendar, null);
        }

        final TextView addNoteButton = view.findViewById(R.id.add_note_button);
        final TextView addNote = view.findViewById(R.id.add_note);
        TextView studentComment = view.findViewById(R.id.student_comment);
        String note = item.getTutorNote();
        if(note.trim().equals("null"))
            note = "";
        addNote.setText(context.getString(R.string.add_note_header, note));
        String comment = item.getComment();
        if(comment.trim().equals("null"))
            comment="";
        studentComment.setText(context.getString(R.string.student_comment_header,
                comment));
        addNoteButton.setOnClickListener(v ->
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view1 = LayoutInflater.from(context).inflate(R.layout.edittext, null);
            final EditText note1 = view1.findViewById(R.id.noteEdit);
            if(!item.getTutorNote().trim().equals("null"))
                note1.setText(item.getTutorNote());

            builder.setPositiveButton(R.string.confirm, (dialog, which) ->
            {
                //first get data
                JSONObject data = new JSONObject();
                try
                {
                    JSONObject queryObj = Util.connectionObject(context);
                    queryObj.put("query_type", "UPDATE_SCHEDULE");
                    queryObj.put("schedule_id", item.getScheduleID());
                    data.put("tutor_note", note1.getText());
                    queryObj.put("data", data);
                    String result = (new CONNECTOR()).execute(queryObj).get();
                    Log.e("result", result);

                    //update field
                    item.setTutorNote(note1.getText().toString());
                    addNote.setText(context.getString(R.string.add_note_header,
                            note1.getText().toString()));
                }catch (JSONException|InterruptedException| ExecutionException e)
                {
                    e.printStackTrace();
                }
            });

            builder.setView(view1);
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        //Map section =============================================================
        TextView location = view.findViewById(R.id.adapter_calendar_location);
        String meetingLocation = item.getLocation();
        location.setText(context.getString(R.string.location_h, meetingLocation));
        final MapView map = view.findViewById(R.id.calendar_location_map); //draw a label on it
        IMapController controller = map.getController();
        controller.setCenter(new GeoPoint(item.getLat(), item.getLng()));
        controller.setZoom(15.0); //guessed zoom level
        //End Map Section==========================================================


        //================================================================
        response_control = view.findViewById(R.id.to_respond);
        cancel = view.findViewById(R.id.adapter_calendar_cancel);
        Button reject = view.findViewById(R.id.adapteR_calendar_reject);
        Button accept = view.findViewById(R.id.adapter_calendar_accept);
        status_final = view.findViewById(R.id.status_text_negative);

        final TextView show_map = view.findViewById(R.id.adapter_calendar_show_map);
        show_map.setOnClickListener(v ->
        {
            if(map.getVisibility() == View.GONE)
            {
                map.setVisibility(View.VISIBLE);
                show_map.setText(R.string.hide_map);
            }
            else
            {
                map.setVisibility(View.GONE);
                show_map.setText(R.string.show_map);
            }
        });
        cancel.setOnClickListener(new scheduleButtonListener(item.getScheduleID(),
                CONST.cancelBtn, response_control, cancel, status_final, item, this));
        accept.setOnClickListener(new scheduleButtonListener(item.getScheduleID(),
                CONST.acceptBtn, response_control, cancel, status_final, item, this));
        reject.setOnClickListener(new scheduleButtonListener(item.getScheduleID(),
                CONST.declineBtn, response_control, cancel, status_final, item, this));


        setStatus(item.getStatus());
        return view;
    }

    private class scheduleButtonListener implements View.OnClickListener
    {
        data_type_tutor_schedule_item mItem;
        int scheduleID;
        int whichButton;
        LinearLayout response_panel;
        Button accept_cancel;
        TextView final_status;
        adapter_calendar Parent;

        scheduleButtonListener(int scheduleID, int whichButton, LinearLayout response_panel,
                               Button accept_cancel, TextView final_status,
                               data_type_tutor_schedule_item item, adapter_calendar ad)
        {
            this.scheduleID = scheduleID;
            this.whichButton = whichButton;
            this.response_panel = response_panel;
            this.accept_cancel = accept_cancel;
            this.final_status = final_status;
            this.mItem = item;
            this.Parent = ad;
        }

        @Override
        public void onClick(View v)
        {
            String new_status="";
            switch (whichButton)
            {
                case CONST.acceptBtn:
                        new_status = CONST.status_accepted;
                        break;
                case CONST.cancelBtn:
                        new_status = CONST.status_cancelled;
                        break;
                case CONST.declineBtn:
                        new_status = CONST.status_declined;
                        break;
                default:
                    new_status = CONST.status_pending;
            }
            mItem.setStatus(new_status);
            Parent.setStatus(new_status);

            Util.removePending(mItem);

            try
            {
                JSONObject queryObj = Util.connectionObject(context);
                JSONObject data = new JSONObject();
                queryObj.put("query_type", "UPDATE_SCHEDULE");
                queryObj.put("schedule_id", scheduleID);
                data.put("schedule_status", new_status);
                queryObj.put("data", data);
                String result = (new CONNECTOR()).execute(queryObj).get();
                Log.e("result", result);
            }catch (ExecutionException|InterruptedException|JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void setStatus(String status)
    {
        status_group.setText(status);
        item.setStatus(status);
        switch (status)
        {
            case "pending":
            {
                response_control.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.GONE);
                status_final.setVisibility(View.GONE);
                status_group.setTextColor(context.getColor(R.color.yellow_mild));
                break;
            }
            case "accepted":
            {
                response_control.setVisibility(View.GONE);
                cancel.setVisibility(View.VISIBLE);
                status_final.setVisibility(View.GONE);
                status_group.setTextColor(context.getColor(R.color.green_mild));
                break;
            }
            case "declined":
            case "cancelled":
            {
                Log.e("Exe", "Exehere");
                response_control.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                status_final.setVisibility(View.VISIBLE);
                status_group.setTextColor(Color.BLACK);
                break;
            }
        }
    }




    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public void onGroupExpanded(int groupPosition)
    {

    }

    @Override
    public void onGroupCollapsed(int groupPosition)
    {

    }

    @Override
    public long getCombinedChildId(long groupId, long childId)
    {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long groupId)
    {
        return 0;
    }
}

