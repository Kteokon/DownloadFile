package com.example.downloadfile;

import android.content.ContentUris;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileListAdapter extends BaseAdapter {
    Context ctx;
    File[] files;

    public FileListAdapter(Context ctx, File[] files) {
        this.ctx = ctx;
        this.files = files;
    }

    @Override
    public int getCount() {
        return files.length;
    }

    @Override
    public Object getItem(int position) {
        return files[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        File f = files[position];

//        music = new MediaPlayer();
//
//        convertView = LayoutInflater.from(ctx).
//                inflate(R.layout.file_item, parent, false);
//        TextView tvName = convertView.findViewById(R.id.name);
//
//        tvName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                long id = m.getId();
//                Uri contentUri = ContentUris.withAppendedId(
//                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
//                try {
//                    if (music.isPlaying()){
//                        music.stop();
//                        music = new MediaPlayer();
//                    }
//                    music.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                    music.setDataSource(ctx, contentUri);
//                    music.prepare();
//                    music.start();
//                } catch (IOException e) {
//                    Log.d("mytag", "error: " + e.getLocalizedMessage());
//                    e.printStackTrace();
//                }
//                //v.setBackgroundColor(Color.RED);
//            }
//        });
//
//        tvName.setText(m.getName());
        return convertView;
    }
}
