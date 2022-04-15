import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface INode {

    //static  final Address SynchronisedMessagingServiceAddress = new Address("127.0.0.1",6000);

    //void init(Request request) throws IOException;

    //void connect() throws IOException;

    //void disconnect() throws IOException;
    //------------------------------------------------------------------------------------------------------------------

    List<Broker> brokers = null;
    abstract void connect();
    void disconnect();
    void init(int x);
    void updateNodes();

}