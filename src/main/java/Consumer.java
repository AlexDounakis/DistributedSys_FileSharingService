import org.apache.cxf.endpoint.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Consumer implements IConsumer {

    private Socket socket;
    private ServerSocket serverSocket;
    private Socket socketToReceive;
    public Address addr;
    private Value value;
    ArrayList<String> topics = new ArrayList();
    private HashMap<String,ArrayList<MultimediaFile>> myMultimediaFiles = new HashMap<>();

    protected ArrayList<Address> brokers = new ArrayList<>(Arrays.asList(
            /// first random broker IP and Port
            new Address("192.168.56.1", 6000)


    ));

    public void disconnect(String s) {}
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

    public void register(String hashtag){
        this.topics.add(hashtag);

        // Thread .run() - thread functionality
        Runnable task = () -> {
            try {
                System.out.println("thread Send Topics started ...");
                AppNode.brokersList
                        .forEach((k, t) ->
                        {
                            System.out.println(k + " " + t + "");
                            if (t.contains(hashtag)) {
                                Socket socketToBroker;
                                try {
                                    socketToBroker = new Socket(k.getIp(), k.getPort());
                                    System.out.println("Connected to " + k.getIp() + ":" + k.getPort());

                                    ObjectOutputStream service_out = new ObjectOutputStream(socketToBroker.getOutputStream());
                                    ObjectInputStream service_in = new ObjectInputStream(socketToBroker.getInputStream());

                                    service_out.writeObject(new Value(this.addr,hashtag , "something",SenderType.CONSUMER));
                                    service_out.flush();

                                    socketToBroker.close();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        });
            } catch (Exception e) {
                e.getStackTrace();
            }

        };
        Thread thread = new Thread(task);
        thread.start();
    }

    public void pull(){
        Runnable task =() ->{
            try{
                 serverSocket = new ServerSocket(addr.getPort()+1);
                while(true){
                    socketToReceive = serverSocket.accept();
                    System.out.println("consumer socket.accept()\n");
                    Runnable _task = () ->{
                        try{
                            ObjectOutputStream out = new ObjectOutputStream(socketToReceive.getOutputStream());
                            ObjectInputStream in = new ObjectInputStream(socketToReceive.getInputStream());

                            Value chunkInValue = (Value)in.readObject();
                            MultimediaFile chunk = chunkInValue.getMultimediaFile();




                        }catch (IOException | ClassNotFoundException e){
                            e.printStackTrace();
                        }
                    };
                    new Thread(_task).start();
                }



            }catch(IOException e){
                e.printStackTrace();
            }
        };
        new Thread(task).start();
    }

    public void receive(){

    }

    public void receiveFile(ObjectInputStream in){
        /*String home = System.getProperty("user.home");
        try {

            Value chunkInValue = (Value) in.readObject();
            //System.out.println(value_in_chunk.get);
            MultimediaFile chunk = chunkInValue.getMultimediaFile();
            //File file = new File(home + "/Downloads/" + chunk.FileName + ".txt");
            System.out.println(chunk.Hashtags);
            System.out.println(chunk.getAbsolutePath());

            long sumOfFiles = chunk.Count;
//            while(sumOfFiles >0){
//                if(chunk.IsFirst){
//                    //saveHashtags(chunk);
//
//                }else if(chunk.IsLast){
//                    System.out.println("GOT File: ");
//                }
//                //saveFile(chunk);
//                chunkInValue = (Value)in.readObject();
//                chunk = chunkInValue.getMultimediaFile();
////                System.out.println(chunk.Hashtags);
//                System.out.println(chunk.getAbsolutePath());
//
//            }
            System.out.println("GOT ALL FILES....");
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }*/
    }

}
