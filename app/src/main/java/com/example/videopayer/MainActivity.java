package com.example.videopayer;

import static com.example.videopayer.AllowAccessActivity.REQUEST_PERMISSION_SETTING;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<MediaFiles> mediaFiles=new ArrayList<>();
    private List<String> allFolderList=new ArrayList<>();
    private RecyclerView recyclerView;
    private VideoFoldersAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //========================================
        /*
        The user may not log in for the first time and manually revoke access to memory,
        memory access should always be checked first
        */
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
            Toast.makeText(this, "Click on permissions and allow storage", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri=Uri.fromParts("package",getPackageName(),null);
            intent.setData(uri);
            startActivityForResult(intent,REQUEST_PERMISSION_SETTING);

        }
        recyclerView=findViewById(R.id.folders_rv);
        swipeRefreshLayout=findViewById(R.id.swipe_refresh_folders);
        showFolders();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showFolders();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        //==========================================


    }


    //===============================Display Videos Folder in Recycler View=========================
    private void showFolders() {
        mediaFiles=fetchMedias();
        adapter=new VideoFoldersAdapter(mediaFiles,allFolderList,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        adapter.notifyDataSetChanged();
    }

    //==============================================================================================
    @SuppressLint("Range")
    private List<MediaFiles> fetchMedias() {
        List<MediaFiles> mediaFilesArray=new ArrayList<>();

        //get videos address from media like : content://media/external/video/media
        Uri uri= MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        //create a cursor in special path (uri) So we can move on items
        Cursor cursor=getContentResolver().query(uri,null,null,null,null);


        //Get all of videos in Media in add in the mediaFiles and get folders name and add in the allFolderList
        if (cursor !=null && cursor.moveToNext()){
            do {

                String id=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String title=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                String displayName=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                String size=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                String duration=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                String path=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                String dateAdded=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));

                MediaFiles mediaFile=new MediaFiles(id,title,displayName,size,duration,path,dateAdded);

                int index=path.lastIndexOf("/");
                String subString=path.substring(0,index);

                if (!allFolderList.contains(subString)){
                    allFolderList.add(subString);
                }
                mediaFilesArray.add(mediaFile);
            }while (cursor.moveToNext());
        }


        return mediaFilesArray;

    }
    //==============================================================================================


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.folder_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id =item.getItemId();
        switch (id){
            case R.id.reteus:
                Uri uri=Uri.parse("https://play.google.com/store/apps/details?id="+getApplicationContext().getPackageName());
                Intent intent=new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
                break;
            case R.id.refresh_folders:
                finish();
                startActivity(getIntent());
                break;
            case R.id.share_app:
                Intent share_intent=new Intent();
                share_intent.setAction(Intent.ACTION_SEND);
                share_intent.putExtra(Intent.EXTRA_TEXT,"Check this app via \n"+
                        "https://play.google.com/store/apps/details?id="+getApplicationContext().getPackageName());
                share_intent.setType("text/plain");
                startActivity(Intent.createChooser(share_intent,"Share App Via"));
                break;
        }
        return true;
    }
}
