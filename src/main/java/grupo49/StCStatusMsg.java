package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


//Client to server status service occupation message
public class StCStatusMsg implements StCMsg {
    private static final byte opcode = 2; // value to distinguish message server side
    private int requestN; // request number (in clients pov) 
    private int mem; // availabe memory
    private int pending; // number of pending tasks

    public StCStatusMsg() {
        // n faz nada, preencher com setters
    }

    public StCStatusMsg(int requestN, int mem, int pending) {
        this.requestN = requestN;
        this.mem = mem;
        this.pending = pending;
    }

    //serialize sends msgType before data, for server msg distinction!!
    public void serialize(DataOutputStream dos) throws IOException{
        dos.writeByte(opcode);
        
        dos.writeInt(this.requestN);
        dos.writeInt(this.mem);
        dos.writeInt(this.pending);
        dos.flush();
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException{
        this.setRequestN(dis.readInt());
        this.setMem(dis.readInt());
        this.setPending(dis.readInt());
    }

    public int getRequestN() {
        return this.requestN;
    }

    public int getMem() {
        return this.mem;
    }

    public int getPending() {
        return this.pending;
    }


    private void setRequestN(int requestN) {
        this.requestN = requestN;
    }

    private void setMem(int mem) {
        this.mem = mem;
    }

    private void setPending(int pending) {
        this.pending = pending;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("reqN: " + this.requestN);
        sb.append("mem: " + this.mem);
        sb.append("pending: " + this.pending);
        return sb.toString();
    }
}