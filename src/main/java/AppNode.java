
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AppNode {

    protected static String Ip;
    protected static int port;
    private static int type;

    // localhost addr
    static InetAddress inetAddress;
    protected static Address address = null;

    public int action ;

    public static void main (String args[]){


        // get localhost IP and port num;
        try{
            System.out.println("port num:");
            port = new Scanner(System.in).nextInt();
            inetAddress = InetAddress.getLocalHost();
            Ip = inetAddress.getHostAddress();
            System.out.println(Ip);
            address = new Address(Ip,port);

        }catch(Exception e){
            e.getStackTrace();
        }

        System.out.print( "Welcome , select user type , 0 to exit , 1 for pub , 2 for consumer " );
        int type= new Scanner(System.in).nextInt();
        while( type != 0){
            // Publisher logic
            if(type == 1) {
                //System.out.println("Type message to send:");
                try {
                    //String text = new BufferedReader(new InputStreamReader(System.in)).readLine();
                    Publisher pub = new Publisher(address, "Test Channel Name");

                    // upload
                    //pub.init(5);
                    //pub.sendText(text);

                    System.out.println(" Select user type , 0 to exit , 1 for pub , 2 for consumer ");
                    type = new Scanner(System.in).nextInt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Consumer Logic
            if(type == 2){
                System.out.println(" SELECT FROM INIT / SEARCH / UNREGISTER");
                String action = System.console().readLine();
                if(action.equalsIgnoreCase("init")) {
//                    new Consumer(address, port, action);
//                    init = true;
                }
            }

        }
        System.out.println("APP NODE EXITING");


    }
}
