package com.tutorgenie.messageportal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class fragment_directory extends Fragment
{
    private ArrayList<data_type_student> data;
    private Context context;
    private adapter_contact adapter;
    private ListView contactList;
    private TextView replaceText;
    private FloatingActionButton addContact;

    private void setReplaceText()
    {
        if(data.size()<=0)
            replaceText.setVisibility(View.VISIBLE);
        else replaceText.setVisibility(View.INVISIBLE);
    }

    public adapter_contact getAdapter()
    {
        return adapter;
    }



    fragment_directory(ArrayList<data_type_student> data, Context context)
    {
        this.data = data;
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_directory, container, false);
        contactList = view.findViewById(R.id.directory_list);
        replaceText = view.findViewById(R.id.directory_replace_text);
        addContact = view.findViewById(R.id.directory_add);
        adapter = new adapter_contact(context, R.layout.adapter_contact, data);
        contactList.setAdapter(adapter);

        addContact.setOnClickListener(addListner);
        contactList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                Log.w("Height", Util.getActionBarHeight(context)+"");
                int remainHeight = Util.getScreenHeight((AppCompatActivity)context)-
                        Util.getRemainingScreenHeight(context)-Util.getActionBarHeight(context);
                ConstraintLayout.LayoutParams params =
                        (ConstraintLayout.LayoutParams) contactList.getLayoutParams();
                params.height = remainHeight;
                contactList.setLayoutParams(params);
                contactList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        setReplaceText();
        return view;
    }

    @SuppressLint("InflateParams")
    private View.OnClickListener addListner = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_addcontact, null);
            EditText emailAdd = view.findViewById(R.id.add_contact_email);
            TextView errMessage = view.findViewById(R.id.add_contact_error_message);
            Button add = view.findViewById(R.id.add_contact_btn_add);
            Button cancel = view.findViewById(R.id.add_contact_btn_cancel);
            builder.setView(view);
            AlertDialog dialog  = builder.create();
            cancel.setOnClickListener(v1 ->
            {
                dialog.dismiss();
            });

            add.setOnClickListener(v2->
            {
                if(emailAdd.getEditableText().toString().trim().length()>5)  //email can't be shorter than 5
                {
                    String newEmail = emailAdd.getEditableText().toString().trim();
                    JSONObject sendObj;
                    try
                    {
                        sendObj = Util.connectionObject(context);
                        sendObj.put("query_type", "GET_STUDENT_INFO");
                        sendObj.put("email", newEmail);
                        String response = (new CONNECTOR()).execute(sendObj).get();
                        if(response.trim().equals(CONST.NOT_FOUND))
                        {
                            errMessage.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            JSONObject obj = (new JSONArray(response)).getJSONObject(0);
                            Util.addToContactCache(obj);
                            GlobalData.DataCache.SortContactList();
                            adapter.notifyDataSetChanged();
                            if(replaceText.getVisibility()==View.VISIBLE)
                                replaceText.setVisibility(View.INVISIBLE);
                            dialog.dismiss();
                            Util.addToContactOnline(context, newEmail);
                        }
                    } catch (JSONException | InterruptedException | ExecutionException e)
                    {
                        e.printStackTrace();
                    }
                }
            });

            dialog.show();
        }
    };
}
