import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MultimediaFile implements Serializable {

    String AbsolutePath;
    String FileName;
    String ChannelName;
    Date DateCreated;
    ArrayList<String> Hashtags = new ArrayList<>();
    List<byte[]> File;

    public long Count = 0;

    String text;


    public MultimediaFile(String absolutePath){
        this.AbsolutePath = absolutePath;
    }

    //Text constructor
    public MultimediaFile(byte[] FileChunk , Date dateCreated){
        this.File.add(FileChunk);
        this.DateCreated = dateCreated;
    }
    public MultimediaFile(List<byte[]> File,Date dateCreated){
        this.File = File;
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
    public List<byte[]> getVideoFileChunks() {
        return File;
    }
    public byte[] getVideoFileChunk(){return File.get(0);}
}
