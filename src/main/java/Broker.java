import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class Broker implements INode{

    static InetAddress inetAddress;
    protected static Address address = null;
    private static String Ip;
    private static int port;

    private Socket socket;
    private ServerSocket serverSocket;

    private ArrayList<Address> registeredPublishers = new ArrayList<>();
    private ArrayList<Address> registeredConsumers = new ArrayList<>();
    private ArrayList<String> topics = new ArrayList<>();

    // Constructor
    public Broker(String Ip , int port){
        this.Ip = Ip;
        this.port = port;

        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    // connect() ++ functions

    // creates server side socket and accepts connections
    // start new thread for each connection
    // serverThread.start()
    public void connect(){

        try{
            System.out.println("Server Socket Up and Running ...\n");
            while(true){

                socket = serverSocket.accept();
                System.out.println("socket.accept()\n");
                Thread thread = new serverThread(socket);
                thread.start();

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
    @Override
    public void init(int x){}

    @Override
    public void updateNodes() { }

    @Override
    public void disconnect(){}

    public class serverThread extends Thread{
        ObjectInputStream service_in;
        ObjectOutputStream service_out;
        public Socket socket;
        private Request request;



        public serverThread(Socket _socket){

            socket = _socket;
        }

        @Override
        public void run(){


            try{
                System.out.println("Server Thread For Pub Triggered");
                service_out = new ObjectOutputStream(socket.getOutputStream());
                service_in = new ObjectInputStream(socket.getInputStream());


                // Read Request serializable and read txt from pub
                request = (Request) service_in.readObject();
                // Register new Publisher
                registeredPublishers.add(request.Address);

                System.out.println(request.text);
//                String text = service_in.readUTF();
//                System.out.println(text);

                // Write serializable back to pub
                System.out.println("Text to send back to pub: \n");
                String reply_text =new BufferedReader(new InputStreamReader(System.in)).readLine();
                System.out.println("my reply is: " + reply_text);
                Request req = new Request(request.Address , reply_text);
                System.out.println("Before .writeUTF ");
                //service_out = new ObjectOutputStream(socket.getOutputStream());
//
                service_out.writeObject(req);
                System.out.println(" .writeObject(req)");

                service_out.flush();
                System.out.println(" .flush()");



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

    }

}
