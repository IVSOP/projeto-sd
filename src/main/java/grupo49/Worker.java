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
	private DataInputStream in;
	private DataOutputStream out;
	private int memory;

	public Worker(String serverAddress, int memory) throws UnknownHostException, IOException{
		this.inputBuffer = new BoundedBuffer<ClientMessage<StWMsg>>(inputBufferSize);
		this.outputBuffer = new BoundedBuffer<ClientMessage<WtSMsg>>(outputBufferSize);
		this.memory = memory;
		this.socket = new Socket(serverAddress, Server.PortToWorker);
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
	} 
	
	public Worker(String serverAddress, String localAddress, int memory) throws UnknownHostException, IOException {
		this.inputBuffer = new BoundedBuffer<ClientMessage<StWMsg>>(inputBufferSize);
		this.outputBuffer = new BoundedBuffer<ClientMessage<WtSMsg>>(outputBufferSize);
		this.memory = memory;
												// por alguma razao local nao pode ser string mas destino pode
		this.socket = new Socket(serverAddress, Server.PortToWorker, InetAddress.getByName(localAddress), Server.PortToWorker); // local port does not matter
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
	}

	public static Worker registerWorker(Worker worker, DataOutputStream out) {	
		WtSMsg msg = new WtSRegMsg(worker.memory);
		try {
			msg.serialize(out);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return worker;
	}

	// create 1 thread on input, 1 thread on output, threadPool taking care of requests
	public void mainLoop() {

			Thread outputThread = new Thread(new WorkerOutputRunnable(this.out, outputBuffer));
			outputThread.start();

			// criar N threads a fazer trabalho
			// isto e uma threadpool muito simples, basta elas estarem em loop no buffer
			int i;
			for (i = 0; i < threadPoolSize; i++) {
				Thread t = new Thread(new WorkerWorkRunnable(inputBuffer, outputBuffer));
				t.start();
			}

			// escusado criar outra thread para input, fica aqui simplesmente
			ClientMessage<StWMsg> message = new ClientMessage<>(new StWExecMsg());
			while (true) {
				try {
					message.deserialize(in);
					// System.out.println("Got new task: " + message.toString());
					System.out.println("Got task " + message.getMessage().getRequestN() + " from client " + message.getClient());
					inputBuffer.push(message);
				} catch (IOException e) {
					System.out.println("Did server die? Exiting");
					System.exit(1);
					// e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
	}

	// main receives server IP and local IP. memory is optional
	// NOT IMPLEMENTED
    public static void main( String[] args )
    {
		Worker worker = WorkerUI.setupWorker();
		System.out.println("Worker mem: " + worker.memory);
		registerWorker(worker,worker.out);
		System.out.println("Starting worker");
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
			//String localAddress = askForInput("Enter local IP: ");
			// receber nome e password do terminal
			int memory = Integer.parseInt(askForInput("Enter memory available: "));
			Worker worker = null;
			try {
				//worker = new Worker(serverAddress, localAddress, Integer.parseInt(memory));
				worker = new Worker(serverAddress, memory);

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
			return worker;
		}
	}
}
