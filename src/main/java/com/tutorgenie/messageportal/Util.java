package com.tutorgenie.messageportal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
import static com.tutorgenie.messageportal.CONST.*;
class Util
{
    static String CapsFirst(String str) {
        String[] words = str.split(" ");
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < words.length; i++) {
            ret.append(Character.toUpperCase(words[i].charAt(0)));
            ret.append(words[i].substring(1));
            if(i < words.length - 1) {
                ret.append(' ');
            }
        }
        return ret.toString();
    }

    static boolean updateProfile(Context context, String field, String value)
    {
        boolean result=false;
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
                    field);
            queryData.put("value", value);
            String response = (new CONNECTOR()).execute(queryData).get();
            if(response.trim().equals("OK")) //update success
            {
                result = true;
            }
        }catch (JSONException | ExecutionException |InterruptedException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    static String changeDateStringLocale(String inDate)
    {
        String format = "yyyy-MM-dd";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sFormat = new SimpleDateFormat(format);
        DateFormat outFormat = SimpleDateFormat.getDateInstance();
        try
        {
            Date date = sFormat.parse(inDate);
            return outFormat.format(date);
        }catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static JSONObject connectionObject(Context context) throws JSONException
    {
        JSONObject queryData = new JSONObject();
        queryData.put("username", GlobalData.username);
        queryData.put("password", GlobalData.password);
        return queryData;
    }

    static int getActionBarHeight(Context context)
    {
       /* TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv,
                true))
        {
            return TypedValue.complexToDimensionPixelSize(tv.data,
                context.getResources().getDisplayMetrics());
        }
        return 0;*/
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    static int getScreenHeight(AppCompatActivity context)
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    static int getRemainingScreenHeight(Context context)
    {

        return context.getResources().getDimensionPixelSize(R.dimen.main_bar_height)+context.getResources().getDimensionPixelSize(R.dimen.sub_bar_height);
    }

    static void sendDataToOtherApps(Context context, String data)
    {
        String dummySubject = "From Tutor-Genie";
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, dummySubject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, data.trim());
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_via)));
    }

    static void checkPermissions(Context context, int callbackId, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions =
                    permissions && ContextCompat.checkSelfPermission(context, p) == PERMISSION_GRANTED;
        }

        if (!permissions)
            ActivityCompat.requestPermissions((Activity)context, permissionsId, callbackId);
    }

    static void removePending(ArrayList<data_type_tutor_schedule_item> tempList)
    {
        if(tempList!=null)
            MainActivity.pendingCount.setValue(GlobalData.DataCache.getPendingList().size()-tempList.size());
    }

    static void addToContactCache(JSONObject temp) throws JSONException
    {
        data_type_student s = new data_type_student();;
        s.setUsername(temp.getString("username"));
        s.setFirstname(temp.getString("firstname"));
        s.setLastname(temp.getString("lastname"));
        s.setDateofbirth(temp.getString("dateofbirth"));
        s.setGender(temp.getString("gender"));
        s.setPhone_number(temp.getString("phone_number"));
        s.setSelf_intro(temp.getString("self_intro"));
        s.setWebtoken(temp.getString("webtoken"));
       // s.setContact_id(temp.getInt("contact_id"));
        GlobalData.DataCache.getContact_list().add(s);
    }

    static boolean addToContactOnline(Context context, String newEmail) throws JSONException, ExecutionException, InterruptedException
    {
        JSONObject sendObj;
        sendObj = Util.connectionObject(context);
        sendObj.put(CONST.QUERY_TYPE, "ADD_CONTACT");
        sendObj.put("email", newEmail);
        String response = (new CONNECTOR()).execute(sendObj).get();
        if(response.trim().equals(CONST.SUCCESS))
        {
            Toast.makeText(context, "Contact added", Toast.LENGTH_SHORT).show();
            return true;
        }
        else
        {
            Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    static data_type_student getSingleStudentInformation(Context context, String studentEmail)
    {
        data_type_student s= new data_type_student();
        try
        {
            JSONObject sendObj = Util.connectionObject(context);
            sendObj.put(QUERY_TYPE, QUERY_SINGLE_STUDENT);
            sendObj.put("email", studentEmail);
            String resp = (new CONNECTOR()).execute(sendObj).get();
            Log.d("Response: ", resp);
            if(!resp.trim().equals(NOT_FOUND))
            {
                JSONObject temp = new JSONArray(resp).getJSONObject(0);
             //   addToContactCache(getObj);  //no need to add here
                s.setUsername(temp.getString("username"));
                s.setFirstname(temp.getString("firstname"));
                s.setLastname(temp.getString("lastname"));
                s.setDateofbirth(temp.getString("dateofbirth"));
                s.setGender(temp.getString("gender"));
                s.setPhone_number(temp.getString("phone_number"));
                s.setSelf_intro(temp.getString("self_intro"));
                s.setWebtoken(temp.getString("webtoken"));
               // s.setContact_id(temp.getInt("contact_id"));
            }
        } catch (InterruptedException | ExecutionException | JSONException e)
        {
            e.printStackTrace();
        }
        return s;
    }

    static void addContactByEmail(Context context, String email)
    {
        /*
        * This function should be the all-encompassing for when you only have the email address
        * */
        try
        {
            boolean result;

            result=Util.addToContactOnline(context, email);
            /*
             * TRUE = Contact will be added
             * FALSE = Contact already exists or Database error
             * */
            if(result)
            {
                //Retrieve the student information
                data_type_student s = Util.getSingleStudentInformation(context, email);
                GlobalData.DataCache.getContact_list().add(s);
                GlobalData.DataCache.SortContactList();
                GlobalData.mainActivityAccess.updateContactList();
            }
        }catch (JSONException|ExecutionException|InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @SuppressLint("SimpleDateFormat")
    static void composeMessage(data_type_message Message, AlertDialog prevDialog, Context context, String to_field)
    {
        if(prevDialog!=null)
            prevDialog.dismiss();
        @SuppressLint("InflateParams") View view =
                LayoutInflater.from(context).inflate(R.layout.dialog_mail_compose_revised, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                R.style.BottomOptionsDialogTheme);
        //full screen dialog
        builder.setView(view);
        final AlertDialog cdialog = builder.create();
        ImageButton send = view.findViewById(R.id.compose_send_button),
                cancel = view.findViewById(R.id.compose_cancel_button);
        TextView title = view.findViewById(R.id.compose_title);
        final EditText header = view.findViewById(R.id.compose_dialog_header);
        EditText toField = view.findViewById(R.id.compose_dialog_to_field);

        if(Message!=null)
        {
            title.setText(context.getString(R.string.word_reply));
            header.setText(Message.getSubject());
            String to_name = Message.getFrom_name();
            if(Message.getFrom().equals(GlobalData.DataCache.getLogin_info().get(
                    "username")))
            {
                to_name = Message.getTo_name();
            }
            toField.setText(context.getString(R.string.to_prefix_fill,
                    to_name));
        }
        else if(to_field!=null)
        {
            toField.setText(to_field);
        }

        final EditText content = view.findViewById(R.id.compose_dialog_text);
        cancel.setOnClickListener(v1 -> cdialog.dismiss());
        send.setOnClickListener(v12 ->
        {
            CONNECTOR conn = new CONNECTOR();
            CONNECTOR connector = new CONNECTOR();
            try
            {
                String to_id, from_id;
                String to_name, from_name;
                to_id = toField.getEditableText().toString();
                JSONObject fObj = new JSONObject();
                fObj.put("function_name", "GET_STUDENT_NAME");
                fObj.put("email", to_id);
                connector.setAccessURL("https://www.greatbayco" +
                        context.getString(R.string.utility_function_url));
                to_name = connector.execute(fObj).get().trim();
                Log.e("Result", to_name);
                if(to_name.equals(CONST.NOT_FOUND))
                {
                    //give out an error message
                    Toast.makeText(context, "User Not Found", Toast.LENGTH_SHORT).show();
                    return;
                }
                from_id = GlobalData.DataCache.getLogin_info().get(
                        "username");
                from_name =
                        GlobalData.DataCache.getProfileData().get(1).getContent() +(String)GlobalData.DataCache.getProfileData().get(2).getContent();  //first name + last name

                JSONObject obj = Util.connectionObject(context);
                obj.put("query_type", "SEND_MESSAGE_TUTOR");
                obj.put("from_id", from_id);
                obj.put("to_id", to_id);
                obj.put("subject", header.getText().toString());
                obj.put("message", content.getText().toString());

                String result = conn.execute(obj).get();

                //now add the message to Data
                //==============================================================================================
                data_type_message m = new data_type_message();
                m.setLabel(CONST.label_inbox);
                Date cur = Calendar.getInstance().getTime();
                m.setDate((new SimpleDateFormat("yyyy-MM-dd")).format(cur)); //this sets the date
                m.setTime((new SimpleDateFormat("HH:mm:ss")).format(cur));
                m.setFrom(from_id);
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
                context.sendBroadcast(i);
                //================================================================================================
                cdialog.dismiss();
            }catch (JSONException | ExecutionException |InterruptedException e)
            {
                e.printStackTrace();
            }
        });
        cdialog.show();
    }
}
