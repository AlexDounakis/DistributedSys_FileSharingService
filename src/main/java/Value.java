import java.io.Serializable;

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

    public MultimediaFile getValue() {return multimediaFile;}

    public Address getAddress() {return address;}

    //@Override
    //public String toString() {return this.multimediaFile +" , "+this.address;}
}
