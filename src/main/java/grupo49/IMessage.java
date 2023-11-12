package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

//generic inteface for all message types
public interface IMessage {
    public void serialize(DataOutputStream dos) throws IOException;
    public void deserialize(DataInputStream dis) throws IOException;
}