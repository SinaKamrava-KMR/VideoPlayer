package com.example.videopayer;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class VolumeDialog extends AppCompatDialogFragment {

    private ImageView close;
    private TextView numVolume;
    private SeekBar seekBar;
    private AudioManager audioManager;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
       LayoutInflater inflater= getActivity().getLayoutInflater();
       View view=inflater.inflate(R.layout.vol_dialog_item,null);
       builder.setView(view);
       getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);


       close=view.findViewById(R.id.vol_close);
       numVolume=view.findViewById(R.id.vol_number);
       seekBar=view.findViewById(R.id.vol_seekbar);

       audioManager= (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
       seekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
       seekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

//       int mediaVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//       int maxVolume=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//
//        double volPre = Math.ceil((((double) mediaVolume / (double) maxVolume) * (double) 100));
//        numVolume.setText(""+volPre);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,i,0);
                int media_Volume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int max_Volume=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                double vol_Pre = Math.ceil((((double) media_Volume / (double) max_Volume) * (double) 100));
                numVolume.setText(""+vol_Pre);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return builder.create();
    }
}
