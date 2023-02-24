package com.example.downloadfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoadMusicActivity extends AppCompatActivity {
    Button playButton;
    ListView lv;
    FileListAdapter adapter;
    ExoPlayer exoPlayer;
    FirebaseStorage storage;

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
            if (button.equals("file")) {
                lyricsFilesListStAccFr();
            }
            else {
                try {
                    playButton = findViewById(R.id.playButton);
                    getFilesFirebase();
                } catch (IOException e) {

                    Log.d("mytag", "Some error");
                    e.printStackTrace();
                }
            }
        }
    }

    private void musicList() {
        //Path path = Paths.get("/storage/emulated/0/Android");

        //String path = MediaStore.Files.getContentUri("external").getPath();
        //Uri uri = MediaStore.Files.getContentUri("external");

        ArrayList<MyMusic> musicList = new ArrayList<>();

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC;
        String[] selectionArgs = null;

        String sortOrder = null;
        ContentResolver cr = this.getContentResolver();

        try (Cursor cursor = cr.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);

                musicList.add(new MyMusic(id, contentUri, name, duration));
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
        ArrayList<MyLyricsFile> filesList = new ArrayList<>();

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
                filesList.add(new MyLyricsFile(id, contentUri, name));
            }
        }

        for (int i = 0; i < filesList.size(); i++) {
            Log.d("mytag", filesList.get(i).getPath() + " " + filesList.get(i).getName());
        }

    }

    // Не совсем подходит, т.к. пользователи обычно складируют файлы в Music, а не в далёкой папке приложения
    private void lyricsFilesListGetFD() {
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
//        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, "/external/file/");

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

            if(resultData != null) {
                uri = resultData.getData();

                String userAgent = Util.getUserAgent(this, "Exo");

                exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
                MediaItem item = MediaItem.fromUri(uri);
                exoPlayer.setMediaItem(item);
                exoPlayer.prepare();
                exoPlayer.play();

//                File f = new File(uri.getPath());
//                try {
//                    Log.d("mytag", f.getName());

//                    InputStream in = getContentResolver().openInputStream(uri);
//
//                    BufferedReader r = new BufferedReader(new InputStreamReader(in));
//                    StringBuilder total = new StringBuilder();
//                    for (String line; (line = r.readLine()) != null; ) {
//                        total.append(line).append('\n');
//                    }
//                    String content = total.toString();
//                    Log.d("mytag", content);
//
//                    String mimeType = null;
//                    if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
//                        ContentResolver cr = this.getContentResolver();
//                        mimeType = cr.getType(uri);
//                        Log.d("mytag", "we're here");
//                    } else {
//                        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
//                                .toString());
//                        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
//                                fileExtension.toLowerCase());
//                        Log.d("mytag", "else");
//                    }
//                    Log.d("mytag", mimeType);

//                } catch (Exception e) {
//                    Log.d("mytag", "some error");
//                }
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

                String displayName = cursor.getString(
                        cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
                Log.i("mytag", "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
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

    private void getFilesGoogleDrive() {
        String fileId = "1AGYaKRvNk1jiqGOZJE6jg71BwGs2DC0B";
        String theKey = "AIzaSyCOtrNRh8aQ4W05AbrFbFr99XxLnkQiX-E";
        //GoogleDriveResponse response = null;
        try {
            URL url = new URL("https://www.googleapis.com/drive/v3/files/" + fileId);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("x-goog-api-key", theKey);
            urlConnection.setDoOutput(true);

            OutputStream stream = urlConnection.getOutputStream();
            String postData = "";
            stream.write(postData.getBytes());

            InputStream inputStream = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);

            Gson gson = new Gson();
            //response = gson.fromJson(reader, GoogleDriveResponse.class);
            urlConnection.disconnect();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            urlConnection.setDoOutput(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        String res = "";
    }

    private void getFilesFirebase() throws IOException {
        boolean download = true;
        if (download) {
            DownloadMusicTask task = new DownloadMusicTask();
            task.execute();
        }

//        StorageReference gsReference = storage.getReferenceFromUrl(spaceRef.getRoot().toString());
//
//        spaceRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                Log.d("mytag", "Success " + uri.toString());
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                Log.d("mytag", "Fail");
//            }
//        });

//        File localFile = File.createTempFile("music", "");
//        spaceRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                Log.d("mytag", "Success");
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                Log.d("mytag", "Fail");
//            }
//        });
    }

    class DownloadMusicTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            List<File> files = new ArrayList<>();
            storage = FirebaseStorage.getInstance();
            StorageReference listRef = storage.getReference().child("music");

            listRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                @Override
                public void onSuccess(ListResult listResult) {
                    Log.d("mytag", "Success");
                    for (StorageReference item : listResult.getItems()) {
                        Log.d("mytag", "Item: " + item.getName());

                        File rootPath = new File(Environment.getExternalStorageDirectory(), "Music");
                        if(!rootPath.exists()) {
                            rootPath.mkdirs();
                        }

                        final File localFile = new File(rootPath, item.getName());

                        item.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Log.d("mytag", localFile.getName() + " " + localFile.getPath());
                                files.add(localFile);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("mytag", "Fail while downloading");
                            }
                        });
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("mytag", "Fail while accessing to the folder");
                }
            });
            return null;
        }
    }
}