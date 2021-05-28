package com.example.newmusicplayer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "PlayerActivity";
    TextView mTitle, mArtist, mDuringTime, mRemainingTime;
    Button mPlayBtn, mPrevBtn, mNextBtn;
    SeekBar seekBar;
    ImageView mImageView;
    Mp3Data mp3Data;
    private final Uri artworkUri = Uri.parse("content://media/internal/audio/albumart");
    ArrayList<Mp3Data> mp3DataArrayList = new ArrayList<>();
    Intent intentService;
    int idx;
    MusicService musicService;
    boolean isService = false;
    String whatPlaying = "stop";

    seekBarHandler seekBarHandler = new seekBarHandler();
    seekBarThread seekBarThread = new seekBarThread();
    uiHandler uiHandler = new uiHandler();
    uiThread uiThread = new uiThread();

    SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName Name, IBinder service) {
            MusicService.MusicServiceBinder mb = (MusicService.MusicServiceBinder) service;
            musicService = mb.getService();
            isService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName Name) {
            isService = false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if(musicService == null) {
            intentService = new Intent(PlayerActivity.this, MusicService.class);
            startService(intentService);
            bindService(intentService, conn, Context.BIND_AUTO_CREATE);
        }

        Intent fromFileIntent = new Intent(this.getIntent());
        mp3DataArrayList = (ArrayList<Mp3Data>) fromFileIntent.getExtras().getSerializable("mp3DataArrayList");
        idx = fromFileIntent.getExtras().getInt("index");
        mp3Data = mp3DataArrayList.get(idx);

        mTitle = findViewById(R.id.title);
        mArtist = findViewById(R.id.artist);
        mDuringTime = findViewById(R.id.duringTime);
        mRemainingTime = findViewById(R.id.remainingTime);
        mPrevBtn = findViewById(R.id.prev);
        mPlayBtn = findViewById(R.id.play);
        mNextBtn = findViewById(R.id.next);
        mImageView = findViewById(R.id.imageView);

        mTitle.setText(mp3Data.getTitle());
        mArtist.setText(mp3Data.getArtist());

        Uri albumArtUri = ContentUris.withAppendedId(artworkUri, mp3Data.getAlbumId());
        Picasso.get().load(albumArtUri).error(R.drawable.ic_launcher_background).into(mImageView);

        OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener();
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        seekBar.setMax(mp3Data.getDuration());

        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  음악이 재생중이지 않을 때
                if(whatPlaying.equals("stop")) {
                    mPlayBtn.setText("pause");
                    whatPlaying = "playing";

                    musicService.prepareMusic(mp3DataArrayList, idx);
                    musicService.playMusic(mp3DataArrayList, idx);

                    if(seekBarThread.isAlive()){
                        seekBarThread.interrupt();
                    }
                    seekBarThread = new seekBarThread();
                    seekBarThread.start();

                    Toast.makeText(getApplicationContext(), "음악을 재생합니다.", Toast.LENGTH_SHORT).show();
                } else if(whatPlaying.equals("playing")) {
                    whatPlaying = "pause";
                    mPlayBtn.setText("play");
                    musicService.pauseMusic();
                    Toast.makeText(getApplicationContext(), "음악을 일시정지 합니다.", Toast.LENGTH_SHORT).show();
                } else if(whatPlaying.equals("pause")) {
                    musicService.reStartMusic();
                    mPlayBtn.setText("pause");
                    whatPlaying = "playing";
                }
            }
        });

        mPrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whatPlaying = "stop";
                // 이전곡 선택하기
                if(idx == 0) {
                    Toast.makeText(getApplicationContext(), "첫번째 곡 입니다.",Toast.LENGTH_SHORT).show();
                } else if(idx > 0) {
                    idx = idx - 1;
                    musicService.prevMusic(mp3DataArrayList, idx);
                    musicService.setSeekTo(0);

                    runThread();
                }

            }
        });

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whatPlaying = "stop";
                // 다음곡 선택하기
                if(mp3DataArrayList.size() == idx) {
                    musicService.nextMusic(mp3DataArrayList, idx);
                    musicService.setSeekTo(0);
                    runThread();
                } else if(mp3DataArrayList.size() > idx) {
                    idx = idx + 1;
                    if(idx == mp3DataArrayList.size()) {
                        Toast.makeText(getApplicationContext(), "마지막 곡 입니다.",Toast.LENGTH_SHORT).show();
                        idx = idx - 1;
                    } else {
                        musicService.nextMusic(mp3DataArrayList, idx);
                        musicService.setSeekTo(0);
                    }
                    runThread();
                }
            }
        });
    }

    private void runThread() {
        // ui 갱신
        if(seekBarThread.isAlive()){
            seekBarThread.interrupt();
        }
        if(uiThread.isAlive()){
            uiThread.interrupt();
        }
        uiThread = new uiThread();
        uiThread.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity on Resume");
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Activity on Start");
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Activity on Stop");
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Activity on Pause");
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "Activity on Restart");
    }
    @Override
    protected void onDestroy() {
//        if(whatPlaying)
        super.onDestroy();
        unbindService(conn);
        Log.d(TAG, "Activity on Destroy");
    }


    class OnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int time = seekBar.getProgress();
            musicService.setSeekTo(time);
            seekBar.setProgress(time);
        }
    }

    class seekBarHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            int currentPositions = bundle.getInt("currentPositions");
            int duration = bundle.getInt("duration");
            seekBar.setProgress(currentPositions);

            String remaining_toTime = dateFormat.format(new Date(duration - currentPositions));
            String currentPositions_toTime = dateFormat.format(new Date(currentPositions));

            mDuringTime.setText(currentPositions_toTime);
            mRemainingTime.setText(remaining_toTime);
        }
    }

    class seekBarThread extends Thread {
        boolean isRun = false;
        @Override
        public void run() {
            isRun = true;
            while (isRun) {  // 음악이 실행중일때 계속 돌아가게 함
                try {
                    Thread.sleep(400); // 0.4초마다 시크바 움직이게 함
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 현재 재생중인 위치를 가져와 시크바에 적용
                int currentPositions = musicService.getCurrentPositions();
                int duration = musicService.getDuration();
                Message message = seekBarHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt("currentPositions", currentPositions);
                bundle.putInt("duration", duration);
                message.setData(bundle);
                seekBarHandler.sendMessage(message);

                if(Thread.interrupted()) {
                    isRun = false;
                    break;
                }
            }
        }
    }

    class uiHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // setText 등 ui 활동
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            Mp3Data mp3Data = (Mp3Data) bundle.getSerializable("mp3Data");

            mTitle.setText(mp3Data.getTitle());
            mArtist.setText(mp3Data.getArtist());
            mPlayBtn.setText("play");
            seekBar.setMax(mp3Data.getDuration());
            seekBar.setProgress(0);

        }
    }

    class uiThread extends Thread {
        @Override
        public void run() {
            Mp3Data mp3Data = mp3DataArrayList.get(idx);
            Message message = uiHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putSerializable("mp3Data", mp3Data);
            message.setData(bundle);
            //sendMessage가 되면 이 handler가 해당되는 핸들러객체가(ValueHandler) 자동으로 호출된다.
            uiHandler.sendMessage(message);
        }
    }
}





































































































