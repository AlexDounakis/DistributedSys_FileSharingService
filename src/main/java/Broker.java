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

    protected static final Map<Address, ArrayList<String>> brokerTopics = new ConcurrentHashMap<Address, ArrayList<String>>();
    protected Map<Address, ArrayList<String>> brokerTopics() {
        return Broker.brokerTopics;
    }
//    private HashMap<Address,ArrayList<String>> brokersList = new HashMap<>() {
//        {
//            put(new Address("192.281.1.1",9000) , new ArrayList<>() {{ add("nice1") ; add("topic1") ; }});
//
//        }
//    };

    // Registered Publishers and Consumers with topics
    private HashMap<Address,ArrayList<String>> registeredPublishers;
    private HashMap<Address,ArrayList<String>>  registeredConsumers;
    // Total of initialized Clients , we dont keep track of topics etc.
    private ArrayList<Address> initClients;

    // this list includes both channel names and specific topics
    public ArrayList<String> topics = new ArrayList<>();

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
        initClients = new ArrayList<>();
        registeredConsumers = new HashMap<>();
        registeredPublishers = new HashMap<>();
        connect();

    }

    public static void main(String args[]) throws IOException{
        System.out.println("port num:");
        port = new Scanner(System.in).nextInt();
        inetAddress = InetAddress.getLocalHost();
        Ip = inetAddress.getHostAddress();
        System.out.println(Ip);
        address = new Address(Ip,port);


        brokerTopics.put(new Address("192.281.1.1",9000) ,new ArrayList<>() {
            {
                add("nice1");
                add("topic1");

            }

        });
        brokerTopics.put(address,new ArrayList<>());


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

                new Thread(new publisherThread(socket))
                        .start();

//                Thread threadConsumer = new consumerThread(socket);
//                threadConsumer.start();
//                System.out.println("cons thread.start()\n");
                //System.out.println("Server Thread started ...\n");
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
    public void init(int x){}

    @Override
    public void updateNodes(Value value) {
        topics.addAll(value.getMultimediaFile().Hashtags);
        topics.add(value.getMultimediaFile().ChannelName);
        topics.stream().forEach( t -> brokerTopics.get(address).add(t) );

        topics.stream().forEach( e -> System.out.println(e));

    }
    @Override
    public void disconnect(){}

///////////////// PUBLISHER THREAD INNER CLASS///////////
    public class publisherThread extends Thread implements Serializable{

        ObjectInputStream service_in;
        ObjectOutputStream service_out;
        private Socket socket;
        private Value value;

        public publisherThread(Socket _socket){
            socket = _socket;
        }

        @Override
        public void run(){


            try{
                System.out.println("Server Thread For Pub Triggered");

                service_out = new ObjectOutputStream(socket.getOutputStream());
                service_in = new ObjectInputStream(socket.getInputStream());

                Value val = (Value)service_in.readObject();
                if(!initClients.contains(val.getAddress())){
                    init();
                    initClients.add(val.getAddress());
                    System.out.println("Address:" + initClients.get(initClients.size()-1) +" is initialized... \n" );
                }else{
                       updatePublishers(val);
                       updateNodes(val);
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
                System.out.println("Init()");
                System.out.println(brokerTopics);

                service_out.writeObject(new HashMap<>(brokerTopics));
                service_out.flush();
                System.out.println("Client Initialized");

            }catch(IOException e){
                e.printStackTrace();
            }
        }

        void updatePublishers(Value value){
            System.out.println("Updating Publisher Topics");
            // We already have the publisher registered to Broker
            if(registeredPublishers.containsKey(value.getAddress())){
                registeredPublishers.get(value.getAddress())
                        .addAll(value.getMultimediaFile().Hashtags);

            }else {
                // Publisher not registered to Broker
                registeredPublishers.put(value.getAddress(),  value.getMultimediaFile().Hashtags);

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
    /// run not implemented
    public class consumerThread extends Thread{
        ObjectInputStream service_in;
        ObjectOutputStream service_out;
        public Socket socket;
        private Value value;

        public consumerThread(Socket _socket){
            this.socket = _socket;
        }

        @Override
        public void run(){
            System.out.println("Consumer Thread running ...\n");
            try{
                service_in = new ObjectInputStream(socket.getInputStream());
                service_out = new ObjectOutputStream(socket.getOutputStream());

                value = (Value)service_in.readObject();
                // consumer already initialized
                if(initClients.contains(value.getAddress())){

                }/// Consumer not Initialized
                else{
                    init(5);
                    initClients.add(value.getAddress());
                    service_out.writeObject("USER REGISTERED");
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
