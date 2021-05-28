package com.example.newmusicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

public class FileActivity extends AppCompatActivity {
    private static final String TAG = "FileActivity";
    ListView listView;
    ListViewAdapter adapter = new ListViewAdapter();
    ArrayList<Mp3Data> mp3DataArrayList = new ArrayList<>();
    Mp3Data mp3Data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        checkPermission();

        listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        queryMp3Files(this);

        for(Mp3Data mp3Data : mp3DataArrayList) {
            adapter.addItem(
                    mp3Data.getmId(),
                    mp3Data.getAlbumId(),
                    mp3Data.getTitle(),
                    mp3Data.getArtist(),
                    mp3Data.getAlbum(),
                    mp3Data.getDuration(),
                    mp3Data.getPath());
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListViewItem item = (ListViewItem) parent.getItemAtPosition(position);

                mp3Data = new Mp3Data(item.getmId(), item.getAlbumId(), item.getTitle(), item.getArtist(),
                                        item.getAlbum(), item.getDuration(), item.getPath());

                Intent intent = new Intent(FileActivity.this, MainActivity.class);

                intent.putExtra("mp3DataArrayList", mp3DataArrayList);
                intent.putExtra("mp3Data", mp3Data);
                intent.putExtra("index", position);
                startActivity(intent);
            }
        });
    }


//    public void allProjections() {
//        String[] mProjection = {
//                MediaStore.Audio.Media.ALBUM,
//                MediaStore.Audio.Media.ALBUM_ID,
//                MediaStore.Audio.Media.ALBUM_KEY,
//                MediaStore.Audio.Media.ARTIST,
//                MediaStore.Audio.Media.ARTIST_ID,
//                MediaStore.Audio.Media.ARTIST_KEY,
//                MediaStore.Audio.Media.BOOKMARK,
//                MediaStore.Audio.Media.COMPOSER,
//                MediaStore.Audio.Media.DURATION,
//                MediaStore.Audio.Media.IS_ALARM,
//                MediaStore.Audio.Media.IS_MUSIC,
//                MediaStore.Audio.Media.IS_NOTIFICATION,
//                MediaStore.Audio.Media.IS_PODCAST,
//                MediaStore.Audio.Media.IS_RINGTONE,
//                MediaStore.Audio.Media.TITLE_KEY,
//                MediaStore.Audio.Media.TRACK,
//                MediaStore.Audio.Media.YEAR
//        };
//    }

//    SELECT * FROM audio WHERE _data LIKE '/storage/emulated/0/Download/%'

    public void queryMp3Files(Context context) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.MediaColumns.DATA
        };

//        String selection = substr(_data, length('/storage/emulated/0/Download/')+1) NOT LIKE '%/%');
//        String sortOrder = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC";

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
//        while(cursor.moveToNext()) {
//            Log.d(TAG, "PATH: " + cursor.getString(7));
//        }

        while(cursor != null && cursor.moveToNext()) {
            Log.d(TAG, cursor.getInt(0) + cursor.getInt(1) + cursor.getString(2) + cursor.getString(3) + cursor.getString(4) + cursor.getInt(5) + String.valueOf(uri));
            Mp3Data mp3Data = new Mp3Data(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5), String.valueOf(uri));
            mp3DataArrayList.add(mp3Data);
        }
    }



    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 마시멜로우 버전과 같거나 이상이라면
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "외부 저장소 사용을 위해 읽기/쓰기 필요", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]
                                {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                        2);  //마지막 인자는 체크해야될 권한 갯수
            } else {
                //Toast.makeText(this, "권한 승인되었음", Toast.LENGTH_SHORT).show();
            }
        }
    }
}