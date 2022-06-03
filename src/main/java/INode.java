import org.apache.cxf.jaxrs.ext.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface INode {

    static  final Address ZookeeperAddress = new Address("10.26.13.175",5000);

    ArrayList<Address> initClients = new ArrayList<>();
    //void init(Request request) throws IOException;

    //void connect() throws IOException;

    //void disconnect() throws IOException;
    //------------------------------------------------------------------------------------------------------------------

    void connect();
    void disconnect();
    void init(int x);
    void updateNodes(Value value);

}