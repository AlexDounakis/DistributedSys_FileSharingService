import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Consumer implements IConsumer {

    private Socket socket;
    public Address addr;
    private Value value;
    ArrayList<String> topics = new ArrayList();

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
                System.out.println("\n Thread for init running...\n");
                socket = new Socket(brokers.get(0).getIp(), brokers.get(0).getPort());

                ObjectOutputStream service_out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream service_in = new ObjectInputStream(socket.getInputStream());
                service_out.writeObject(new Value(this.addr,SenderType.CONSUMER,false));

                AppNode.brokersList = (HashMap) service_in.readObject();
                AppNode.brokersList.forEach((k,v)
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
        this.topics.addAll(topics);

        // Thread .run() - thread functionality
        Runnable task = () -> {
            try {
                System.out.println("thread Send Topics started ...");
                ArrayList<String> temp = new ArrayList();

                //AtomicReference<Address> brokerAddress = null;

                this.topics.forEach(s -> {

                            System.out.println(s);
                            System.out.println(AppNode.brokersList);
                            AppNode.brokersList.forEach((k, t)
                                    -> {
                                System.out.println(k + " " + t + "");
                                if (t.contains(s)) {
                                    System.out.println("INSIDE Contains");
                                    temp.add(s);
                                    Socket socketToBroker;
                                    try {
                                        System.out.println(k.getPort());
                                        socketToBroker = new Socket(k.getIp(), k.getPort());
                                        System.out.println("Connected to " + k.getIp() + ":" + k.getPort());

                                        ObjectOutputStream service_out = new ObjectOutputStream(socketToBroker.getOutputStream());
                                        ObjectInputStream service_in = new ObjectInputStream(socketToBroker.getInputStream());

                                        service_out.writeObject(new Value(this.addr, temp, SenderType.CONSUMER));
                                        service_out.flush();
                                        temp.clear();

                                        System.out.println("waiting for files .... ");
                                        receiveFile(service_in);

                                    } catch (Exception e) {

                                        e.printStackTrace();
                                    }

//                                finally {
//                                    try {
//                                        // close socket connection
////                                        socketToBroker.close();
//                                        System.out.println("send topics socket.close()");
//                                    } catch (IOException ioException) {
//                                        ioException.printStackTrace();
//                                    }
//                                }
                                }

                            });
                        }
                );
            } catch (Exception e) {
                e.getStackTrace();
            }

        };
        Thread thread = new Thread(task);
        thread.start();
    }

    public void receiveFile(ObjectInputStream in){
        String home = System.getProperty("user.home");
        try {
            Value value_in_chunk = (Value) in.readObject();
            MultimediaFile chunk = value_in_chunk.getMultimediaFile();
            //File file = new File(home + "/Downloads/" + chunk.FileName + ".txt");
            System.out.println(chunk.text);

//            while(true){
//
//            }
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

}
