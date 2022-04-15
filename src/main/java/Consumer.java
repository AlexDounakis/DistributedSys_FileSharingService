import java.util.List;

public class Consumer implements IConsumer {

    public void disconnect(String s) {}
    public void register(String s) {}
    public void showConversationData(String s, Value v) {}

    public Consumer(List<Broker> brokers) { super(brokers); }

}
