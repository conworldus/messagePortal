package com.tutorgenie.messageportal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class fragment_mailview extends Fragment
{
    private ArrayList<data_type_message> data;
    private ArrayList<data_type_message> delete_list = new ArrayList<>();
    private ArrayList<Integer> delete_position = new ArrayList<>();
    private Context context;
    private Button deleteButton;
    private adapter_message adapter;
    private ListView mailList;

    fragment_mailview(ArrayList<data_type_message> data, Context context, Button deleteButton)
    {
        this.data = data;
        this.context = context;
        this.deleteButton = deleteButton;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_mailview, container, false);
        mailList = view.findViewById(R.id.messageList);
        adapter = new adapter_message(context, R.layout.adapter_message, data);
        mailList.setAdapter(adapter);
        mailList.setOnItemClickListener(onItemClickListener);
        mailList.setOnItemLongClickListener(onItemLongClickListener);

        deleteButton.setOnClickListener(deleteListener);
        return view;
    }

    private View.OnClickListener deleteListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        if (delete_list.size() > 0)
        {
            delete_list.clear();
            deleteButton.setVisibility(View.INVISIBLE);
            for (int p : delete_position)
            {
                View rowView = mailList.getChildAt(p);
                if (rowView != null)
                {
                    rowView.setBackgroundColor(Color.WHITE);
                }
            }
        }
    }

    //Listener
    private AdapterView.OnItemClickListener onItemClickListener =
            new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    AlertDialog dialog = (new AlertDialog.Builder(context)).create();
                    dialog.setMessage("Please confirm to delete");
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            deleteMessages();
                        }
                    });

                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
                }
            };

    private AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            if (delete_list.contains(data.get(position)))
            {
                delete_list.remove(data.get(position));
                delete_position.remove(position);
                view.setBackgroundColor(Color.WHITE);
                if (delete_list.size() == 0)
                {
                    deleteButton.setVisibility(View.INVISIBLE);
                }
            } else
            {
                delete_list.add(data.get(position));
                delete_position.add(position);
                view.setBackgroundColor(Color.GRAY);
                deleteButton.setVisibility(View.VISIBLE);
            }
            return false;
        }
    };

    private void deleteMessages()
    {
        if (delete_list.size() == 0)
            return;
        JSONArray array = new JSONArray();
        for (data_type_message m : delete_list)
        {
            array.put(m.getMessageID());
        }
        JSONObject obj = new JSONObject();
        try
        {
            obj.put("query_type", "DELETE_MESSAGE");
            obj.put("data", array);
            CONNECTOR connector = new CONNECTOR();
            String result = connector.execute(obj).get();
            Log.e("Result", result);
        } catch (JSONException e)
        {
            e.printStackTrace();
            return;
        } catch (Exception e)
        {
            Log.e(e.getClass().getSimpleName(), e.getMessage());
        }

    }
    public int getDeleteListSize()
    {
        return delete_list.size();
    }
}
