package com.tutorgenie.messageportal;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class CONNECTOR_DOWNLOAD extends AsyncTask<JSONObject, Integer, String>
{
    private final String accessURL = "https://www.greatbayco.com/appaccess/index.php";
    private StringBuilder builder = new StringBuilder();
    public CONNECTOR.progressRetriever retriever;
    private String currentState="";
    private String result="";
    @Override
    protected String doInBackground(JSONObject... params)
    {
        try
        {
            URL url = new URL(accessURL);
            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(params[0].toString());
            out.flush();
            out.close();

            InputStream in = conn.getInputStream();

            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            int BytesAvailable = Integer.parseInt(conn.getHeaderField("Length"));
            if(BytesAvailable<=0)
            {
                in.close();
                return "Bad";
            }
            byte[] buffer = new byte[1024];  //make a 1kb buffer
            int BytesRead = 0;
            while (true)
            {
                int p = Math.round(100 * (float)BytesRead / BytesAvailable);
                int n = in.read(buffer);
                if (n < 0) break;
                byteOut.write(buffer, 0, n);
                BytesRead += n;
            }
            byte[] buf = byteOut.toByteArray();
            if (buf.length < 50)
            {
                String temp = new String(buf, StandardCharsets.UTF_8);
                Log.d("SERVER ERROR: ", temp);
            }

            GlobalData.DataCache.setProfileImg(BitmapFactory.decodeByteArray(buf, 0,
                buf.length));

            return "Downloaded "+buf.length+" bytes";
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return "ok";
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        retriever.writeProgress(values[0]);
    }
}