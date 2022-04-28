import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;

//public class Publisher extends AppNode extends Thread implements IPublisher implements Runnable {
public class Publisher extends Thread implements IPublisher, Runnable {

    private Socket socket;
    private ServerSocket serverSocket;
    public Address addr;
    public String channelName;
    public String text;
    private Value value;

    //ProfileName profileName;


    protected ArrayList<Address> brokers = new ArrayList<>(Arrays.asList(
            /// first random broker IP and Port
            new Address("192.168.56.1", 6000)
    ));

    public Publisher(){}

    public Publisher(Address _addr , String _channelName){
        this.addr = _addr;
        this.channelName = _channelName;
    }

    public static void main (String args[]) throws TikaException, IOException, SAXException {

        Publisher pub = new Publisher();
        String path = "C:\\Users\\alex\\source\\repos\\distributed_sys_streamer\\data\\sample3.mp4";

        HashMap<String,String> my_video = pub.getMetadata(path);
        MultimediaFile video_bytes = new MultimediaFile(IOUtils.toByteArray(new FileInputStream(path))  ,
                "ChannelName_test",
                my_video.get("Creation-Date"));


        System.out.println(video_bytes.getVideoFileChunk());
        System.out.println(video_bytes.DateCreated);
    }

    // Create Server Socket of Publisher
    @Override
    public void run(){
        try{
            System.out.println("Publisher ready to push ...\n");
            while(true){

                socket = serverSocket.accept();
                System.out.println("socket.accept()\n");

                Runnable task = () -> {
                    try {
                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                        var requestedTopics = (ArrayList<String>) in.readObject();
                        for (String topic : requestedTopics) {
                            for (String filePath : FileCollection.keySet())
                                if (FileCollection.get(filePath).contains(topic)) {
                                    Value file = new Value(filePath);
                                    push(file, FileCollection.get(filePath),out);
                                }
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

    public void push(Value file, ArrayList<String> topics, ObjectOutputStream outputStream) throws TikaException, IOException, SAXException {

        String path = file.getPath();
        ArrayList<Value> chunks = generateChunks(file);
        /*for (Value chunk : chunks) {
            chunk.setHashtags(topics);
        }*/

        for (Value chunk : chunks) {
            outputStream.writeObject(chunk);
        }
    }

    /// Extra Function
    // sendText summons a thread to deal with passing through a message - reading the response
    public void sendText(String _text , ArrayList<String> hashTags){
        this.text = _text;
        // Thread .run() - thread functionality
        Runnable task = () -> {
            try {

                System.out.println("thread started ...");
                //hashing
                hashTags.forEach(s
                        -> {
                    try {
                        Address address = hashTopic(s);
                        //AppNode.brokersList.put(address, ArrayList.add(s));
                        Broker.getBrokerList().get(address).add(s);
                        socket = new Socket(address.getIp(), address.getPort());
                        ObjectOutputStream service_out = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream service_in = new ObjectInputStream(socket.getInputStream());

                        MultimediaFile file =new MultimediaFile(this.channelName,text);
                        file.setHashtags(hashTags);

                        service_out.writeObject(new Value( file,this.addr, SenderType.PUBLISHER));
                        System.out.println("Pub .flush()");
                        service_out.flush();


                    } catch (NoSuchAlgorithmException | IOException e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.getStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();


    }

    public HashMap<String, String> getMetadata(String file){
        HashMap<String, String> data = new HashMap<>();

        try (FileInputStream f = new FileInputStream(new File(file))) {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            ParseContext pcontext = new ParseContext();
            MP4Parser MP4Parser = new MP4Parser();
            MP4Parser.parse(f, handler, metadata, pcontext);
            String[] metadataNames = metadata.names();

            for (String name : metadataNames) {
                //System.out.println(name);
                switch (name) {
                    case "Creation-Date":
                        data.put("Creation-Date", metadata.get(name));
                        break;
                    case "xmpDM:duration":
                        data.put("xmpDM:duration", metadata.get(name));
                        break;
                    case "tiff:ImageWidth":
                        data.put("tiff:ImageWidth", metadata.get(name));
                        break;
                    case "tiff:ImageLength":
                        data.put("tiff:ImageLength", metadata.get(name));
                        break;
                }
            }
        }
        catch (Exception e ) {
            System.out.println("Exception in Publisher" + e.getStackTrace());
        }
        //System.out.println(data);
        return data;
    }

    // Override Functions Implementation
    @Override
    public void init(int x){
        Runnable task = () ->{
            try {
                System.out.println("\n Thread for init running...\n");
                socket = new Socket(brokers.get(0).getIp(), brokers.get(0).getPort());

                ObjectOutputStream service_out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream service_in = new ObjectInputStream(socket.getInputStream());

                service_out.writeObject(new Value(this.addr,SenderType.PUBLISHER));
                service_out.flush();

                AppNode.brokersList = (HashMap) service_in.readObject();
                AppNode.brokersList.forEach((k,v)
                        -> System.out.println("Address: " + k + "   Topics:" +  v)
                );
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
    public void connect(){}
    @Override
    public void disconnect(){}
    @Override
    public ArrayList<MultimediaFile> generateChunks(File file) throws TikaException, IOException, SAXException {
        ArrayList<MultimediaFile> chunks = new ArrayList<MultimediaFile>();
        byte[] videoFileChunk = new byte[1024 * 1024];// 1MB chunk
        var metaMap = getMetadata(file.getAbsolutePath());

        try (FileInputStream fileInputStream = new FileInputStream(new File(file.getAbsolutePath()))) {
            while (fileInputStream.read(videoFileChunk, 0, videoFileChunk.length) > 0) {
                chunks.add(new MultimediaFile( videoFileChunk ,"FileNameTest" ,this.channelName, metaMap.get("Creation-Date")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //MARK LAST CHUNK AS LAST TO HELP BROKER WITH ORDERING
        chunks.get(chunks.size() - 1).setIsLast(true);
        return chunks;
    }
    @Override
    public void updateNodes(Value value){}
    @Override
    public void getBrokerList() {
        Runnable task = () -> {
            try{
                System.out.println("Updating Brokers List...\n");
                socket = new Socket(brokers.get(0).getIp(), brokers.get(0).getPort());

                ObjectOutputStream service_out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream service_in = new ObjectInputStream(socket.getInputStream());

                service_out.writeObject(new Value(this.addr,"get brokers",SenderType.PUBLISHER));
                service_out.flush();
                AppNode.brokersList = (HashMap) service_in.readObject();
                //System.out.println("HashMap Read:\n");
                AppNode.brokersList.forEach((k,v)
                        -> System.out.println("Address: " + k + "   Topics:" +  v)
                );

                socket.close();
            }catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
            }
        };
        new Thread(task).start();
    }

    @Override
    public Address hashTopic(String topic) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(topic.getBytes(), 0, topic.length());
        String md5 = new BigInteger(1, digest.digest()).toString(16);
        BigInteger decimal = new BigInteger(md5, 16);
        BigInteger result = decimal.mod(BigInteger.valueOf(3));
        int mod = result.intValue();
        switch (mod) {
            case 0 -> {
                System.out.println("Broker 1 will handle this topic.");
                System.out.println(Broker.getBrokerList().keySet().toArray()[0]);
                return (Address) Broker.getBrokerList().keySet().toArray()[0];
            }
            case 1 -> {
                System.out.println("Broker 2 will handle this topic.");
                System.out.println(Broker.getBrokerList().keySet().toArray()[1]);
                return (Address) Broker.getBrokerList().keySet().toArray()[1];
            }
            case 2 -> {
                System.out.println("Broker 3 will handle this topic.");
                System.out.println(Broker.getBrokerList().keySet().toArray()[2]);
                return (Address) Broker.getBrokerList().keySet().toArray()[2];
            }
        }


        return null;
    }
    @Override
    public void notifyBrokersNewMessage(String s) {}
    @Override
    public void notifyFailure(Broker broker) {}
    @Override
    public void push(String s, Value v) {}
}
