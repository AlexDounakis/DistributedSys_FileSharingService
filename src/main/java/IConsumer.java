public interface IConsumer extends IAppNode {

    void disconnect(String s);
    void register(String s);
    void showConversationData(String s, Value v);

}
