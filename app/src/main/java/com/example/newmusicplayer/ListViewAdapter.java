package com.example.newmusicplayer;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<>();
    private final Uri artworkUri = Uri.parse("content://media/internal/audio/albumart");

    public ListViewAdapter() {}

    // listView의 데이터의 갯수를 리턴
    @Override
    public int getCount() {
        return listViewItemList.size();
    }


    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final int pos = position;
        final Context context = viewGroup.getContext();

        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_view_item, viewGroup, false);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        TextView titleTextView = (TextView) view.findViewById(R.id.title);
        TextView singerTextView = (TextView) view.findViewById(R.id.singer);

        ListViewItem listViewItem = listViewItemList.get(position);
        Uri albumArtUri = ContentUris.withAppendedId(artworkUri, listViewItem.getAlbumId());
        Picasso.get().load(albumArtUri).error(R.drawable.ic_launcher_background).into(imageView);

        titleTextView.setText(listViewItem.getTitle());
        singerTextView.setText(listViewItem.getArtist());

        return view;
    }

    public void addItem(int mId, int albumId, String title, String artist, String album, int duration, String path) {
        ListViewItem item = new ListViewItem(albumId, title, artist);

        item.setmId(mId);
        item.setAlbumId(albumId);
        item.setTitle(title);
        item.setArtist(artist);
        item.setAlbum(album);
        item.setDuration(duration);
        item.setPath(path);

        listViewItemList.add(item);
    }
}
