
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

public class AppNode {

    protected static String Ip;
    protected static int port;
    private static int type;

    // localhost addr
    static InetAddress inetAddress;
    protected static Address address = null;

    private int action ;

    /// USERS BROKER LIST
    public static HashMap<Address , ArrayList<String>> brokersList;

    public static void main (String args[]) throws IOException {


        // get localhost IP and port num;
        try{
            System.out.println("port num:");
            port = new Scanner(System.in).nextInt();
            System.out.println("Your port num is:  "+ port);
            inetAddress = InetAddress.getLocalHost();
            Ip = inetAddress.getHostAddress();
            System.out.println("Your Ip is:   "+Ip);
            address = new Address(Ip,port);

        }catch(Exception e){
            e.getStackTrace();
        }

        System.out.println("Enter Publisher Channel Name:  ");
        String channelName =  new BufferedReader(new InputStreamReader(System.in)).readLine();
        Publisher pub = new Publisher(address, channelName);
        Consumer con = new Consumer(address);
        //Publisher pub = new Publisher(address, channelName);

        System.out.print( "Welcome , select user type , 0 to exit , 1 for pub , 2 for consumer  , 3 for Updating Broker Info" );
        int type= new Scanner(System.in).nextInt();
        while( type != 0){
            // Publisher logic
            if(type == 1) {
                try {
                    //switch case with user input to determine multimedia file , text photo or video
                    System.out.println("Enter text to share: \n");
                    String text = new BufferedReader(new InputStreamReader(System.in)).readLine();

                    System.out.println("Enter HashTag ...  type end to Stop");
                    ArrayList<String> hashTags = new ArrayList();
                    BufferedReader br  = new BufferedReader(new InputStreamReader(System.in));
                    String hashtag;

                    while(!(hashtag = br.readLine()).equals("end")){

                        hashTags.add(hashtag);
                        System.out.println(hashtag + " added to hashtags\n");
                    }
                    /// USER INPUT FOR DATE CREATED
                    //
                    Date dateCreated = new Date();
                    /// video case --> text should be the path location selected after the switch case
                    pub.setFileCollection(text,hashTags);
                    System.out.println("FileCollection:\n");
                    System.out.println(pub.getFileCollection());

                    pub.sendFile(text,hashTags,dateCreated);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Consumer Logic
            if(type == 2) {
                try {

                    System.out.println("Type 2 to register / Type 1 to view conversation data ....\n");
                    int a = new Scanner(System.in).nextInt();
                    BufferedReader br  = new BufferedReader(new InputStreamReader(System.in));
                    String topic;

                    switch (a){
                        case 1:
                            System.out.println("Enter topic to show history: \n");
                            topic = br.readLine();
                            con.showConversationData(topic);
                            break;
                        case 2:
                            System.out.println("Enter topics of interest: \n");
                            while (!(topic = br.readLine()).equals("end")) {
                                con.register(topic);
                                System.out.println("Registered to: "+topic );
                                System.out.println("Type end to Stop");
                            }
                            break;
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(type == 3){
                pub.getBrokerList();

            }
            System.out.println(" select user type , 0 to exit , 1 for pub , 2 for consumer  , 3 for Updating Broker Info");
            type = new Scanner(System.in).nextInt();
        }

        System.out.println("APP NODE EXITING");


    }
}
