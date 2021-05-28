package com.example.newmusicplayer;

import android.provider.MediaStore;

import java.io.Serializable;

public class Mp3Data implements MediaStore.MediaColumns, Serializable {
    private int mId;
    private int albumId;
    private String title;
    private String artist;
    private String album;
    private int duration;
    private String path;
    private String id;

    public Mp3Data(int mId, int albumId, String title, String artist, String album, int duration, String path) {
        this.mId = mId;
        this.albumId = albumId;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
