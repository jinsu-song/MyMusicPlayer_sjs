package com.example.mymusicplayer_sjs;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.CustomViewHolder> implements Parcelable {
    private Context context;
    private ArrayList<MusicData> musicList;

    // 리스트 포지션 저장할 내부 인터페이스 타입 멤버변수
    private OnItemClickListener mListener = null;

    // 생성자
    private MusicAdapter(){}
    public MusicAdapter(Context context) {
        this.context = context;
    }
    public MusicAdapter(Context context, ArrayList<MusicData> musicList) {
        this.context = context;
        this.musicList = musicList;
    }

    // recycler_item.xml 을 인플레시션 시킴
    @NonNull
    @Override
    public MusicAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item,viewGroup,false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    // recycler_item.xml 에 데이터를 셋팅
    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.CustomViewHolder customViewHolder, int position) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        Bitmap albumImg = getAlbumImg(context,Long.parseLong(musicList.get(position).getAlbumArt()),200);
        if (albumImg != null){

            customViewHolder.albumArt.setImageBitmap(albumImg);
        }

        // 이미지는 Content Provider를 통해서 가져온다.
        customViewHolder.title.setText(musicList.get(position).getTitle());
        customViewHolder.artist.setText(musicList.get(position).getArtists());
        customViewHolder.duration.setText(simpleDateFormat.format(Integer.parseInt(musicList.get(position).getDuration())));
    }   // end of onBindViewHolder

    // 리스트 개수
    @Override
    public int getItemCount() {
        return (musicList != null) ? musicList.size() : 0;
    }

    // 앨범아트 가져오는 함수
    public Bitmap getAlbumImg(Context context, Long albumArt, int imgMaxSize) {
        // parseInt <-albumArt 번호
        /*컨텐트 프로바이더(Content Provider)는 앱 간의 데이터 공유를 위해 사용됨.
        특정 앱이 다른 앱의 데이터를 직접 접근해서 사용할 수 없기 때문에
        무조건 컨텐트 프로바이더를 통해 다른 앱의 데이터를 사용해야만 한다.
        다른 앱의 데이터를 사용하고자 하는 앱에서는 Uri를 이용하여 컨텐트 리졸버(Content Resolver)를 통해
        다른 앱의 컨텐트 프로바이더에게 데이터를 요청하게 되는데
        요청받은 컨텐트 프로바이더는 Uri를 확인하고 내부에서 데이터를 꺼내어 컨텐트 리졸버에게 전달한다.
        */
        // 안드로이드에서는 URL(Uniform Resource Location)이 아닌 Uri를 사용해야 한다.
        BitmapFactory.Options options = new BitmapFactory.Options();
        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = Uri.parse("content://media/external/audio/albumart/" + albumArt);

        if (uri != null){
            ParcelFileDescriptor fd = null;
            try{
                fd = contentResolver.openFileDescriptor(uri, "r");

                // 메모리 할당을 하지 않으면서 해당된 정보를 읽어올 수 있다.
                options.inJustDecodeBounds = true;
                int scale = 0;  // 이미지 사이즈를 결정하기

                // 실제 이미지 사이즈가 초과되면 그에 알맞게 사이즈를 조정한다.
                if(options.outHeight > imgMaxSize || options.outWidth > imgMaxSize){
                    scale = (int)Math.pow(2,(int) Math.round(Math.log(imgMaxSize /
                            (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
                }

                // 비트맵을 위해서 메모리를 할당하겠다.
                options.inJustDecodeBounds = false;
                options.inSampleSize = scale;

                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),null,options);

                if (bitmap != null){
                    if (options.outWidth != imgMaxSize || options.outHeight != imgMaxSize){
                        // 다시 만들겠다. 내가 원하는 크기로
                        Bitmap tmp = Bitmap.createScaledBitmap(bitmap,imgMaxSize,imgMaxSize,true);
                        bitmap.recycle();
                        bitmap = tmp;
                    }
                }
                return bitmap;

            }catch (IOException ioe){
                Log.d("MusicAdapter","content resolver 에러 발생");
            }finally{
                if (fd != null){
                    try{
                        fd.close();
                    }catch (IOException ioe){

                    }
                }
            }   // end of finally
        }   // end of if
        return null;
    }   // end of getAlbumImg

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public void setMusicList(ArrayList<MusicData> arrayList) {
        this.musicList = arrayList;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder{
        private ImageView albumArt;
        private TextView title,artist,duration;

        public CustomViewHolder(@NonNull View itemView){
            super(itemView);
            albumArt = itemView.findViewById(R.id.d_ivAlbum);
            title = itemView.findViewById(R.id.d_tvTitle);
            artist = itemView.findViewById(R.id.d_tvArtist);
            duration = itemView.findViewById(R.id.d_tvDuration);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        // 해당 뷰와 그 포지션을 저장
                        mListener.onItemClick(view,position);
                    }
                }
            });
        }
    }   // end of CustomViewHolder Class

    // item position을 저장하는 interface
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    // setter함수
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }
}
