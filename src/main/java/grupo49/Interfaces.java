package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

//generic inteface for all message types
interface IMessage extends Cloneable {
    public void serialize(DataOutputStream dos) throws IOException;
    public void serializeWithoutFlush(DataOutputStream dos) throws IOException;
    public void deserialize(DataInputStream dis) throws IOException;
    public int getRequestN();
    public IMessage clone();
}

interface CtSMsg extends IMessage {
    public void setRequestN(int reqN);
    @Override
    CtSMsg clone();
}

interface StCMsg extends IMessage {
    public byte[] getResultInBytes();
    @Override
    StCMsg clone();
};

interface WtSMsg extends IMessage {
    @Override
    WtSMsg clone();
};

interface StWMsg extends IMessage {
    @Override
    StWMsg clone();
}; // na prática só existe StWExecMsg, mas permite expansão no futuro
