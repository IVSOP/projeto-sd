package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


//Client to server execution request message
public class CtSExecMsg implements CtSMsg {
    private static final byte opcode = 2; // value to distinguish message server side
    private int requestN; // request number (in clients pov)
    private int mem; // necessary memory in bytes
    private byte[] data; // request data

    public CtSExecMsg() {
        // n faz nada, preencher com setters
    }

    public CtSExecMsg(int mem, byte[] bArray) {
        //this.requestN = requestN;
        this.mem = mem;
        this.data = bArray; //bArray.clone()?
    }

    public CtSExecMsg(CtSExecMsg msg) {
        this.requestN = msg.requestN;
        this.mem = msg.mem;
        this.data = msg.data.clone();
    }

    //serialize sends msgType before data, for server msg distinction!!
    public void serialize(DataOutputStream dos) throws IOException{
        dos.writeByte(opcode);

        dos.writeInt(this.requestN);
        dos.writeInt(this.mem);
        dos.writeInt(this.data.length);
        dos.write(this.data);
        dos.flush();
    }

    public void serializeWithoutFlush(DataOutputStream dos) throws IOException{
        dos.writeByte(opcode);
        dos.writeInt(this.requestN);
        dos.writeInt(this.mem);
        dos.writeInt(this.data.length);
        dos.write(this.data);
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException {
        this.setRequestN(dis.readInt());
        this.setMem(dis.readInt());
        int arrSize = dis.readInt();
        byte[] data = new byte[arrSize];
        dis.readFully(data,0,arrSize);
        this.setData(data);
    }

    public byte[] getData() {
        return this.data;
    }

    public int getMem() {
        return this.mem;
    }

    public int getRequestN() {
        return this.requestN;
    }

    private void setData(byte[] data) {
        this.data = data;
    }

    private void setMem(int mem) {
        this.mem = mem;
    }

    public void setRequestN(int n) {
        this.requestN = n;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" reqN: " + this.requestN);
        sb.append(" mem: " + this.mem);
        String byteData = "data..";
        try {
        byteData = new String(this.data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sb.append(" data in stringFormat: " + byteData);
        return sb.toString();
    }

    @Override
    public CtSMsg clone() {
        return new CtSExecMsg(this);
    }
}