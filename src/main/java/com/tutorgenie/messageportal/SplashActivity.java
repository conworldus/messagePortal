package com.tutorgenie.messageportal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class SplashActivity extends AppCompatActivity
{
    Context context  =this;
    public static MutableLiveData<String> status_mutable = new MutableLiveData<>();
    //Download DATA



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final ContentLoadingProgressBar bar = findViewById(R.id.splashProgress);
        bar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN);
        final TextView status = findViewById(R.id.progress_status);
        status_mutable.observe(this, new Observer<String>()
        {
            @Override
            public void onChanged(String s)
            {
                status.setText(s);
            }
        });

        bar.setMax(100);
        final CONNECTOR.progressRetriever retriever = new CONNECTOR.progressRetriever()
        {
            @Override
            public void writeProgress(int p)
            {
                bar.setProgress(p);
            }

            @Override
            public void writeCurrentState(String s)
            {

            }
        };
        Thread thread = new Thread()
        {
            public void run()
            {
                JSONObject queryData = new JSONObject();
                try
                {
                    GlobalData.DataCache.getInbox_messages().clear();
                    GlobalData.DataCache.getSent_messages().clear();
                    GlobalData.DataCache.getSchedule_map().clear();

                    status_mutable.postValue("Loading Inbox Messages");
                    //===Load Inbox Messages
                    queryData.put("username", getString(R.string.test_username));
                    queryData.put("password", getString(R.string.test_password));
                    queryData.put("query_type", "INBOX_MESSAGES");

                    CONNECTOR conn = new CONNECTOR();
                    conn.retriever = retriever;
                    JSONArray messageArray =
                            new JSONArray(conn.execute(queryData).get());
                    Log.e("Message", messageArray.toString());
                    JSONObject temp;
                    data_type_message m;
                    for (int i = 0; i < messageArray.length(); i++)
                    {
                        m = new data_type_message();
                        temp = messageArray.getJSONObject(i);
                        m.setDate(temp.getString("date"));
                        m.setMessage(temp.getString("message"));
                        m.setFrom(temp.getString("from_id"));
                        m.setTo(temp.getString("to_id"));
                        m.setMessageID(temp.getInt("message_id"));
                        m.setSubject(temp.getString("subject"));
                        m.setFrom_name(temp.getString("from_name"));
                        m.setTo_name(temp.getString("to_name"));
                        m.setTime(temp.getString("time"));
                        m.setLabel(temp.getString("label"));
                        int r = temp.getInt("is_read");
                        if (r == 0)
                            m.setRead(false);
                        else m.setRead(true);
                        GlobalData.DataCache.getInbox_messages().add(m);
                    }

                    status_mutable.postValue("Loading Sent Messages");
                    //===Load Sent Messages
                    queryData = new JSONObject();
                    queryData.put("username", getString(R.string.test_username));
                    queryData.put("password", getString(R.string.test_password));
                    queryData.put("query_type", "SENT_MESSAGES");
                    conn = new CONNECTOR();
                    conn.retriever = retriever;
                    messageArray =
                            new JSONArray(conn.execute(queryData).get());
                    Log.e("Executed", "Done");
                    for (int i = 0; i < messageArray.length(); i++)
                    {
                        m = new data_type_message();
                        temp = messageArray.getJSONObject(i);
                        m.setDate(temp.getString("date"));
                        m.setMessage(temp.getString("message"));
                        m.setFrom(temp.getString("from_id"));
                        m.setTo(temp.getString("to_id"));
                        m.setMessageID(temp.getInt("message_id"));
                        m.setSubject(temp.getString("subject"));
                        m.setFrom_name(temp.getString("from_name"));
                        m.setTo_name(temp.getString("to_name"));
                        m.setTime(temp.getString("time"));
                        m.setLabel(temp.getString("label"));
                        int r = temp.getInt("is_read");
                        if (r == 0)
                            m.setRead(false);
                        else m.setRead(true);
                        GlobalData.DataCache.getSent_messages().add(m);
                    }

                    Log.e("Mark", "Sent Done");

                    status_mutable.postValue("Loading Schedules");
                    //===Load Schedules
                    queryData = new JSONObject();
                    queryData.put("username", getString(R.string.test_username));
                    queryData.put("password", getString(R.string.test_password));
                    queryData.put("query_type", "TUTOR_SCHEDULE_COMPLETE");
                    conn = new CONNECTOR();
                    conn.retriever = retriever;
                    messageArray =
                            new JSONArray(conn.execute(queryData).get());
                    for (int i = 0; i < messageArray.length(); i++)
                    {
                        data_type_tutor_schedule_item s = new data_type_tutor_schedule_item();
                        temp = messageArray.getJSONObject(i); //not message, schedule here
                       /* String topLine = HELPER.timeToString(obj.getInt("schedule_hour")) + "-" +
                                HELPER.timeToString(HELPER.timeAddition(obj.getInt("schedule_hour"),
                                        obj.getInt("session_length")));*/
                        String status = temp.getString("schedule_status");
                        String duration = temp.getString("session_length");
                        String tutorname = temp.getString("schedule_owner_name");
                        String subject = temp.getString("subject");
                        String date = temp.getString("schedule_date");
                        String location = temp.getString("location");
                        String studentNote = temp.getString("student_note");
                        s.setStatus(status);
                        s.setStudentNote(studentNote);
                        s.setLocation(location);
                        s.setHourInt(temp.getInt("schedule_hour"));
                        s.setDateInt(Integer.parseInt(date.replace("-", "")));
                        s.setDateStr(date);
                        s.setSubject(subject);
                        s.setDuration(duration);
                        s.setTutorname(tutorname);
                        s.setScheduleID(temp.getInt("schedule_entry_id"));
                        String student_id = temp.getString("student_id");
                        if(!GlobalData.DataCache.getSchedule_map().containsKey(date))
                            GlobalData.DataCache.getSchedule_map().put(date,
                                    new ArrayList<data_type_tutor_schedule_item>());
                        GlobalData.DataCache.getSchedule_map().get(date).add(s);
                    }
                }catch (JSONException e)
                {
                    Log.e("JSON Error", e.getMessage());
                    e.printStackTrace();
                }
                catch (InterruptedException e)
                {
                    Log.e("Interrupt", e.getMessage());
                }
                catch (ExecutionException e)
                {
                    Log.e("Execution", e.getMessage());
                }finally
                {
                    Log.e("Inbox Size", GlobalData.DataCache.getInbox_messages().size()+"");
                    Log.e("Sent Size", GlobalData.DataCache.getSent_messages().size()+"");
                    Log.e("Schedule Size", GlobalData.DataCache.getSchedule_map().size()+"");
                }

                startActivity(new Intent(context, MainActivity.class));
                finish();
            }
        };
        thread.start();
    }
}
