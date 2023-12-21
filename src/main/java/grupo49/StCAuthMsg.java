package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// possivelmente elimiar esta classe e mandar só a string de erro no byte[] do StCExecMsg

//Server to client error occured msg
// this message type is also used from worker to server, since message is the same

public class StCAuthMsg implements StCMsg {
    private static final byte opcode = 3; // value to distinguish message server side
    private boolean success; // if authentication was or not successful
    private String info; // error info

    public StCAuthMsg() {};

    public StCAuthMsg(boolean success, String info) {
        this.success = success;
        this.info = info;
    }

    public StCAuthMsg(StCAuthMsg msg) {
        this.success = msg.success;
        this.info = msg.info;
    }

    //serialize sends msgType before data, for server msg distinction!!
    public void serialize(DataOutputStream dos) throws IOException{
        dos.writeByte(opcode);

        dos.writeBoolean(this.success);
        dos.writeUTF(this.info);
        dos.flush();
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException{
        this.setSuccess(dis.readBoolean());
        this.setInfo(dis.readUTF());
    }

    public boolean getSuccess() {
        return this.success;
    }

    public String getInfo() {
        return this.info;
    }

    public int getRequestN() {
        return -1;
    }

    public byte[] getResultInBytes() {
        return null;
    }

    private void setSuccess(boolean success) {
        this.success = success;
    }
    
    private void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("successfull: " + this.success);
        sb.append(" info: +" + this.info);
        return sb.toString();
    }

    @Override
    public StCMsg clone() {
        return new StCAuthMsg(this);
    }
}