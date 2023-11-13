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

	private InetAddress address;

	public HandleWorkerInput(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
		this.address = socket.getInetAddress();
	}

	@Override
	public void run() {
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			BoundedBuffer<...> outputBuffer = new BoundedBuffer<>(Server.localOutputBufferWorkerSize);

			server.putWorkerOutputBuffer(address, outputBuffer);

			Thread outThread = new Thread(new HandleWorkerOutput(out, outputBuffer));
			outThread.start();

			try {
				// ler da socket e meter no input buffer do servidor, usar pushInputBufferWorker
				while(true) {
					in.readByte();
				}
			} catch (EOFException e) { // chamada quando socket fecha do outro lado e temos erro a dar read
				in.close();
				out.close();
				socket.close();

				server.removeWorker(this.address);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

falta separar mainLoop em 2 funcs