//public class MainActivity extends AppCompatActivity {
//    private static final String TAG = "MainActivity";
//    TextView mTitle, mArtist;
//    Button mPlayBtn, mPrevBtn, mNextBtn;
//    SeekBar seekBar;
//    ImageView mImageView;
//    Thread thread = new seekBarThread();
//    MyMusicService myMusicService;
//    String isPlaying = "pause";
//    Mp3Data mp3Data;
//    private final Uri artworkUri = Uri.parse("content://media/internal/audio/albumart");
//    ArrayList<Mp3Data> mp3DataArrayList = new ArrayList<>();
//    Intent intentService;
//    int idx;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        Intent fromFileIntent = new Intent(this.getIntent());
//        mp3Data = (Mp3Data) fromFileIntent.getExtras().getSerializable("mp3Data");
//        mp3DataArrayList = (ArrayList<Mp3Data>) fromFileIntent.getExtras().getSerializable("mp3DataArrayList");
//        idx = fromFileIntent.getExtras().getInt("index");
//
//        OnClickListener onClickListener = new OnClickListener();
//
//        mTitle = findViewById(R.id.title);
//        mArtist = findViewById(R.id.artist);
//
//        mTitle.setText(mp3Data.getTitle());
//        mArtist.setText(mp3Data.getArtist());
//
//        mPrevBtn = findViewById(R.id.prev);
//        mPlayBtn = findViewById(R.id.play);
//        mNextBtn = findViewById(R.id.next);
//        mImageView = findViewById(R.id.imageView);
//
//        mPrevBtn.setOnClickListener(onClickListener);
//        mPlayBtn.setOnClickListener(onClickListener);
//        mNextBtn.setOnClickListener(onClickListener);
//
//        Uri albumArtUri = ContentUris.withAppendedId(artworkUri, mp3Data.getAlbumId());
//        Picasso.get().load(albumArtUri).error(R.drawable.ic_launcher_background).into(mImageView);
//
//        OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener();
//        seekBar = (SeekBar) findViewById(R.id.seekbar);
//        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
//        seekBar.setMax(mp3Data.getDuration());
//
//        if(myMusicService == null) {
//            intentService = new Intent(MainActivity.this, MyMusicService.class);
//            startService(intentService);
//            bindService(intentService, conn, 0);
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//    ServiceConnection conn = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {  // MusicService 클래스에 onBind 리턴 값(MusicService 참조 값[주소])이 iBinder로 넘어옴
//            //연결되었을 때 실행되는 콜백메소드
//            //연결된 터널을 통해 서비스객체의 참조값 얻어오기
//            //서비스로 부터 넘어온 외교관 객체(iBinder) 에게 서비스 객체의 참조값 얻어오기
//            MyMusicService.ServiceBinder binder= (MyMusicService.ServiceBinder) iBinder;
//            myMusicService = binder.getService();
//        }
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//        }
//    };
//
//    class OnClickListener implements View.OnClickListener {
//        @Override
//        public void onClick(View view) {
//            if (view.getId() == R.id.play) {
//                if(isPlaying.equals("pause") && myMusicService != null && !myMusicService.isPlaying()) {
//                    mPlayBtn.setText("pause");
//                    isPlaying = "play";
//                    myMusicService.playMusic();
//                    thread = new seekBarThread();
//                    thread.start();
//                    Toast.makeText(getApplicationContext(), "음악을 재생합니다.", Toast.LENGTH_SHORT).show();
//                } else if(isPlaying.equals("play") && myMusicService != null && myMusicService.isPlaying()) {
//                    mPlayBtn.setText("play");
//                    isPlaying = "pause";
//                    myMusicService.pauseMusic();
////                    thread.interrupt();
//                    Toast.makeText(getApplicationContext(), "음악을 일시정지 합니다.", Toast.LENGTH_SHORT).show();
//                }
//
//            } else if (view.getId() == R.id.prev) {
//                myMusicService.prevMusic();
//            } else if (view.getId() == R.id.next) {
//                myMusicService.nextMusic();
//            }
//        }
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//    }
//
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unbindService(conn);
//    }
//
//    class OnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
//        @Override
//        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//        }
//
//        @Override
//        public void onStartTrackingTouch(SeekBar seekBar) {
//        }
//
//        @Override
//        public void onStopTrackingTouch(SeekBar seekBar) {
//        }
//    }
//
//    public static boolean isServiceRunning(Context context) {
//        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//
//        for(ActivityManager.RunningServiceInfo rsi : am.getRunningServices(Integer.MAX_VALUE)) {
//            if(MyMusicService.class.getName().equals(rsi.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    class seekBarThread extends Thread {
//        @Override
//        public void run() {
//            while (myMusicService.isPlaying()) {  // 음악이 실행중일때 계속 돌아가게 함
//                try {
//                    Thread.sleep(1000); // 1초마다 시크바 움직이게 함
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                // 현재 재생중인 위치를 가져와 시크바에 적용
//                int time = 0;
//                if(myMusicService != null) {
//                    time = myMusicService.getCurrentPositions();
//                }
//                seekBar.setProgress(time);
//
//                Log.d(TAG, "mp3Data.getDuration() : " + mp3Data.getDuration() + " myMusicService.getCurrentPositions() : " + myMusicService.getCurrentPositions());
//
//                if(mp3Data.getDuration() == myMusicService.getCurrentPositions()) {
//                    seekBar.setProgress(0);
//                    myMusicService.stopMusic();
//                }
//            }
//        }
//    }
//}

