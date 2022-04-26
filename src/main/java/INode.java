import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface INode {

    static  final Address ZookeeperAddress = new Address("127.0.0.1",5000);

    //void init(Request request) throws IOException;

    //void connect() throws IOException;

    //void disconnect() throws IOException;
    //------------------------------------------------------------------------------------------------------------------

    void connect();
    void disconnect();
    void init(int x);
    void updateNodes(Value value);

}