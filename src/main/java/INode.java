import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public interface INode
{
    static  final Address SynchronisedMessagingServiceAddress = new Address("127.0.0.1",6000);

    void init(Request request) throws IOException;

    void connect() throws IOException;

    void disconnect() throws IOException;
}