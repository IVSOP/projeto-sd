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

			try {
				/////////////////////////////////// WORKER FIRST MESSAGE

				//mensagem será sempre do tipo reg, n vale a pena ler opcodes
				WtSRegMsg regMsg = new WtSRegMsg();
				regMsg.deserialize(in);
				this.data = server.registerWorker(regMsg.getMemAvail());

				outThread = new Thread(new HandleWorkerOutput(out, this.data.outputBuffer));
				outThread.start();

				/////////////////////////////////// WORKER INFINITE LOOP

				ClientMessage<StCMsg> msgToPush = null;
				int memUsed = 0;

				while(true) {
					byte opcode = in.readByte();
					switch (opcode) {

						case 1: // se mensagem vinda do worker era de resultado de execução
							ClientMessage<WtSExecMsg> workerExecMsg = new ClientMessage<WtSExecMsg>();
							workerExecMsg.deserialize(in);
							StCMsg execMsg = 
								new StCExecMsg(workerExecMsg.getMessage().getRequestN(),workerExecMsg.getMessage().getData());

							msgToPush = new ClientMessage<>(workerExecMsg.getClient(),execMsg);
							memUsed = workerExecMsg.getMessage().getMemUsed();

							break;

						case 2: // se mensagem vinda de worker era de erro

							ClientMessage<WtSErrorMsg> workerErrorMsg = new ClientMessage<WtSErrorMsg>();
							workerErrorMsg.deserialize(in);
							StCMsg errorMsg = 
								new StCErrorMsg(workerErrorMsg.getMessage().getRequestN(), workerErrorMsg.getMessage().getError());

							msgToPush = new ClientMessage<>(workerErrorMsg.getClient(),errorMsg);
							memUsed = workerErrorMsg.getMessage().getMemUsed();
							
							break;
					}

					server.pushInputBufferWorker(msgToPush, this.data, memUsed);
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
