package grupo49;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import grupo49.Server.OcupationData;

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

			System.out.println("New client connected");
			try {
				boolean authSuccessful = false;
				
				while (!authSuccessful) {
					byte opcode = in.readByte();
					boolean clientExists;
					// Authenticate client first
					switch (opcode) {
						case 0: // register
							CtSRegMsg authMsg = new CtSRegMsg();
							authMsg.deserialize(in);
							clientExists = server.clientExists(authMsg.getName());
							if (clientExists) {
								System.out.println( "Received Client register: name already exists");
								StCAuthMsg regFail = new StCAuthMsg(false, "Client already exists");
								regFail.serialize(out);
							} else {
								System.out.println("Received Client register: successful register");
								this.data = server.registerClient(authMsg.getName(), authMsg.getPassword());
								authSuccessful = true;
								StCAuthMsg regTrue = new StCAuthMsg(true, "Client registered correctly");
								regTrue.serialize(out);
							}
							break;
						case 1: //login
							CtSLoginMsg loginMsg = new CtSLoginMsg();
							loginMsg.deserialize(in);
							clientExists = server.clientExists(loginMsg.getName());
							if (!clientExists) { // se login correu mal
								System.out.println("Received Client login: name doesn't exist");
							StCAuthMsg logFail = new StCAuthMsg(false, "Client not registered");
							// não é necessário locks, só esta thread escreve 
							logFail.serialize(out);
							}
							else if ((this.data = server.loginClient(loginMsg.getName(), loginMsg.getPassword())) == null) { // se login correu mal
								System.out.println("Received Client login: couldn't login credentials");
								StCAuthMsg logFail = new StCAuthMsg(false, "Password doesn't match");
								// não é necessário locks, só esta thread escreve 
								logFail.serialize(out);

							} else { //se login correu bem
								authSuccessful = true;
								System.out.println("Received Client login: login successfull");
								StCAuthMsg logCorr = new StCAuthMsg(true, "Client login successful");
								// não é necessário locks, só esta thread escreve 
								logCorr.serialize(out);
							}
							break;
						default: //qualquer outra mensagem inicial, nunca acho que aconteça, mas por segurança
							System.out.println("Received Client not reg/log: wrong type");
							StCAuthMsg other = new StCAuthMsg(false, "First register or login to authenticate");
							// não é necessário locks, só esta thread escreve 
							other.serialize(out);
					}
				}	

				outThread = new Thread(new AnswerClientOutput(out, data.outputBuffer)); // thread writing to the socket
				outThread.start();

				// Client infinite loop
				CtSMsg baseMsg = null;
				ClientMessage<CtSMsg> msgToPush = new ClientMessage<>(); // message received from client with clientID

				while (true) {
					// receber dados da socket e colocar no buffer do servidor
					byte opcode = in.readByte();

					switch (opcode) {
						// mensagens de execução são encaminhadas para buffer global em servidor
						case 2:
							baseMsg = new CtSExecMsg();
							baseMsg.deserialize(in);
							System.out.println("Client " + data.ID + " asking for exec " + baseMsg.getRequestN());
							// System.out.println(baseMsg.toString()); // debug

							msgToPush.setClient(data.ID);
							msgToPush.setMessage(baseMsg);
							server.pushInputBufferClient(msgToPush, data); // push message to global server input array
							System.out.println("Client " + data.ID + " pushed exec request " + baseMsg.getRequestN());

							break;

						// mensagens de status são tratadas aqui, para caso fica locked a inserir no outputBuffer do cliente, so afetar esta thread
						// Senão ter-se-ia de empurrar a mensagem para o buffer global, e possivelmente dar lock a uma das threads nesse buffer, o que atrasava o processamento de pedidos de outros clientes
						case 3:
							System.out.println("Client " + data.ID + " asking for status");
							baseMsg = new CtSStatusMsg();
							baseMsg.deserialize(in);
							// System.out.println("Received from client\n" + baseMsg.toString()); // debug

							try {
								OcupationData ocupation = server.getOcupationData();
								StCStatusMsg statusMsg = 
									new StCStatusMsg(((CtSStatusMsg) baseMsg).getRequestN(), ocupation.getMemRemaining(),ocupation.getCurrentJobs());
									
								data.outputBuffer.push(statusMsg); // meter no output buffer de cliente
							
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							break;
					}
				}

			} catch (EOFException e) { // chamada quando socket fecha do outro lado e temos erro a dar read
				System.out.println("Client " + data.ID + " disconnected, closing socket");
				// outThread.interrupt();
				in.close();
				out.close();
				socket.close();
				throw new IOException(e); // mesmo que corra bem damos throw, assim fecha-se tudo em baixo

			}
		} catch (IOException e) {
			// e.printStackTrace();
			// kill output thread
			outThread.interrupt();

			data.removeOutputBuffer();
		}
    }
}
