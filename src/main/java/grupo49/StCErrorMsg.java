package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// possivelmente elimiar esta classe e mandar só a string de erro no byte[] do StCExecMsg

//Client to server status service occupation message
public class StCErrorMsg implements StCMsg {
    private static final byte opcode = 2; // value to distinguish message server side
    private int requestN; // request number (in clients pov) 
    private String error; // error info

    public StCErrorMsg() {
        // n faz nada, preencher com setters
    }

    public StCErrorMsg(int requestN, String error) {
        this.requestN = requestN;
        this.error = error;
    }

    //serialize sends msgType before data, for server msg distinction!!
    public void serialize(DataOutputStream dos) throws IOException{
        dos.writeByte(opcode);

        dos.writeInt(this.requestN);
        byte[] data = this.error.getBytes("UTF-8");
        dos.writeInt(this.error.length());
        dos.write(data);
        dos.flush();
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException{
        this.setRequestN(dis.readInt());
        byte[] data = new byte[dis.readInt()];
        dis.readFully(data);
        this.setError(new String(data,"UTF-8"));
    }

    private int getRequestN() {
        return this.requestN;
    }

    private String getError() {
        return this.error;
    }

    private void setRequestN(int requestN) {
        this.requestN = requestN;
    }
    
    private void setError(String error) {
        this.error = error;
    }

}