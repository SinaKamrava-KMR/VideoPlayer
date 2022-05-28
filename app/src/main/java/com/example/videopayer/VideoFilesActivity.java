package com.example.videopayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;

import java.util.ArrayList;
import java.util.List;

public class VideoFilesActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String MY_PREF = "my_pref";
    private RecyclerView recyclerView;
    private ArrayList<MediaFiles> videoFilesList=new ArrayList<>();
    private VideoFilesAdapter adapter;
    private String folderName;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String sort_order;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_files);


        recyclerView=findViewById(R.id.videos_rv);
        folderName=getIntent().getStringExtra("folderName");

        SharedPreferences.Editor editor=getSharedPreferences(MY_PREF,MODE_PRIVATE).edit();
        editor.putString("playListFolderName",folderName);
        editor.apply();


        getSupportActionBar().setTitle(folderName);
        swipeRefreshLayout=findViewById(R.id.swipe_refresh_videos);
        showVideoFiles();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showVideoFiles();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


    }

    private void showVideoFiles() {
        videoFilesList=fetchMedia(folderName);
        adapter=new VideoFilesAdapter(videoFilesList,this,0);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("Range")
    private ArrayList<MediaFiles> fetchMedia(String folderName) {

        SharedPreferences preferences=getSharedPreferences(MY_PREF,MODE_PRIVATE);
        String sort_value=preferences.getString("sort","not_set");
        ArrayList<MediaFiles> videoFiles=new ArrayList<>();
        Uri uri= MediaStore.Video.Media.EXTERNAL_CONTENT_URI;


        if (sort_value.equals("sortName")){
        sort_order= MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
        }else if (sort_value.equals("sortSize")){
            sort_order= MediaStore.MediaColumns.SIZE + " DESC";
        }else if (sort_value.equals("sortDate")){
            sort_order= MediaStore.MediaColumns.DATE_ADDED + " DESC";
        }else if (sort_value.equals("sortLength")){
            sort_order= MediaStore.MediaColumns.DURATION + " DESC";
        }



        String selection=MediaStore.Video.Media.DATA+" like?";
        String[] selectionArg=new String[]{"%"+folderName+"%"};
        Cursor cursor=getContentResolver().query(uri,null,selection,selectionArg,sort_order);
        if (cursor!=null && cursor.moveToNext()){
            do {
                String id=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String title=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                String displayName=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                String size=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                String duration=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                String path=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                String dateAdded=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));

                MediaFiles mediaFile=new MediaFiles(id,title,displayName,size,duration,path,dateAdded);
                Log.i("cursor", "path : "+path);
                videoFiles.add(mediaFile);

            }while (cursor.moveToNext());
        }
        return videoFiles;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_menu,menu);
        MenuItem menuItem=menu.findItem(R.id.search_video);
        SearchView searchView= (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences preferences=getSharedPreferences(MY_PREF,MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        int id=item.getItemId();
        switch (id){
            case R.id.refresh_files:
                finish();
                startActivity(getIntent());
                break;
            case R.id.sort_by_title:
                AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
                alertDialog.setTitle("Sort By");
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.apply();
                        finish();
                        startActivity(getIntent());
                        dialogInterface.dismiss();
                    }
                });

                String[] items={"Name (A-Z)","Size (Big-Small)","Date (New - Old)","Length (Long - Short)"};
                alertDialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                editor.putString("sort","sortName");
                                break;
                            case 1:
                                editor.putString("sort","sortSize");
                                break;
                            case 2:
                                editor.putString("sort","sortDate");
                                break;
                            case 3:
                                editor.putString("sort","sortLength");
                                break;
                        }
                    }
                });



                alertDialog.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        String inputs=s.toLowerCase();
        ArrayList<MediaFiles> mediaFiles=new ArrayList<>();
        for (MediaFiles media:videoFilesList) {
            if (media.getTitle().toLowerCase().contains(inputs)){
                mediaFiles.add(media);
            }
        }
        adapter.updateVideoFiles(mediaFiles);
        return true;
    }
}