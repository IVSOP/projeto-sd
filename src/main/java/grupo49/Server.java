package grupo49;

import java.io.IOException;
import java.net.InetAddress;
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
	public Server() {
		this.clientID_counter = 0;
		this.clientEmailMap = new HashMap<String, ClientData>();
		this.clientEmailMapLock = new ReentrantReadWriteLock();
		this.clientOutputBufferMap = new HashMap<Integer, BoundedBuffer<StCMsg>>();
		this.clientOutputBufferMapLock = new ReentrantReadWriteLock(); // (true)??????????????????
		this.inputBufferClient = new BoundedBuffer<ClientMessage<CtSMsg>>(Server.inputBufferClientSize);

		this.workerInfoMap = new HashMap<>();
		this.workerInfoMapLock = new ReentrantReadWriteLock();
		this.workerOutputBufferMap = new HashMap<>();
		this.workerOutputBufferMapLock = new ReentrantReadWriteLock();
		this.inputBufferWorker = new BoundedBuffer<>(Server.inputBufferWorkerSize);

		try {
			this.socketToClients = new ServerSocket(Server.PortToClient);
			this.socketToWorkers = new ServerSocket(Server.PortToWorker);


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/////////////////////////////////////// CLIENTE ///////////////////////////////////////
	public class ClientData {
		public String password;
		public int ID;
		// email nao e preciso, fica inferido

		public ClientData(String _password, int _ID) {
			this.password = _password;
			this.ID = _ID;
		}
	}

	public static final int PortToClient = 12345;
	public static final int inputBufferClientSize = 100;
	public static final int localOutputBufferClientSize = 10; // local porque ha 1 por cliente

	private ServerSocket socketToClients;
	private BoundedBuffer<ClientMessage<CtSMsg>> inputBufferClient; // client requests are all written to this buffer

	private int clientID_counter; // counter to assign new client ID. does not have lock since the lock for clientEmailMap is used

	private Map<String, ClientData> clientEmailMap; // email, dados do cliente
	private ReentrantReadWriteLock clientEmailMapLock;
	private Map<Integer, BoundedBuffer<StCMsg>> clientOutputBufferMap; // buffers de output para os clientes
	private ReentrantReadWriteLock clientOutputBufferMapLock;

	public void registerClient(String email, String password) {
		try {
			clientEmailMapLock.writeLock().lock();
	
			ClientData data = new ClientData(password, clientID_counter);
			clientID_counter++;
			
			// adicionar infos
			clientEmailMap.put(email, data);

		} finally {
			clientEmailMapLock.writeLock().unlock();
		}
	}

	// returns a client's output buffer, you can use it freely
	public BoundedBuffer<StCMsg> getClientOutputBuffer(int clientID) {
		try {
			clientOutputBufferMapLock.readLock().lock();

			return clientOutputBufferMap.get(clientID);

		} finally {
			clientOutputBufferMapLock.readLock().unlock();
		}
	}

	// assign an output buffer to a client (it does not create it)
	public void putClientOutputBuffer(int clientID, BoundedBuffer<StCMsg> buff) {
		try {
			clientOutputBufferMapLock.writeLock().lock();

			clientOutputBufferMap.put(clientID, buff);
		} finally {
			clientOutputBufferMapLock.writeLock().unlock();
		}
	}

	// remove a client's output buffer
	public void removeClientOutputBuffer(int clientID) {
		try {
			clientOutputBufferMapLock.writeLock().lock();

			clientOutputBufferMap.remove(clientID);
		} finally {
			clientOutputBufferMapLock.writeLock().unlock();
		}
	}

	// push message into global input buffer for clients
	public void pushInputBufferClient(ClientMessage<CtSMsg> message) {
		try {
			inputBufferClient.push(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/////////////////////////////////////// WORKER ///////////////////////////////////////
	public class WorkerData {
		// .......
	}

	public static final int PortToWorker = 12346;
	public static final int inputBufferWorkerSize = 100;
	public static final int localOutputBufferWorkerSize = 10; // local porque ha 1 por worker

	private ServerSocket socketToWorkers;
	private BoundedBuffer<...> inputBufferWorker;

	private Map<InetAddress, WorkerData> workerInfoMap;
	private ReentrantReadWriteLock workerInfoMapLock;
	private Map<InetAddress, BoundedBuffer<...>> workerOutputBufferMap;
	private ReentrantReadWriteLock workerOutputBufferMapLock;

	// returns a worker's ouput buffer, you can use it freely
	public BoundedBuffer<...> getWorkerOutputBuffer(InetAddress address) {
		try {
			workerOutputBufferMapLock.readLock().lock();

			return workerOutputBufferMap.get(address);
		} finally {
			workerOutputBufferMapLock.readLock().unlock();
		}
	}

	// assign an output buffer to a worker (it does not create it)
	public void putWorkerOutputBuffer(InetAddress address, BoundedBuffer<...> buff) {
		try {
			workerOutputBufferMapLock.writeLock().lock();

			workerOutputBufferMap.put(address, buff);
		} finally {
			workerOutputBufferMapLock.writeLock().unlock();
		}
	}

	// remove a worker's output buffer
	public void removeWorkerOutputBuffer(InetAddress address) {
		try {
			workerOutputBufferMapLock.writeLock().lock();

			workerOutputBufferMap.remove(address);
		} finally {
			workerOutputBufferMapLock.writeLock().unlock();
		}
	}

	// remove buffer e todas as infos do worker
	public void removeWorker(InetAddress address) {
		removeWorkerOutputBuffer(address);
		try {
			workerInfoMapLock.writeLock().lock();

			workerInfoMap.remove(address);
		} finally {
			workerInfoMapLock.writeLock().unlock();
		}
	}

	// push message into global input buffer for workers
	public void pushInputBufferWorker(... message) {
		try {
			inputBufferWorker.push(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/////////////////////////////////////// MAINS ///////////////////////////////////////
	public class ClientLoop implements Runnable {
		ServerSocket socketToClients;
		Server server;
		public ClientLoop(ServerSocket socketToClients, Server server) {
			this.socketToClients = socketToClients;
			this.server = server;
		}

		@Override
		public void run() {
			try {
				while (true) {
					Socket socket = socketToClients.accept();
					
					// pelos nomes nao se percebe muito bem, depois mudar
					// mas a de input vai criar a de output assim que o buffer estiver pronto (ela vai cria lo)
					Thread clientThread = new Thread(new AnswerClientInput(socket, server));
					clientThread.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public class WorkerLoop implements Runnable {
		ServerSocket socketToWorkers;
		Server server;
		public WorkerLoop(ServerSocket socketToWorkers, Server server) {
			this.socketToWorkers = socketToWorkers;
			this.server = server;
		}

		@Override
		public void run() {
			try {
				while (true) {
					Socket socket = socketToWorkers.accept();
					
					// pelos nomes nao se percebe muito bem, depois mudar
					// mas a de input vai criar a de output assim que o buffer estiver pronto (ela vai cria lo)
					Thread workerThread = new Thread(new HandleWorkerInput(socket, server));
					workerThread.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void mainLoop() {
		Thread clientLoopThread = new Thread(new ClientLoop(socketToClients, this));
		clientLoopThread.start();

		// vou correr workers aqui em vez de numa thread, assim servidor bloqueia em vez de ficar no background
		WorkerLoop workerloop = new WorkerLoop(socketToWorkers, this);
		workerloop.run();
	}

    public static void main( String[] args ) {
		Server server = new Server();
		server.mainLoop();
    }
}
