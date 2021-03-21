package com.example.mymusicplayer_sjs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FragmentAllMusicList extends Fragment {
    private RecyclerView recyclerViewAtFragment;
    private MusicAdapter musicAdapterAtFragment;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_layout,container,false);

        recyclerViewAtFragment = view.findViewById(R.id.recyclerViewFragment);
        musicAdapterAtFragment = getArguments().getParcelable("Adapter");


        // 어댑터와 리사이클러뷰를 연결해줄 매니저를 가져온다.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        // 매니저 셋팅
        recyclerViewAtFragment.setLayoutManager(linearLayoutManager);

        // 리사이클러 뷰와 어댑터를 연결
        recyclerViewAtFragment.setAdapter(musicAdapterAtFragment);
        return view;
    }   // end of onCreateView
}
