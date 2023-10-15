package bean;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VideoDatabaseHelper extends SQLiteOpenHelper {
    public static final String DbName="Play.db";
    public static final String DataName="playprogress";
    public static final int DbVersion=1;
    public static final String SortSQL="select * from playprogress order by date desc";
    private Context context;
    public VideoDatabaseHelper(Context context) {
        super(context, DbName, null, DbVersion);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String AddSQL = "create table playprogress ("
                + "video_id integer primary key autoincrement, "
                + "progress integer, "
                + "date integer) ";
        db.execSQL(AddSQL);
    }

    /**
     * 添加数据
     * @param video_id 视频ID
     * @param progress 视频播放进度
     */
    public void InsertVideo(int video_id,long progress) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();// 临时变量

        values.put("video_id",video_id);
        values.put("progress",progress);
        values.put("date", System.currentTimeMillis());

        int rows = db.update(DataName, values, "video_id = ?", new String[]{String.valueOf(video_id)});

        if (rows == 0) {
            // 如果没有更新任何行，那么插入新的记录
            db.insert(DataName, null, values);
        }
        values.clear(); //清空values的值
        db.close(); //关闭数据库
    }

    /**
     * 根据video_id查询progress
     * @param video_id 视频ID
     * @return 视频进度，为空返回-1
     */
    public long getProgress(int video_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DataName, new String[]{"progress"}, "video_id = ?", new String[]{String.valueOf(video_id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") long progress = cursor.getLong(cursor.getColumnIndex("progress"));
            cursor.close();
            db.close();
            return progress;
        } else {
            db.close();
            return -1; // 如果没有找到匹配的video_id，返回-1
        }
    }

    /**
     * 遍历最近的历史记录
     * @param cnt 显示数量
     * @return 返回最近记录
     */
    @SuppressLint("Range")
    public List<VideoHistory> getVideoLastHistories(int cnt) {
        List<VideoHistory> videoHistories = new ArrayList<>();
        VideoProvider videoProvider=new VideoProvider(context);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SortSQL, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                cnt--;
                int video_id = cursor.getInt(cursor.getColumnIndex("video_id"));
                long progress = cursor.getLong(cursor.getColumnIndex("progress"));
                String title = videoProvider.getNameFromId(video_id);
                if (!Objects.equals(title, "")) {
                    videoHistories.add(new VideoHistory(video_id, progress, title));
                }
                if(cnt==0) break;
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return videoHistories;
    }

    /**
     * 遍历历史记录
     * @return 返回所有记录
     */
    @SuppressLint("Range")
    public List<VideoHistory> getVideoHistories() {
        List<VideoHistory> videoHistories = new ArrayList<>();
        VideoProvider videoProvider=new VideoProvider(context);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SortSQL, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int video_id = cursor.getInt(cursor.getColumnIndex("video_id"));
                long progress = cursor.getLong(cursor.getColumnIndex("progress"));
                String title = videoProvider.getNameFromId(video_id);
                if (!Objects.equals(title, "")) {
                    videoHistories.add(new VideoHistory(video_id, progress, title));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return videoHistories;
    }

    /**
     * 删除指定的记录
     * @param video_id 视频ID
     */
    public void DeleteVideo(int video_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DataName,"video_id=?",new String[]{String.valueOf(video_id)});
        db.close();
        this.close();
    }

    /**
     * 获得数据库文件
     * @return 返回文件
     */
    public File getDatabaseFile() {
        return context.getDatabasePath(DbName);
    }

    /**
     * 替换数据库文件
     * @param newDatabaseFile 新的数据库
     */
    public void replaceDatabaseFile(File newDatabaseFile) {
        File oldDatabaseFile = getDatabaseFile();

        if (oldDatabaseFile.exists()) {
            oldDatabaseFile.delete();
        }

        try {
            FileInputStream fis = new FileInputStream(newDatabaseFile);
            FileOutputStream fos = new FileOutputStream(oldDatabaseFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.flush();
            fis.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除数据库
     */
    public void deleteDatabaseFile() {
        File DatabaseFile = getDatabaseFile();

        if (DatabaseFile.exists()) {
            DatabaseFile.delete();
        }
    }

    /*数据库更新，重写SQLiteOpenHelper类抽象方法*/
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists playprogress");
        onCreate(db);
    }
}