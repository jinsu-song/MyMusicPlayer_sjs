package com.example.mymusicplayer_sjs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ImageView imgLikeMusic,imgAllMusic;
    public static final int REQEUST_CODE_ALLMUSIC = 1000;
    public static final int REQEUST_CODE_LIKEMUSIC = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 뷰 아이디 가져오기
        findViewByIdFunc();

        // 권한 물어보기
        requestPermissionsFunc();

        // 뷰 이벤트 처리
        eventHandlerFunc();

    }   // end of onCreate

    // 아이디 셋팅
    private void findViewByIdFunc() {
        imgAllMusic = findViewById(R.id.imgAllMusic);
        imgLikeMusic = findViewById(R.id.imgLikeMusic);
    }   // end of findViewByIdFunc

    // 이벤트 처리 함수
    private void eventHandlerFunc() {

        // 모든 노래 리스트 가져오기
        imgAllMusic.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this,MusicListActivity.class);
            intent.putExtra("requestCode",REQEUST_CODE_ALLMUSIC);
            startActivity(intent);
        });

        // 좋아요한 노래 리스트 가져오기
        imgLikeMusic.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this,MusicListActivity.class);
            intent.putExtra("requestCode",REQEUST_CODE_LIKEMUSIC);
            startActivity(intent);
        });
    }   // end of eventHandlerFunc

    // 권한설정 물어보기
    private void requestPermissionsFunc() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE},MODE_PRIVATE);
    }   // end of requestPermissionsFunc

}