package com.tutorgenie.messageportal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static android.app.Activity.RESULT_OK;


public class fragment_profile extends Fragment
{
    private Context context;
    private String tempPath; //temporary path for picture files
    private ImageView profileImage;
    private ConstraintLayout imageOptionPane, imagePane;
    private final static int REQUEST_CAMERA=101;
    private Button take_pic, hide_pic, show_pic;
    private int finalHeight;
    private ListView entryList;

    private final static int REQUEST_TAKE_PHOTO = 100;

    fragment_profile(Context context)
    {
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profileImage = view.findViewById(R.id.profile_image);

        if(GlobalData.DataCache.getProfileImg()!=null)
            //profileImage.setImageBitmap(Bitmap.createScaledBitmap(GlobalData.DataCache
            // .getProfileImg(), 500, 500, true));
        {
            profileImage.setLayoutParams(new ConstraintLayout.LayoutParams(GlobalData.getScreenWidth(),
                    GlobalData.getScreenWidth()));
            profileImage.setImageBitmap(GlobalData.DataCache.getProfileImg());
        }
        else profileImage.setImageResource(R.drawable.ic_photo_camera_black_24dp);
        profileImage.setOnClickListener(v ->
        {
            if(imageOptionPane.getVisibility()==View.VISIBLE)
                imageOptionPane.setVisibility(View.INVISIBLE);
            else
                imageOptionPane.setVisibility(View.VISIBLE);
        });
        imagePane = view.findViewById(R.id.imagePane);
        imageOptionPane = view.findViewById(R.id.imageOptionPane);
        imageOptionPane.setLayoutParams(new ConstraintLayout.LayoutParams(profileImage.getLayoutParams()));
        imageOptionPane.setVisibility(View.INVISIBLE);
        take_pic = view.findViewById(R.id.take_pic_button);
        hide_pic = view.findViewById(R.id.hide_pic_button);
        show_pic = view.findViewById(R.id.show_pic_button);
        take_pic.setOnClickListener(cameraListner);
        hide_pic.setOnClickListener(v ->
        {
            profileImage.setVisibility(View.GONE);
            imageOptionPane.setVisibility(View.GONE);
            show_pic.setVisibility(View.VISIBLE);
            setListHeight(true);
        });
        show_pic.setOnClickListener(v->
        {
            setListHeight(false);
            profileImage.setVisibility(View.VISIBLE);
            imageOptionPane.setVisibility(View.INVISIBLE);
            show_pic.setVisibility(View.GONE);
        });

        entryList = view.findViewById(R.id.entryList);
        //adjust
        adapter_profile adapter = new adapter_profile(context);
        entryList.setAdapter(adapter);
        entryList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                int height = entryList.getHeight();
                int actionbarHeight = Util.getActionBarHeight(context);
                finalHeight = height - actionbarHeight;
                setListHeight(false);

                int mWidth = profileImage.getWidth();
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)profileImage.getLayoutParams();
                params.height = mWidth;
                profileImage.setLayoutParams(params);
                imageOptionPane.setLayoutParams(params);
                entryList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


