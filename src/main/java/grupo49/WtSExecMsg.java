package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

//Worker to server exec msg
public class WtSExecMsg implements WtSMsg {
    private static final byte opcode = 1; // value to distinguish message server side
    private int requestN; // request number (in clients pov) 
    private int memUsed; // memUsed in process // could be stored in server, easier this way
    private byte[] data; // error info

    public WtSExecMsg() {
        // n faz nada, preencher com setters
    }

    public WtSExecMsg(int requestN, int mem, byte[] data) {
        this.requestN = requestN;
        this.memUsed = mem;
        this.data = data;
    }

    public WtSExecMsg(WtSExecMsg msg) {
        this.requestN = msg.requestN;
        this.memUsed = msg.memUsed;
        this.data = msg.data.clone();
    }

    //serialize sends msgType before data, for server msg distinction!!
    public void serialize(DataOutputStream dos) throws IOException{
        dos.writeByte(opcode);

        dos.writeInt(this.requestN);
        dos.writeInt(this.memUsed);
        dos.writeInt(this.data.length);
        dos.write(this.data);
        dos.flush();
    }

    public void serializeWithoutFlush(DataOutputStream dos) throws IOException{
        dos.writeByte(opcode);

        dos.writeInt(this.requestN);
        dos.writeInt(this.memUsed);
        dos.writeInt(this.data.length);
        dos.write(this.data);
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException{
        this.setRequestN(dis.readInt());
        this.setMemUsed(dis.readInt());
        int arrSize = dis.readInt();
        byte[] data = new byte[arrSize];
        dis.readFully(data);
        this.setData(data);
    }

    public int getRequestN() {
        return this.requestN;
    }

    public int getMemUsed() {
        return this.memUsed;
    }
    
    public byte[] getData() {
        return this.data;
    }
    
    private void setRequestN(int requestN) {
        this.requestN = requestN;
    }

    private void setMemUsed(int mem) {
        this.memUsed = mem;
    }

    private void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("reqN: " + this.requestN);
        sb.append(" mem: " + this.memUsed);
        String byteData = "";
        try {
            byteData = new String(this.data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sb.append(" data in stringFormat: " + byteData);
        return sb.toString();
    }

    @Override
    public WtSMsg clone() {
        return new WtSExecMsg(this);
    }
}

