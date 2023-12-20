package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

//generic inteface for all message types
 interface IMessage {
    public void serialize(DataOutputStream dos) throws IOException;
    public void deserialize(DataInputStream dis) throws IOException;
    public int getRequestN();
}

interface CtSMsg extends IMessage {
    public void setRequestN(int reqN);
}

interface StCMsg extends IMessage {
};

interface WtSMsg extends IMessage {
};

interface StWMsg extends IMessage {
}; // na prática só existe StWExecMsg, mas permite expansão no futuro
