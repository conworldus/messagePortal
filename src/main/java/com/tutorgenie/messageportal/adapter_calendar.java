package com.tutorgenie.messageportal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.zip.Inflater;

public class adapter_calendar implements ExpandableListAdapter
{
    //use data schedule
    ArrayList<data_type_tutor_schedule_item> schedule_list = new ArrayList<>();
    Context context;
    public final  static int BYDATE=0;
    public final static int BYMONTH=2;
    public final static int BYWEEK=1;
    public final static int ALL=3;
    boolean note_shown = false;
    boolean map_shown = false;
    LinearLayout response_control;
    Button accept, reject, cancel;
    TextView status_final, status_group;
    adapter_calendar(Context context)
    {
        this.context =context;
    }
    data_type_tutor_schedule_item item = null;

    public void setSchedule_list(String dateStr)
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



    public void setScheduleList(int METHOD, Long... params)
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
        View view = convertView;
        if(view==null)
        {
            view = LayoutInflater.from(context).inflate(R.layout.adapter_calendar_group, null);
        }

        //now progress data
        item = schedule_list.get(groupPosition);

        //get the views

        TextView date = view.findViewById(R.id.schedule_item_date),
                time = view.findViewById(R.id.timeMarker),
                subjectSign = view.findViewById(R.id.subject_sign),
                subject = view.findViewById(R.id.subject),
                studentName = view.findViewById(R.id.student_name),
                duration = view.findViewById(R.id.durationMarker);
        status_group = view.findViewById(R.id.request_status);

        String du = item.getDuration()+" minutes";
        duration.setText(du);

        date.setText(Util.changeDateStringLocale(item.getDateStr()));
        time.setText(item.getTimeStamp());
        subjectSign.setText(item.getSubject().substring(0, 1));
        subject.setText(context.getString(R.string.subject_prefix, item.getSubject()));
        studentName.setText(item.getStudentname());

        Log.e("S name", item.getStudentname());
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
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        View view = convertView;
        if(view==null)
        {
            view = LayoutInflater.from(context).inflate(R.layout.adapter_calendar, null);
        }

        item = schedule_list.get(groupPosition);
        final TextView addNoteButton = view.findViewById(R.id.add_note_button);
        final TextView addNote = view.findViewById(R.id.add_note);
        TextView studentComment = view.findViewById(R.id.student_comment);
        addNote.setText(context.getString(R.string.add_note_header, item.getTutorNote()));
        studentComment.setText(context.getString(R.string.student_comment_header,
                item.getComment()));
        addNoteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View view = LayoutInflater.from(context).inflate(R.layout.edittext, null);
                final EditText note = view.findViewById(R.id.noteEdit);
                note.setText(item.getTutorNote());

                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //first get data
                        JSONObject data = new JSONObject();
                        try
                        {
                            JSONObject queryObj = Util.connectionObject(context);
                            queryObj.put("query_type", "UPDATE_SCHEDULE");
                            queryObj.put("schedule_id", item.getScheduleID());
                            data.put("tutor_note", note.getText());
                            queryObj.put("data", data);
                            String result = (new CONNECTOR()).execute(queryObj).get();
                            Log.e("result", result);

                            //update field
                            item.setTutorNote(note.getText().toString());
                            addNote.setText(context.getString(R.string.add_note_header,
                                    note.getText().toString()));
                        }catch (JSONException|InterruptedException| ExecutionException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

                builder.setView(view);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });



        TextView location = view.findViewById(R.id.adapter_calendar_location);
        String meetingLocation = item.getLocation()+" Lat: "+item.getLat()+" Lng: "+item.getLng();
        location.setText(context.getString(R.string.location_h, meetingLocation));
        final MapView map = view.findViewById(R.id.calendar_location_map); //draw a label on it
        IMapController controller = map.getController();
        controller.setCenter(new GeoPoint(item.getLat(), item.getLng()));
        controller.setZoom(15.0); //guessed zoom level
        response_control = view.findViewById(R.id.to_respond);
        cancel = view.findViewById(R.id.adapter_calendar_cancel);
        reject = view.findViewById(R.id.adapteR_calendar_reject);
        accept = view.findViewById(R.id.adapter_calendar_accept);
        status_final = view.findViewById(R.id.status_text_negative);

        final TextView show_map = view.findViewById(R.id.adapter_calendar_show_map);
        show_map.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
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
            }
        });
        setStatus(item.getStatus());

        cancel.setOnClickListener(new scheduleButtonListener(item.getScheduleID(),
                constants.cancelBtn, response_control, cancel, status_final));
        accept.setOnClickListener(new scheduleButtonListener(item.getScheduleID(), constants.acceptBtn, response_control, cancel, status_final));
        reject.setOnClickListener(new scheduleButtonListener(item.getScheduleID(), constants.declineBtn, response_control, cancel, status_final));
        return view;
    }

    private class scheduleButtonListener implements View.OnClickListener
    {
        int scheduleID;
        int whichButton;
        LinearLayout response_panel;
        Button accept_cancel;
        TextView final_status;
        TextView status_group;

        scheduleButtonListener(int scheduleID, int whichButton, LinearLayout response_panel,
                               Button accept_cancel, TextView final_status)
        {
            this.scheduleID = scheduleID;
            this.whichButton = whichButton;
            this.response_panel = response_panel;
            this.accept_cancel = accept_cancel;
            this.final_status = final_status;
        }

        @Override
        public void onClick(View v)
        {
            String new_status="";
            switch (whichButton)
            {
                case constants.acceptBtn:
                        new_status = constants.status_accepted;
                        break;
                case constants.cancelBtn:
                        new_status = constants.status_cancelled;
                        break;
                case constants.declineBtn:
                        new_status =constants.status_declined;
                        break;
                default:
                    new_status = constants.status_pending;
            }

            item.setStatus(new_status);
            switch (new_status)
            {
                case "pending":
                {
                    response_panel.setVisibility(View.VISIBLE);
                    accept_cancel.setVisibility(View.GONE);
                    final_status.setVisibility(View.GONE);
                    break;
                }
                case "accepted":
                {
                    response_panel.setVisibility(View.GONE);
                    accept_cancel.setVisibility(View.VISIBLE);
                    final_status.setVisibility(View.GONE);
                    break;
                }
                case "declined":
                case "cancelled":
                {
                    Log.e("Exe", "Exehere");
                    response_panel.setVisibility(View.GONE);
                    accept_cancel.setVisibility(View.GONE);
                    final_status.setVisibility(View.VISIBLE);
                    final_status.setText(new_status);
                    break;
                }
            }
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
        Log.e("Setting", status);
        status_group.setText(status);
        item.setStatus(status);
        switch (status)
        {
            case "pending":
            {
                response_control.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.GONE);
                status_final.setVisibility(View.GONE);
                break;
            }
            case "accepted":
            {
                response_control.setVisibility(View.GONE);
                cancel.setVisibility(View.VISIBLE);
                status_final.setVisibility(View.GONE);
                break;
            }
            case "declined":
            case "cancelled":
            {
                Log.e("Exe", "Exehere");
                response_control.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                status_final.setVisibility(View.VISIBLE);
                status_final.setText(status);
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

