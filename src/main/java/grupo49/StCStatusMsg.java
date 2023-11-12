package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


//Client to server status service occupation message
public class StCStatusMsg {
    private static final byte msgType = 2; // value to distinguish message server side
    private int mem; // availabe memory
    private int pending; // number of pending tasks
    private int requestN; // request number (in clients pov) 

    public StCStatusMsg() {
        // n faz nada, preencher com setters
    }

    public StCStatusMsg(int mem, int pending, int requestN) {
        this.mem = mem;
        this.pending = pending;
        this.requestN = requestN;
    }

    public void serialize(DataOutputStream dos) throws IOException{
        dos.writeByte(msgType);

        dos.writeInt(this.mem);
        dos.writeInt(this.pending);
        dos.writeInt(this.requestN);
        dos.flush();
    }

    public void deserialize(DataInputStream dis) throws IOException{
        this.setMem(dis.readInt());
        this.setPending(dis.readInt());
        this.setRequestN(dis.readInt());
    }

    private int getMem() {
        return this.mem;
    }

    private int getPending() {
        return this.pending;
    }

    private int getRequestN() {
        return this.requestN;
    }

    private void setMem(int mem) {
        this.mem = mem;
    }

    private void setPending(int pending) {
        this.pending = pending;
    }

    private void setRequestN(int requestN) {
        this.requestN = requestN;
    }
}