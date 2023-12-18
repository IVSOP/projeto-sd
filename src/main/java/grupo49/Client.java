package grupo49;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;


public class Client
{
	public static final int inputBufferSize = 10;
	public static final int outputBufferSize = 10;

	private int requestID = 0;

	BoundedBuffer<StCMsg> inputBuffer; // receives from server

	BoundedBuffer<CtSMsg> outputBuffer; // writes to server

	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
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

			this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            // thread dedicada a output
            Thread outputThread = new Thread(new ClientOutputRunnable(out, outputBuffer));
            outputThread.start();

            // thread dedicada a input (nao fica aqui porque ClientUI ao chamar isto nao pode ficar bloqueada)
            Thread inputThread = new Thread(new ClientInputRunnable(in, inputBuffer));
            inputThread.start();

        } catch (IOException e) {
            //what to do if thread streams break
            e.printStackTrace();
		}
	}

	public Client(String serverAddress, String localAddress, String name, String password) {
		this.inputBuffer = new BoundedBuffer<StCMsg>(inputBufferSize);
		this.outputBuffer = new BoundedBuffer<CtSMsg>(outputBufferSize);

		this.requestID = 0;
		this.name = name;
		this.password = password;

		try {													// por alguma razao local nao pode ser string mas destino pode
			this.socket = new Socket(serverAddress, Server.PortToClient, InetAddress.getByName(localAddress), Server.PortToClient); // local port does not matter

			this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            // thread dedicada a output
            Thread outputThread = new Thread(new ClientOutputRunnable(out, outputBuffer));
            outputThread.start();

            // thread dedicada a input (nao fica aqui porque ClientUI ao chamar isto nao pode ficar bloqueada)
            Thread inputThread = new Thread(new ClientInputRunnable(in, inputBuffer));
            inputThread.start();

        } catch (IOException e) {
            //what to do if thread streams break
            e.printStackTrace();
		}
	}

	private void sendRequest(CtSMsg msg) throws InterruptedException {
	requestID++;
	msg.setRequestN(requestID);
	outputBuffer.push(msg);
	}

	public boolean registerClient() throws IOException, InterruptedException{
		CtSMsg msg = (CtSMsg) new CtSRegMsg(name,password);
		outputBuffer.push(msg);
		StCAuthMsg response = new StCAuthMsg();
		response.deserialize(in);
		return response.getSuccess();
	}

	public boolean loginClient() throws IOException, InterruptedException {
		CtSMsg msg = (CtSMsg) new CtSLoginMsg(name,password);
		outputBuffer.push(msg);
		StCAuthMsg response = new StCAuthMsg();
		response.deserialize(in);
		return response.getSuccess();
	}
	 
	public StCMsg getNextAnswer() throws InterruptedException{
        return inputBuffer.pop();
	}

	public void sendExecMsg(int mem, byte[] barray) throws InterruptedException {
		CtSMsg msg = new CtSExecMsg(mem,barray);
		sendRequest(msg);
	}

	public void sendStatusMsg() throws InterruptedException {
		CtSMsg msg = new CtSStatusMsg();
		sendRequest(msg);
	}

	//acho que isto é desnecessário se fizermos "implements AutoCloseable" na classe
	public void closeSocket() throws IOException {
		socket.shutdownOutput();
		socket.shutdownInput();
		socket.close();
	}

	// main receives server IP and local IP. in the future add info to automate sending work?
	// NOT IMPLEMENTED
	// public static void main( String[] args )
    // {
    //     System.out.println( "Hello World client!" );
    //     try {
    //         Socket socket = new Socket("localhost", Server.PortToClient); // o host é conhecido do lado do cliente, neste caso o host do servidor é "localhost"

    //         DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    //         DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

    //         CtSRegMsg testeMsg = new CtSRegMsg("User","pass");
    //         testeMsg.serialize(out);

    //         // CtSExecMsg testeMsg2 = new CtSExecMsg(1,150,"É o nosso guiador".getBytes());
    //         // testeMsg2.serialize(out);

    //         TimeUnit.SECONDS.sleep(10);
                
    //         socket.shutdownOutput();
    //         socket.shutdownInput();
    //         socket.close();

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }

    // }
}
