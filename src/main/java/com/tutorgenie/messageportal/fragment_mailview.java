package com.tutorgenie.messageportal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class fragment_mailview extends Fragment
{
    private ArrayList<data_type_message> data;
    private ArrayList<data_type_message> delete_list = new ArrayList<>();
    private ArrayList<Integer> delete_position = new ArrayList<>();
    private Context context;
   // private Button deleteButton;
    private adapter_message adapter;
    private View view;
    private ListView mailList;

    void updateMailList()
    {
        if(adapter!=null)
        {
            mailList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }


    fragment_mailview(ArrayList<data_type_message> data, Context context)
    {
        this.data = data;
        this.context = context;
    //    this.deleteButton = deleteButton;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_mailview, container, false);
        mailList = view.findViewById(R.id.messageList);
        adapter = new adapter_message(context, R.layout.adapter_message, data);
        mailList.setAdapter(adapter);
        mailList.setOnItemClickListener(onItemClickListener);
        mailList.setOnItemLongClickListener(onItemLongClickListener);
      //  deleteButton.setOnClickListener(deleteListener);
        setButtonBarFunctions();
        mailList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                int height = mailList.getHeight();
                int actionbarHeight = Util.getActionBarHeight(context);
                int finalHeight = height - actionbarHeight - 80;
                ConstraintLayout.LayoutParams params =
                        (ConstraintLayout.LayoutParams)mailList.getLayoutParams();
                params.height = finalHeight;
                //mailList.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout
                // .LayoutParams.MATCH_PARENT, finalHeight));

                mailList.setLayoutParams(params);
                mailList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        return view;
    }

    private View.OnClickListener deleteListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

        }
    };

    private void setButtonBarFunctions()
    {
        ImageButton refreshBtn = view.findViewById(R.id.mailRefresh);
        ImageButton deleteBtn = view.findViewById(R.id.mailDelete);
        ImageButton composeBtn = view.findViewById(R.id.mailCompose);
        ImageButton shareBtn = view.findViewById(R.id.mailShare);

        refreshBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //contact the server for updated list, then refresh
                mailList.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });

        deleteBtn.setOnClickListener(v ->
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(getString(R.string.confirm_delete));
            builder.setPositiveButton(R.string.confirm, (dialog, which) ->
            {
                try
                {
                    JSONArray listData = new JSONArray();
                    for(data_type_message m: delete_list)
                    {
                        listData.put(m.getMessageID());
                    }
                    JSONObject sentData = Util.connectionObject(context);
                    sentData.put(CONST.query_type, "DELETE_MESSAGE");
                    sentData.put(CONST.data, listData);

                    GlobalData.connector = new CONNECTOR();
                    String result = GlobalData.connector.execute(sentData).get();
                    Log.d("Result", result);
                    if(result.trim().equals(CONST.SUCCESS))
                    {
                        //clear the list view of all background changes
                        GlobalData.DataCache.getSent_messages().removeAll(delete_list);
                        GlobalData.DataCache.getInbox_messages().removeAll(delete_list);
                        delete_list.clear();
                        adapter.notifyDataSetChanged();
                        for(Integer p:delete_position)
                        {
                            if(p<mailList.getChildCount())
                                mailList.getChildAt(p).setBackgroundColor(Color.TRANSPARENT);
                        }
                        delete_position.clear();
                        //clear all background

                    }
                } catch (JSONException|ExecutionException|InterruptedException e)
                {
                    e.printStackTrace();
                }
            });

            builder.setNegativeButton(R.string.cancel, (dialog, which) ->
            {

            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        composeBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //pop up app list to be shared
                //wechat, whatsapp, gmail
            }
        });
    }


    @Override
    public void onResume()
    {
        super.onResume();
        delete_list.clear();
      //  deleteButton.setVisibility(View.INVISIBLE);
    }

    //Listener
    private AdapterView.OnItemClickListener onItemClickListener =
            new AdapterView.OnItemClickListener()
            {
                @SuppressLint("SetTextI18n")
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
                {
                    final data_type_message iMessage = data.get(position);

                    final ImageView listItemMark = view.findViewById(R.id.mark);

                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(context, R.style.BottomOptionsDialogTheme);
                    View mailview = getLayoutInflater().inflate(R.layout.dialog_single_mail, null);
                    builder.setView(mailview);

                    TextView from_brief = mailview.findViewById(R.id.single_mail_sender_brief);
                    TextView from = mailview.findViewById(R.id.from_content);
                    TextView to = mailview.findViewById(R.id.to_content);
                    TextView time = mailview.findViewById(R.id.time_content);
                    final TextView subject = mailview.findViewById(R.id.single_mail_subject);
                    TextView content = mailview.findViewById(R.id.single_mail_body);
                    ImageButton close = mailview.findViewById(R.id.single_mail_cancel);
                    final ImageButton reply = mailview.findViewById(R.id.single_mail_reply),
                            delete = mailview.findViewById(R.id.single_mail_delete),
                            mark = mailview.findViewById(R.id.single_mail_mark);

                    TextView view_detail = mailview.findViewById(R.id.single_mail_view_detail);
                    final TableLayout detail_table = mailview.findViewById(R.id.single_mail_detail_panel);
                    final ConstraintLayout brief_table =
                            mailview.findViewById(R.id.single_mail_brief_panel);
                    TextView hide_detail = mailview.findViewById(R.id.single_mail_hide_detail);

                    view_detail.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            detail_table.setVisibility(View.VISIBLE);
                            brief_table.setVisibility(View.GONE);
                        }
                    });

                    hide_detail.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            detail_table.setVisibility(View.GONE);
                            brief_table.setVisibility(View.VISIBLE);
                        }
                    });


                    from_brief.setText(iMessage.getFrom_name());
                    from.setText(iMessage.getFrom());
                    to.setText(iMessage.getTo());
                    time.setText(iMessage.getDate()+" "+iMessage.getTime());
                    subject.setText(iMessage.getSubject());
                    content.setText(iMessage.getMessage());

                    if(iMessage.getLabel().equals(CONST.label_marked))
                        mark.setColorFilter(Color.YELLOW);

                    final AlertDialog dialog = builder.create();
                    close.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            dialog.dismiss();
                        }
                    });

                    reply.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            dialog.dismiss();
                            //composeDialog().show();
                            View view =
                                    LayoutInflater.from(context).inflate(R.layout.dialog_mail_compose_revised, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(context,
                                    R.style.BottomOptionsDialogTheme);
                            //full screen dialog
                            builder.setView(view);

                            final AlertDialog cdialog = builder.create();
                            ImageButton send = view.findViewById(R.id.compose_send_button),
                                    cancel = view.findViewById(R.id.compose_cancel_button);
                            TextView title = view.findViewById(R.id.compose_title);
                            title.setText(context.getString(R.string.word_reply));
                            final EditText header = view.findViewById(R.id.compose_dialog_header);
                            TextView toField = view.findViewById(R.id.compose_dialog_to_field);
                            {
                                //set up the header information
                                header.setText(iMessage.getSubject());

                                String to_name = iMessage.getFrom_name();
                                if(iMessage.getFrom().equals(GlobalData.DataCache.getLogin_info().get(
                                        "username")))
                                {
                                    to_name = iMessage.getTo_name();
                                }

                                toField.setText(context.getString(R.string.to_prefix_fill,
                                        to_name));
                            }
                            final EditText content = view.findViewById(R.id.compose_dialog_text);
                            cancel.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    cdialog.dismiss();
                                }
                            });
                            send.setOnClickListener(new View.OnClickListener()
                            {
                                @SuppressLint("SimpleDateFormat")
                                @Override
                                public void onClick(View v)
                                {
                                    CONNECTOR conn = new CONNECTOR();
                                    try
                                    {
                                        String self = GlobalData.DataCache.getLogin_info().get(
                                                "username");

                                        Log.e("Login Retrieved", self);
                                        String to_id;
                                        String to_name, from_name;
                                        if(iMessage.getFrom().equals(self))
                                        {
                                            to_id = iMessage.getTo();
                                            to_name = iMessage.getTo_name();
                                            from_name = iMessage.getFrom_name();
                                        }
                                        else
                                        {
                                            to_id = iMessage.getFrom();
                                            to_name = iMessage.getFrom_name();
                                            from_name = iMessage.getTo_name();
                                        }

                                        JSONObject obj = Util.connectionObject(context);
                                        obj.put("query_type", "SEND_MESSAGE_TUTOR");
                                        obj.put("from_id", self);
                                        obj.put("to_id", to_id);
                                                GlobalData.DataCache.getLogin_info().get(
                                                        "username");
                                        obj.put("subject", header.getText().toString());
                                        obj.put("message", content.getText().toString());

                                        String result = conn.execute(obj).get();

                                        //now add the message to Data
                                        data_type_message m = new data_type_message();
                                        m.setLabel(CONST.label_inbox);
                                        Date cur = Calendar.getInstance().getTime();
                                        m.setDate((new SimpleDateFormat("yyyy-MM-dd")).format(cur)); //this sets the date
                                        m.setTime((new SimpleDateFormat("HH:mm:ss")).format(cur));
                                        m.setFrom(self);
                                        m.setTo(to_id);
                                        m.setFrom_name(from_name);
                                        m.setTo_name(to_name);
                                        m.setMessageID(Integer.parseInt(result.trim()));
                                        m.setRead(false);
                                        m.setSubject(header.getText().toString());
                                        m.setMessage(content.getText().toString());
                                        m.setToDelete(false);
                                        Log.e("Size Pre",
                                                GlobalData.DataCache.getSent_messages().size()+"");
                                        GlobalData.DataCache.getSent_messages().add(0, m);
                                        Log.e("Size Post",
                                                GlobalData.DataCache.getSent_messages().size()+"");

                                        Intent i = new Intent();
                                        i.putExtra("Update", 1);
                                        i.setAction(CONST.mailUpdateAction);
                                        Objects.requireNonNull(getActivity()).sendBroadcast(i);
                                        Log.e("Broadcast", "Sent");
                                        cdialog.dismiss();


                                    }catch (JSONException| ExecutionException|InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            cdialog.show();
                        }
                    });

                    delete.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            try
                            {
                                JSONObject obj = Util.connectionObject(context);
                                obj.put("query_type", "DELETE_MESSAGE");
                                //construct the data part
                                JSONArray deleteList = new JSONArray();
                                deleteList.put(iMessage.getMessageID());
                                obj.put("data", deleteList);
                                CONNECTOR conn = new CONNECTOR();
                                String result = conn.execute(obj).get();
                                Log.e("DeleteResult", result);

                                //clean data list, delete list
                                data.remove(iMessage);
                                delete_list.remove(iMessage);
                                GlobalData.DataCache.getSent_messages().remove(iMessage);
                                GlobalData.DataCache.getInbox_messages().remove(iMessage);
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }catch (JSONException| ExecutionException|InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });

                    mark.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            //unmark it
                            String newStatus= CONST.label_inbox;
                            if(iMessage.getLabel().equals(CONST.label_inbox))
                                newStatus= CONST.label_marked;
                            adapter_message.updateLabel(iMessage.getMessageID(), newStatus,
                                    context);
                            if(newStatus.equals(CONST.label_inbox))
                            {
                                mark.setColorFilter(Color.GRAY);
                                listItemMark.setColorFilter(Color.GRAY);
                            }
                            else
                            {
                                mark.setColorFilter(Color.YELLOW);
                                listItemMark.setColorFilter(Color.YELLOW);

                            }
                            iMessage.setLabel(newStatus);
                        }
                    });

                    dialog.show();
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
                for(Integer p:delete_position)
                {
                    if(p.compareTo(position)==0)
                    {
                        delete_position.remove(p);
                        break;
                    }
                }
                view.setBackgroundColor(Color.TRANSPARENT);
            } else
            {
                delete_list.add(data.get(position));
                delete_position.add(position);
                view.setBackgroundColor(Color.GRAY);
              //  deleteButton.setVisibility(View.VISIBLE);
            }
            return false;
        }
    };


    private final int ComposeType_Compose = 100, ComposeType_Reply = 101, ComposeType_Forward = 102;

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
