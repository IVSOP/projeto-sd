package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


//Worker to server status service occupation message
public class WtSErrorMsg implements WtSMsg {
    private static final byte opcode = 1; // value to distinguish message server side
    private int requestN; // request number (in clients pov) 
    private String error; // error info

    public WtSErrorMsg() {
        // n faz nada, preencher com setters
    }

    public WtSErrorMsg(int requestN, String error) {
        this.requestN = requestN;
        this.error = error;
    }

    //serialize sends msgType before data, for server msg distinction!!
    public void serialize(DataOutputStream dos) throws IOException{
        dos.writeByte(opcode);

        dos.writeInt(this.requestN);
        dos.writeUTF(this.error);
        dos.flush();
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException{
        this.setRequestN(dis.readInt());
        this.setError(dis.readUTF());
    }


    public int getRequestN() {
        return this.requestN;
    }

    public String getError() {
        return this.error;
    }

    public void setRequestN(int requestN) {
        this.requestN = requestN;
    }
    
    public void setError(String error) {
        this.error = error;
    }

}