package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Worker 
{
	public static final int inputBufferSize = 10;
	public static final int outputBufferSize = 10;
	public static final int threadPoolSize = 4;

	private Socket socket; // socket to server
	private BoundedBuffer<ClientMessage<StWMsg>> inputBuffer;
	private BoundedBuffer<ClientMessage<WtSMsg>> outputBuffer;
	private int memory;

	public Worker(String serverAddress, int memory) throws UnknownHostException, IOException{
		this.inputBuffer = new BoundedBuffer<ClientMessage<StWMsg>>(inputBufferSize);
		this.outputBuffer = new BoundedBuffer<ClientMessage<WtSMsg>>(outputBufferSize);
		this.memory = 0;

		this.socket = new Socket(serverAddress, Server.PortToWorker);
	} 
	
	public Worker(String serverAddress, String localAddress, int memory) throws UnknownHostException, IOException {
		this.inputBuffer = new BoundedBuffer<ClientMessage<StWMsg>>(inputBufferSize);
		this.outputBuffer = new BoundedBuffer<ClientMessage<WtSMsg>>(outputBufferSize);
		this.memory = 0;
												// por alguma razao local nao pode ser string mas destino pode
		this.socket = new Socket(serverAddress, Server.PortToWorker, InetAddress.getByName(localAddress), Server.PortToWorker); // local port does not matter
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
		Worker worker = WorkerUI.setupWorker();
		worker.mainLoop();

    }

	static class WorkerUI {

		private static Scanner scanner = new Scanner(System.in);

		private static String askForInput(String msg) {
			System.out.print(msg);
			return scanner.nextLine();
		}

		public static Worker setupWorker() {
			String serverAddress = askForInput("Enter server IP: ");
			// receber address do terminal (so apra evitar conflitos como ta tudo em localhost)
			// String localAddress = String.valueOf(InetAddress.getLocalHost()); // cursed
			String localAddress = askForInput("Enter local IP: ");
			// receber nome e password do terminal
			String memory = askForInput("Enter memory available: ");
			Worker worker = null;
			try {
				worker = new Worker(serverAddress, localAddress, Integer.parseInt(memory));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
			return worker;
		}
	}
}
