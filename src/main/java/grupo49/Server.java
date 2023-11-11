package grupo49;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Hello world!
 *
 */
public class Server 
{
	public static final int PortToClient = 12345;
	public static final int BufferSize = 10;

	private ServerSocket socketToClients;
	private BoundedBuffer<Integer> inputBuffer;
	private BoundedBuffer<Integer> outputBuffer;

	public Server() {
		try {
			socketToClients = new ServerSocket(PortToClient);

			inputBuffer = new BoundedBuffer<Integer>(BufferSize);
			outputBuffer = new BoundedBuffer<Integer>(BufferSize);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void mainLoop() {
		try {
			while (true) { // 1 thread para cada cliente
				Socket socket = socketToClients.accept();

				Thread t = new Thread(new AnswerClientRunnable(socket));
				t.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public static void main( String[] args ) {
		Server server = new Server();
		server.mainLoop();
    }
}
