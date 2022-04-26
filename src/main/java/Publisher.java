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

//public class Publisher extends AppNode extends Thread implements IPublisher implements Runnable {
public class Publisher extends Thread implements IPublisher, Runnable {

    protected Socket socket;
    public Address addr;
    public String channelName;
    public String text;
    protected Request reply_request;

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
    public void sendText(String _text){
        this.text = _text;
        //Request req = new Request(addr , text);
        // Thread .run() - thread functionality
        Runnable task = () -> {
            try {


                socket = new Socket(brokers.get(0).getIp(), brokers.get(0).getPort());




                ObjectOutputStream service_out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream service_in = new ObjectInputStream(socket.getInputStream());

                System.out.println("thread started ...");
                Request req_response = new Request(addr,_text);
                service_out.writeObject(req_response);
                System.out.println("Pub .flush()");
                service_out.flush();


                Request text= (Request)service_in.readObject();
                System.out.println(text.text);


            } catch (Exception e) {
                e.getStackTrace();
            } finally {
                try {
                    // close socket connection
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();


    }

    public HashMap<String, String> getMetadata(String file) throws TikaException, SAXException, IOException {
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
    public void init(int x){}
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
    public void updateNodes(){}
    @Override
    public void getBrokerList() {}
    @Override
    public Broker hashTopic(String s) { return new Broker("some_ip", 123); }
    @Override
    public void notifyBrokersNewMessage(String s) {}
    @Override
    public void notifyFailure(Broker broker) {}
    @Override
    public void push(String s, Value v) {}
}