///mnt/sdcard/Music : 음악파일 폴더


//            if (view.getId() == R.id.play) {
//                    if(myMusicService != null && !myMusicService.isPlaying() && !isPlaying.equals("play")) {
//                    myMusicServce.playMusic();i
//                    seekBar.setMax(mp3Data.getDuration());
//                    isPlaying = "play";
//                    if(!thread.isInterrupted()) {
//                    thread = new seekBarThread();
//                    thread.start();
//                    }
//                    Toast.makeText(getApplicationContext(), "음악을 재생합니다.", Toast.LENGTH_SHORT).show();
//                    }
//
//                    } else if (view.getId() == R.id.stop) {
//                    if(myMusicService != null && myMusicService.isPlaying() && !isPlaying.equals("stop") || isPlaying.equals("pause")) {
//                    myMusicService.stopMusic();
//                    if(thread.isAlive()) {
//                    thread.interrupt();
//                    }
//                    Toast.makeText(getApplicationContext(), "음악을 정지합니다.", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(MainActivity.this, MyMusicService.class);
//        stopService(intent);
//        seekBar.setProgress(0);
//        isPlaying = "stop";
//
//        } else if(isPlaying.equals("stop")){
//        Toast.makeText(getApplicationContext(), "음악을 재생시켜 주세요.", Toast.LENGTH_SHORT).show();
//        }
//
//        } else if (view.getId() == R.id.pause) {
//        if(myMusicService != null && myMusicService.isPlaying() && !isPlaying.equals("pause")) {
//        myMusicService.pauseMusic();
//        isPlaying = "pause";
//        if(thread.isAlive()) {
//        thread.interrupt();
//        }
//        Toast.makeText(getApplicationContext(), "음악을 일시정지 합니다.", Toast.LENGTH_SHORT).show();
//        }
//        }