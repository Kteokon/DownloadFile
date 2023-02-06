package com.example.downloadfile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LoadMusicActivity extends AppCompatActivity {

    ListView lv;
    FileListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_file);

        lv = findViewById(R.id.list);

        String button = getIntent().getStringExtra("button");

        if (button.equals("music")) {
            musicList();
        }
        else {
            lyricsFilesListStAccFr();
        }
    }

    private void musicList() {
        //Path path = Paths.get("/storage/emulated/0/Android");

        //String path = MediaStore.Files.getContentUri("external").getPath();
        //Uri uri = MediaStore.Files.getContentUri("external");

        ArrayList<Music> musicList = new ArrayList<>();

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

// every column, although that is huge waste, you probably need
// BaseColumns.DATA (the path) only.
        //String[] projection = null;

        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

// exclude media files, they would be here also.
        String selection = MediaStore.Audio.Media.IS_MUSIC;
        String[] selectionArgs = null; // there is no ? in selection so null here

        String sortOrder = null; // unordered
        ContentResolver cr = this.getContentResolver();

        try (Cursor cursor = cr.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                musicList.add(new Music(id, contentUri, name, duration));
            }
        }

        for (int i = 0; i < musicList.size(); i++) {
            Log.d("mytag", musicList.get(i).getPath() + " " + musicList.get(i).getName());
        }

        MusicListAdapter adapter = new MusicListAdapter(this, musicList);

        lv.setAdapter(adapter);

        //Cursor c = cr.query(uri, projection, null, selectionArgs, sortOrder);
        //Log.d("mytag",  Integer.toString(c.getCount()) + " files");
        //Log.d("mytag", String.valueOf(c.getColumnNames()));

//        while (c.moveToNext()) {
//            c.getType(2);
//        }

        //SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.file_item, c, fields, views);
        //lv.setAdapter(adapter);

//        String path = getIntent().getStringExtra("path");
//
    }

    // Не видит текстовые файлы
    private void lyricsFilesListMSt() {
        ArrayList<LyricsFile> filesList = new ArrayList<>();

        Uri collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;

        String sortOrder = null; // unordered

        ContentResolver cr = this.getContentResolver();

        try (Cursor cursor = cr.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"), id);
                String name = cursor.getString(nameColumn);
                filesList.add(new LyricsFile(id, contentUri, name));
            }
        }

        for (int i = 0; i < filesList.size(); i++) {
            Log.d("mytag", filesList.get(i).getPath() + " " + filesList.get(i).getName());
        }

    }

    // Не совсем подходит, т.к. пользователи обычно складируют файлы в Music, а не в далёкой папке приложения
    private void lyricsFilesListGExtFD() {
        File dir = this.getExternalFilesDir(null);
        Log.d("mytag", dir.getAbsolutePath());
    }


    private void lyricsFilesListStAccFr() {
        int PICK_FILE = 2;

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimetypes = {"audio/mpeg", "audio/x-wav"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, "/external/file/"); //

        startActivityForResult(intent, PICK_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.

            Uri uri = null;

//            if(resultData != null) {
//                uri = resultData.getData();
//                File f = new File(uri.getPath());
//                try {
//                    InputStream in = getContentResolver().openInputStream(uri);
//
//                    BufferedReader r = new BufferedReader(new InputStreamReader(in));
//                    StringBuilder total = new StringBuilder();
//                    for (String line; (line = r.readLine()) != null; ) {
//                        total.append(line).append('\n');
//                    }
//                    String content = total.toString();
//                    Log.d("mytag", content);
//                }catch (Exception e) {
//
//                }

//                try {
//                    File f = new File(text);
//                    Log.d("mytag", f.getName());
//                    FileInputStream is = new FileInputStream(f); // Fails on this line
//                    int size = is.available();
//                    byte[] buffer = new byte[size];
//                    is.read(buffer);
//                    is.close();
//                    text = new String(buffer);
//                    Log.d("mytag", text);
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }

            MediaPlayer music = new MediaPlayer();
            if (resultData != null) {
                uri = resultData.getData();
                File f = new File(uri.toString());
                Log.d("mytag", uri.toString());
                Uri uri2 = Uri.parse(uri.toString());
                try {
                    if (music.isPlaying()){
                        music.stop();
                        music = new MediaPlayer();
                    }
                    music.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    music.setDataSource(this, uri2);
                    music.prepare();
                    music.start();
                } catch (IOException e) {
                    Log.d("mytag", "error: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void lyricsFilesListScSt() {

        Cursor cursor = this.getContentResolver()
                .query(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), null, null, null, null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows. Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name". This is
                // provider-specific, and might not necessarily be the file name.
                String displayName = cursor.getString(
                        cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
                Log.i("mytag", "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null. But because an
                // int can't be null, the behavior is implementation-specific,
                // and unpredictable. So as
                // a rule, check if it's null before assigning to an int. This will
                // happen often: The storage API allows for remote files, whose
                // size might not be locally known.
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i("mytag", "Size: " + size);
            }
        } finally {
            cursor.close();
        }
    }

    // Получаем доступ только к файлам из внутренней памяти
    // Устаревший способ
    private void lyricsFilesListGExtStD() {

        /*StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
        List <StorageVolume> svl = sm.getStorageVolumes();
        StorageVolume sv = svl.get(0); // 1 для SD, 0 для внутреннего
        File f = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            f = new File(sv.getDirectory().getPath());
        }
        Log.d("mytag", f.getAbsolutePath());*/

        String path = Environment.getExternalStorageDirectory().getPath() + "/Music";
        File root = new File(path);
        File[] filesAndFolders = root.listFiles();
        if (filesAndFolders.equals(null) || filesAndFolders.length == 0) {
            Log.d("mytag", "No files in " + path);
        }
        else {
            Log.d("mytag", "There's " + Integer.toString(filesAndFolders.length) + " files in " + path);
        }

        for (int i = 0; i < filesAndFolders.length; i++){
            Log.d("mytag", filesAndFolders[i].getName());
        }

        adapter = new FileListAdapter(this, filesAndFolders);
        lv.setAdapter(adapter);
//        ArrayList<LyricsFile> lyricsList = new ArrayList<>();
//        Uri collection;
//
//        collection = MediaStore.Files.getContentUri("external");
//
//        String[] projection = null;
//
//        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
//        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
//        String[] selectionArgs = new String[]{ mimeType };
//
//        String sortOrder = null;
//        ContentResolver cr = this.getContentResolver();
//
//        try (Cursor cursor = cr.query(
//                collection,
//                projection,
//                selection,
//                selectionArgs,
//                sortOrder
//        )) {
//            Log.d("mytag", Integer.toString(cursor.getCount()));
//            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
//            int nameColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
//
//            while (cursor.moveToNext()) {
//                long id = cursor.getLong(idColumn);
//                Uri contentUri = ContentUris.withAppendedId(
//                        MediaStore.Files.getContentUri("external"), id);
//                String name = cursor.getString(nameColumn);
//                lyricsList.add(new LyricsFile(id, contentUri, name));
//            }
//        }
//
//        for (int i = 0; i < lyricsList.size(); i++) {
//            Log.d("mytag", lyricsList.get(i).getPath() + " " + lyricsList.get(i).getName());
//        }
    }
}