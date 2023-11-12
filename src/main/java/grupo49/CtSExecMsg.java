package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


//Client to server execution request message
public class CtSExecMsg implements IMessage{
    private static final byte msgType = 1; // value to distinguish message server side
    private byte[] data; // request
    private int mem; // necessary memory in bytes
    private int requestN; // request number (in clients pov) 

    // o requestN já é o código de tarefa do enunciado ??????????

    public CtSExecMsg() {
        // n faz nada, preencher com setters
    }

    public CtSExecMsg(byte[] bArray, int mem, int requestN) {
        this.data = bArray; //bArray.clone()?
        this.mem = mem;
        this.requestN = requestN;
    }

    public void serialize(DataOutputStream dos) throws IOException{
        dos.writeByte(msgType);

        dos.writeInt(this.data.length);
        dos.write(this.data);
        dos.writeInt(this.mem);
        dos.writeInt(this.requestN);
        dos.flush();
    }

    public void deserialize(DataInputStream dis) throws IOException {
        byte[] data = new byte[dis.readInt()];
        dis.readFully(data);
        this.setData(data);
        this.setMem(dis.readInt());
        this.setRequestN(dis.readInt());
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

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }

    public void setRequestN(int n) {
        this.requestN = n;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String byteData = null;
        try {
        byteData = new String(this.data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sb.append(" data in stringFormat: " + byteData);
        sb.append(" mem: " + this.mem);
        sb.append(" reqN: " + this.requestN);
        return sb.toString();
    }


}