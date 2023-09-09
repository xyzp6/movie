package bean;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Xml;

import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class SharedPreferencesBackup {
    public static final int REQUEST_CODE_CREATE_FILE = 2;
    public static final int REQUEST_CODE_OPEN_FILE = 3;
    private static Map<String, ?> allEntries;

    public static void backupSharedPreferences(Activity activity, String sharedPreferencesName) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
        allEntries = sharedPreferences.getAll();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String currentDate = dateFormat.format(new Date());
        String backupFileName = "movie_backup_" + currentDate + ".xml";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/xml");
        intent.putExtra(Intent.EXTRA_TITLE, backupFileName);
        activity.startActivityForResult(intent, REQUEST_CODE_CREATE_FILE);
    }

    public static void restoreSharedPreferences(Activity activity) {
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
                    XmlSerializer xmlSerializer = Xml.newSerializer();
                    StringWriter writer = new StringWriter();
                    xmlSerializer.setOutput(writer);
                    xmlSerializer.startDocument("UTF-8", true);
                    xmlSerializer.startTag(null, "map");
                    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                        if(!Objects.equals(entry.getKey(), "play_situation")) {
                            xmlSerializer.startTag(null, "entry");
                            xmlSerializer.attribute(null, "key", entry.getKey());
                            xmlSerializer.attribute(null, "value", entry.getValue().toString());
                            xmlSerializer.endTag(null, "entry");
                        }
                    }
                    xmlSerializer.endTag(null, "map");
                    xmlSerializer.endDocument();
                    xmlSerializer.flush();
                    String dataWrite = writer.toString();
                    outputStream.write(dataWrite.getBytes());
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
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(inputStream, "UTF-8");
                    int eventType = parser.getEventType();
                    SharedPreferences sharedPreferences = activity.getSharedPreferences("Playing", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && "entry".equals(parser.getName())) {
                            String key = parser.getAttributeValue(null, "key");
                            long value = Long.parseLong(parser.getAttributeValue(null, "value"));
                            editor.putLong(key, value);
                        }
                        eventType = parser.next();
                    }
                    editor.apply();
                    inputStream.close();
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
