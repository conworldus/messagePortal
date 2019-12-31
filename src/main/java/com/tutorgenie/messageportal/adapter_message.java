package com.tutorgenie.messageportal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class adapter_message extends ArrayAdapter<data_type_message>
{
    private ArrayList<data_type_message> data;
    private ArrayList<data_type_message> delete_list;
    private int ResourcePoint;
    private Context context;
    private fragment_mailview.mailType type;

    public void setType(fragment_mailview.mailType type)
    {
        this.type = type;
    }

    public adapter_message(Context context, int ResourcePoint, ArrayList<data_type_message> data)
    {
        super(context, ResourcePoint, data);
        this.data = data;
        this.ResourcePoint = ResourcePoint;
        this.context = context;

        //set all the get full date
        for(data_type_message m : data)
        {
            m.setFull_date(m.getDate(), m.getTime());
        }
        sortData();
    }

    private void deleteMessage()
    {

    }

    private void sortData()
    {
        Collections.sort(data, new Comparator<data_type_message>()
        {
            @Override
            public int compare(data_type_message o1, data_type_message o2)
            {
                if(o1.getFull_date().getTime()<o2.getFull_date().getTime())
                    return 1;
                else if(o1.getFull_date().getTime()>o2.getFull_date().getTime())
                    return -1;
                else return 0;
                //return o1.getFull_date().getTime()>o2.getFull_date().getTime();
            }
        });
    }

    @Override
    public int getCount()
    {
        if(data!=null)
            return data.size();
        else return 0;
    }

    @Override
    public int getViewTypeCount()
    {
        if(getCount()==0)
            return 1;
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public data_type_message getItem(int position)
    {
        return data.get(position);
    }


    @SuppressLint("SimpleDateFormat")
    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent)
    {
        View view = convertView;
        TextView from, date_time, subject, time;
        final ImageView img;
        if(view==null)
        {
            view = LayoutInflater.from(context).inflate(ResourcePoint, null);
        }
        img=view.findViewById(R.id.mark);
        from=view.findViewById(R.id.from);
        date_time = view.findViewById(R.id.date_time);
        subject = view.findViewById(R.id.subject);
        time = view.findViewById(R.id.time);
        final data_type_message m = data.get(position);

        if(m.getLabel().equals("Marked"))
            img.setColorFilter(context.getColor(R.color.yellow_mild));
        else
            img.setColorFilter(Color.GRAY);

        img.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(m.getLabel().equals("Inbox"))
                {
                    m.setLabel("Marked");
                    img.setColorFilter(context.getColor(R.color.yellow_mild));
                    //send a code to online
                    updateLabel(m.getMessageID(), "Marked", context);
                }
                else
                {
                    m.setLabel("Inbox");
                    img.setColorFilter(Color.GRAY);
                    //send a code to online db
                    updateLabel(m.getMessageID(), "Inbox", context);
                }
            }
        });

        if(type== fragment_mailview.mailType.INBOX)
            from.setText(m.getFrom_name());
        else from.setText(m.getTo_name());
        //m.setFull_date(m.getDate(), m.getTime());
        DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        date_time.setText(format.format(m.getFull_date()));
        format = new SimpleDateFormat("HH:mm:ss");
        time.setText(format.format(m.getFull_date()));
        subject.setText(m.getSubject());
        return view;
    }

    public static void updateLabel(int ID, String status, Context tContext)
    {
        try
        {
            JSONObject obj = new JSONObject();
            obj.put("query_type", "UPDATE_LABEL");
            obj.put("message_id", String.valueOf(ID));
            obj.put("label", status);
            obj.put("username", tContext.getString(R.string.test_username));
            obj.put("password", tContext.getString(R.string.test_password));
            CONNECTOR conn = new CONNECTOR();
            String result = conn.execute(obj).get();
            Log.e("Result", result);
        }catch (JSONException e)
        {
            Log.e(e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
        }catch (ExecutionException e)
        {
            e.getLocalizedMessage();
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
