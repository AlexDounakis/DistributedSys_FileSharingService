
import org.apache.cxf.service.model.BindingInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
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

    private int action ;
    private boolean initialized = false;


    public static void main (String args[]) throws IOException {


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

        System.out.println("Enter Publisher Channel Name:  ");
        String channelName =  new BufferedReader(new InputStreamReader(System.in)).readLine();

        Publisher pub = new Publisher(address, channelName);

        System.out.print( "Welcome , select user type , 0 to exit , 1 for pub , 2 for consumer  , 3 for Updating Broker Info" );
        int type= new Scanner(System.in).nextInt();
        while( type != 0){
            // Publisher logic

            if(type == 1) {
                //System.out.println("Type message to send:");
                try {


                    System.out.println("Enter text to share: \n");
                    String text = new BufferedReader(new InputStreamReader(System.in)).readLine();

                    System.out.println("Enter HashTag ...  type end to Stop");
                    ArrayList<String> hashTags = new ArrayList();
                    BufferedReader br  = new BufferedReader(new InputStreamReader(System.in));
                    String hashtag;
                    //!hashtag.readLine().equalsIgnoreCase("end")
                    while(!(hashtag = br.readLine()).equals("end")){

                        hashTags.add(hashtag);
                        System.out.println(hashtag + " added to hashtags\n");
                    }
                    pub.sendText(text,hashTags);

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
            if(type == 3){
                pub.getBrokerList();

            }
            System.out.println(" Select user type , 0 to exit , 1 for pub , 2 for consumer ");
            type = new Scanner(System.in).nextInt();
        }
        System.out.println("APP NODE EXITING");


    }
}
