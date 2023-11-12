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
public class AnswerClientInput implements Runnable {

    private Socket socket;
	private Server server;

	// nao gosto nada de passar o server mas e a unica forma de nao ficar completamente ilegivel,
	// visto que quero criar e destruir aqui o buffer
    public AnswerClientInput(Socket socket, Server server) {
        this.socket = socket;
		this.server = server;
    }

    @Override
    public void run() {
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			int clientID = -1; // -1 para o java ficar contente, vou assumir que e impossivel chegar a exception sem ter mudado de valor
	
			try {
				// READ FIRST MESSAGE HERE
				// register/login client, get his ID or return error if wrong password
				//////////////////////////

				BoundedBuffer<ClientMessage<IMessage>> outputBuffer = new BoundedBuffer<>(Server.localOutputBufferClientSize);
				server.putClientOutputBuffer(clientID, outputBuffer); // allocate buffer for this client on the server
	
				Thread outThread = new Thread(new AnswerClientOutput(out, outputBuffer)); // thread writing to the socket
				outThread.start();
	
				while (true) {
					// receber dados da socket e colocar no buffer do servidor, usar pushInputBufferClient
					in.readByte();
				}
	
			} catch (EOFException e) { // chamada quando socket fecha do outro lado e temos erro a dar read
				in.close();
				out.close();
				socket.close();

				server.removeClientOutputBuffer(clientID);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
