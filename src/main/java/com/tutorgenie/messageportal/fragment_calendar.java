package com.tutorgenie.messageportal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class fragment_calendar extends Fragment
{
    private Calendar calendar = Calendar.getInstance();
    private Context context;
    private Button dateView;
    private String dateStr;
    private String monthStr;
    private Pair weekPair;
    private adapter_calendar adapter;
    private ExpandableListView entryList;
    private Spinner method;
    private ArrayList<data_type_tutor_schedule_item> tempRepliedList = new ArrayList<>();

    private int lastExpanded = -1;

    @SuppressLint("SimpleDateFormat")
    fragment_calendar(Context context)
    {
        this.context = context;
        Date date = new Date();
        //build a month string
        monthStr = new SimpleDateFormat("MMMM").format(date);
        dateStr = (new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        //now build week str
        int weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
        int yearNumber = calendar.get(Calendar.YEAR);
        weekPair = getWeekRange(yearNumber, weekNumber);
    }

    private Pair<Long, Long> getWeekRange(int year, int week_no)
    {
        Log.e(year+"", week_no+"");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.WEEK_OF_YEAR, week_no);
        Date saturday = cal.getTime();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.WEEK_OF_YEAR, week_no);
        Date sunday = cal.getTime();
        return new Pair<>(sunday.getTime(), saturday.getTime());
    }

    private Pair<Long, Long> getMonthRange(Long pickedDate)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(pickedDate);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date monthBegin = cal.getTime();

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date monthEnd = cal.getTime();

        return new Pair<>(monthBegin.getTime(), monthEnd.getTime());
    }


    @SuppressLint("SimpleDateFormat")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        //first check permission
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            String [] writePermission = new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(writePermission, 0);
        }
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        dateView = view.findViewById(R.id.schedule_select_date);
        method = view.findViewById(R.id.calendar_display_method);
        dateView.setText(Util.changeDateStringLocale(dateStr));
        final TextView replaceText = view.findViewById(R.id.calendarReplaceText);
        ArrayList<String> method_list = new ArrayList<>();
        method_list.add(getString(R.string.date_view));
        method_list.add(getString(R.string.weekly_view));
        method_list.add(getString(R.string.month_view));
        method_list.add(getString(R.string.pending_view));

        method.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1,
                method_list
        ));

        dateView.setOnClickListener(v ->
        {
            final DatePicker picker = new DatePicker(context);
            picker.updateDate(/*year, month, day*/calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(picker);
            AlertDialog dialog;
            builder.setPositiveButton(R.string.confirm, (dialog1, which) ->
            {
                //clear temp list right now
                if(tempRepliedList.size()>0)
                {
                    GlobalData.DataCache.getPendingList().removeAll(tempRepliedList);
                    tempRepliedList.clear();
                }



                //set date
                @SuppressLint("DefaultLocale")
                String d_str =
                        picker.getYear()+"-"+String.format("%02d", (picker.getMonth()+1))+"-"+picker.getDayOfMonth();
                //dateView.setText(Util.changeDateStringLocale(d_str));
                dateStr = d_str;
                calendar.set(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
                monthStr = new SimpleDateFormat("MMMM").format(calendar.getTime());
                int yearNum = picker.getYear();
                if(calendar.get(Calendar.WEEK_OF_YEAR)==1)
                {
                    if(picker.getMonth()==11)
                        yearNum+=1; //account for the last week of the year.
                }
                weekPair = getWeekRange(yearNum,
                        calendar.get(Calendar.WEEK_OF_YEAR));

                //now check method
                adapter = new adapter_calendar(context);
                adapter.setTempList(tempRepliedList);
                switch (method.getSelectedItemPosition())
                {
                    case 0: //date view
                        dateView.setText(Util.changeDateStringLocale(dateStr));
                        adapter.setSchedule_list(dateStr);
                        break;
                    case 1: //week view
                        String display =
                                Util.changeDateStringLocale(sdf.format(new Date((long)weekPair.first)))+
                                        " " +
                                        "- "+Util.changeDateStringLocale(sdf.format(new Date((long)weekPair.second)));
                        dateView.setText(display);
                        Pair<Long, Long> weekRange = getWeekRange(yearNum,
                                calendar.get(Calendar.WEEK_OF_YEAR));
                        adapter.setScheduleList(adapter_calendar.BYWEEK, weekRange.first,
                                weekRange.second);
                        break;
                    case 2: //month view
                        String display_m = monthStr + ", " + picker.getYear();
                        Pair<Long, Long> monthRange =
                                getMonthRange(calendar.getTimeInMillis());
                        adapter.setScheduleList(adapter_calendar.BYMONTH,
                                monthRange.first, monthRange.second);
                        dateView.setText(display_m);
                        break;
                    case 3: //pending view
                        adapter.setSchedule_list(GlobalData.DataCache.getPendingList());
                        break;
                }
                entryList.setAdapter(adapter);
                entryList.invalidateViews();
                setReplaceText(replaceText);
                dialog1.dismiss();
            });
            dialog = builder.create();
            dialog.show();
        });
        entryList = view.findViewById(R.id.schedule_list);
        adapter = new adapter_calendar(context);
        adapter.setTempList(tempRepliedList);
        adapter.setSchedule_list(dateStr);

        //set alternative view
        setReplaceText(replaceText);

        entryList.setAdapter(adapter);
        entryList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
        .OnGlobalLayoutListener()

        {
            @Override
            public void onGlobalLayout()
            {
                int height = entryList.getHeight();
                int actionbarHeight = Util.getActionBarHeight(context);
                int finalHeight = height - actionbarHeight;
                entryList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, finalHeight));
                entryList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        method.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                //first clear anything in the tempList
                if(tempRepliedList.size()>0)
                {
                    GlobalData.DataCache.getPendingList().removeAll(tempRepliedList);
                    tempRepliedList.clear();
                }

                try
                {
                    calendar.setTimeInMillis(sdf.parse(dateStr).getTime());
                    adapter = new adapter_calendar(context);
                    adapter.setTempList(tempRepliedList);
                    switch (method.getSelectedItemPosition())
                    {
                        case 0: //date view
                            dateView.setText(Util.changeDateStringLocale(dateStr));
                            adapter.setSchedule_list(dateStr);
                            break;
                        case 1: //week view
                            weekPair = getWeekRange(calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.WEEK_OF_YEAR));
                            String display =
                                    Util.changeDateStringLocale(sdf.format(new Date((long)weekPair.first)))+
                                            " " +
                                            "- "+Util.changeDateStringLocale(sdf.format(new Date((long)weekPair.second)));
                            dateView.setText(display);
                            adapter.setScheduleList(adapter_calendar.BYWEEK, (long)weekPair.first,
                                    (long)weekPair.second);
                            break;
                        case 2: //month view
                            String display_m = monthStr + ", " + calendar.get(Calendar.YEAR);
                            Pair<Long, Long> monthRange =
                                    getMonthRange(calendar.getTimeInMillis());
                            adapter.setScheduleList(adapter_calendar.BYMONTH,
                                    monthRange.first, monthRange.second);
                            dateView.setText(display_m);
                            break;
                        case 3: //available view
                            adapter.setSchedule_list(GlobalData.DataCache.getPendingList());
                            break;
                    }

                    entryList.setAdapter(adapter);
                    entryList.invalidateViews();
                }catch (ParseException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    setReplaceText(replaceText);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        //check Calendar Permission
        if(context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR)==PackageManager.PERMISSION_GRANTED
            &&context.checkSelfPermission(Manifest.permission.READ_CALENDAR)==PackageManager.PERMISSION_GRANTED)
            adapter.calendarPermission=true;

        else
        {
            int PermissionId = 200;
            Util.checkPermissions(context, PermissionId,
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR);
        }

        entryList.setOnGroupExpandListener(groupPosition ->
        {
            if(lastExpanded == -1)
                lastExpanded = groupPosition;
            else if(lastExpanded!=groupPosition)
            {
                entryList.collapseGroup(lastExpanded);
                lastExpanded = groupPosition;
            }
        });



        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], @NonNull int[] grantResults)
    {
        if(requestCode==200)
        {
            adapter.calendarPermission= grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void setReplaceText(TextView replaceText)
    {
        if (adapter.schedule_list == null || adapter.schedule_list.size() <= 0)
        {
            //schedule is empty
            replaceText.setVisibility(View.VISIBLE); //default is "GONE"
            String filling = context.getString(R.string.word_date);

            switch (method.getSelectedItemPosition())
            {
                case 0: break;
                case 1: filling = context.getString(R.string.word_week); break;
                case 2: filling = context.getString(R.string.word_month); break;
            }
            replaceText.setText(context.getString(R.string.schedule_empty, filling));
        }
        else
        {
            replaceText.setVisibility(View.GONE);
        }
    }
}
