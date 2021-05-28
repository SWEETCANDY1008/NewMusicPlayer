package com.example.newmusicplayer;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MusicFragment extends Fragment {
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

    public MusicFragment() {
        // Required empty public constructor
    }

    public static MusicFragment newInstance(String param1, String param2) {
        MusicFragment fragment = new MusicFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_music, container, false);

        if(musicService == null) {
            intentService = new Intent(getActivity().getApplicationContext(), MusicService.class);
            getActivity().startService(intentService);
            getActivity().bindService(intentService, conn, Context.BIND_AUTO_CREATE);
        }

        Bundle extra = this.getArguments();
        if(extra != null) {
            extra = getArguments();

            mp3DataArrayList = (ArrayList<Mp3Data>) extra.getSerializable("mp3DataArrayList");
            idx = extra.getInt("index");
            mp3Data = mp3DataArrayList.get(idx);
        }

        mTitle = v.findViewById(R.id.title);
        mArtist = v.findViewById(R.id.artist);
        mDuringTime = v.findViewById(R.id.duringTime);
        mRemainingTime = v.findViewById(R.id.remainingTime);
        mPrevBtn = v.findViewById(R.id.prev);
        mPlayBtn = v.findViewById(R.id.play);
        mNextBtn = v.findViewById(R.id.next);
        mImageView = v.findViewById(R.id.imageView);

        mTitle.setText(mp3Data.getTitle());
        mArtist.setText(mp3Data.getArtist());

        Uri albumArtUri = ContentUris.withAppendedId(artworkUri, mp3Data.getAlbumId());
        Picasso.get().load(albumArtUri).error(R.drawable.ic_launcher_background).into(mImageView);

        OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener();
        seekBar = (SeekBar) v.findViewById(R.id.seekbar);
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

                    Toast.makeText(getActivity().getApplicationContext(), "음악을 재생합니다.", Toast.LENGTH_SHORT).show();
                } else if(whatPlaying.equals("playing")) {
                    whatPlaying = "pause";
                    mPlayBtn.setText("play");
                    musicService.pauseMusic();
                    Toast.makeText(getActivity().getApplicationContext(), "음악을 일시정지 합니다.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity().getApplicationContext(), "첫번째 곡 입니다.",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity().getApplicationContext(), "마지막 곡 입니다.",Toast.LENGTH_SHORT).show();
                        idx = idx - 1;
                    } else {
                        musicService.nextMusic(mp3DataArrayList, idx);
                        musicService.setSeekTo(0);
                    }
                    runThread();
                }
            }
        });

        return v;
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










//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.d(TAG, "Activity on Resume");
//    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//        Log.d(TAG, "Activity on Start");
//    }
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.d(TAG, "Activity on Stop");
//    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.d(TAG, "Activity on Pause");
//    }
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        Log.d(TAG, "Activity on Restart");
//    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unbindService(conn);
//        Log.d(TAG, "Activity on Destroy");
//    }
//
//
//
//
//
//
//
