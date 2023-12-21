package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// possivelmente elimiar esta classe e mandar só a string de erro no byte[] do StCExecMsg

//Server to client error occured msg
// this message type is also used from worker to server, since message is the same

public class StCErrorMsg implements StCMsg {
    private static final byte opcode = 1; // value to distinguish message server side
    private int requestN; // request number (in clients pov) 
    private String error; // error info

    public StCErrorMsg() {
        // n faz nada, preencher com setters
    }

    public StCErrorMsg(int requestN, String error) {
        this.requestN = requestN;
        this.error = error;
    }

    public StCErrorMsg(StCErrorMsg msg) {
        this.requestN = msg.requestN;
        this.error = msg.error;
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

    public byte[] getResultInBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append("error: " + this.error);
        return sb.toString().getBytes();
    }

    private void setRequestN(int requestN) {
        this.requestN = requestN;
    }
    
    private void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("reqN: " + this.requestN);
        sb.append(" error: +" + this.error);
    return sb.toString();
    }

    @Override
    public StCMsg clone() {
        return new StCErrorMsg(this);
    }
}