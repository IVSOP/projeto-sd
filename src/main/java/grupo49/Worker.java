package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Worker 
{
	public static final int inputBufferSize = 10;
	public static final int outputBufferSize = 10;
	public static final int threadPoolSize = 4;

	private Socket socket; // socket to server
	private BoundedBuffer<ClientMessage<StWMsg>> inputBuffer;
	private BoundedBuffer<ClientMessage<WtSMsg>> outputBuffer;
	private int memory;

	public Worker(String serverAddress, int memory) {
		this.inputBuffer = new BoundedBuffer<ClientMessage<StWMsg>>(inputBufferSize);
		this.outputBuffer = new BoundedBuffer<ClientMessage<WtSMsg>>(outputBufferSize);
		this.memory = 0;

		try {
			this.socket = new Socket(serverAddress, Server.PortToWorker);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Worker(String serverAddress, String localAddress, int memory) {
		this.inputBuffer = new BoundedBuffer<ClientMessage<StWMsg>>(inputBufferSize);
		this.outputBuffer = new BoundedBuffer<ClientMessage<WtSMsg>>(outputBufferSize);
		this.memory = 0;
		
		try {													// por alguma razao local nao pode ser string mas destino pode
			this.socket = new Socket(serverAddress, Server.PortToWorker, InetAddress.getByName(localAddress), Server.PortToWorker); // local port does not matter
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// create 1 thread on input, 1 thread on output, threadPool taking care of requests
	public void mainLoop() {
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			Thread outputThread = new Thread(new WorkerOutputRunnable(out, outputBuffer));
			outputThread.start();

			// criar N threads a fazer trabalho
			// isto e uma threadpool muito simples, basta elas estarem em loop no buffer
			int i;
			for (i = 0; i < threadPoolSize; i++) {
				Thread t = new Thread(new WorkerWorkRunnable(inputBuffer, outputBuffer));
				t.start();
			}

			// escusado criar outra thread para input, fica aqui simplesmente
			// try {
				while (true) {
					// receber da socket e meter no buffer ............................
				}

			// }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// main receives server IP and local IP. memory is optional
	// NOT IMPLEMENTED
    public static void main( String[] args )
    {
        System.out.println( "Hello World worker!" );
    }
}