        return view;
    }

    private void setListHeight(boolean fullScreen)
    {
        if(fullScreen)
        {
            entryList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, finalHeight-50));
        }
        else
        {
            entryList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, finalHeight - profileImage.getHeight()));
        }
    }

    private View.OnClickListener cameraListner = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            //first check if there is a photo
            if(GlobalData.DataCache.getProfileImg()!=null)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(getString(R.string.image_exist));
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(ContextCompat.checkSelfPermission(context,
                                Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
                        {
                            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                        }
                        else
                        {
                            takePic();
                        }
                    }
                });

                builder.setNegativeButton(R.string.cancel, (dialog, which) ->
                {

                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else
            {
                if(ContextCompat.checkSelfPermission(context,
                        Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                            new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                }
                else
                    takePic();
            }
        }
    };

    private void takePic()
    {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(context.getPackageManager()) != null)
        {
            File photoFile = null;
            try
            {
                String imageFileName = "JPEG_TEMP_";
                File storageDir =
                        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                Log.e("Ev", storageDir.getAbsolutePath());
                photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);
                tempPath = photoFile.getAbsolutePath();
                Log.e("Tp", tempPath);
            } catch (IOException e)
            {
                Log.e("File I/O Error", e.getMessage());
            }
            if (photoFile != null)
            {
                Uri photoUri = FileProvider.getUriForFile(context, "com.tutorgenie.messageportal.fileprovider",
                        photoFile);

                Log.e("ERROR", photoUri.getPath());
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                startActivityForResult(takePicture, REQUEST_TAKE_PHOTO);
            } else
            {
                Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Log.e("Access", "Access Granted");
                takePic();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK)
                {
                    Log.e("Result", "OK");
                    Bitmap img = BitmapFactory.decodeFile(tempPath);
                    int w = img.getWidth();
                    int h = img.getHeight();
                    int startx, starty, span;
                    if(w>h)
                    {
                        startx = (w-h)/2;
                        starty=0;
                        span = h;
                    }
                    else
                    {
                        startx = 0;
                        starty = (h-w)/2;
                        span = w;
                    }

                    final Bitmap new_img = Bitmap.createBitmap(img, startx, starty, span, span);

                    AlertDialog.Builder builder =new AlertDialog.Builder(context);

                    @SuppressLint("InflateParams")
                    View dialogView =
                            LayoutInflater.from(context).inflate(R.layout.dialog_pic_upload, null);

                    ImageView img_v;
                    final TextView progress_text;
                    final ProgressBar progressBar;
                    final Button confirmBtn;
                    final Button cancelBtn;

                    {
                        img_v = dialogView.findViewById(R.id.upload_image);
                        progress_text = dialogView.findViewById(R.id.progress_bar_text);
                        progressBar = dialogView.findViewById(R.id.PROGRESS_BAR);
                        progressBar.setMax(100);
                        confirmBtn = dialogView.findViewById(R.id.upload_btn);
                        cancelBtn = dialogView.findViewById(R.id.cancel_btn);
                    }
                    img_v.setImageBitmap(Bitmap.createScaledBitmap(new_img, 500, 500, true));
                    builder.setView(dialogView);

                    final AlertDialog dialog = builder.create();

                    cancelBtn.setOnClickListener(v -> dialog.dismiss());

                    confirmBtn.setOnClickListener(v ->
                    {
                        Bitmap newpic =
                                Bitmap.createScaledBitmap(new_img, 500, 500, true);
                        GlobalData.DataCache.setProfileImg(newpic);
                        profileImage.setImageBitmap(newpic);
                        //invoke data upload now
                        CONNECTOR_UPLOAD upload = new CONNECTOR_UPLOAD(context);
                        upload.setHandle(new CONNECTOR_UPLOAD.progressHandle()
                        {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void pub_progress(int p)
                            {
                                progress_text.setText(p+"%");
                                progressBar.setProgress(p);
                            }

                            @Override
                            public void fin_progress()
                            {
                                confirmBtn.setText(R.string.done);
                                confirmBtn.setEnabled(true);
                                confirmBtn.setFocusable(true);
                                confirmBtn.setTextColor(Color.WHITE);
                                confirmBtn.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        dialog.dismiss();
                                    }
                                });
                            }

                            @Override
                            public void start_progress()
                            {
                                cancelBtn.setFocusable(false);
                                cancelBtn.setEnabled(false);
                                confirmBtn.setEnabled(false);
                                confirmBtn.setFocusable(false);
                                confirmBtn.setTextColor(Color.GRAY);
                                cancelBtn.setTextColor(Color.GRAY);
                            }
                        });
                        upload.execute();
                    });
                    dialog.show();
                }
                break;
        }
    }
}