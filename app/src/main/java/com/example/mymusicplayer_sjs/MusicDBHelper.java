package com.example.mymusicplayer_sjs;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

public class MusicDBHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DB_NAME = "musicDB";
    private static final int VERSION = 1;

    // 싱글톤
    private static MusicDBHelper musicDBHelper;

    // 매개변수 생성자
    public MusicDBHelper(Context context) {
        super(context,DB_NAME,null, VERSION);
        this.context = context;
    }

    public static MusicDBHelper getInstance(Context context){
        if(musicDBHelper == null){
            musicDBHelper = new MusicDBHelper(context);
        }
        return musicDBHelper;
    }

    // 테이블 생성
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {

            sqLiteDatabase.execSQL(
                    "CREATE TABLE IF NOT EXISTS musicTBL(" +
                            "id VARCHAR(15) PRIMARY KEY," +
                            "artist VARCHAR(15)," +
                            "title VARCHAR(15)," +
                            "albumArt VARCHAR(15)," +
                            "duration VARCHAR(15)," +
                            "liked INTEGER );");
        } catch (SQLException sqle){
            Log.d("MusicDBHelper","테이블 생성 실패1");
        } catch (Exception e){
            Log.d("MusicDBHelper","테이블 생성 실패2");
        }
    }   // end of onCreate

    // 테이블 삭제
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
//        sqLiteDatabase.execSQL("drop table if exists musicTBL");
        onCreate(sqLiteDatabase);
    }   // end of onUpgrade

    // DB 검색하기
    public ArrayList<MusicData> selectMusicTbl(){

        ArrayList<MusicData> musicDataArrayList = new ArrayList<>();
        SQLiteDatabase sqLinteDatabase = this.getWritableDatabase();
        Cursor cursor = null;
        try{
            cursor = sqLinteDatabase.rawQuery("SELECT * FROM musicTBL;", null);
            while (cursor.moveToNext()) {
                MusicData musicData = new MusicData(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5));
                musicDataArrayList.add(musicData);
            }   // end of while

        } catch(SQLException e){
            Log.d("MusicDBHelper", " select에러");
        } catch (Exception e){
            Log.d("MusicDBHelper", " select에서 에러");
        } finally {
            cursor.close();
            sqLinteDatabase.close();
        }
        return musicDataArrayList;
    }   // end of selectMusicTbl

    // 좋아요 업데이트 시키기
    public boolean updateMusicDataToDB(MusicData musicData){
        boolean returnValue = false;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        try{
//            for (MusicData data : arrayList) {
                String query = "UPDATE musicTBL SET liked = " + musicData.getLiked() + " WHERE id = '" + musicData.getId() + "';";
                sqLiteDatabase.execSQL(query);
//            }

            returnValue = true;
        } catch(SQLException sqle){
            return false;
        }
        return returnValue;

    }   // end of updateMusicDataToDB

    // 좋아요 리스트 가져오기
    public ArrayList<MusicData> saveLikeList(){
        ArrayList<MusicData> likeMusicList = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("select * from musicTBL where liked = 1;", null);
        while (cursor.moveToNext()) {
            MusicData musicData = new MusicData(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getInt(5)
                    );
            likeMusicList.add(musicData);
        }   // end of while

        return likeMusicList;

    }   // end of saveLikeList

    public boolean insertMusicDataToDB(ArrayList<MusicData> arrayList){
        boolean returnValue = false;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        try{
            for (MusicData data : arrayList) {
                ArrayList<MusicData> dbList = selectMusicTbl();

                if (!dbList.contains(data)) {
                    String query = "INSERT INTO musicTBL VALUES("
                            + "'" + data.getId() + "',"
                            + "'" + data.getArtists() + "',"
                            + "'" + data.getTitle() + "',"
                            + "'" + data.getAlbumArt() + "',"
                            + "'" + data.getDuration() + "',"
                            + data.getLiked() + ");";
                    sqLiteDatabase.execSQL(query);
                }
            }   // end of for-each
            returnValue = true;

        } catch (SQLException sqle){
            returnValue = false;
            Log.d("MusicDBHelper","insert 에러");
        }catch(Exception e){
            returnValue = false;
            Log.d("MusicDBHelper", "insert 에서 에러");
            e.printStackTrace();
        }finally{
        }
        return returnValue;
    }   // end of insertMusicDataToDB

    // 저장 메모리 안의 mp3 파일을 가져온다.
    private ArrayList<MusicData> findMusicFromContentProvider() {
        ArrayList<MusicData> sdCardList = new ArrayList<>();
        // 컨텐트 프로바이더에서는 핸드폰에서 다운로드했던 음악파일은 모두 관리되고 있다.
        String[] data = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};

        // resolver에 요청한다 query문으로...
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                data,null,null,data[2] + " ASC");    // Cursor타입이 반환됨, 원하는 정보를 찾아줌
        // data가 내가 보고 싶은 항목, 그리고 TITLE 항목으로 오름차순으로 가져와라
        if(cursor != null){
            while(cursor.moveToNext()){
                String id = cursor.getString(cursor.getColumnIndex(data[0]));
                String artist = cursor.getString(cursor.getColumnIndex(data[1]));
                String title = cursor.getString(cursor.getColumnIndex(data[2]));
                String albumArt = cursor.getString(cursor.getColumnIndex(data[3]));
                String duration = cursor.getString(cursor.getColumnIndex(data[4]));

                MusicData musicData = new MusicData(id,artist,title,albumArt,duration,0);
                sdCardList.add(musicData);
            }   // end of while
        }
        return sdCardList;
    }   // end of findContentProviderMP3ToArrayList

    public ArrayList<MusicData> compareArrayList() {
        // selectMusicTbl()과 findMusicFromContentProvider와 비교한다.
        ArrayList<MusicData> dbList = selectMusicTbl();
        ArrayList<MusicData> sdCardList = findMusicFromContentProvider();

        if(dbList.isEmpty()){
            return sdCardList;
        }

        // DB에 sdCard 정보가 있다면
        if(dbList.containsAll(sdCardList)){
            return dbList;
        }
        int size = dbList.size();
        for (int i = 0 ; i < size;i++){
            if(dbList.contains(sdCardList.get(i))){
                continue;
            }
            dbList.add(sdCardList.get(i));
            ++size;
        }

        return dbList;
    }   // end of compareArrayList
}
