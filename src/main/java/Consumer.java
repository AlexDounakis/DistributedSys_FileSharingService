import java.util.List;

public class Consumer extends AppNode implements IConsumer {

    public void disconnect(String s) {}
    public void register(String s) {}
    public void showConversationData(String s, Value v) {}

    Consumer(List<Broker> brokers) { super(brokers); }

}
