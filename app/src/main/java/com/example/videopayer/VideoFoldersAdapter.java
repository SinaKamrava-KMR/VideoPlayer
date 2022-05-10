package com.example.videopayer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VideoFoldersAdapter extends RecyclerView.Adapter<VideoFoldersAdapter.VideoViewHolder> {

    private List<MediaFiles> mediaFiles;
    private List<String> folderPath;
    private Context context;

    public VideoFoldersAdapter(List<MediaFiles> mediaFiles, List<String> folderPath, Context context) {
        this.mediaFiles = mediaFiles;
        this.folderPath = folderPath;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.folder_item,parent,false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {

        //Data in folderPath list is like this " /storage/emulated/0/Pictures/Telegram " and we have to separate the last piece
        int indexPath=folderPath.get(position).lastIndexOf("/");
        String nameOFFolder=folderPath.get(position).substring(indexPath+1);
        Log.i("cursor_data", "onBindViewHolder name of folder : "+nameOFFolder);
        holder.folderName.setText(nameOFFolder);
        holder.folderPath.setText(folderPath.get(position));
        holder.noOfFiles.setText(noOfFiles(folderPath.get(position))+" Videos");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,VideoFilesActivity.class);
                intent.putExtra("folderName",nameOFFolder);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return folderPath.size();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

         TextView folderName,folderPath,noOfFiles;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName=itemView.findViewById(R.id.folder_name);
            folderPath=itemView.findViewById(R.id.folder_path);
            noOfFiles=itemView.findViewById(R.id.noOfFiles);
        }
    }

    public int noOfFiles(String folder_name){
        int files_num=0;
        for (MediaFiles mediaFiles:mediaFiles) {
            if (mediaFiles.getPath().substring(0,mediaFiles.getPath().lastIndexOf("/"))
            .endsWith(folder_name)){
            files_num++;
            }
        }
        return files_num;

    }

}
