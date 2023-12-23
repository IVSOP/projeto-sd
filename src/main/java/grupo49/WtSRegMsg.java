package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

//Worker to server exec msg
public class WtSRegMsg implements WtSMsg {
    private static final byte opcode = 0; // só ha uma msg, por isso não será usado opcode
    private int memAvailable;

    public WtSRegMsg() {};
    
    public WtSRegMsg(int memAvailable) {
        this.memAvailable = memAvailable;
    }

    public WtSRegMsg(WtSRegMsg msg) {
        this.memAvailable = msg.memAvailable;
    }

    //serialize sends msgType before data, for server msg distinction!!
    public void serialize(DataOutputStream dos) throws IOException{
        // só há uma mensagem de autenticação WtoS por isso n serializo com opcode
        dos.writeInt(this.memAvailable);
        dos.flush();
    }

    public void serializeWithoutFlush(DataOutputStream dos) throws IOException{
        // só há uma mensagem de autenticação WtoS por isso n serializo com opcode
        dos.writeInt(this.memAvailable);
        dos.flush();
    }
    
    //deserialize assumes opcode was previously read, only uses information after opcode
    public void deserialize(DataInputStream dis) throws IOException{
        this.memAvailable =  dis.readInt();
    }

    public int getMemAvail() {
        return this.memAvailable;
    }

    public int getRequestN() {
        return -1;
    }

    @Override
    public WtSMsg clone() {
        return new WtSRegMsg(this);
    }
}