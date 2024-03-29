package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

////Client to server status request message
public class CtSStatusMsg implements CtSMsg {
    private static final byte opcode = 3; // value to distinguish message server side
    private int requestN; // request number (in clients pov)

    public CtSStatusMsg() {
        // n faz nada, preencher com setters
    }

    public CtSStatusMsg(int requestN) {
        this.requestN = requestN;
    }

    public CtSStatusMsg(CtSStatusMsg msg) {
        this.requestN = msg.requestN;
    }

    //serialize sends msgType before data, for server msg distinction!!
    public void serialize(DataOutputStream dos) throws IOException{
        dos.writeByte(opcode);

        dos.writeInt(this.requestN);
        dos.flush();
    }

    public void serializeWithoutFlush(DataOutputStream dos) throws IOException{
        dos.writeByte(opcode);

        dos.writeInt(this.requestN);
    }

    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException {
        this.setRequestN(dis.readInt());
    }

    public int getRequestN() {
        return this.requestN;
    }

    public void setRequestN(int reqN) {
        this.requestN = reqN;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" reqN: " + this.requestN);
        return sb.toString();
    }

    @Override
    public CtSMsg clone() {
        return new CtSStatusMsg(this);
    }
}