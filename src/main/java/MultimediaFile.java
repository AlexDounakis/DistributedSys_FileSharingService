import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;

public class MultimediaFile implements Serializable {

    String AbsolutePath;
    String FileName;
    String ChannelName;
    String DateCreated;
    String Length;
    String Framerate;
    String FrameWidth;
    String FrameHeight;
    ArrayList<String> Hashtags = new ArrayList<>();
    byte[] FileChunk;
    boolean IsLast = false;

    String text;


    //Video Constructor
    public MultimediaFile(String videoName, String channelName, String dateCreated, String length, String Framerate,
                          String frameWidth, String frameHeight , ArrayList<String> hashtags, byte[] FileChunk , String absolutePath) {
        this.FileName = videoName;
        this.ChannelName = channelName;
        this.DateCreated = dateCreated;
        this.Length = length;
        this.Framerate = Framerate;
        this.FrameWidth = frameWidth;
        this.FrameHeight = frameHeight;
        this.Hashtags = hashtags;
        this.FileChunk = FileChunk;
        this.AbsolutePath = absolutePath;
    }

    public MultimediaFile(byte[] FileChunk , String fileName , String dateCreated){
        this.FileChunk = FileChunk;
        this.FileName =fileName;
        this.DateCreated = dateCreated;
    }

    //Photo constructor
    public MultimediaFile(byte[] FileChunk, String fileName, String channelName, String dateCreated, String frameWidth, String frameHeight ){
        this.FileChunk = FileChunk;
        this.FileName =fileName;
        this.ChannelName = channelName;
        this.DateCreated = dateCreated;
        this.FrameWidth = frameWidth;
        this.FrameHeight = frameHeight;
    }

    //Text constructor
    public MultimediaFile(byte[] FileChunk, String fileName, String channelName, String dateCreated){
        this.FileChunk = FileChunk;
        this.FileName =fileName;
        this.ChannelName = channelName;
        this.DateCreated = dateCreated;
    }
    public MultimediaFile( String channelName , String text){
        //this.FileChunk = FileChunk;
        this.ChannelName = channelName;
        this.text = text;
    }

    public String getVideoName() {
        return FileName;
    }
    public void setHashtags(ArrayList<String> hashtags) {
        this.Hashtags= hashtags;
    }
    public void setHashtag(String topic){this.Hashtags.add(topic);}
    public ArrayList<String> getHashtags() {
        return Hashtags;
    }
    public void addHashtag(String hashtag){
        if(!Hashtags.contains(hashtag))
            Hashtags.add(hashtag);
    }

    public String getAbsolutePath() {return AbsolutePath;}

    public void setIsLast(boolean isLast){
        this.IsLast = isLast;
    }
    public boolean IsLast(){return this.IsLast;}
    public byte[] getVideoFileChunk() {
        return FileChunk;
    }
}
