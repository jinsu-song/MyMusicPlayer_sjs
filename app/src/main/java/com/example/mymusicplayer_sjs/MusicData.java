package com.example.mymusicplayer_sjs;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class MusicData implements Parcelable {
    private String id;
    private String artists;
    private String title;
    private String albumArt;
    private String duration;
    private int liked;

    public MusicData(){}
    public MusicData(String id, String artists, String title, String albumArt, String duration, int liked) {
        this.id = id;
        this.artists = artists;
        this.title = title;
        this.albumArt = albumArt;
        this.duration = duration;
        this.liked = 0;
    }

    public int getLiked() {
        return liked;
    }
    public void setLiked(int liked) {
        this.liked = liked;
    }

    public String getId() { return id;  }
    public void setId(String id) {this.id = id;  }

    public String getArtists() {return artists;  }
    public void setArtists(String artists) {this.artists = artists;  }

    public String getTitle() {return title;  }
    public void setTitle(String title) {this.title = title;  }

    public String getAlbumArt() {return albumArt;  }
    public void setAlbumArt(String albumArt) {this.albumArt = albumArt;  }

    public String getDuration() {return duration;  }
    public void setDuration(String duration) {this.duration = duration;  }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicData musicData = (MusicData) o;
        return Objects.equals(id, musicData.id) && Objects.equals(albumArt, musicData.albumArt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, albumArt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
