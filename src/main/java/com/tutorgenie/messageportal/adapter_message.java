package com.tutorgenie.messageportal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class adapter_message extends ArrayAdapter<data_type_message>
{
    private ArrayList<data_type_message> data;
    private int ResourcePoint;
    private Context context;
    public adapter_message(Context context, int ResourcePoint, ArrayList<data_type_message> data)
    {
        super(context, ResourcePoint, data);
        this.data = data;
        this.ResourcePoint = ResourcePoint;
        this.context = context;
    }

    @Override
    public int getCount()
    {
        if(data!=null)
            return data.size();
        else return 0;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent)
    {
        View view = convertView;
        TextView from, date_time, subject;
        if(view==null)
        {
            view = LayoutInflater.from(context).inflate(ResourcePoint, null);
        }

        from=view.findViewById(R.id.from);
        date_time = view.findViewById(R.id.date_time);
        subject = view.findViewById(R.id.subject);
    }

}
