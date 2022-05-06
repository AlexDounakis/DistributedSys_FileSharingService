import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;
import java.io.*;
import java.io.File;
import java.net.Socket;
import java.util.*;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//public class Publisher extends AppNode extends Thread implements IPublisher implements Runnable {
public class Publisher {

    private Socket socket;
    public Address addr;
    public String channelName;
    private HashMap<String,ArrayList<String>> FileCollection = new HashMap<>();
    protected ArrayList<Address> brokers = new ArrayList<>(Arrays.asList(
            /// first random broker IP and Port
            new Address("192.168.56.1", 6000)
    ));

    /// CONSTRUCTORS
    public Publisher(){}

    public Publisher(Address _addr , String _channelName){
        this.addr = _addr;
        System.out.println(this.addr);
        this.channelName = _channelName;
    }

    public void setFileCollection(String text , ArrayList<String> topics){
        this.FileCollection.put(text , topics);
    }
    public HashMap<String,ArrayList<String>> getFileCollection(){
        return this.FileCollection;
    }

    public static void main (String args[]){
//
//        Publisher pub = new Publisher();
//        String path = "C:\\Users\\alex\\source\\repos\\distributed_sys_streamer\\data\\sample3.mp4";
//
//        HashMap<String,String> my_video = pub.getMetadata(path);
//
////        MultimediaFile video_bytes = new MultimediaFile(IOUtils.toByteArray(new FileInputStream(path))  ,
////                "ChannelName_test",
////                my_video.get("Creation-Date"));
//
//
//        System.out.println(video_bytes.getVideoFileChunk());
//        System.out.println(video_bytes.DateCreated);
    }

    public HashMap<String, String> getMetadata(String file){
        HashMap<String, String> data = new HashMap<>();

        try  {
            FileInputStream f = new FileInputStream(new File(file));
            File _file = new File(file);
            long lengthInKb = (_file.length()/1024);
            var _name = _file.getAbsolutePath().substring(57);
            System.out.println(_name +"\n"+  Long.toString(lengthInKb));
            data.put("LengthInKb" , Long.toString(lengthInKb));
            data.put("name" , _name);
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

    public ArrayList<byte[]> generateChunks(File file) throws TikaException, IOException, SAXException {
        ArrayList<byte[]> chunks = new ArrayList<>();
        byte[] videoFileChunk = new byte[1024 * 1024/2];// 512KB chunk
//        var metaMap = getMetadata(file.getAbsolutePath());

        try (FileInputStream fileInputStream = new FileInputStream(new File(file.getAbsolutePath()))) {
            while (fileInputStream.read(videoFileChunk, 0, videoFileChunk.length) > 0) {
                chunks.add(videoFileChunk);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //MARK LAST CHUNK AS LAST TO HELP BROKER WITH ORDERING
//        chunks.get(chunks.size() - 1).setIsLast(true);
//        chunks.get(0).IsFirst = true;
        //chunks.get(0).Count = sumOfFiles;

        return chunks;
    }

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

    public Address hashTopic(String topic) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(topic.getBytes(), 0, topic.length());
        String md5 = new BigInteger(1, digest.digest()).toString(16);
        BigInteger decimal = new BigInteger(md5, 16);
        BigInteger result = decimal.mod(BigInteger.valueOf(3));
        int mod = result.intValue();
        switch (mod) {
            case 0 -> {
                System.out.println("Broker 1 will handle topic:" + topic);
                System.out.println(Broker.getBrokerList().keySet().toArray()[0]);
                return (Address)Broker.getBrokerList().keySet().toArray()[0];
//                return (Address) Broker.getBrokerList().keySet().toArray()[0];
            }
            case 1 -> {
                System.out.println("Broker 2 will handle topic:" + topic);
                System.out.println(Broker.getBrokerList().keySet().toArray()[1]);
                return (Address) Broker.getBrokerList().keySet().toArray()[1];
            }
            case 2 -> {
                System.out.println("Broker 3 will handle topic:" + topic);
                System.out.println(Broker.getBrokerList().keySet().toArray()[2]);
                return (Address) Broker.getBrokerList().keySet().toArray()[2];
            }
//            AppNode.brokersList.forEach((k,v)
//                    -> System.out.println( "Address:"+ k + "something"+ v));
        }


        return null;
    }

    public void sendFile(String text,ArrayList<String> hashtags , Date dateCreated) {
        Runnable task = () -> {
            try {
                hashtags.forEach(hashtag
                        -> {
                    try {
                        System.out.println("Thread sending file started...");
                        notifyBroker(text,hashtag,dateCreated);
                        System.out.println("Thread sending text ended ....");

                    } catch (Exception e) {
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
    public void notifyBroker(String text,String hashtag, Date dateCreated){
        try{
            Address address = hashTopic(hashtag);
            System.out.println("Notifying Broker: " + address  + " for:  "+ hashtag);

            Socket socketBroker = new Socket(address.getIp(), address.getPort());
            ObjectOutputStream serv_out = new ObjectOutputStream(socketBroker.getOutputStream());

            ArrayList<String> listOfHashtag = new ArrayList<>();
            listOfHashtag.add(hashtag);
            serv_out.writeObject(new Value(this.addr,hashtag , "something",SenderType.PUBLISHER));
            serv_out.flush();

            /// send video to broker ///
            ArrayList<byte[]> chunks = new ArrayList<>();
            var metaMap = new HashMap<String,String>();

            if(text.endsWith(".mp4") || text.endsWith(".jpg")) {
                System.out.println("GenerateChunks for video or photo");

                int flag = 0;
                File file = null;
                String fileName = null;
                File directory = new File("data");
                String[] fileList = directory.list();
                if (fileList == null) {
                    System.out.println("Empty directory");
                }
                else {
                    for (int i = 0; i < fileList.length; i++) {
                        fileName = fileList[i];
                        if (fileName.equalsIgnoreCase(text)) { //file found
                            file = new File(fileName);
                            flag = 1;
                        }
                    }
                }
                if (flag == 0) { //file not found
                    System.out.println(text + " not found");
                }
                chunks = generateChunks(file);
            }
            else {
                System.out.println("GenerateChunks for text");
                try {

                    String home = System.getProperty("user.home");
                    String fileName = text.substring(0,50);
                    File file = new File("data/" + fileName + ".txt");
                    file.createNewFile();
                    FileWriter myWriter = new FileWriter(file);
                    myWriter.write(text);
                    myWriter.close();
                    metaMap = getMetadata(file.getAbsolutePath());
                    chunks = generateChunks(file);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            /// we have to set IsFirst / IsLast in Value obj

            for(int i=0;i<chunks.size();i++){
                push(i, chunks ,dateCreated,serv_out);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }

    }
    public void push(int i , ArrayList<byte[]> chunks, Date dateCreated, ObjectOutputStream serv_out) throws IOException {

        if(i==chunks.size()-1){
            Value valueToSend = new Value(new MultimediaFile(chunks.get(i), dateCreated), this.addr ,SenderType.PUBLISHER);
            valueToSend.isLast = true;
            serv_out.writeObject(valueToSend);

        }else {
            serv_out.writeObject(new Value(new MultimediaFile(chunks.get(i), dateCreated), this.addr, SenderType.PUBLISHER));
        }
        serv_out.flush();
    }
}
