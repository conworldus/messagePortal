package com.tutorgenie.messageportal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.tutorgenie.messageportal.CONST.QUERY_TYPE;
import static com.tutorgenie.messageportal.CONST.SUCCESS;

public class adapter_contact extends ArrayAdapter<data_type_student>
{
    private ArrayList<data_type_student> data;
    private Context context;
    private int ResourcePt;
    adapter_contact(@NonNull Context context, int resource, @NonNull ArrayList<data_type_student> objects)
    {
        super(context, resource, objects);
        this.context = context;
        this.ResourcePt = resource;
        this.data = objects;
    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent)
    {
        data_type_student Student = data.get(position);
        View view =convertView;
        if(view==null)
            view= LayoutInflater.from(context).inflate(ResourcePt, null);
        CircleImageView profileImg = view.findViewById(R.id.thumb_image);
        TextView name = view.findViewById(R.id.directory_name),
                email = view.findViewById(R.id.directory_email);
        ImageButton sendmail = view.findViewById(R.id.directory_send_mail),
                delete = view.findViewById(R.id.directory_delete);
        sendmail.setOnClickListener(new compose_listener(null, null, context, Student.getUsername()));
        delete.setOnClickListener(v->
        {
            try
            {
                JSONObject sendObj = Util.connectionObject(context);
                sendObj.put(QUERY_TYPE, "DELETE_CONTACT");
                sendObj.put("owned_id", String.valueOf(Student.getUsername()));
                String response = (new CONNECTOR()).execute(sendObj).get();
                if(response.trim().equals(SUCCESS))
                {
                    GlobalData.DataCache.getContact_list().remove(Student);
                    notifyDataSetChanged();
                }
            } catch (InterruptedException | ExecutionException | JSONException e)
            {
                e.printStackTrace();
            }
        });
        name.setText(Student.getFirstname()+" "+Student.getLastname());
        email.setText(Student.getUsername());
        if(Student.getThumb()==null)
            profileImg.setImageDrawable(context.getDrawable(R.drawable.ic_person_black_24dp));
        else profileImg.setImageBitmap(Student.getThumb());
        return view;
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
    public data_type_student getItem(int position)
    {
        return data.get(position);
    }

}
