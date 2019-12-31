package com.tutorgenie.messageportal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class SplashActivity extends AppCompatActivity
{
    File logininfo;
    public static MutableLiveData<String> status_mutable = new MutableLiveData<>();
    Context context = this;
    ContentLoadingProgressBar bar;
    final CONNECTOR.progressRetriever retriever = p ->
    {
        Log.e("Setting", p+"");
        bar.setProgress(p);
    };
    //Download DATA
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        logininfo = new File(getExternalFilesDir(null)+getString(R.string.file_path));
        if(!logininfo.exists())
            setLogin();

        else
        {
            //read from the login file
            //if login fails, setLogin
            //otherwise load Global  Static value and data
            try
            {
                InputStream in = new FileInputStream(logininfo);
                byte [] b = new byte[in.available()]; //should be accurate since file is small
                int readResult = in.read(b);
                if(readResult<=0)
                    setLogin();
                else
                {
                    String [] lp = (new String(b)).split("\n");
                    if(lp.length!=2)
                    {

                        Log.e("ERROR", "File is corrput");
                        setLogin();
                    }
                    else 
                    {
                        if(!(loginAttempt(lp[0], lp[1])))
                        {
                            Log.e("ERROR", "Incorrect login in file");
                            setLogin();
                        }
                        else
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage(getString(R.string.Login_Prompt, lp[0]));
                            builder.setPositiveButton(R.string.confirm, (dialog, which) ->
                            {
                                GlobalData.username = lp[0];
                                GlobalData.password = lp[1];
                                dialog.dismiss();
                                loadUserdata();
                            });
                            builder.setNegativeButton(R.string.cancel, (dialog, which) ->
                            {
                                dialog.dismiss();
                                setLogin();
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private boolean loginAttempt(String username, String password)
    {
        try
        {
            JSONObject obj = new JSONObject();
            obj.put("username", username);
            obj.put("password", password);
            obj.put("query_type", "VALIDATE");
            String ret = (new CONNECTOR()).execute(obj).get();
            if(ret.trim().equals(CONST.OK))
                return true;
            else return false;
        }
         catch (JSONException | InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private void setLogin()
    {
        setContentView(R.layout.login_activity);

        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        Button login  = findViewById(R.id.login), register = findViewById(R.id.register);
        CheckBox autologin = findViewById(R.id.autologinCheck);

        register.setOnClickListener(v->
        {
            String url = getString(R.string.register_url);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        login.setOnClickListener(v ->
        {
            String u=username.getText().toString(), p= password.getText().toString();
            if(loginAttempt(u, p))
            {
                if (autologin.isChecked())
                {
                    String collect = u + "\n" + p;
                    byte[] writedata = collect.getBytes();
                    try
                    {
                        boolean c = logininfo.createNewFile();
                        if (!c)
                            Log.e("Error", "Failed to create new file");
                        OutputStream os = new FileOutputStream(logininfo);
                        Log.e("Writing to: ", logininfo.getAbsolutePath());
                        os.write(writedata);
                    }catch (IOException e)
                    {
                        Log.e("I/O Error", e.getLocalizedMessage());
                        //this simply means the file writing failed, but the application can
                        // continue
                    }
                }
                GlobalData.username = u;
                GlobalData.password = p;
                loadUserdata();
            }
        });
    }

    private void loadUserdata()
    {
        {
            //read from file
            byte [] loginbytes = new byte[(int)logininfo.length()];
            try
            {
                FileInputStream stream = new FileInputStream(logininfo);
                int r = stream.read(loginbytes, 0, (int)logininfo.length());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            setContentView(R.layout.activity_splash);
            bar = findViewById(R.id.splashProgress);
            bar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary),
                    android.graphics.PorterDuff.Mode.SRC_IN);
            final TextView status = findViewById(R.id.progress_status);
            status_mutable.observe(this, status::setText);
            bar.setMax(100);
            Thread thread = new Thread()
            {
                public void run()
                {

                    try
                    {
                        GlobalData.DataCache.getInbox_messages().clear();
                        GlobalData.DataCache.getSent_messages().clear();
                        GlobalData.DataCache.getSchedule_map().clear();
                        GlobalData.DataCache.getProfileData().clear();
                        CONNECTOR conn = new CONNECTOR();
                        conn.retriever = new CONNECTOR.progressRetriever()
                        {
                            @Override
                            public void writeProgress(int p)
                            {
                                bar.setProgress(p);
                            }
                        };

                        JSONArray messageArray = null;
                        JSONObject temp;
                        JSONObject queryData;

                        status_mutable.postValue("Downloading Login Information");
                        queryData = new JSONObject();
                        queryData.put("username", GlobalData.username);
                        queryData.put("password", GlobalData.password);
                        queryData.put("query_type", "LOGIN_INFO");

                        JSONObject login_info = new JSONObject(conn.execute(queryData).get());
                        Log.e("Login: ", login_info.toString());

                        Iterator<String> pkeys = login_info.keys();
                        while(pkeys.hasNext())
                        {
                            String key = pkeys.next();
                            GlobalData.DataCache.getLogin_info().put(key, login_info.getString(key));
                            //load the entire profile into a map
                        }

                        Log.e("login size", GlobalData.DataCache.getLogin_info().size()+"");

                        status_mutable.postValue("Downloading profile picture");
                        queryData = new JSONObject();
                        queryData.put("username", GlobalData.username);
                        queryData.put("password", GlobalData.password);
                        queryData.put("query_type", "_FILE_REQUEST");

                        CONNECTOR_DOWNLOAD down = new CONNECTOR_DOWNLOAD();
                        down.retriever = retriever;
                        String result = down.execute(queryData).get();
                        Log.e("Result", result);


                        status_mutable.postValue("Loading Inbox Messages");
                        //===Load Inbox Messages
                        data_type_message m;

                        conn = new CONNECTOR();
                        conn.setRetriever(retriever);


                        if(conn.retriever!=null)
                            Log.i("RETRIEVER", "NOT NULL");

                        queryData = new JSONObject();
                        queryData.put("username", GlobalData.username);
                        queryData.put("password", GlobalData.password);
                        queryData.put("query_type", "INBOX_MESSAGES");

                        String resp = conn.execute(queryData).get();

                        if(conn.retriever!=null)
                            Log.i("RETRIEVER", "Still NOT NULL");

                        if(!resp.trim().equals(CONST.NO_MESSAGE))
                        {
                            messageArray =
                                    new JSONArray(resp);
                            Log.e("Message", messageArray.toString());

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
                        }

                        status_mutable.postValue("Loading Sent Messages");
                        conn = new CONNECTOR();
                        conn.retriever = retriever;

                        //===Load Sent Messages
                        queryData = new JSONObject();
                        queryData.put("username", GlobalData.username);
                        queryData.put("password", GlobalData.password);
                        queryData.put("query_type", "SENT_MESSAGES");
                        conn = new CONNECTOR();
                        resp = conn.execute(queryData).get();

                        if(!resp.trim().equals(CONST.NO_MESSAGE))
                        {
                            messageArray =
                                    new JSONArray(resp);
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
                        }

                        Log.e("Mark", "Sent Done");

                        status_mutable.postValue("Loading Schedules");
                        conn = new CONNECTOR();
                        conn.retriever = retriever;

                        //===Load Schedules
                        queryData = new JSONObject();
                        queryData.put("username", GlobalData.username);
                        queryData.put("password", GlobalData.password);
                        queryData.put("query_type", "TUTOR_SCHEDULE_COMPLETE");
                        conn = new CONNECTOR();

                        resp = conn.execute(queryData).get();
                        if(!resp.trim().equals(CONST.NO_MESSAGE))
                        {
                            messageArray =
                                    new JSONArray(resp);
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
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
                                Date numericDate = dateFormat.parse(date);
                                String location = temp.getString("location");
                                String studentNote = temp.getString("student_note");
                                String studentName = temp.getString("student_name");
                                String tutorNote = temp.getString("tutor_note");
                                double lat = temp.getDouble("latitude");
                                double lng = temp.getDouble("longitude");
                                String comment = temp.getString("comment_content");

                                String timeStamp;
                                {
                                    int start = temp.getInt("schedule_hour");
                                    int length = temp.getInt("session_length");
                                    int minute = start % 100; //this is minute marker
                                    int hour = (start - minute) / 100;  // this the hour marker
                                    //start hour string = hour+":"+minute
                                    int terminal = (hour * 60 + minute + length) % 1440; //this so it does not
                                    // exceed the date end
                                    int terminalMin = terminal % 60;
                                    int terminalHour = (terminal - terminalMin) / 60;

                                    timeStamp = hour + ":" + minute + " - " + terminalHour + ":" + terminalMin;
                                    s.setTimeStamp(timeStamp);
                                }

                                s.setStudentname(studentName);
                                s.setStatus(status);
                                s.setComment(comment);
                                s.setStudentNote(studentNote);
                                s.setLocation(location);
                                s.setHourInt(temp.getInt("schedule_hour"));
                                s.setDateInt(Integer.parseInt(date.replace("-", "")));
                                s.setDateStr(date);
                                s.setSubject(subject);
                                s.setDuration(duration);
                                s.setTutorname(tutorname);
                                s.setTutorNote(tutorNote);
                                s.setLat(lat);
                                s.setLng(lng);
                                s.setScheduleID(temp.getInt("schedule_entry_id"));
                                String student_id = temp.getString("student_id");
                                if (!GlobalData.DataCache.getSchedule_map().containsKey(date))
                                    GlobalData.DataCache.getSchedule_map().put(date,
                                            new ArrayList<data_type_tutor_schedule_item>());
                                Objects.requireNonNull(GlobalData.DataCache.getSchedule_map().get(date)).add(s);


                                if (!GlobalData.DataCache.getSchedule_map_date().containsKey(numericDate.getTime()))
                                    GlobalData.DataCache.getSchedule_map_date().put(numericDate.getTime(),
                                            new ArrayList<>());
                                Objects.requireNonNull(GlobalData.DataCache.getSchedule_map_date().get(numericDate.getTime())).add(s);

                                //finally
                                if(s.getStatus().equals(CONST.status_pending))
                                    GlobalData.DataCache.getPendingList().add(s);
                            }
                        }

                        status_mutable.postValue("Loading Profile");
                        conn = new CONNECTOR();
                        conn.retriever = retriever;

                        //===Load Schedules
                        queryData = new JSONObject();
                        queryData.put("username", GlobalData.username);
                        queryData.put("password", GlobalData.password);
                        queryData.put("query_type", "TUTOR_PROFILE");
                        conn = new CONNECTOR();
                        messageArray =
                                new JSONArray(conn.execute(queryData).get());

                        temp = messageArray.getJSONObject(0); //there should be only one
                        //now load profile
                        Iterator<String> keys = temp.keys();
                        while(keys.hasNext())
                        {
                            String key = keys.next();
                            profile_entry entry = new profile_entry();
                            entry.setTitle(key);
                            entry.setContent(temp.get(key));
                            switch(key)
                            {
                                case "date_of_birth": entry.setType(profile_entry.ENTRY_TYPE.DATE); break;
                                case "gender":
                                case "nationality":
                                case "native_language":
                                case "second_language":
                                case "education_level":
                                    entry.setType(profile_entry.ENTRY_TYPE.MULTISELECT); break;
                                case "service_radius":
                                case "phone_number":
                                case "price_min":
                                    entry.setType(profile_entry.ENTRY_TYPE.CURRENCY); break;

                                default: break;
                            }
                            GlobalData.DataCache.getProfileData().add(entry);
                        }

                        status_mutable.postValue("Loading System Data");
                        conn = new CONNECTOR();
                        conn.retriever = retriever;

                        //===Load Schedules
                        queryData = new JSONObject();
                        queryData.put("username", GlobalData.username);
                        queryData.put("password", GlobalData.password);
                        queryData.put("query_type", "SUBJECT_LIST");
                        conn = new CONNECTOR();
                        messageArray =
                                new JSONArray(conn.execute(queryData).get());

                        for(int i=0; i<messageArray.length(); i++)
                        {
                            GlobalData.DataCache.getComplete_subject_list().add(messageArray.getString(i));
                        }

                        queryData = new JSONObject();
                        queryData.put("username", GlobalData.username);
                        queryData.put("password", GlobalData.password);
                        queryData.put("query_type", "NATIONALITY");
                        conn = new CONNECTOR();
                        messageArray =
                                new JSONArray(conn.execute(queryData).get());

                        for(int i=0; i<messageArray.length(); i++)
                        {
                            JSONObject obj = messageArray.getJSONObject(i);
                            GlobalData.DataCache.getNationality_map().put(obj.getString(
                                    "full_nationality"), obj.getString("two_letter"));

                            GlobalData.DataCache.getNationality_list().add(obj.getString(
                                    "full_nationality"));
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
                    }
                    catch (ParseException e)
                    {
                        Log.e("Parse error", e.getMessage());
                    }

                    finally
                    {
                        Log.e("Inbox Size", GlobalData.DataCache.getInbox_messages().size()+"");
                        Log.e("Sent Size", GlobalData.DataCache.getSent_messages().size()+"");
                        Log.e("Schedule Size", GlobalData.DataCache.getSchedule_map().size()+"");
                        Log.e("Profile Size", GlobalData.DataCache.getProfileData().size()+"");
                        Log.e("Nationality Size", GlobalData.DataCache.getNationality_map().size()+"");
                    }

                    startActivity(new Intent(context, MainActivity.class));
                    finish();
                }
            };
            thread.start();

        }
    }
}
