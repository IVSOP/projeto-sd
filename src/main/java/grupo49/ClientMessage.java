package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// basically a pair<clientId,IMessage>, but looks cleaner this way
// Used when passing a specific client msg to global server buffer or worker buffers

//"T extends IMessage" improves type-safety and doesn´t need explicit casting

public class ClientMessage<T extends IMessage> {
    private int clientId; // client identifier (assigned by server)
    private T message; // client message: CtSMsg, StCMsg, WtSMsg

    ClientMessage () {
        //fill with setters
    }
    
    ClientMessage(int id, T msg) {
        this.clientId = id;
        this.message = msg;
    }

    public int getClient() {
        return this.clientId;
    }

    public T getMessage() {
        return this.message;
    }

    public void setClient(int info) {
        this.clientId = info;
    }

    public void setMessage(T msg) {
        this.message = msg;
    }
    
    public void serialize(DataOutputStream dos) throws IOException{
        this.message.serialize(dos);
        dos.writeInt(clientId);
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException {
        this.message.deserialize(dis);
        this.setClient(dis.readInt());
    }


}