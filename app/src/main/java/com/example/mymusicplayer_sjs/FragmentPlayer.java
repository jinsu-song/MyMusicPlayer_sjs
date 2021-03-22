package com.example.mymusicplayer_sjs;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FragmentPlayer extends Fragment implements View.OnClickListener{
    private MusicListActivity musicListActivity;

    private ImageView imgAlbumArt;
    private ImageButton ibPrevious, ibPauseAndPlay, ibForward, ibLike;
    private TextView tvMusicName, tvSingerName, tvStartTime, tvDuration;
    private SeekBar seekBarMP3;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    private int index;
    private MusicData musicData = new MusicData();
    private ArrayList<MusicData> allMusicSdCardList = null;
    private ArrayList<MusicData> likeMusicList = null;
    private ArrayList<MusicData> musicList;
    private MusicDBHelper musicDBHelper;
    private MusicAdapter musicAdapter;
    private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss"); // 1. 시간을 가져오기 방식
    private Thread thread;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // MusicListActivity에서 가져와야하는 값 모두 가져오기
        musicListActivity = (MusicListActivity) getActivity();
        musicDBHelper = musicListActivity.getMusicDBHelper();
        musicList = musicListActivity.getMusicList();
        likeMusicList = musicListActivity.getLikeMusicList();
        musicAdapter = musicListActivity.getMusicAdapter();
        index = musicListActivity.getPosition();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // 재생중인 음악과 스레드 멈추기기
       if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        musicListActivity = null;
        Log.d("FragmentPlayer","onDetach");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.player_layout,container,false);

        // 뷰 아이디 가져오기
        findViewByIdFunc(view);

        setPlayerData(index);

        // 시크바 스레드
        seekBarChangeMethod();

        return view;
    }   // end of onCrateView

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ibPauseAndPlay:
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    ibPauseAndPlay.setImageResource(R.drawable.button_play);
                }else{
                    mediaPlayer.start();
                    ibPauseAndPlay.setImageResource(R.drawable.button_pause);

                    setSeekBarThread();
                }
                break;
            case R.id.ibPrevious:
                mediaPlayer.stop();
                mediaPlayer.reset();
                if (index == 0){
                    index = musicListActivity.getMusicList().size();
                }
                index--;
                setPlayerData(index);
                break;
            case R.id.ibForward:
                mediaPlayer.stop();
                mediaPlayer.reset();
                if (index == musicList.size() - 1){
                    index = -1;
                }
                index++;
                setPlayerData(index);
                break;
            case R.id.ibLike:
                if (musicData.getLiked() == 1){
                    ibLike.setActivated(true);
                    ibLike.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    musicData.setLiked(0);
                    musicDBHelper.updateMusicDataToDB(musicData);
                    musicAdapter.notifyDataSetChanged();;
                    Toast.makeText(musicListActivity, "좋아요 취소!!", Toast.LENGTH_SHORT).show();
                }else{
                    ibLike.setActivated(false);
                    ibLike.setImageResource(R.drawable.ic_baseline_favorite_24);
                    musicData.setLiked(1);
                    musicDBHelper.updateMusicDataToDB(musicData);
                    musicAdapter.notifyDataSetChanged();
                    Toast.makeText(musicListActivity, "좋아요 !!", Toast.LENGTH_SHORT).show();
                }

                break;
            default: break;
        }   // end of switch
    }   // end of onClick

    // 플레이어 화면 처리
//    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setPlayerData(int pos){
        index = pos;

        mediaPlayer.stop();
        mediaPlayer.reset();

//        musicList = musicDBHelper.compareArrayList();

        musicData = musicList.get(pos);

        tvMusicName.setText(musicData.getTitle());
        tvSingerName.setText(musicData.getArtists());
        tvDuration.setText(sdf.format(Integer.parseInt(musicData.getDuration())));

        Toast.makeText(musicListActivity, "musicData.getLiked() = " + musicData.getLiked(), Toast.LENGTH_SHORT).show();
        if (musicData.getLiked() == 1){
//            ibLike.setActivated(true);
            ibLike.setImageResource(R.drawable.ic_baseline_favorite_24);
        }else{
//            ibLike.setActivated(false);
            ibLike.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }

        Bitmap albumImg = musicAdapter.getAlbumImg(musicListActivity,Long.parseLong(musicData.getAlbumArt()), 200);
        if(albumImg != null){
            imgAlbumArt.setImageBitmap(albumImg);
        }else{
            imgAlbumArt.setImageResource(R.drawable.apple);
        }

        // 음악 재생
        Uri musicURI = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,musicData.getId());

        try{
            mediaPlayer.setDataSource(musicListActivity, musicURI);
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBarMP3.setProgress(0);
            seekBarMP3.setMax(Integer.parseInt(musicData.getDuration()));
            ibPauseAndPlay.setActivated(true);
            ibPauseAndPlay.setImageResource(R.drawable.button_pause);

            setSeekBarThread();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    ibForward.callOnClick();
                }
            });
        } catch (IOException ioe){
            Log.d("FragmentPlayer","재생 오류");
        }
    }   // end of setPlayerData

    private void seekBarChangeMethod(){
        seekBarMP3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }   // end of seekBarChangeMethod

    private void setSeekBarThread() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(mediaPlayer.isPlaying()){
                    seekBarMP3.setProgress(mediaPlayer.getCurrentPosition());
                    musicListActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvStartTime.setText(sdf.format(mediaPlayer.getCurrentPosition()));
                        }
                    });
                    SystemClock.sleep(100);
                }   // end of while
            }
        }); // end of Thread
        thread.start();
    }   // end of setSeekBarThread

    private void findViewByIdFunc(View view) {
        imgAlbumArt = view.findViewById(R.id.imgAlbumArt);
        tvMusicName = view.findViewById(R.id.tvMusicName);
        tvSingerName = view.findViewById(R.id.tvSingerName);
        ibPrevious = view.findViewById(R.id.ibPrevious);
        ibPauseAndPlay = view.findViewById(R.id.ibPauseAndPlay);
        ibForward = view.findViewById(R.id.ibForward);
        ibLike = view.findViewById(R.id.ibLike);
        seekBarMP3 = view.findViewById(R.id.seekBarMP3);
        tvStartTime = view.findViewById(R.id.tvStartTime);
        tvDuration = view.findViewById(R.id.tvDuration);

        ibPauseAndPlay.setOnClickListener(this);
        ibForward.setOnClickListener(this);
        ibPrevious.setOnClickListener(this);
        ibLike.setOnClickListener(this);
    }   // end of findViewByIdFunc
}

