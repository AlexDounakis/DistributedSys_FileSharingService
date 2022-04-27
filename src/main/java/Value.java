import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Value implements Serializable {

    private MultimediaFile multimediaFile;
    private ArrayList<String> topics;
    private Address address;
    private String topic;

    public Value (MultimediaFile m) {
        this.multimediaFile = m;
    }

    public Value (Address address) {
        this.address = address;
    }

    public Value (MultimediaFile m, Address address) {
        this.multimediaFile = m;
        this.address = address;
    }

    public Value (Address address,ArrayList<String> topics){
        this.address = address;
        this.topics = topics;
    }

    public Value (Address address, String s){
        this.topic = s;
        this.address = address;
    }

    public MultimediaFile getMultimediaFile() {return multimediaFile;}

    public Address getAddress() {return address;}

    public ArrayList<String> getTopics(){
        return topics;
    }

    public String getTopic() {return topic;}

    //@Override
    //public String toString() {return this.multimediaFile +" , "+this.address;}
}
