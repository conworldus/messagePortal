package com.tutorgenie.messageportal;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class CONNECTOR extends AsyncTask<JSONObject, Integer, String>
{
    private String accessURL = "https://www.greatbayco.com/appaccess/index.php";  //default
    private StringBuilder builder = new StringBuilder();
    progressRetriever retriever;

    CONNECTOR()
    {

    }
    void setAccessURL(String newURL)
    {
        accessURL = newURL;
    }

    public void setRetriever(progressRetriever sRetriever)
    {
        this.retriever = sRetriever;
    }

    @Override
    public String doInBackground(JSONObject... objects)
    {
        try
        {
            URL obj = new URL(accessURL);
            HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            //connection.setDoInput(true);
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.write(objects[0].toString().getBytes());
            Log.e("Sent", objects[0].toString());
            outputStream.flush();
            outputStream.close();
            Map<String, List<String>> map = connection.getHeaderFields();
            InputStream in = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            //String size=connection.getHeaderField("Content-Length");
            //Log.e("SSSSSS", size);
            Log.e("Mark", "sss");
            int size;
            if(map.containsKey("Length"))
                size = Integer.parseInt(map.get("Length").get(0));
            else size=0;
            Log.e("Mark2", "sss");
            int count = 0;
            int readData = reader.read();
            if (readData == -1)
                return null;
            while (readData != -1)
            {
                builder.append((char) readData);
                readData = reader.read();
                count++;
                if(count%10==0&&size>0)  //update progress every 100
                {
                    int p = Math.round((float)count/size * 100);
                    publishProgress(p);
                }
            }
            String result = builder.toString();
            if(result.trim().length()==0)
            {
                return "Bad Request";
            }
            Log.e("e", result);
            return result;
        }
        catch (MalformedURLException e)
        {
            Log.e("URL", e.getMessage());
        }
        catch (ProtocolException e)
        {
            Log.e("Protocol", "Protocol"+e.getMessage());
        }
        catch (Exception e)
        {
            Log.e("Connector", e.getMessage());
            Log.e(e.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
        Log.d("Publish", values[0]+"");
        if(retriever!=null)
        {
            Log.d("Calling", "Retriever");
            retriever.writeProgress(values[0]);
        }
        else Log.i("Retrieve", "IS NULL!");
    }

    public interface progressRetriever
    {
        void writeProgress(int p);
    }
}
