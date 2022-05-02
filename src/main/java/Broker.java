import net.didion.jwnl.data.Exc;

import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class Broker implements INode{

    static InetAddress inetAddress;
    private static Address address = null;
    private static String Ip;
    private static int port;

    private Socket socket;
    private ServerSocket serverSocket;

    // Registered Publishers and Consumers with topics
    private HashMap<Address,ArrayList<String>> registeredPublishers;
    private HashMap<Address,ArrayList<String>>  registeredConsumers;
    // Total of initialized Clients , we dont keep track of topics etc.


    // this list includes both channel names and specific topics
    public static ArrayList<String> topics = new ArrayList<>();

    // Constructor
    public Broker(String Ip , int port){
        this.Ip = Ip;
        this.port = port;
        //this.address = new Address(this.Ip ,this.port);
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        registeredConsumers = new HashMap<>();
        registeredPublishers = new HashMap<>();
        init(5);
        connect();

    }

    public static void main(String args[]) throws IOException{
        System.out.println("port num:");
        port = new Scanner(System.in).nextInt();
        inetAddress = InetAddress.getLocalHost();
        Ip = inetAddress.getHostAddress();
        System.out.println(Ip);
        address = new Address(Ip,port);

        new Broker(Ip , port);
    }

    // creates server side socket and accepts connections
    // start new thread for each connection
    // serverThread.start()
    public void connect(){
        try{
            System.out.println("Server Socket Up and Running ...\n");
            while(true){

                socket = serverSocket.accept();
                System.out.println("socket.accept()\n");
                System.out.println(socket.getPort());

                Runnable task = () -> {
                    try {
                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                        Value value = (Value)in.readObject();
                        System.out.println(value.sender);
                        if(value.sender == SenderType.PUBLISHER){
                            new Thread(new publisherThread(socket , in , out ,value))
                                    .start();
                            System.out.println("pub thread.start()\n");
                        }else{
                            new Thread(new consumerThread(socket , in , out,value))
                                    .start();
                            System.out.println("cons thread.start()\n");
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }


                };
                new Thread(task).start();
            }

        }catch (IOException  e) { //| ClassNotFoundException
            e.printStackTrace();

        } finally {
            try {
                // close socket connection
                serverSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    /// Broker init() is responsible serving the client(either pub or cons), the brokersList {< <Ip,Port>,ArrayList<String>(Topics) >}
    @Override
    public void init(int x){
        updateBrokerInfo();
    }
    @Override
    public void updateNodes(Value value) {

        //topics.stream().forEach(t -> t.equalsIgnoreCase(value.getMultimediaFile().Hashtags.stream().forEach();));
        value.getMultimediaFile().Hashtags.forEach(hashtag ->{
            if(!topics.contains(hashtag))
                topics.add(hashtag);
        });
//        topics.stream()
//                .anyMatch(s -> s.equals(value.getMultimediaFile().ChannelName)) ? topics.add(value.getMultimediaFile().ChannelName) : System.out.println("hi");
        //topics.stream().forEach( t -> brokerTopics.get(address).add(t) );

//        if(!topics.contains(value.getMultimediaFile().ChannelName)) {
//            topics.add(value.getMultimediaFile().ChannelName);
//        }
        topics.stream().forEach( e -> System.out.println(e));

    }
    @Override
    public void disconnect(){}

    public static HashMap<Address,ArrayList<String>> getBrokerList(){
        HashMap<Address,ArrayList<String>> brokers = new HashMap<>();
        ArrayList<Address> a = new ArrayList<Address>(brokers.keySet());

        try(Socket service = new Socket(INode.ZookeeperAddress.getIp() , INode.ZookeeperAddress.getPort())){
            ObjectOutputStream service_out = new ObjectOutputStream(service.getOutputStream());
            ObjectInputStream service_in = new ObjectInputStream(service.getInputStream());

            service_out.writeObject("get brokers");
            service_out.flush();
            brokers = (HashMap<Address, ArrayList<String>>) service_in.readObject();
        }catch(IOException | ClassNotFoundException | ClassCastException e){
            e.printStackTrace();
            System.out.println("Problem synchronising brokers");
            return null;
        }
        return brokers;
    }

    public void updateBrokerInfo(){
        try{
            Socket serviceSocket = new Socket(INode.ZookeeperAddress.getIp(), INode.ZookeeperAddress.getPort());

            ObjectOutputStream out = new ObjectOutputStream(serviceSocket.getOutputStream());
            out.writeObject("insert or update broker");
            out.flush();

            out.writeObject(new Value(address , topics,SenderType.BROKER));
            out.flush();

            System.out.println("Broker send info to Zookeeper");
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void pull(ObjectOutputStream consumer_out , Address pubAddress , ArrayList<String> topics){
        ObjectInputStream pub_in;
        ObjectOutputStream pub_out;

//        ObjectInputStream cons_in;
//        ObjectOutputStream cons_out;

        MultimediaFile chunk;

        try{

            Socket pubSocket = new Socket(pubAddress.getIp(),pubAddress.getPort()+1);
            System.out.println(pubSocket.getPort() + "EXPECTED: " + (pubAddress.getPort()+1));

            pub_in = new ObjectInputStream(pubSocket.getInputStream());
            pub_out = new ObjectOutputStream(pubSocket.getOutputStream());
            System.out.println("PubSocket open ");

            pub_out.writeObject(topics);
            pub_out.flush();

            //cons_out = new ObjectOutputStream(consumerSocket.getOutputStream());
            //cons_in = new ObjectInputStream(consumerSocket.getInputStream());
            //System.out.println("Consumer Socket open ");
            while(true){

                Value value = (Value)pub_in.readObject();
                System.out.println("GOT CHUNK");
                chunk = value.getMultimediaFile();
//                if(chunk.IsFirst){
//                    ArrayList<String> _topics = chunk.Hashtags;
//                }
                consumer_out.writeObject(new Value(chunk,SenderType.BROKER));
                System.out.println(chunk.getAbsolutePath());
                System.out.println("SENT CHUNK");
                break;
//                if(chunk.IsLast){
//                    System.out.println("Received all chunks");
//                    break;
//                }
            }

        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

///////////////// PUBLISHER THREAD INNER CLASS///////////
    public class publisherThread extends Thread implements Serializable{

        ObjectInputStream service_in;
        ObjectOutputStream service_out;
        private final Socket socket;
        private Value value;

        public publisherThread(Socket _socket, ObjectInputStream in , ObjectOutputStream out, Value value) {
            this.socket = _socket;
            this.value = value;
            this.service_in = in;
            this.service_out = out;
        }

        @Override
        public void run(){
            try{
                System.out.println("Server Thread For Pub Triggered");

                if( value.getAction().equals("get brokers")){
                    init();
                    System.out.println("Get Brokers........");
                }else{
                    System.out.println(value.getAction());
                    updateRegisteredPublishers(value);
                    updateNodes(value);
                    updateBrokerInfo();
                }

            }catch(Exception e){
                e.printStackTrace();
            }finally {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        void init(){
            try {

                service_out.writeObject(new HashMap<>(getBrokerList()));
                service_out.flush();

            }catch(IOException e){
                e.printStackTrace();
            }
        }

        void updateRegisteredPublishers(Value value){

            // We already have the publisher registered to Broker
            if(registeredPublishers.containsKey(value.getAddress())){
                registeredPublishers.get(value.getAddress())
                        .addAll(value.getMultimediaFile().Hashtags);
                System.out.println("Pub updated ....");
            }else {
                // Publisher not registered to Broker
                registeredPublishers.put(value.getAddress(),  value.getMultimediaFile().Hashtags);
                System.out.println("Pub is now registered...");
            }

            registeredPublishers.forEach((k,v)
                    -> System.out.println("Publisher Address: " + k + "  Topics: " +v)
            );

        }

        public void replyText(){
            try{
                System.out.println("Method replyText() triggered ... \n");
                // Read Request serializable and read txt from pub
                value = (Value) service_in.readObject();

                // Register new Publisher
                registeredPublishers.put(value.getAddress() ,value.getMultimediaFile().Hashtags );

                System.out.println(value.getMultimediaFile().Hashtags);
//                String text = service_in.readUTF();
//                System.out.println(text);

                // Write serializable back to pub
                System.out.println("Text to send back to pub: \n");
                String reply_text =new BufferedReader(new InputStreamReader(System.in)).readLine();
                System.out.println("my reply is: " + reply_text);

                Request req = new Request(value.getAddress() , reply_text);
                System.out.println("Before .writeUTF ");
                //service_out = new ObjectOutputStream(socket.getOutputStream());
//
                service_out.writeObject(req);
                System.out.println(" .writeObject(req)");

                service_out.flush();
                System.out.println(" .flush()");

            }catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
            }

        }

    }


    ///////////////// CONSUMER THREAD INNER CLASS///////////
    public class consumerThread extends Thread{
        ObjectInputStream service_in;
        ObjectOutputStream service_out;
        public Socket socket;
        private Value value;

        public consumerThread(Socket _socket , ObjectInputStream in , ObjectOutputStream out , Value value){
            this.socket = _socket;
            this.value = value;
            this.service_in = in;
            this.service_out = out;
        }

        @Override
        public void run(){

            ArrayList<String> requestedTopics;

            try{

                System.out.println("Consumer Thread running ...\n");
                if(!value.initialized){

                    init();
                    INode.initClients.add(value.getAddress());
                    //service_out.writeObject("USER REGISTERED");

                }/// Consumer Initialized
                else{
                    updateConsumers(value);

                    requestedTopics = value.getTopics();
                    System.out.println(requestedTopics);
                    for(Address pubAddress : registeredPublishers.keySet()){
                        var pubTopics = registeredPublishers.get(pubAddress);
                        System.out.println(pubTopics);
                        if(pubTopics.stream()
                                .anyMatch(requestedTopics::contains)){
                            System.out.println("PULLING");
                            pull(service_out,pubAddress,requestedTopics);
                        }
                    }
                }
                System.out.println("Consumer thread ended....");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        void init(){
            try {
                service_out.writeObject(new HashMap<>(getBrokerList()));
                service_out.flush();

            }catch(IOException e){
                e.printStackTrace();
            }
        }

        void updateConsumers(Value value){

            // We already have the consumer registered to Broker
            if(registeredConsumers.containsKey(value.getAddress())){
                registeredConsumers.get(value.getAddress())
                        .add(value.getTopic());
                System.out.println("Con updated ....");
            }else {
                // Publisher not registered to Broker
                registeredConsumers.put(value.getAddress(),  value.getTopics());
                System.out.println("Con is now registered...");
            }

            registeredConsumers.forEach((k,v)
                    -> System.out.println("Consumers Address: " + k + "  Topics: " +v)
            );

        }
    }

}
