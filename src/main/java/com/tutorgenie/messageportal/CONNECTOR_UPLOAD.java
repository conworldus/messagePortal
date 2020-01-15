package com.tutorgenie.messageportal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.net.ssl.HttpsURLConnection;

public class CONNECTOR_UPLOAD extends AsyncTask<JSONObject, Integer, String>
{
    @SuppressLint("StaticFieldLeak")
    private Context context;

    CONNECTOR_UPLOAD(Context context)
    {
        this.context = context;
    }

    public interface progressHandle
    {
        void pub_progress(int p);
        void fin_progress();
        void start_progress();
    }

    private progressHandle handle;

    void setHandle(progressHandle handle)
    {
        this.handle = handle;
    }

    @Override
    public String doInBackground(JSONObject... objects)
    {
        HttpsURLConnection connection;
        final String twoHyphens = "--";
        final String boundaryMarker = "--XxXxXxX";
        final String lineEnd = "\r\n";
        DataOutputStream outputStream;
        URL obj;
        try
        {
            Bitmap data = GlobalData.DataCache.getProfileImg();
            if (data == null)
            {
                Log.e("ERROR", "Profile picture does not exist");
                return null;
            }
            String sendData = Util.toBase64(data);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            data.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            int BytesCount = sendData.getBytes().length;
            //int BytesCount = data.getAllocationByteCount();
            //ByteBuffer bitbuf = ByteBuffer.allocate(BytesCount);
            //data.copyPixelsToBuffer(bitbuf);
            ByteArrayInputStream bs = new ByteArrayInputStream(sendData.getBytes());
                    //new ByteArrayInputStream(bitbuf.array());
            //bitbuf is no longer needed
            Log.e("Length", String.valueOf(BytesCount));

            String url = context.getString(R.string.uploadURL);
            obj = new URL(url);
            connection = (HttpsURLConnection) obj.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.addRequestProperty("connection", "keep-alive");
            connection.addRequestProperty("enctype", "multipart/form-data");
            connection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryMarker);
            connection.addRequestProperty("Webtoken", GlobalData.DataCache.getLogin_info().get(
                    "randomseed"));
            connection.addRequestProperty("Username", GlobalData.username);
            connection.addRequestProperty("uploaded_file", "part.jpg");
            outputStream = new DataOutputStream(connection.getOutputStream());
            Log.e("Stream","Obtained");
            outputStream.writeBytes(twoHyphens + boundaryMarker + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"part.jpg\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            int TotalBytesRead=0;
            int size_buffer = Math.min(BytesCount, 64);
            byte[] buffer = new byte[size_buffer];
            int BytesRead = bs.read(buffer, 0, size_buffer);

            Log.e("Start", "writing");
            while (BytesRead > 0) //data remain in the file
            {
                outputStream.write(buffer, 0, size_buffer);
                TotalBytesRead += size_buffer;
                //now publishProgress
                //Log.e("Bytes", TotalBytesRead+"/"+BytesCount);
                publishProgress(Math.round(100 * (float)TotalBytesRead /BytesCount));
                size_buffer = Math.min((BytesCount-BytesRead), 64);
                BytesRead = bs.read(buffer, 0, size_buffer);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundaryMarker + twoHyphens + lineEnd);



            outputStream.flush();
            outputStream.close();

            Log.e("RESPONSE", connection.getResponseMessage());

            InputStream in = connection.getInputStream();
            if (in == null)
            {
                //then there is an error
                in = connection.getErrorStream();
                if (in == null)
                    return null;
            }
            InputStreamReader read = new InputStreamReader(in);
            StringBuilder retString = new StringBuilder();

            int readData = read.read();
            while (readData != -1)
            {
                retString.append((char) readData);
                readData = read.read();
            }
            Log.e("Response: ", retString.toString());
            return retString.toString();
        } catch (NullPointerException e)
        {
            //In this case there is an error


            //  InputStream in = connection.getErrorStream()
        } catch (ProtocolException e)
        {
            Log.e("HTTPSPROTOCOL", e.getMessage());
        } catch (MalformedURLException e)
        {
            Log.e("URL", e.getMessage());
        } catch (IOException e)
        {
            Log.e("IO", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        handle.start_progress();
    }

    @Override
    protected void onPostExecute(String params)
    {
        super.onPostExecute(params);
        handle.fin_progress();
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
        handle.pub_progress(values[0]);
        Log.e("updating", values[0]+"");
    }
}
