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
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import androidx.core.app.NotificationCompat;

public class MusicService extends Service {
    private final static String TAG = "MusicService";
    private static final String CHANNEL_ID = "musicPlayer";
    private final IBinder mBinder = new MusicServiceBinder();
    MediaPlayer mediaPlayer;

    public class MusicServiceBinder extends Binder {
        // 클라이언트에게 서비스 객체를 주기 위함
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Start service in onBind");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(false);
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service Start");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service End");
        super.onDestroy();
    }

    public void prepareMusic(ArrayList<Mp3Data> mp3DataArrayList, int index) {
        Mp3Data mp3Data = mp3DataArrayList.get(index);

        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
        }
        Log.d(TAG, "isPlaying : " + mediaPlayer.isPlaying());
        Uri uri = Uri.parse(mp3Data.getPath() + "/" + mp3Data.getmId());

        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void playMusic(ArrayList<Mp3Data> mp3DataArrayList, int index) {
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                notifications("음악 플레이테스트", "백그라운드에서 음악이 플레이되고있어요");
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // 다음 작업
                nextMusic(mp3DataArrayList, index+1);
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                // 다음작업이나 에러알림
                return false;
            }
        });
    }

    public void pauseMusic() {
        mediaPlayer.pause();
    }

    public void reStartMusic() {
        mediaPlayer.start();
    }

    public void nextMusic(ArrayList<Mp3Data> mp3DataArrayList, int index) {
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        // 다음곡 선택하기
        prepareMusic(mp3DataArrayList, index);
    }

    public void prevMusic(ArrayList<Mp3Data> mp3DataArrayList, int index) {
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        prepareMusic(mp3DataArrayList, index);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPositions() {
        return mediaPlayer.getCurrentPosition();
    }

    public void setSeekTo(int time) {
        mediaPlayer.seekTo(time);
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