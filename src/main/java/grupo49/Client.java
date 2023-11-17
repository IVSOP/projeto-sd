package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;


public class Client
{
	public static final int inputBufferSize = 10;
	public static final int outputBufferSize = 10;

	private int requestID = 0;

	BoundedBuffer<StCMsg> inputBuffer; // receives from server

	BoundedBuffer<CtSMsg> outputBuffer; // writes to server

	Socket socket;

	String name;
	String password;

	public Client(String serverAddress, String name, String password) {
		this.inputBuffer = new BoundedBuffer<StCMsg>(inputBufferSize);
		this.outputBuffer = new BoundedBuffer<CtSMsg>(outputBufferSize);

		this.requestID = 0;
		this.name = name;
		this.password = password;

		try {
			this.socket = new Socket(serverAddress, Server.PortToClient);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void mainLoop() {
        try {
            Socket socket = new Socket("localhost", Server.PortToClient); // o host é conhecido do lado do cliente, neste caso o host do servidor é "localhost"

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // thread dedicada a output
            Thread outputThread = new Thread(new ClientOutputRunnable(out, outputBuffer));
            outputThread.start();

            // thread dedicada a input (nao fica aqui porque ClientUI ao chamar isto nao pode ficar bloqueada)
            Thread inputThread = new Thread(new ClientInputRunnable(in, inputBuffer));
            inputThread.start();

            // AUTENTICAR AQUI

        } catch (IOException e) {
            //what to do if thread streams break
            e.printStackTrace();
    	}
    }

	public StCMsg getNextAnswer() throws InterruptedException{
        return inputBuffer.pop();
	}

	public void sendRequest(CtSMsg msg) throws InterruptedException {
		requestID++;
        msg.setRequestN(requestID);
		outputBuffer.push(msg);
	}

	public void closeSocket() throws IOException {
		socket.shutdownOutput();
		socket.shutdownInput();
		socket.close();
	}

	public static void main( String[] args )
    {
        System.out.println( "Hello World client!" );
        try {
            Socket socket = new Socket("localhost", Server.PortToClient); // o host é conhecido do lado do cliente, neste caso o host do servidor é "localhost"

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            CtSAutMsg testeMsg = new CtSAutMsg("User","pass");
            testeMsg.serialize(out);

            CtSExecMsg testeMsg2 = new CtSExecMsg(1,150,"É o nosso guiador".getBytes());
            testeMsg2.serialize(out);

            TimeUnit.SECONDS.sleep(10);
                
            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
