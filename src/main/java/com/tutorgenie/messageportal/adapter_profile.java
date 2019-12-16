package com.tutorgenie.messageportal;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.tutorgenie.messageportal.Util.CapsFirst;
import static com.tutorgenie.messageportal.Util.updateProfile;

public class adapter_profile extends ArrayAdapter<profile_entry>
{
    private Context context;
    ArrayList<profile_entry> data;
    public static int subjectPosition=-1;
    private final JSONArray currentArray = new JSONArray();
    private final ArrayList<String> list = new ArrayList<>();
    public adapter_profile(@NonNull Context context)
    {
        super(context, R.layout.adapter_profile);
        this.context=context;
        data = GlobalData.DataCache.getProfileData();
    }

    @Override
    public int getCount()
    {
        if(data==null)
            return 0;
        return data.size();
    }
    @Override
    public int getViewTypeCount()
    {
        return getCount();
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }


    @SuppressLint("SimpleDateFormat")
    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent)
    {
        TextView titleText;
        View view = convertView;
        if(view!=null)
            return view;
        else
        {
            view = LayoutInflater.from(context).inflate(R.layout.adapter_profile, null);
        }
        View contentView = view.findViewById(R.id.content);
        titleText = view.findViewById(R.id.entry_title);

        if(contentView==null)
        {
            Log.e("Error on", data.get(position).getTitle());
            return view;
        }
        if(GlobalData.params==null)
        {
            GlobalData.params = contentView.getLayoutParams();
        }
        final LinearLayout layout = (LinearLayout) contentView.getParent();
        layout.removeView(contentView);
        final String title = data.get(position).getTitle();
        Object dataObj = data.get(position).getContent();
        switch (title)
        {
            case "latitude":
            case "longitude":
            case "service_radius":
            {
                view = LayoutInflater.from(context).inflate(R.layout.blanklayout, null);
                break;
            }
            case "profile_picture":
            {
                //Object is a picture
                ImageView contentImage = new ImageView(context);
                contentImage.setLayoutParams(GlobalData.params);
                contentImage.setMaxHeight(400);
                contentImage.setMaxWidth(400);
                contentImage.setScaleType(ImageView.ScaleType.FIT_XY);
                contentImage.setImageBitmap((Bitmap) dataObj);
                layout.addView(contentImage);
                break;
            }

            case "subject":  //this should be an arraylist
            {
                String subjectList = (String)dataObj;
                try
                {
                    JSONArray array = new JSONArray(subjectList);

                    for(int i = 0; i<array.length(); i++)
                    {
                        list.add(array.getString(i));
                    }
                    final FlexboxLayout subjectBox = new FlexboxLayout(context);
                    subjectBox.setFlexWrap(FlexWrap.WRAP);
                    subjectBox.setLayoutParams(GlobalData.params);
                    final FlexboxLayout.LayoutParams tParams =
                            new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                    tParams.setMargins(10, 3, 10, 3);
                    for(String s:list)
                    {
                        currentArray.put(s);
                        final TextView t = new TextView(context);
                        t.setLayoutParams(tParams);
                        t.setPadding(10, 3, 10, 3);
                        t.setBackgroundResource(R.drawable.clickable_textview);
                        t.setText(s);
                        subjectBox.addView(t);
                        t.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(final View v)
                            {
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                                builder.setMessage("Are you sure you want to remove "+t.getText()+"?");
                                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        String subText = ((TextView)v).getText().toString();
                                        try
                                        {
                                            for (int i = 0; i < currentArray.length(); i++)
                                            {
                                                if (currentArray.get(i).toString().trim().equals(subText.trim()))
                                                {
                                                    currentArray.remove(i);
                                                    list.remove(i);
                                                    break;
                                                }
                                            }
                                            JSONObject queryData =
                                                    new JSONObject();
                                            queryData.put("username",
                                                    context.getString(R.string.test_username));
                                            queryData.put("password",
                                                    context.getString(R.string.test_password));
                                            queryData.put("query_type",
                                                    "UPDATE_SUBJECT");
                                            queryData.put("data",
                                                    currentArray.toString());
                                            String res =
                                                    (new CONNECTOR()).execute(queryData).get();
                                            if(res.trim().equals("OK"))
                                            {
                                                Toast.makeText(context,
                                                        "res",
                                                        Toast.LENGTH_SHORT).show();

                                                GlobalData.DataCache.getProfileData().get(position).setContent(currentArray.toString());
                                                //finally, update the view
                                                subjectBox.removeView(v);
                                            }
                                            else Log.e("ER"
                                                    , res);
                                        }catch (JSONException |ExecutionException|InterruptedException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {

                                    }
                                });

                                android.app.AlertDialog dialog =builder.create();
                                dialog.show();
                            }
                        });
                    }

                    final ImageView addBox= new ImageView(context);
                    addBox.setLayoutParams(tParams);
                    addBox.setImageResource(R.drawable.ic_add_box_black_24dp);;
                    subjectBox.addView(addBox);
                    addBox.setOnClickListener(new View.OnClickListener()
                                              {
                                                  @Override
                                                  public void onClick(View v)
                                                  {
                                                      final PopupMenu submenu = new PopupMenu(context,
                                                              addBox);
                                                      Menu m = submenu.getMenu();
                                                      for(String s: GlobalData.DataCache.getComplete_subject_list())
                                                      {
                                                          if(!list.contains(s))
                                                            m.add(s);
                                                      }
                                                      submenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                                                      {
                                                          @Override
                                                          public boolean onMenuItemClick(MenuItem item)
                                                          {
                                                              try
                                                              {
                                                                  JSONObject queryData =
                                                                          new JSONObject();
                                                                  queryData.put("username",
                                                                          context.getString(R.string.test_username));
                                                                  queryData.put("password",
                                                                          context.getString(R.string.test_password));
                                                                  queryData.put("query_type",
                                                                          "UPDATE_SUBJECT");
                                                                  currentArray.put(item.getTitle());
                                                                  queryData.put("data",
                                                                          currentArray.toString());
                                                                  String res =
                                                                          (new CONNECTOR()).execute(queryData).get();
                                                                  if(res.trim().equals("OK"))
                                                                  {
                                                                      Toast.makeText(context,
                                                                              "res",
                                                                              Toast.LENGTH_SHORT).show();
                                                                      list.add(item.getTitle().toString()); //add to the list
                                                                      GlobalData.DataCache.getProfileData().get(position).setContent(currentArray.toString());
                                                                      //finally, update the view
                                                                      TextView t = new TextView(context);
                                                                      t.setLayoutParams(tParams);
                                                                      t.setPadding(10, 3, 10, 3);
                                                                      t.setBackgroundResource(R.drawable.clickable_textview);
                                                                      t.setText(item.getTitle());
                                                                      subjectBox.addView(t,
                                                                              subjectBox.getChildCount()-1);
                                                                      t.setOnClickListener(new View.OnClickListener()
                                                                      {
                                                                          @Override
                                                                          public void onClick(View v)
                                                                          {
                                                                              String subText = ((TextView)v).getText().toString();
                                                                              try
                                                                              {
                                                                                  for (int i = 0; i < currentArray.length(); i++)
                                                                                  {
                                                                                      if (currentArray.get(i).toString().trim().equals(subText.trim()))
                                                                                      {
                                                                                          currentArray.remove(i);
                                                                                          list.remove(i);
                                                                                          break;
                                                                                      }
                                                                                  }
                                                                                  JSONObject queryData =
                                                                                          new JSONObject();
                                                                                  queryData.put("username",
                                                                                          context.getString(R.string.test_username));
                                                                                  queryData.put("password",
                                                                                          context.getString(R.string.test_password));
                                                                                  queryData.put("query_type",
                                                                                          "UPDATE_SUBJECT");
                                                                                  queryData.put("data",
                                                                                          currentArray.toString());
                                                                                  String res =
                                                                                          (new CONNECTOR()).execute(queryData).get();
                                                                                  if(res.trim().equals("OK"))
                                                                                  {
                                                                                      Toast.makeText(context,
                                                                                              "res",
                                                                                              Toast.LENGTH_SHORT).show();

                                                                                      GlobalData.DataCache.getProfileData().get(position).setContent(currentArray.toString());
                                                                                      //finally, update the view
                                                                                      subjectBox.removeView(v);
                                                                                  }
                                                                                  else Log.e("ER"
                                                                                          , res);
                                                                              }catch (JSONException |ExecutionException|InterruptedException e)
                                                                              {
                                                                                  e.printStackTrace();
                                                                              }
                                                                          }
                                                                      });

                                                                  }else Log.e("Err", res);
                                                              }catch (JSONException | ExecutionException | InterruptedException e)
                                                              {
                                                                  e.printStackTrace();
                                                              }
                                                              return false;
                                                          }
                                                      });
                                                      submenu.show();
                                                  }
                                              });
                    layout.addView(subjectBox);
                }catch (JSONException  e)
                {
                    e.printStackTrace();
                }


                break;
            }
            case "tutor_status":
            {
                Switch status_switch = new Switch(context);
                status_switch.setLayoutParams(GlobalData.params);

                if(data.get(position).getContent().toString().trim().equals("0"))
                    status_switch.setChecked(false);
                else status_switch.setChecked(true);

                status_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        if(!isChecked)
                            updateProfile(context, "tutor_status", "0");
                        else updateProfile(context, "tutor_status", "1");
                    }
                });
                layout.addView(status_switch);
                break;
            }

            default:
            {
                final TextView contentText = new TextView(context);
                contentText.setLayoutParams(GlobalData.params);
                contentText.setPadding(20, 5, 10, 5);
                contentText.setGravity(Gravity.END);
                if(!data.get(position).getContent().toString().trim().equals("null"))
                {
                    if (!title.equals("nationality"))
                        contentText.setText(data.get(position).getContent().toString());
                    else
                        contentText.setText(GlobalData.DataCache.getNationality_map().inverse().get(data.get(position).getContent().toString()));
                }
                layout.addView(contentText);
                contentText.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //popup edit box
                        //do another switch to determine type
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        @SuppressLint("InflateParams") View editDialog = LayoutInflater.from(context).inflate(R.layout.dialog_edit,
                                null);

                        final EditText edit = editDialog.findViewById(R.id.edit_field);
                        switch (title)
                        {
                            case "username":
                            case "gender":
                            case "first_name":
                            case "last_name":
                                return;
                            case "date_of_birth":
                                edit.setFocusable(false);
                                edit.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        DatePickerDialog pickerDialog =
                                                new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener()
                                                {
                                                    @SuppressLint("SetTextI18n")
                                                    @Override
                                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                                                    {
                                                        edit.setText(year+"-"+month+"-"+dayOfMonth);
                                                    }
                                                }, 2000, 0, 1); //default date set
                                        pickerDialog.show();
                                    }
                                });
                                break;
                            case "nationality":
                                edit.setFocusable(false);
                                edit.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        AlertDialog.Builder builder =
                                                new AlertDialog.Builder(context);

                                        Spinner spinner = new Spinner(context);
                                        builder.setView(spinner);
                                        final AlertDialog dialog = builder.create();
                                        spinner.setLayoutParams(GlobalData.params);
                                        spinner.setAdapter((new ArrayAdapter<String>(context,
                                                android.R.layout.simple_spinner_item,
                                                GlobalData.DataCache.getNationality_list())));
                                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                                        {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                                            {
                                                edit.setText(GlobalData.DataCache.getNationality_list().get(position));
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent)
                                            {

                                            }
                                        });
                                        dialog.show();
                                    }
                                });

                                break;
                            case "price_min":
                            case "phone_number":
                                edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                                break;
                            default:
                                edit.setInputType(InputType.TYPE_CLASS_TEXT);
                                break;
                        }
                        edit.setHint(Util.CapsFirst(title.replace("_", " ")));
                        builder.setView(editDialog);
                        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                String data="";
                                if(title.equals("nationality"))
                                {
                                    //change data
                                    String full=edit.getText().toString();
                                    if(!GlobalData.DataCache.getNationality_map().containsKey(full))
                                    {
                                        Toast.makeText(context, "Nationality Doesn't Exist",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    else data =
                                            GlobalData.DataCache.getNationality_map().get(full);
                                    //the short version of nationality
                                }
                                else
                                    data = edit.getText().toString();

                                try
                                {
                                    JSONObject queryData =
                                            new JSONObject();
                                    queryData.put("username",
                                            context.getString(R.string.test_username));
                                    queryData.put("password",
                                            context.getString(R.string.test_password));
                                    queryData.put("query_type",
                                            "UPDATE_PROFILE");
                                    queryData.put("field",
                                            title);
                                    queryData.put("value", data);
                                    String response = (new CONNECTOR()).execute(queryData).get();
                                    if(response.trim().equals("OK")) //update success
                                    {
                                        if(title.equals("nationality"))
                                            contentText.setText(GlobalData.DataCache.getNationality_map().inverse().get(data));
                                        else contentText.setText(data);
                                    }
                                }catch (JSONException |ExecutionException|InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        });

                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }
        }
        titleText.setText(Util.CapsFirst(title.replace("_", " "))); //format
        return view;
    }

}
