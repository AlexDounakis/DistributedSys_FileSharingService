import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
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
//            System.out.println("HI , SELECT YOUR ACTIONS ,0 to exit ,  1 for consumer , 2 for pub");
//            type = System.in.read();

        }catch(Exception e){
            e.getStackTrace();
        }

        System.out.print( "HI , SELECT YOUR ACTIONS ,0 to exit ,  1 for pub" );
        int type= new Scanner(System.in).nextInt();
        while( type != 0){
            System.out.println("Hi, type text to send:");

            try{
                String text =new BufferedReader(new InputStreamReader(System.in)).readLine();
                Publisher pub = new Publisher(address,"Test Channel Name");

                pub.sendText(text);
                //System.out.println("sendtext()");

                System.out.println("HI , SELECT YOUR ACTIONS ,0 to exit ,  1 for consumer , 2 for pub");
                type = new Scanner(System.in).nextInt();
            }catch(Exception e ){
                e.printStackTrace();
            }


        }
        System.out.println("APP NODE EXITING");


    }
}
