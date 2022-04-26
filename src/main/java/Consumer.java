import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

public class Consumer implements IConsumer {

    private Socket socket;
    static InetAddress inetAddress;
    protected static Address address;
    private static String Ip;
    private static int port;
    private boolean isInit = false;

    public void disconnect(String s) {}
    public void register(String s) {}
    public void showConversationData(String s, Value v) {}

    public Consumer(List<Broker> brokers) { super(brokers); }
    private HashMap<Address, ArrayList<String>> brokerslist;

    public  Consumer(Address address) {
        this.address = address;
    }

    public void init(){
        //anoigma thread gia ton karfoto broker
        //o broker koitaei an einai initialised o sugkekrimenos consumer (pedio), an den einai epistrefei olh th lista
        //line 14 gemisma, brokers.get[i]
        try {
            // establish the connection with server
            Socket s = new Socket(Ip, port);
            Thread thread = new Thread(s);
            thread.start();
            System.out.println("Thread started ...\n");
            if (!isInit) {
                System.out.println("Initialising new user ...\n");
                brokerslist.toString();
            }
        }catch(Exception e ){
            e.printStackTrace();
        }
    }


}
