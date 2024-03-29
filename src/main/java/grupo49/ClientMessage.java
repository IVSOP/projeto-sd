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

    //to be filled in deserialize
    ClientMessage () {
    }
    
    ClientMessage(T msg) {
        this.message = msg;
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
        this.message.serializeWithoutFlush(dos);
        dos.writeInt(clientId);
		dos.flush();
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException {
        this.message.deserialize(dis);
        this.setClient(dis.readInt());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("clientID: " + this.clientId);
       sb.append(this.message.toString());
        return sb.toString();
    }

    @Override
    public ClientMessage<T> clone() {
        return new ClientMessage(this.clientId, this.message.clone());
    } 
}