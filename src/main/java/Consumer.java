import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Consumer implements IConsumer {

    private Socket socket;
    public Address addr;
    private Value value;
    ArrayList<String> topics = new ArrayList();

    private HashMap<Address , ArrayList<String>> brokersList;

    protected ArrayList<Address> brokers = new ArrayList<>(Arrays.asList(
            /// first random broker IP and Port
            new Address("192.168.56.1", 6000)


    ));

    public void disconnect(String s) {}
    public void register(String s) {}
    public void showConversationData(String s, Value v) {}

    public Consumer(){}

    public Consumer(Address _addr){
        this.addr = _addr;
        init(5);
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void init(int x) {
        Runnable task = () ->{
            try {
                System.out.println("Thread for init running...\n");
                socket = new Socket(brokers.get(0).getIp(), brokers.get(0).getPort());

                ObjectOutputStream service_out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream service_in = new ObjectInputStream(socket.getInputStream());
                service_out.writeObject(new Value(this.addr));
                brokersList = (HashMap) service_in.readObject();
                brokersList.forEach((k,v)
                        -> System.out.println("Address: " + k + "   Topics:" +  v));

            }catch(Exception e){
                e.printStackTrace();
            }try{
                socket.close();
                System.out.println("Thread for init closed...");
            }catch (IOException e){
                e.printStackTrace();
            }

        };
        Thread initThread = new Thread(task);
        initThread.start();
    }

    @Override
    public void updateNodes(Value value) {

    }

    public void sendTopics(ArrayList<String> topics){
        this.topics = topics;

        // Thread .run() - thread functionality
        Runnable task = () -> {
            try {
                System.out.println("thread started ...");
                ArrayList<String> temp = new ArrayList();
                    //get address from brokers list hashmap
                    topics.forEach(s ->
                            AppNode.brokersList.forEach((k,t)
                                    -> {
                                if(t.contains(s)){
                                    temp.add(s);
                                    try {
                                        socket = new Socket(k.getIp(), k.getPort());
                                        System.out.println("Connected to " + k.getIp() + ":" + k.getPort());

                                        ObjectOutputStream service_out = new ObjectOutputStream(socket.getOutputStream());
                                        ObjectInputStream service_in = new ObjectInputStream(socket.getInputStream());

                                        service_out.writeObject(new Value(this.addr, temp));
                                        temp.clear();
                                        service_out.flush();

                                        System.out.println("Con .flush()");
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }try{
                                        socket.close();
                                        System.out.println("Thread for init closed...");
                                    }catch (IOException e){
                                        e.printStackTrace();
                                    }
                                }
                            })
                    );



            } catch (Exception e) {
                e.getStackTrace();
            } finally {
                try {
                    // close socket connection
                    socket.close();
                    System.out.println("Send text socket.close()");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();


    }

}
