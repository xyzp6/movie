package bean;

public class VideoHistory{
    private int videoid;
    private long progress;
    private String title;

    public VideoHistory(int videoid, long progress,String title) {
        this.videoid = videoid;
        this.progress = progress;
        this.title = title;
    }

    public int getVideoId() {
        return videoid;
    }

    public void setVideoId(int videoid) {
        this.videoid = videoid;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}