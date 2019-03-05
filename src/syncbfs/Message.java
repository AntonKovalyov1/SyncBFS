package syncbfs;

public class Message {
    
    public static final int HELLO_TYPE = 0;
    public static final int ACK_TYPE = 1;
    public static final int NACK_TYPE = 2;
    
    public final int type;
    public final int senderID;
    
    public Message(final int type, final int senderID) {
        this.type = type;
        this.senderID = senderID;
    }
    
    public boolean isHelloMessage() {
        return type == HELLO_TYPE;
    }
    
    public boolean isACKMessage() {
        return type == ACK_TYPE;
    }
    
    public boolean isNACKMessage() {
        return type == NACK_TYPE;
    }
}
