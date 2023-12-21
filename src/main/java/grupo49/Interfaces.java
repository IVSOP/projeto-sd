package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

//generic inteface for all message types
interface IMessage extends Cloneable {
    public void serialize(DataOutputStream dos) throws IOException;
    public void deserialize(DataInputStream dis) throws IOException;
    public int getRequestN();
    public IMessage clone();
}

interface CtSMsg extends IMessage {
    public void setRequestN(int reqN);
    CtSMsg clone();
}

interface StCMsg extends IMessage {
    public byte[] getResultInBytes();
    StCMsg clone();
};

interface WtSMsg extends IMessage {
    WtSMsg clone();
};

interface StWMsg extends IMessage {
    StWMsg clone();
}; // na prática só existe StWExecMsg, mas permite expansão no futuro
