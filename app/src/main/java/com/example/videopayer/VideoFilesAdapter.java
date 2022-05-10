package com.example.videopayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.VideoBitmapDecoder;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.cert.Extension;
import java.util.ArrayList;
import java.util.List;

public class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.VideoViewHolder> {


    private List<MediaFiles> videoList;
    private Context context;
    private BottomSheetDialog bottomSheetDialog;
    double milliSeconds ;

    public VideoFilesAdapter(List<MediaFiles> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }


    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoViewHolder(LayoutInflater.from(context).inflate(R.layout.video_item,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.videoName.setText(videoList.get(position).getDisplayName());
        String size=videoList.get(position).getSize();
        holder.videoSize.setText(android.text.format.Formatter.formatFileSize(context, Long.parseLong(size)));

        try{
            milliSeconds= Double.parseDouble(videoList.get(position).getDuration());
            holder.videoDuration.setText(timeConversion((long) milliSeconds));

        }catch (Exception e){
            Log.i("Exception", " error message : "+e.getMessage());
        }

        Glide.with(context)
                .load(new File(videoList.get(position).getPath()))
                .error(R.mipmap.vpicon)
                .into(holder.thumbnail);


        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               bottomSheetDialog=new BottomSheetDialog(context,R.style.BottomSheetTheme);
               View bsView=LayoutInflater.from(context).inflate(R.layout.video_bs_layout, view.findViewById(R.id.bottom_Sheet),false);
               bsView.findViewById(R.id.bs_play).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.itemView.performClick();
                        bottomSheetDialog.dismiss();
                    }
                });

               bsView.findViewById(R.id.bs_rename).setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       AlertDialog.Builder alertBuilder=new AlertDialog.Builder(context);
                       alertBuilder.setTitle("Rename to");
                       EditText editText=new EditText(context);
                       String path=videoList.get(position).getPath();
                       final  File file=new File(path);
                       String videoName=file.getName();
                       videoName=videoName.substring(0,videoName.lastIndexOf("."));
                       editText.setText(videoName);
                       alertBuilder.setView(editText);
                       editText.requestFocus();
                       alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                                if (TextUtils.isEmpty(editText.getText().toString())){
                                    Toast.makeText(context, "Can't rename empty file", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String onlyPath= file.getParentFile().getAbsolutePath();
                                String ext=file.getAbsolutePath();
                                ext=ext.substring(ext.lastIndexOf("."));
                                String newPath=onlyPath+"/"+editText.getText().toString()+ext;
                                File newFile=new File(newPath);
                                boolean rename= file.renameTo(newFile);
                                if (rename){
                                    ContentResolver resolver=context.getApplicationContext().getContentResolver();
                                    resolver.delete(MediaStore.Files.getContentUri("external"),MediaStore.MediaColumns.DATA+"=?",
                                            new String[]{file.getAbsolutePath()});
                                    Intent intent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    intent.setData(Uri.fromFile(newFile));
                                    context.getApplicationContext().sendBroadcast(intent);
                                    notifyDataSetChanged();

                                    Toast.makeText(context, "Video Renamed", Toast.LENGTH_SHORT).show();

                                    SystemClock.sleep(200);
                                    ((Activity) context).recreate();
                                }else {
                                    Toast.makeText(context, "Progress Failed", Toast.LENGTH_SHORT).show();

                                }
                           }
                       });
                       alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               dialogInterface.dismiss();
                           }
                       });
                       alertBuilder.create().show();
                       bottomSheetDialog.dismiss();
                   }
               });

               bsView.findViewById(R.id.bs_share).setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       Uri uri=Uri.parse(videoList.get(position).getPath());
                       Intent shareIntent=new Intent(Intent.ACTION_SEND);
                       shareIntent.setType("video/*");
                       shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
                       context.startActivity(Intent.createChooser(shareIntent,"Share Video"));
                       bottomSheetDialog.dismiss();
                   }
               });

               bsView.findViewById(R.id.bs_delete).setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       AlertDialog.Builder alertDialog=new AlertDialog.Builder(context);
                       alertDialog.setTitle("Delete");
                       alertDialog.setMessage("Do you want to delete this video ?");
                       alertDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               Uri contentUri= ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                               , Long.parseLong(videoList.get(position).getId()));

                               File file=new File(videoList.get(position).getPath());
                               boolean delete=file.delete();
                               if (delete){
                                   context.getContentResolver().delete(contentUri,null,null);
                                   videoList.remove(position);
                                   notifyItemRemoved(position);
                                   notifyItemRangeChanged(position,videoList.size());
                                   Toast.makeText(context, "Video Deleted ", Toast.LENGTH_SHORT).show();

                               }else {
                                   Toast.makeText(context, "Can't Deleted this video !!!", Toast.LENGTH_SHORT).show();

                               }
                           }
                       });
                       alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               dialogInterface.dismiss();
                           }
                       });
                       alertDialog.create().show();
                       bottomSheetDialog.dismiss();
                   }
               });

               bsView.findViewById(R.id.bs_properties).setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       AlertDialog.Builder alertDialog=new AlertDialog.Builder(context);
                       alertDialog.setTitle("Properties");

                       //===================================
                       String one="File: "+videoList.get(position).getDisplayName();
                       String path=videoList.get(position).getPath();
                       int indexPath=path.lastIndexOf("/");
                       String two="path: "+path.substring(0,indexPath);
                       String three="Size : "+android.text.format.Formatter.formatFileSize(context, Long.parseLong(videoList.get(position).getSize()));

                       String four="Length : "+timeConversion((long) milliSeconds);
                       String nameWithFormat=videoList.get(position).getDisplayName();
                       int index=nameWithFormat.lastIndexOf(".");
                       String format=nameWithFormat.substring(index+1);
                       String five="Format : "+format;

                       MediaMetadataRetriever metadataRetriever=new MediaMetadataRetriever();
                       metadataRetriever.setDataSource(videoList.get(position).getPath());
                       String height=metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                       String width =metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

                       String six="Resolution : "+width+"x"+height;

                       //=========================================================

                       alertDialog.setMessage(one+"\n\n"+two+"\n\n"+three+"\n\n"+four+"\n\n"+five+"\n\n"+six);
                       alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               dialogInterface.dismiss();
                           }
                       });

                       alertDialog.create().show();
                       bottomSheetDialog.dismiss();
                   }
               });

               bottomSheetDialog.setContentView(bsView);
               bottomSheetDialog.show();
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,VideoPlayerActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("video_title",videoList.get(position).getDisplayName());
                Bundle bundle=new Bundle();
                bundle.putParcelableArrayList("videoArrayList", (ArrayList<? extends Parcelable>) videoList);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return videoList.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder{

         ImageView thumbnail,menuMore;
         TextView videoName, videoSize,videoDuration;
        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail=itemView.findViewById(R.id.thumbnail);
            menuMore=itemView.findViewById(R.id.video_menu_more);
            videoName=itemView.findViewById(R.id.video_name);
            videoSize=itemView.findViewById(R.id.video_size);
            videoDuration=itemView.findViewById(R.id.video_duration);

        }
    }

    @SuppressLint("DefaultLocale")
    public String timeConversion(long value){
        String videoTime;
        int duration= (int) value;
        int hrs=(duration/3600000);
        int mns=(duration/60000)%60000;
        int scs=duration%60000/1000;
        if (hrs>0){
            videoTime=String.format("%02d:%02d:%02d",hrs,mns,scs);
            Log.i("StringFormat", "timeConversion:  hrs : "+hrs +"/ mns : "+mns+"/scs: "+scs);
        }else  {
            videoTime=String.format("%02d:%02d",mns,scs);
            Log.i("StringFormat", "timeConversion in Else:  hrs : "+hrs +"/ mns : "+mns+"/scs: "+scs);

        }

        return videoTime;
    }

    public void updateVideoFiles(ArrayList<MediaFiles> files){
        videoList=new ArrayList<>();
        videoList.addAll(files);
        notifyDataSetChanged();

    }
}
