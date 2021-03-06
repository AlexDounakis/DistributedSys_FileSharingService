import com.uwyn.jhighlight.fastutil.Hash;
import net.didion.jwnl.data.Exc;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//public class Publisher extends AppNode extends Thread implements IPublisher implements Runnable {
public class Publisher extends Thread implements IPublisher, Runnable {

    private Socket socket;
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
    public void run(){ }

    public void push(File file, ArrayList<String> topics, ObjectOutputStream outputStream) throws TikaException, IOException, SAXException {

        String path = file.getAbsolutePath();
        ArrayList<MultimediaFile> chunks = generateChunks(file);
        for (MultimediaFile chunk : chunks) {
            chunk.setHashtags(topics);
        }

        for (MultimediaFile chunk : chunks) {
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

                socket = new Socket(brokers.get(0).getIp(), brokers.get(0).getPort());
                ObjectOutputStream service_out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream service_in = new ObjectInputStream(socket.getInputStream());

                MultimediaFile file =new MultimediaFile(this.channelName,text);
                file.setHashtags(hashTags);

                service_out.writeObject(new Value( file,this.addr));
                System.out.println("Pub .flush()");
                service_out.flush();

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

                service_out.writeObject(new Value(this.addr));
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

                service_out.writeObject(new Value(this.addr,"get brokers"));
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
    public Broker hashTopic(String s) { return new Broker("some_ip", 123); }
    @Override
    public void notifyBrokersNewMessage(String s) {}
    @Override
    public void notifyFailure(Broker broker) {}
    @Override
    public void push(String s, Value v) {}
}
