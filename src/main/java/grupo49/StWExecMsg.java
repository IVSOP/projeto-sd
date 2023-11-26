package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class StWExecMsg implements StWMsg {
    private static final byte opcode = 0; // só ha uma msg, por isso não será usado opcode
    private int requestN; // request number (in clients pov) 
    private int memUsed; // memUsed in process // could be stored in server, easier to just make worker return it on response
    private byte[] data; // error info

    public StWExecMsg() {
        // n faz nada, preencher com setters
    }

    public StWExecMsg(int requestN, int memUsed, byte[] data) {
        this.requestN = requestN;
        this.memUsed = memUsed;
        this.data = data;
    }

    public void serialize(DataOutputStream dos) throws IOException{
        //n escreve opcode 
        dos.writeInt(this.requestN);
        dos.writeInt(this.memUsed);
        dos.writeInt(this.data.length);
        dos.write(this.data);
        dos.flush();
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException{
        this.setRequestN(dis.readInt());
        this.setMemUsed(dis.readInt());
        int arrSize = dis.readInt();
        byte[] data = new byte[arrSize];
        dis.readFully(data,0,arrSize);
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
        sb.append(" reqN: " + this.requestN);
        String byteData = "data..";
        try {
            byteData = new String(this.data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sb.append(" data in stringFormat: " + byteData);
        return sb.toString();
    }

}

