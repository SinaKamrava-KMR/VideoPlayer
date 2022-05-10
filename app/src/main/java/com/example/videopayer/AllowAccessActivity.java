package com.example.videopayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

public class AllowAccessActivity extends AppCompatActivity {

    public static final int STORAGE_PERMISSION = 1;
    public static final int REQUEST_PERMISSION_SETTING = 12;
    private Button allow_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allow_access);

        //================================================
        /*
         If the user enters the app for the first time,
         it will be saved in SharedPreferences,
         if not, it will go to the main page
         */
        SharedPreferences sharedPreferences=getSharedPreferences("AllowAccess",MODE_PRIVATE);
        String value=sharedPreferences.getString("Allow","");
        if (value.equals("Ok")){
            startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
            finish();
        }else {
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("Allow","Ok");
            editor.apply();
        }

        //==========================================




        /*
            If the user clicked the Allow Access button
            There are 2 modes to check whether the user is allowed to access the phone memory
            If it allows access, it goes to the main page.
            If not,
            we have to request access

            The result of these requests in
            the onRequestPermissionsResult() function
            Is checked
         */
        allow_btn = findViewById(R.id.allow_access);
        allow_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                    finish();
                } else {
                    ActivityCompat.requestPermissions(AllowAccessActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION) {
            for (int i = 0; i < permissions.length; i++) {

                //the user's Permissions
                String per = permissions[i];

                //  If it does not allow access, a dialog must be opened and moved to the access page
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(per);
                    if (!showRationale) {
                        //user click down never ask again
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Add Permission")
                                .setMessage("For playing video you must allow to access this permission" +
                                        "\n\n" + "Now follow the below steps" + "\n\n" + "Open Settings from below button " +
                                        "\n" + "Click on Permissions " + "\n" + "Allow access for storage")
                                .setPositiveButton("open settings", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri=Uri.fromParts("package",getPackageName(),null);
                                        intent.setData(uri);
                                        startActivityForResult(intent,REQUEST_PERMISSION_SETTING);
                                    }
                                }).create().show();

                    } else {
                        //user click on denied
                        ActivityCompat.requestPermissions(AllowAccessActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);

                    }
                } else {
                    startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                    finish();
                }
            }
        }
    }


    /*
        The app checks as soon as it is executed
        if the allowed access goes directly to the main page
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AllowAccessActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
        }
    }
}