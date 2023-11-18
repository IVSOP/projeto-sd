package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

// input

	//  criar buffer
	// 	ler da socket
	// 	meter no buffer
	// 	close socket quando read falhar
	//  destruir buffer


// output

	// 	ler do buffer
	// 	mandar

// no fim tenta fechar a socket e remove o buffer do map do servidor
// ao iniciar conexao, recebe dados do cliente e cria buffer para ele
// depois, cria thread de output com esse buffer

// ao recebermos registo do cliente, guardamos o ClientData correspondente para evitar lookups futuros no servidor
public class AnswerClientInput implements Runnable {

    private Socket socket;
	private Server server;
	private ClientData data;
	private Thread outThread;

	// nao gosto nada de passar o server mas e a unica forma de nao ficar completamente ilegivel,
	// visto que quero criar e destruir aqui o buffer
    public AnswerClientInput(Socket socket, Server server) {
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

				/////////////////////////////////////////////// CLIENT FIRST MESSAGE
				// get login or register info

				if (login) {
					this.data = server.loginClient(name, password);
					if (this.data == null) {
						// client does not exist or passwords dont match
					}
				} else if (register) {
					this.data = server.registerClient(name, password);
				}


				outThread = new Thread(new AnswerClientOutput(out, data.outputBuffer)); // thread writing to the socket
				outThread.start();

				/////////////////////////////////////////////// CLIENT INFINITE LOOP
				// ............























				byte opcode;
				CtSAutMsg authMsg = null; // message received from client

				// Authenticate client first
				authMsg = new CtSAutMsg();
				authMsg.deserialize(in);
				System.out.println(authMsg.toString()); // debug
				this.data = server.registerClient(authMsg.getName(), authMsg.getPassword());

				// login client, get his ID or return error if wrong password
				// ...???
	
	
				CtSMsg baseMsg = null;
				ClientMessage<CtSMsg> msgToPush = new ClientMessage<>(); // message received from client with clientID

				while (true) {
					// receber dados da socket e colocar no buffer do servidor
					opcode = in.readByte();
					switch (opcode) {
						case 0:
							baseMsg = new CtSExecMsg();
							baseMsg.deserialize(in);
							System.out.println(baseMsg.toString()); // debug
							break;
						case 1: 
							baseMsg = new CtSStatusMsg();
							baseMsg.deserialize(in);
							System.out.println(baseMsg.toString()); // debug
							break;
					}
					msgToPush.setClient(data.ID);
					msgToPush.setMessage(baseMsg);
					server.pushInputBufferClient(msgToPush, data); // push message to global server input array
				}
	
			} catch (EOFException e) { // chamada quando socket fecha do outro lado e temos erro a dar read
				in.close();
				out.close();
				socket.close();

				// kill output thread
				outThread.interrupt();

				data.removeOutputBuffer();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
