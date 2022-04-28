import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Value implements Serializable {

    private MultimediaFile multimediaFile;
    private ArrayList<String> topics;
    private Address address;
    private String action = "something";
    private String topic;
    SenderType sender = null;


    public Value (MultimediaFile m,SenderType senderType) {
        this.multimediaFile = m;
        this.sender = senderType;
    }

    public Value (Address address,SenderType senderType) {
        this.address = address;
        this.sender = senderType;
    }

    public Value (MultimediaFile m, Address address,SenderType senderType) {
        this.multimediaFile = m;
        this.address = address;
        this.sender = senderType;
    }
    public Value(Address address , String action,SenderType senderType){
        this.address = address;
        this.action = action;
        this.sender = senderType;
    }
    public Value (Address address,ArrayList<String> topics,SenderType senderType){
        this.address = address;
        this.topics = topics;
        this.sender = senderType;
    }

    public MultimediaFile getMultimediaFile() {return multimediaFile;}

    public Address getAddress() {return address;}

    public ArrayList<String> getTopics(){
        return topics;
    }

    public String getAction(){
        return action;
    }

    public String getTopic() {return topic;}

    //@Override
    //public String toString() {return this.multimediaFile +" , "+this.address;}
}
