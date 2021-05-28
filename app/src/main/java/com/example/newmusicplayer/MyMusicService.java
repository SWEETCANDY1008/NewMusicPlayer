package com.example.newmusicplayer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import androidx.core.app.NotificationCompat;

public class MyMusicService extends Service {
    private static final String TAG = "MyMusicService";
    private static final String CHANNEL_ID = "musicPlayer";
    MediaPlayer mediaPlayer;
    Mp3Data mp3Data;
    ArrayList<Mp3Data> mp3DataArrayList = new ArrayList<>();
    int idx;
    Uri uri;


    public MyMusicService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(true);
//        mp3Data = (Mp3Data) intent.getExtras().getSerializable("mp3Data");
        mp3DataArrayList = (ArrayList<Mp3Data>) intent.getExtras().getSerializable("mp3DataArrayList");
        idx = intent.getExtras().getInt("index");
        // 아이템의 인덱스가 넘어와야 한다.

        mp3Data = mp3DataArrayList.get(idx);

        uri = Uri.parse(mp3Data.getPath() + "/" + mp3Data.getmId());
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder();
    }

    class ServiceBinder extends Binder {
        MyMusicService getService() {
            return MyMusicService.this;
        }
    }

    public void playMusic() {
        notifications("음악 플레이테스트", "백그라운드에서 음악이 플레이되고있어요");
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            if(mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pauseMusic() {
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void prevMusic() {
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
//            mediaPlayer.release();

            idx -= 1;
            if(idx < 0){
                idx = mp3DataArrayList.size() - 1;
            }
            mp3Data = mp3DataArrayList.get(idx);
            Uri uri = Uri.parse(mp3Data.getPath() + "/" + mp3Data.getmId());
            try {
                mediaPlayer.setDataSource(getApplicationContext(), uri);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

        } else {
//            Toast.makeText(MainActivity, "재생중이 아닙니다",Toast.LENGTH_LONG).show();
        }
    }


    public void nextMusic() {
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            idx += 1;
            if(idx >= mp3DataArrayList.size()){
                idx = 0;
            }
            mp3Data = mp3DataArrayList.get(idx);
            Uri uri = Uri.parse(mp3Data.getPath() + "/" + mp3Data.getmId());
            try {
                mediaPlayer.setDataSource(getApplicationContext(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.prepareAsync();
        }
    }


    public void stopMusic() {
        if(mediaPlayer != null) {
            notifications("음악 플레이테스트", "백그라운드에서 음악이 정지되었어요");
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public int getCurrentPositions() {
        int time = 0;
        if(mediaPlayer != null) {
            time = mediaPlayer.getCurrentPosition();
        }
        return time;
    }

    public boolean isPlaying() {
        if(mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        } else {
            return false;
        }
    }

    public int getDuration() {
        if(mediaPlayer != null) {
            return mediaPlayer.getDuration();
        } else {
            return 0;
        }
    }


    private void notifications(String contentTitle, String contentText) {
        createNotificationChannel();
        Intent mMainIntent = new Intent(this, MainActivity.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(
                this, 1, mMainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.btn_star)
                        .setContentTitle(contentTitle)
                        .setContentIntent(mPendingIntent)
                        .setContentText(contentText);
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(1, mBuilder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}