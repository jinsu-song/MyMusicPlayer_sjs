package com.example.mymusicplayer_sjs;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FragmentPlayer extends Fragment implements View.OnClickListener{
    private MusicListActivity musicListActivity;

    private ImageView imgAlbumArt,imgPrevious,imgPauseAndPlay,imgForward,imgLike;
    private TextView tvMusicName, tvSingerName, tvStartTime, tvDuration;
    private SeekBar seekBarMP3;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    private int index;
    private MusicData musicData = new MusicData();
    private ArrayList<MusicData> allMusicSdCardList = null;
    private ArrayList<MusicData> likeMusicList = null;
    private MusicAdapter musicAdapter;
    private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss"); // 1. 시간을 가져오기 방식

//    private MusicAdapter allMusicAdapter;

    private boolean isBackPressed;

//    private OnFragmentInteractionListener mListener;
    private Thread thread;

    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        musicListActivity = (MusicListActivity) getActivity();
        allMusicSdCardList = musicListActivity.getAllMusicSdCardList();
        likeMusicList = musicListActivity.getLikeMusicList();
        musicAdapter = musicListActivity.getLikeMusicAdapter();
//        allMusicAdapter = musicListActivity.getAllMusicAdapter();

//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener)context;
//        }else{
//            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
//        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener.onFragmentInteraction(mediaPlayer);


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

        index = musicListActivity.getPosition();


        setPlayerData(index,true);
        seekBarChangeMethod();



//        eventHandler(view);
        return view;
    }   // end of onCrateView

    private void eventHandler(View view) {

    }   // end of eventHandler

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgPauseAndPlay:
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    imgPauseAndPlay.setImageResource(R.drawable.button_play);
                }else{
//                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    imgPauseAndPlay.setImageResource(R.drawable.button_pause);
//                    mListener.onFragmentInteraction(mediaPlayer);
                    try{
//                        thread.interrupt();
                        thread.sleep(100);
                    }catch (InterruptedException ite){

                    }
                    setSeekBarThread();
                }
                break;
            case R.id.imgPrevious:
                mediaPlayer.stop();
                mediaPlayer.reset();
                if (index == 0){
                    index = musicListActivity.getAllMusicSdCardList().size();
                }
                index--;
                setPlayerData(index,true);
                break;
            case R.id.imgForward:
                mediaPlayer.stop();
                mediaPlayer.reset();
                if (index == allMusicSdCardList.size() - 1){
                    index = -1;
                }
                index++;
                setPlayerData(index,true);
                break;
            case R.id.imgLike:
                if (imgLike.isActivated()){
                    imgLike.setActivated(false);
                    imgLike.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    musicData.setLiked(0);
                    likeMusicList.remove(musicData);
                    musicAdapter.notifyDataSetChanged();;
                    Toast.makeText(musicListActivity, "좋아요 취소!!", Toast.LENGTH_SHORT).show();
                }else{
                    imgLike.setActivated(true);
                    musicData.setLiked(1);
                    imgLike.setImageResource(R.drawable.ic_baseline_favorite_24);
                    likeMusicList.add(musicData);
                    musicAdapter.notifyDataSetChanged();
                    Toast.makeText(musicListActivity, "좋아요 !!", Toast.LENGTH_SHORT).show();
                }

                break;
            default: break;
        }   // end of switch
    }   // end of onClick

    // 플레이어 화면 처리
//    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setPlayerData(int pos,boolean flag){
        index = pos;

        mediaPlayer.stop();
        mediaPlayer.reset();

        MusicAdapter musicAdapter = new MusicAdapter(musicListActivity);

        // 플래그에 따라 musicData에 좋아요한 뮤직 리스트와 아닌 리스트를 가져온다.
        if (flag){
            musicData = musicListActivity.getAllMusicSdCardList().get(pos);
        }else{
            // 좋아요한 뮤직 리스트 가져오기
//            musicData = musicListActivity.getAllMusicSdCardList().get(pos);
        }

        tvMusicName.setText(musicData.getTitle());
        tvSingerName.setText(musicData.getArtists());
        tvDuration.setText(sdf.format(Integer.parseInt(musicData.getDuration())));

        if (musicData.getLiked() == 1){
            imgLike.setActivated(true);
        }else{
            imgLike.setActivated(false);
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
            imgPauseAndPlay.setActivated(true);
            imgPauseAndPlay.setImageResource(R.drawable.button_pause);

            setSeekBarThread();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    imgForward.callOnClick();
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
        imgPrevious = view.findViewById(R.id.imgPrevious);
        imgPauseAndPlay = view.findViewById(R.id.imgPauseAndPlay);
        imgForward = view.findViewById(R.id.imgForward);
        imgLike = view.findViewById(R.id.imgLike);
        seekBarMP3 = view.findViewById(R.id.seekBarMP3);
        tvStartTime = view.findViewById(R.id.tvStartTime);
        tvDuration = view.findViewById(R.id.tvDuration);

        imgPauseAndPlay.setOnClickListener(this);
        imgForward.setOnClickListener(this);
        imgPrevious.setOnClickListener(this);
        imgLike.setOnClickListener(this);
    }   // end of findViewByIdFunc

//    public interface OnFragmentInteractionListener{
//        void onFragmentInteraction(MediaPlayer mediaPlayer);
//    }
}
