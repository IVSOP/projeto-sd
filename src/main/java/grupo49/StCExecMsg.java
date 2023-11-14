package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


//Server to client exec msg
//Also used on worker to server, since message is the same
public class StCExecMsg implements StCMsg {
    private static final byte opcode = 0; // value to distinguish message client side
    private int requestN; // request number (in clients pov) 
    private byte[] data; // output data

    public StCExecMsg() {
        // n faz nada, preencher com setters
    }

    public StCExecMsg(int requestN, byte[] data) {
        this.requestN = requestN;
        this.data = data;
    }

    //serialize sends msgType before data, for server msg distinction!!
    public void serialize(DataOutputStream dos) throws IOException{
        dos.writeByte(opcode);

        dos.writeInt(this.requestN);
        dos.writeInt(this.data.length);
        dos.write(this.data);
        dos.flush();
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException{
        this.setRequestN(dis.readInt());
        int arrSize = dis.readInt();
        byte[] data = new byte[arrSize];
        dis.readFully(data,0,arrSize);
        this.setData(data);
    }

    public int getRequestN() {
        return this.requestN;
    }

    public byte[] getData() {
        return this.data;
    }

    private void setRequestN(int requestN) {
        this.requestN = requestN;
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


