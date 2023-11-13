package grupo49;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Hello world!
 *
 */
public class Server 
{
	public static final int PortToClient = 12345;
	public static final int PortToWorker = 12346;
	public static final int inputBufferClientSize = 100;
	public static final int localOutputBufferClientSize = 10; // local porque ha 1 por cliente

	private ServerSocket socketToClients;
	private BoundedBuffer<ClientMessage<IMessage>> inputBufferClient; // client requests are all written to this buffer

	private ServerSocket socketToWorkers;

	public class ClientData {
		public String password;
		public int ID;
		// email nao e preciso, fica inferido

		public ClientData(String _password, int _ID) {
			this.password = _password;
			this.ID = _ID;
		}
	}

	private int clientID_counter;

	private Map<String, ClientData> clientEmailMap; // email, dados do cliente
	private Map<Integer, BoundedBuffer<ClientMessage<IMessage>>> clientOutputBufferMap; // buffers de output para os clientes
	private ReentrantReadWriteLock clientOutputBufferMapLock;


	public Server() {
		clientID_counter = 0;
		clientEmailMap = new HashMap<String, ClientData>();
		clientOutputBufferMap = new HashMap<Integer, BoundedBuffer<ClientMessage<IMessage>>>();
		clientOutputBufferMapLock = new ReentrantReadWriteLock(); // (true)??????????????????

		try {
			socketToClients = new ServerSocket(PortToClient);
			socketToWorkers = new ServerSocket(PortToWorker);

			inputBufferClient = new BoundedBuffer<ClientMessage<IMessage>>(inputBufferClientSize);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void registerClient(String email, String password) {
		ClientData data = new ClientData(password, clientID_counter);
		clientID_counter++;
		
		// adicionar infos
		clientEmailMap.put(email, data);
	}

	// faz ele proprio lock
	public BoundedBuffer<ClientMessage<IMessage>> getClientOutputBuffer(int clientID) {
		BoundedBuffer<ClientMessage<IMessage>> ret = null;
		try {
			clientOutputBufferMapLock.readLock().lock();

			ret = clientOutputBufferMap.get(clientID);

			return ret;
		} finally {
			clientOutputBufferMapLock.readLock().unlock();
		}
	}

	// faz ele proprio lock
	public void putClientOutputBuffer(int clientID, BoundedBuffer<ClientMessage<IMessage>> buff) {
		try {
			clientOutputBufferMapLock.writeLock().lock();

			clientOutputBufferMap.put(clientID, buff);
		} finally {
			clientOutputBufferMapLock.writeLock().unlock();
		}
	}

	// faz ele proprio lock
	public void removeClientOutputBuffer(int clientID) {
		try {
			clientOutputBufferMapLock.writeLock().lock();

			clientOutputBufferMap.remove(clientID);
		} finally {
			clientOutputBufferMapLock.writeLock().unlock();
		}
	}

	public void pushInputBufferClient(ClientMessage<IMessage> message) {
		try {
			inputBufferClient.push(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void mainLoop() {
		try {
			while (true) { // 2 threads para cada cliente
				Socket socket = socketToClients.accept();

				Thread tiput = new Thread(new AnswerClientInput(socket, this));
				tiput.start();
				// pelos nomes nao se percebe muito bem, depois mudar
				// mas a de input vai criar a de output assim que o buffer estiver pronto (ela vai cria lo)
				// Thread toutput = new Thread(new AnswerClientOutput(socket, this));
				// toutput.start();
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
