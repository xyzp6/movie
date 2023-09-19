package bean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

public class VideoProvider implements AbstructProvider {
    private Context context;
    private List<Video> list; //储存变量，防止多次运行产生不必要的耗费
    public VideoProvider(Context context) {
        this.context = context;
        list=getList();
    }

    public void Updatelist() {
        list=getList();
    }

    public List<Video> getloclist() {
        return list;
    }

    @Override
    public List<Video> getList() {
        List<Video> list = null;
        if (context != null) {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
                    null, null);
            if (cursor != null) {
                list = new ArrayList<Video>();
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    int width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH));
                    int height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    String uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id).toString();
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

                    // 获取视频文件所在的文件夹名称
                    File videoFile = new File(path);
                    File parentFolder = videoFile.getParentFile();
                    String folderName = parentFolder.getName();
                    String title = videoFile.getName();
                    //去除后缀
                    int dotIndex = title.lastIndexOf('.');
                    if (dotIndex != -1) {
                        title = title.substring(0, dotIndex);
                    }

                    Video video = new Video(id,width,height, title, path, uri, size, folderName);

                    list.add(video);
                }
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 根据id获得名字
     */
    public String getNameFromId(int id) {
        String path=getPathFromId(id);
        File videoFile = new File(path);
        String title = videoFile.getName();
        //去除后缀
        int dotIndex = title.lastIndexOf('.');
        if (dotIndex != -1) {
            title = title.substring(0, dotIndex);
        }
        return title;
    }

    /**
     * 根据id获得地址
     */
    public String getPathFromId(int id) {
        String path="";
        String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA};
        String selection = MediaStore.Video.Media._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            path = cursor.getString(nameColumn);
            cursor.close();
        }
        return path;
    }

    /**
     * 根据id获得视频时长
     */
    public long getDurationFromId(int id) {
        long duration = 0;
        String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DURATION};
        String selection = MediaStore.Video.Media._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            duration = cursor.getInt(durationColumn);
            cursor.close();
        }
        return duration;
    }


    /**
     * 对应文件夹的视频
     */
    public List<Video> getMapList(String folder) {
        List<Video> newlist = new ArrayList<>();
        for (Video video : list) {
            if(video.getFolderName().equals(folder)) {
                newlist.add(video);
            }
        }
        newlist.sort(new Comparator<Video>() {
            @Override
            public int compare(Video v1, Video v2) {
                return v1.getTitle().compareTo(v2.getTitle());
            }
        });
        return newlist;
    }

    /**
     * 统计所有文件夹
     */
    public List<String> getFolderList() {
        List<String> newlist = new ArrayList<>();
        for (Video video : list) {
            if(!newlist.contains(video.getFolderName())) newlist.add(video.getFolderName());
        }
        Collections.sort(newlist);
        return newlist;
    }
}