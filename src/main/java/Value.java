import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Value implements Serializable {

    private MultimediaFile multimediaFile;
    private Address address;

    public Value (MultimediaFile m) {
        this.multimediaFile = m;
    }

    public Value (Address address) {
        this.address = address;
    }

    public Value (MultimediaFile m, Address address) {
        this.multimediaFile = m;
        this.address = address;
    }

    public MultimediaFile getMultimediaFile() {return multimediaFile;}

    public Address getAddress() {return address;}

    //@Override
    //public String toString() {return this.multimediaFile +" , "+this.address;}
}
