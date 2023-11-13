package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Worker 
{
	public static final int inputBufferSize = 10;
	public static final int outputBufferSize = 10;
	public static final int threadPoolSize = 4;

	private Socket socket; // socket to server
	BoundedBuffer<ClientMessage<StWMsg>> inputBuffer;
	BoundedBuffer<ClientMessage<WtSMsg>> outputBuffer;

	public Worker() {
		this.inputBuffer = new BoundedBuffer<ClientMessage<StWMsg>>(inputBufferSize);
		this.outputBuffer = new BoundedBuffer<ClientMessage<WtSMsg>>(outputBufferSize);

		try {
			this.socket = new Socket("localhost", Server.PortToWorker);
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

    public static void main( String[] args )
    {
        System.out.println( "Hello World worker!" );
    }
}
