package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


//Worker to server status service occupation message
public class WtSErrorMsg implements WtSMsg {
    private static final byte opcode = 2; // value to distinguish message server side
    private int requestN; // request number (in clients pov) 
    private int memUsed; // memUsed in process // could be stored in server, easier this way
    private String error; // error info

    public WtSErrorMsg() {
        // n faz nada, preencher com setters
    }

    public WtSErrorMsg(int requestN, int memUsed, String error) {
        this.requestN = requestN;
        this.memUsed = memUsed;
        this.error = error;
    }

    public WtSErrorMsg(WtSErrorMsg msg) {
        this.requestN = msg.requestN;
        this.memUsed = msg.memUsed;
        this.error = msg.error;
    }

    //serialize sends msgType before data, for server msg distinction!!
    public void serialize(DataOutputStream dos) throws IOException{
        dos.writeByte(opcode);

        dos.writeInt(this.requestN);
        dos.writeInt(this.memUsed);
        dos.writeUTF(this.error);
        dos.flush();
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException{
        this.setRequestN(dis.readInt());
        this.setMemUsed(dis.readInt());
        this.setError(dis.readUTF());
    }


    public int getRequestN() {
        return this.requestN;
    }

    public int getMemUsed() {
        return this.memUsed;
    }
    
    public String getError() {
        return this.error;
    }

    private void setRequestN(int requestN) {
        this.requestN = requestN;
    }
    
    private void setError(String error) {
        this.error = error;
    }

    private void setMemUsed(int mem) {
        this.memUsed = mem;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("reqN: " + this.requestN);
        sb.append(" mem: " + this.memUsed);
        sb.append(" error: " + this.error);
        return sb.toString();
    }

    @Override
    public WtSMsg clone() {
        return new WtSErrorMsg(this);
    }
}