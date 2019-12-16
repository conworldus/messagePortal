package com.tutorgenie.messageportal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class fragment_mailview extends Fragment
{
    private ArrayList<data_type_message> data;
    private ArrayList<data_type_message> delete_list = new ArrayList<>();
    private ArrayList<Integer> delete_position = new ArrayList<>();
    private Context context;
    private Button deleteButton;
    private adapter_message adapter;
    private ListView mailList;
    private int CurrentSelection = -1;

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
                @SuppressLint("SetTextI18n")
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
                {
                    final data_type_message iMessage = data.get(position);

                    final ImageView listItemMark = view.findViewById(R.id.mark);

                    CurrentSelection = position;
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

                    if(iMessage.getLabel().equals(constants.label_marked))
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
                            View view = LayoutInflater.from(context).inflate(R.layout.dialog_mail_compose, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(context,
                                    R.style.BottomOptionsDialogTheme);
                            //full screen dialog
                            builder.setView(view);

                            final AlertDialog cdialog = builder.create();
                            Button send = view.findViewById(R.id.compose_send_button),
                                    cancel = view.findViewById(R.id.compose_cancel_button);

                            TextView header = view.findViewById(R.id.compose_dialog_header);
                            {
                                //set up the header information
                                String sBuilder = "Re: " +
                                        iMessage.getSubject() +
                                        '\n' +
                                        "To: " +
                                        iMessage.getFrom_name();
                                header.setText(sBuilder);
                            }
                            EditText content = view.findViewById(R.id.compose_dialog_text);
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
                                @Override
                                public void onClick(View v)
                                {

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
                            String newStatus=constants.label_inbox;
                            if(iMessage.getLabel().equals(constants.label_inbox))
                                newStatus=constants.label_marked;
                            adapter_message.updateLabel(iMessage.getMessageID(), newStatus,
                                    context);
                            if(newStatus.equals(constants.label_inbox))
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


    private final int ComposeType_Compose = 100, ComposeType_Reply = 101, ComposeType_Forward = 102;
    private AlertDialog composeDialog()
    {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_mail_compose, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                R.style.BottomOptionsDialogTheme);
        //full screen dialog
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        Button send = view.findViewById(R.id.compose_send_button),
                cancel = view.findViewById(R.id.compose_cancel_button);

        TextView header = view.findViewById(R.id.compose_dialog_header);
        EditText content = view.findViewById(R.id.compose_dialog_text);
        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        return dialog;
    }

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
