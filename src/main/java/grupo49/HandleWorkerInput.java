package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

// nome pessimo, depois mudar
public class HandleWorkerInput implements Runnable {
	private Socket socket;
	private Server server;
	private WorkerData data;
	private Thread outThread;

	public HandleWorkerInput(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
		this.data = null;
		this.outThread = null;
	}

	@Override
	public void run() {
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			/////////////////////////////////// WORKER FIRST MESSAGE
			// sends total available memory

			server.registerWorker(memory);

			outThread = new Thread(new HandleWorkerOutput(out, this.data.outputBuffer));
			outThread.start();




			/////////////////////////////////// WORKER INFINITE LOOP
			//.....






















			try {
				// ler da socket e meter no input buffer do servidor, usar pushInputBufferWorker
				while(true) {
					ClientMessage<StCMsg> msg = new ClientMessage<>();
					msg.deserialize(in);
					server.pushInputBufferWorker(msg);
				}
			} catch (EOFException e) { // chamada quando socket fecha do outro lado e temos erro a dar read
				// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! por agora nao ha controlo sobre isto, se worker morrer vai haver muita coisa a correr mal
				in.close();
				out.close();
				socket.close();

				// kill output thread
				outThread.interrupt();

				// server.removeWorker(...);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

// falta separar mainLoop em 2 funcs
