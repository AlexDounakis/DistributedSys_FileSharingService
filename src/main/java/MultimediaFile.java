import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;

public class MultimediaFile {

    String AbsolutePath;
    String VideoName;
    String ChannelName;
    String DateCreated;
    String Length;
    String Framerate;
    String FrameWidth;
    String FrameHeight;
    ArrayList<String> Hashtags;
    byte[] VideoFileChunk;
    boolean IsLast = false;


    //Constructor
    public MultimediaFile(String videoName, String channelName, String dateCreated, String length, String framerate,
                          String frameWidth, String frameHeight , ArrayList<String> hashtags, byte[] videoFileChunk , String absolutePath) {
        this.VideoName = videoName;
        this.ChannelName = channelName;
        this.DateCreated = dateCreated;
        this.Length = length;
        this.Framerate = framerate;
        this.FrameWidth = frameWidth;
        this.FrameHeight = frameHeight;
        this.Hashtags = hashtags;
        this.VideoFileChunk = videoFileChunk;
        this.AbsolutePath = absolutePath;
    }

    public MultimediaFile(byte[] videoFileChunk , String fileName , String dateCreated){
        this.VideoFileChunk = videoFileChunk;
        this.VideoName =fileName;
        this.DateCreated = dateCreated;
    }
    public String getVideoName() {
        return VideoName;
    }
    public void setHashtags(ArrayList<String> hashtags) {
        this.Hashtags= hashtags;
    }

    public ArrayList<String> getHashtags() {
        return Hashtags;
    }
    public void addHashtag(String hashtag){
        if(!Hashtags.contains(hashtag))
            Hashtags.add(hashtag);
    }

    public void setIsLast(boolean isLast){
        this.IsLast = isLast;
    }
    public boolean IsLast(){return this.IsLast;}
    public byte[] getVideoFileChunk() {
        return VideoFileChunk;
    }
}
