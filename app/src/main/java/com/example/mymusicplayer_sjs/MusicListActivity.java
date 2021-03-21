package com.example.mymusicplayer_sjs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class MusicListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MusicAdapter allMusicAdapter;
    private MusicAdapter likeMusicAdapter;

    private int position;
    private MediaPlayer mediaPlayer;

    MusicDBHelper musicDBHelper = MusicDBHelper.getInstance(this);
    private ArrayList<MusicData> allMusicSdCardList = new ArrayList<>();
    private ArrayList<MusicData> likeMusicList = new ArrayList<>();

    public ArrayList<MusicData> getAllMusicSdCardList() {
        return allMusicSdCardList;
    }
    public MusicAdapter getLikeMusicAdapter(){
        return this.likeMusicAdapter;
    }

    public int getPosition(){
        return position;
    }
    public void setPosition(int position){this.position = position;}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        // 음악 리스트 가져오기
        allMusicSdCardList = musicDBHelper.compareArrayList();

        // 음악 DB에 저장
        insertDB(allMusicSdCardList);

        recyclerView = findViewById(R.id.recyclerView);
        allMusicAdapter = new MusicAdapter(getApplicationContext(), allMusicSdCardList);

        likeMusicAdapter = new MusicAdapter(getApplicationContext(),getLikeMusicList());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setAdapter(allMusicAdapter);
        recyclerView.setAdapter(likeMusicAdapter);

        fragmentSwitcher();
        eventHandlerFunc();
    }   // end of onCreate
    public ArrayList<MusicData> getLikeMusicList(){
        likeMusicList = musicDBHelper.saveLikeList();
        if (likeMusicList.isEmpty()){
            Toast.makeText(this, "좋아요 리스트 가져오기 실패", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "좋아요 리스트 가져오기 성공", Toast.LENGTH_SHORT).show();
        }
        return this.likeMusicList;
    }

    // 좋아요 리스트 가져오기


    @Override
    protected void onStop() {
        super.onStop();
        boolean returnValue = musicDBHelper.updateMusicDataToDB(allMusicSdCardList);

        if (returnValue){
            Toast.makeText(this, "업뎃 성공", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "업뎃 실패", Toast.LENGTH_SHORT).show();
        }
    }

    public void likeRecyclerViewListUpdate(ArrayList<MusicData> arrayList){
        likeMusicAdapter.setMusicList(arrayList);

        recyclerView.setAdapter(likeMusicAdapter);
        likeMusicAdapter.notifyDataSetChanged();
    }

    private void eventHandlerFunc() {
        allMusicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//                final int positionToFragment = position;
//                if (mediaPlayer != null && mediaPlayer.isPlaying()){
//                    Toast.makeText(MusicListActivity.this, "mediaPlayer 안멈췄음", Toast.LENGTH_SHORT).show();
//                    mediaPlayer.stop();
//                    mediaPlayer.reset();
//
//                }
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                Fragment fragmentPlayMusic = new FragmentPlayer();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("allMusicSdCardList", allMusicSdCardList);
//                bundle.putParcelable("allMusicAdapter",allMusicAdapter);
//                bundle.putInt("position",position);
                setPosition(position);
                fragmentTransaction.replace(R.id.frameLayout,fragmentPlayMusic);
                fragmentTransaction.commit();
            }
        });
    }   // end of eventHandlerFunc

    private void insertDB(ArrayList<MusicData> arrayList){

        boolean returnValue = musicDBHelper.insertMusicDataToDB(arrayList);

        if(returnValue){
            Toast.makeText(getApplicationContext(), "삽입 성공", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "삽입 실패", Toast.LENGTH_SHORT).show();
        }

    }   // end of insertDB

    private void fragmentSwitcher() {
        Intent getIntent = getIntent();
        int requestCode = getIntent.getIntExtra("requestCode",0);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragmentMusicList = null;

        switch (requestCode){
            case MainActivity.REQEUST_CODE_ALLMUSIC:
                fragmentMusicList = new FragmentAllMusicList();
                Bundle bundle = new Bundle(1);
                bundle.putParcelable("allMusicAdapter", allMusicAdapter);
                fragmentMusicList.setArguments(bundle);
                fragmentTransaction.replace(R.id.frameLayout,fragmentMusicList);
                break;
            case MainActivity.REQEUST_CODE_LIKEMUSIC: break;
        }   // end of switch
        fragmentTransaction.commit();

    }   // end of fragmentSwitcher


//    @Override
//    public void onFragmentInteraction(MediaPlayer mediaPlayer) {
//        this.mediaPlayer = mediaPlayer;
//    }
}