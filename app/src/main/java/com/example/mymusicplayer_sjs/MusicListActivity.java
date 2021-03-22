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
    private MusicAdapter musicAdapter;

    private int position;
    private int requestCode;

    MusicDBHelper musicDBHelper = MusicDBHelper.getInstance(this);
    private ArrayList<MusicData> allMusicSdCardList = new ArrayList<>();
    private ArrayList<MusicData> likeMusicList = new ArrayList<>();
    private ArrayList<MusicData> musicList = new ArrayList<>();
    public MusicAdapter getMusicAdapter(){
        return this.musicAdapter;
    }


    public MusicAdapter getLikeMusicAdapter(){
        return this.likeMusicAdapter;
    }

    public int getPosition(){
        return position;
    }
    public void setPosition(int position){this.position = position;}

    public MusicDBHelper getMusicDBHelper(){
        return this.musicDBHelper;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        // 요청코드에 따라 모든 뮤직, 좋아요 뮤직 선택
        Intent intent = getIntent();
        requestCode = intent.getIntExtra("requestCode",0);

        // 음악 리스트 가져오기
        allMusicSdCardList = musicDBHelper.compareArrayList();

        // 음악 DB에 저장
        insertDB(allMusicSdCardList);

        musicListHandler(requestCode);

        fragmentSwitcher();
        eventHandlerFunc();
    }   // end of onCreate

    public void musicListHandler(int requestCode){
        // 리사이클러뷰 아이디 찾기
        recyclerView = findViewById(R.id.recyclerView);

        // 어댑터에 모든 뮤직 리스트 설정하기
        allMusicAdapter = new MusicAdapter(getApplicationContext(), allMusicSdCardList);

        // 어댑터에 좋아요 뮤직 리스트 설정하기
        likeMusicAdapter = new MusicAdapter(getApplicationContext(),getLikeMusicList());

        // 리사이클러 뷰와 어댑터를 연결해줄 LinearLayoutManager를 불러온다.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        // 만약 requestCode가 ALLMUSIC 이라면
        if (requestCode == MainActivity.REQEUST_CODE_ALLMUSIC){
            musicList = allMusicSdCardList;

            // 리사이클러뷰에 모든 뮤직의 어댑터를 연결
            recyclerView.setAdapter(allMusicAdapter);
            allMusicAdapter.notifyDataSetChanged();
            musicAdapter = allMusicAdapter;

            // 만약 requestCode가 LIKEMUSIC 이라면
        } else if (requestCode == MainActivity.REQEUST_CODE_LIKEMUSIC){
            musicList = getLikeMusicList();

            // 리사이클러뷰에 좋아요한 뮤직의 어댑터를 연결
            recyclerView.setAdapter(likeMusicAdapter);
            likeMusicAdapter.notifyDataSetChanged();
            musicAdapter = likeMusicAdapter;
        }
    }   // end of musicListHandler

    public ArrayList<MusicData> getMusicList(){
        return this.musicList;
    }

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
    }

    public void likeRecyclerViewListUpdate(ArrayList<MusicData> arrayList){
        likeMusicAdapter.setMusicList(arrayList);

        recyclerView.setAdapter(likeMusicAdapter);
        likeMusicAdapter.notifyDataSetChanged();
    }

    private void eventHandlerFunc() {
        if (musicAdapter == null){
            Toast.makeText(this, "musicAdapter 널임", Toast.LENGTH_SHORT).show();
        }
        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                allMusicSdCardList = musicDBHelper.selectMusicTbl();

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                Fragment fragmentPlayMusic = new FragmentPlayer();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("allMusicSdCardList", allMusicSdCardList);

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

}