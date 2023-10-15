package bean;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class DatabaseBackup {
    public static final int REQUEST_CODE_CREATE_FILE = 2;
    public static final int REQUEST_CODE_OPEN_FILE = 3;
    private static VideoDatabaseHelper videoDatabaseHelper;

    public static void backup(Activity activity) {
        videoDatabaseHelper=new VideoDatabaseHelper(activity);
        File dbFile = videoDatabaseHelper.getDatabaseFile();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String currentDate = dateFormat.format(new Date());
        String backupFileName = "movie_backup_" + currentDate + ".db";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/db");
        intent.putExtra(Intent.EXTRA_TITLE, backupFileName);
        activity.startActivityForResult(intent, REQUEST_CODE_CREATE_FILE);
    }

    public static void restore(Activity activity) {
        videoDatabaseHelper=new VideoDatabaseHelper(activity);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        activity.startActivityForResult(intent, REQUEST_CODE_OPEN_FILE);
    }

    public static void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CREATE_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    OutputStream outputStream = activity.getContentResolver().openOutputStream(uri);
                    FileInputStream inputStream = new FileInputStream(videoDatabaseHelper.getDatabaseFile());
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.flush();
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == REQUEST_CODE_OPEN_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    InputStream inputStream = activity.getContentResolver().openInputStream(uri);
                    File newDatabaseFile = new File(activity.getCacheDir(), "temp.db");
                    OutputStream outputStream = new FileOutputStream(newDatabaseFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.flush();
                    inputStream.close();
                    outputStream.close();

                    videoDatabaseHelper.replaceDatabaseFile(newDatabaseFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
