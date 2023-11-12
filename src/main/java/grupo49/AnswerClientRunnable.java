package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
// import java.net.ServerSocket;
import java.net.Socket;
// import java.util.ArrayList;
// import java.util.List;


public class AnswerClientRunnable implements Runnable {

    private Socket socket;
    private BoundedBuffer<ClientMessage<IMessage>> input;

    public AnswerClientRunnable(Socket socket,BoundedBuffer<ClientMessage<IMessage>> input) {
        this.socket = socket;
        this.input = input;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            byte msgType;
            IMessage msg = null;
            msgType = in.readByte();
            switch (msgType) {
                case 0:
                    msg = new CtSAutMsg();
                    ((CtSAutMsg) msg).deserialize(in);
                    System.out.println(msg.toString()); // teste
                    break;

                case 1:
                    msg = new CtSExecMsg();
                    ((CtSExecMsg) msg).deserialize(in);
                    System.out.println(msg.toString()); // teste
                    break;
            }

            String clientInfo = null; //construir string identificadora
            ClientMessage<IMessage> clientMsg = new ClientMessage<IMessage>(clientInfo, msg);
            try {
                input.push(clientMsg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
