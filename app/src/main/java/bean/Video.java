package bean;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.Serializable;

public class Video implements Serializable{
    private static final long serialVersionUID = -7920222595800367956L;
    private int id;
    private int width;
    private int height;
    private String title;
    private String path;
    private String uri;
    private long size;
    private String folderName;

    /**
     *
     */
    public Video() {
        super();
    }

    public Video(int id,int width,int height, String title, String path,String uri, long size,String folderName) {
        super();
        this.id = id;
        this.width = width;
        this.height = height;
        this.title = title;
        this.path = path;
        this.uri=uri;
        this.size = size;
        this.folderName=folderName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}