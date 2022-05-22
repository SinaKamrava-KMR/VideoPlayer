package com.example.videopayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;

import com.google.android.exoplayer2.ui.PlayerView;

import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;


import java.io.File;

import java.util.ArrayList;

import java.util.Objects;


public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<MediaFiles> mVideoFiles = new ArrayList<>();
    private PlayerView playerView;
    private ConcatenatingMediaSource concatenatingMediaSource;
    private SimpleExoPlayer player;
    private int position;
    private String videoTitle;
    private TextView title;
    private ControlsMode controlsMode;


    public enum ControlsMode {
        LOCK, FULLSCREEN;
    }

    private ImageView nextButton, previousButton;
    private ImageView videoBack, lock, unlock, scaling;
    private RelativeLayout root;

    //horizontal recycler variables

    private ArrayList<IconModel> iconModelArrayList = new ArrayList<>();
    private PlaybackIconsAdapter iconsAdapter;
    private RecyclerView recyclerViewIcons;
    private boolean expand = false;
    private View nightMode;
    private boolean dark = false;
    private boolean mute=false;
    private PlaybackParameters parameters;
    private float speed;

    private DialogProperties dialogProperties;
    private FilePickerDialog filePickerDialog;
    private Uri uriSubtitle;
    //horizontal recycler variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_video_player);
        Objects.requireNonNull(getSupportActionBar()).hide();

        playerView = findViewById(R.id.exoplayer_view);
        title = findViewById(R.id.video_title);
        nextButton = findViewById(R.id.exo_next);
        previousButton = findViewById(R.id.exo_prev);

        //==============

        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unLock);
        scaling = findViewById(R.id.exo_scaling);
        root = findViewById(R.id.root_layout);

        nightMode = findViewById(R.id.night_mode);

        //============
        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);

        position = getIntent().getIntExtra("position", 1);
        videoTitle = getIntent().getStringExtra("video_title");
        mVideoFiles = getIntent().getExtras().getParcelableArrayList("videoArrayList");
        screenOrientation();

        recyclerViewIcons = findViewById(R.id.recyclerView_icon);

        dialogProperties=new DialogProperties();
        filePickerDialog=new FilePickerDialog(VideoPlayerActivity.this);
        filePickerDialog.setTitle("Select a Subtitle File");
        filePickerDialog.setPositiveBtnName("Ok");
        filePickerDialog.setNegativeBtnName("Cancel");



        iconModelArrayList.add(new IconModel(R.drawable.ic_right, ""));
        iconModelArrayList.add(new IconModel(R.drawable.ic_night, "Night"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_volume_off, "Mute"));
        iconModelArrayList.add(new IconModel(R.drawable.ic_rotation, "Rotate"));

        iconsAdapter = new PlaybackIconsAdapter(iconModelArrayList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);
        recyclerViewIcons.setLayoutManager(linearLayoutManager);
        recyclerViewIcons.setAdapter(iconsAdapter);
        iconsAdapter.notifyDataSetChanged();
        iconsAdapter.setOnItemClickListener(new PlaybackIconsAdapter.OnItemClickListener() {
            @SuppressLint("Range")
            @Override
            public void onItemClick(int position) {

                //left or right icon for colapsig
                if (position == 0) {
                    if (expand) {
                        iconModelArrayList.clear();
                        iconModelArrayList.add(new IconModel(R.drawable.ic_right, ""));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_night, "Night"));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_volume_off, "Mute"));
                        iconModelArrayList.add(new IconModel(R.drawable.ic_rotation, "Rotate"));
                        iconsAdapter.notifyDataSetChanged();
                        expand = false;
                    } else {
                        if (iconModelArrayList.size() == 4) {
                            iconModelArrayList.add(new IconModel(R.drawable.ic_volume, "Volume"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_brightness, "Brightness"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_speed, "Speed"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_subtitles, "Subtitle"));
                        }
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_left, ""));
                        iconsAdapter.notifyDataSetChanged();
                        expand = true;
                    }
                }
                //night mode
                if (position == 1) {
                    if (dark) {
                        nightMode.setVisibility(View.GONE);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_night, "Night"));
                        iconsAdapter.notifyDataSetChanged();
                        dark = false;
                    } else {
                        nightMode.setVisibility(View.VISIBLE);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_night, "Day"));
                        iconsAdapter.notifyDataSetChanged();
                        dark = true;
                    }
                }
                //mute or unMute
                if (position == 2) {
                    if (mute){
                        player.setVolume(100);
                        iconModelArrayList.set(position,new IconModel(R.drawable.ic_volume_off,"Mute"));
                        iconsAdapter.notifyDataSetChanged();
                        mute=false;
                    }else {
                        player.setVolume(0);
                        iconModelArrayList.set(position,new IconModel(R.drawable.ic_volume,"UnMute"));
                        iconsAdapter.notifyDataSetChanged();
                        mute=true;
                    }
                }
                //ORIENTATION Landscape
                if (position == 3) {
                    if (getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        iconsAdapter.notifyDataSetChanged();
                    }else if (getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        iconsAdapter.notifyDataSetChanged();
                    }

                }

                //volume
                if (position==4){
                    VolumeDialog volumeDialog=new VolumeDialog();
                    volumeDialog.show(getSupportFragmentManager(),"Dialog");
                    iconsAdapter.notifyDataSetChanged();

                }
                //brightness
                if (position==5){
                    BrightnessDialog brightnessDialog=new BrightnessDialog();
                    brightnessDialog.show(getSupportFragmentManager(),"dialog");
                    iconsAdapter.notifyDataSetChanged();

                }
                //Equalizer
                if (position==6){

                    Intent intent=new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                    if ((intent).resolveActivity(getPackageManager())!=null){
                        startActivityForResult(intent,123);
                    }else {
                        Toast.makeText(VideoPlayerActivity.this, "No Equalizer Found", Toast.LENGTH_SHORT).show();
                    }
                    iconsAdapter.notifyDataSetChanged();
                }

                //speed
                if (position==7){
                    AlertDialog.Builder alertDialog=new AlertDialog.Builder(VideoPlayerActivity.this);
                    alertDialog.setTitle("Select Playback speed")
                            .setPositiveButton("Ok",null);
                    String[] items={"0.5x","1x Normal Speed","1.25x","1.5x","2x"};
                    int checkedItem=-1;
                    alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i){
                                case 0:
                                    speed=0.5f;
                                    parameters=new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 1:
                                    speed=1f;
                                    parameters=new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 2:
                                    speed=1.25f;
                                    parameters=new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 3:
                                    speed=1.5f;
                                    parameters=new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 4:
                                    speed=2f;
                                    parameters=new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });

                    AlertDialog alert= alertDialog.create();
                    alert.show();

                }

                //subtitle
                if (position==8){

                    //it means you can select one subtitle file
                    dialogProperties.selection_mode= DialogConfigs.SINGLE_MODE;
                    dialogProperties.extensions=new String[]{".srt"};
                    dialogProperties.root=new File("/storage/emulated/0");
                    filePickerDialog.setProperties(dialogProperties);
                    filePickerDialog.show();
                    filePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
                        @Override
                        public void onSelectedFilePaths(String[] files) {
                            for (String path :files) {
                                File file=new File(path);
                                uriSubtitle=Uri.parse(file.getAbsolutePath().toString());

                            }
                            playVideoSubtitle(uriSubtitle);
                        }
                    });

                }
            }
        });

        title.setText(videoTitle);

        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        scaling.setOnClickListener(firstListener);

        playVideo();

    }

    private void playVideo() {
        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i < mVideoFiles.size(); i++) {
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        if (parameters!=null){
            player.setPlaybackParameters(parameters);
        }
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);

        playError();
    }

    private void playVideoSubtitle(Uri subtitle) {
        long oldPosition=player.getCurrentPosition();
        player.stop();

        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i < mVideoFiles.size(); i++) {
           new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));





            MediaItem.SubtitleConfiguration configuration= new MediaItem.SubtitleConfiguration
                    .Builder(Uri.parse(String.valueOf(subtitle)))
                    .setMimeType(MimeTypes.APPLICATION_SUBRIP) // The correct MIME type (required).
                    .setLanguage("app") // The subtitle language (optional).
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)// Selection flags for the track (optional).
                    .build();



             MediaSource subtitleSource=new SingleSampleMediaSource.Factory(dataSourceFactory).setTreatLoadErrorsAsEndOfStream(true)
                     .createMediaSource(configuration,C.TIME_UNSET);


            // Plays the video with the side loaded subtitle.
            MergingMediaSource marginMediaSource =
                    new MergingMediaSource(mediaSource, subtitleSource);


            concatenatingMediaSource.addMediaSource(marginMediaSource);


        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        if (parameters!=null){
            player.setPlaybackParameters(parameters);
        }
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, oldPosition);

        playError();
    }





    private void screenOrientation(){
        try {
            MediaMetadataRetriever retriever=new MediaMetadataRetriever();
            Bitmap bitmap;
            String path=mVideoFiles.get(position).getPath();
            Uri uri=Uri.parse(path);
            retriever.setDataSource(this,uri);
            bitmap=retriever.getFrameAtTime();

            int videoWidth= bitmap.getWidth();
            int videoHeight=bitmap.getHeight();
            if (videoWidth>videoHeight){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

        }catch (Exception e){
            Log.e("MediaMetadataRetriever", "screenOrientation Error in Try Catch : "+e.getMessage() );
        }
    }

    private void playError() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                // Player.Listener.super.onPlayerError(error);
                Toast.makeText(VideoPlayerActivity.this, "Video Playing Error", Toast.LENGTH_SHORT).show();
            }
        });
        player.setPlayWhenReady(true);
    }

    @Override
    public void onBackPressed() {
        if (player.isPlaying()) {
            player.stop();
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        player.setPlayWhenReady(false);
        player.getPlaybackState();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();

    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video_back:
                if (player != null) {
                    player.release();
                }
                finish();
                break;
            case R.id.lock:
                controlsMode = ControlsMode.FULLSCREEN;
                root.setVisibility(View.VISIBLE);
                lock.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "unlocked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.unLock:
                controlsMode = ControlsMode.LOCK;
                root.setVisibility(View.INVISIBLE);
                lock.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();

                break;
            case R.id.exo_next:
                try {
                    player.stop();
                    position++;
                    playVideo();
                } catch (Exception e) {
                    Toast.makeText(this, "No Next Video", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case R.id.exo_prev:
                try {
                    player.stop();
                    position--;
                    playVideo();
                } catch (Exception e) {
                    Toast.makeText(this, "No Previous Video", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;


        }

    }

    View.OnClickListener firstListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fullscreen);

            Toast.makeText(VideoPlayerActivity.this, "Full Screen", Toast.LENGTH_SHORT).show();

            scaling.setOnClickListener(secondListener);
        }
    };

    View.OnClickListener secondListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.zoom);
            Toast.makeText(VideoPlayerActivity.this, "Zoom", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(thirdListener);
        }
    };

    View.OnClickListener thirdListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fit);

            Toast.makeText(VideoPlayerActivity.this, "Fit", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(firstListener);
        }
    };
}