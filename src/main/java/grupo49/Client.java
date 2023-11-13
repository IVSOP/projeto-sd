package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;


public class Client
{
	public static final int inputBufferSize = 10;
	public static final int outputBufferSize = 10;

	private int requestID;

	BoundedBuffer<...> inputBuffer;
	BoundedBuffer<...> outputBuffer;

	Socket socket;

	String email;
	String password

	public Client(String serverAddress, String email, String password) {
		this.inputBuffer = new BoundedBuffer<...>(inputBufferSize);
		this.outputBuffer = new BoundedBuffer<...>(outputBufferSize);

		this.requestID = 0;
		this.email = email;
		this.password = password;

		try {
			this.socket = new Socket(serverAddress, Server.PortToClient);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void mainLoop() {
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());

		// thread dedicada a output
		Thread outputThread = new Thread(new ClientOutputRunnable(out, outputBuffer));
		outputThread.start();

		// thread dedicada a input (nao fica aqui porque ClientUI ao chamar isto nao pode ficar bloqueada)
		Thread inputThread = new Thread(new ClientOutputRunnable(in, inputBuffer));
		inputThread.start();




		// AUTENTICAR AQUI
	}

	public ... getNextAnswer() {
		return inputBuffer.pop();
	}

	public void sendRequest(????) {
		nao esquecer requestID++;
		outputBuffer.push(msg);
	}

	public void closeSocket() {
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

            //testar
            // CtSAutMsg testeMsg = new CtSAutMsg("Nestor","pastor");
            // testeMsg.serialize(out);
            CtSExecMsg testeMsg2 = new CtSExecMsg("É o nosso guiador".getBytes(),150,1);
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
