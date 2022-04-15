import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

public interface IPublisher extends INode {

    /*void addHashTag(String hashtag);

    void removeHashTag(String hashtag);

    //HashMap<Address, ArrayList<String>> getBrokersList();

    void push(File file, ArrayList<String> topics, ObjectOutputStream outputStream) throws TikaException, IOException, SAXException;

    void notifyFailure(Broker broker);

    void notifyBrokerForHashtags(String string);

    //ArrayList<MultimediaFile> generateChunks(File file) throws TikaException, IOException, SAXException;*/

    //------------------------------------------------------------------------------------------------------------------

    ProfileName profileName;
    ArrayList<Value> generateChunks(MultimediaFile mf);
    void getBrokerList();
    Broker hashTopic(String s);
    void notifyBrokersNewMessage(String s);
    void notifyFailure(Broker broker);
    void push(String s, Value v);

}