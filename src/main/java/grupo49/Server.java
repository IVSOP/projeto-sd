package grupo49;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Hello world!
 *
 */
public class Server 
{
	/////////////////////////////////////// CLIENT ///////////////////////////////////////
	// ClientData foi colocado no seu proprio ficheiro, para ser tudo public

	public static final int PortToClient = 12345;
	public static final int inputBufferClientSize = 100;
	public static final int localOutputBufferClientSize = 10; // local porque ha 1 por cliente
	
	private ServerSocket socketToClients;
	private BoundedBuffer<ClientMessage<CtSMsg>> inputBufferClient; // client requests are all written to this buffer
	
	private int clientID_counter; // counter to assign new client ID. does not have lock since the lock for clientMap is used
	private Map<Integer, ClientData> clientMap; // ID -> dados
	private ReentrantReadWriteLock clientMapLock;
	// private Map<Integer, BoundedBuffer<StCMsg>> clientOutputBufferMap; // buffers de output para os clientes (passou para o ClientData em si)
	// private ReentrantReadWriteLock clientOutputBufferMapLock;

	private int MaxJobsPerClient;

	public Server() {
		this.clientID_counter = 0;
		this.clientMap = new HashMap<>();
		this.clientMapLock = new ReentrantReadWriteLock();
		// this.clientOutputBufferMap = new HashMap<Integer, BoundedBuffer<StCMsg>>();
		// this.clientOutputBufferMapLock = new ReentrantReadWriteLock();
		this.inputBufferClient = new BoundedBuffer<ClientMessage<CtSMsg>>(Server.inputBufferClientSize);

		this.workerInfoMap = new HashMap<>();
		this.workerInfoMapLock = new ReentrantReadWriteLock();
		this.workerOutputBufferMap = new HashMap<>();
		this.workerOutputBufferMapLock = new ReentrantReadWriteLock();
		this.inputBufferWorker = new BoundedBuffer<>(Server.inputBufferWorkerSize);

		this.MaxJobsPerClient = 5;

		try {
			this.socketToClients = new ServerSocket(Server.PortToClient);
			this.socketToWorkers = new ServerSocket(Server.PortToWorker);


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ClientData getClient(int clientID) {
		try {
			clientMapLock.readLock().lock();

			return clientMap.get(clientID);
		} finally {
			clientMapLock.readLock().unlock();
		}
	}

	// returns created ClientData
	// OUTPUT BUFFER IS NOT CREATED
	public ClientData registerClient(String email, String password) {
		try {
			clientMapLock.writeLock().lock();

			ClientData data = new ClientData(email, password, clientID_counter);
			
			// adicionar infos
			clientMap.put(clientID_counter, data);
			clientID_counter++;

			return data;
		} finally {
			clientMapLock.writeLock().unlock();
		}
	}

	// push message into global input buffer for clients
	// WILL BLOCK if the client has exceeded number of jobs it can have
	// ClientData is passed for efficieny, avoiding a loopkup in the map of all clients
	public void pushInputBufferClient(ClientMessage<CtSMsg> message, ClientData clientInfo) {
		try {
			clientInfo.serverPushLock.lock();

			while (clientInfo.n_currentJobs >= MaxJobsPerClient) {
				// client sent too many requests too quick, will have to stay blocked until other jobs finish
				clientInfo.permissionToPush.await();
			}

			inputBufferClient.push(message);
			clientInfo.n_currentJobs ++;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			clientInfo.serverPushLock.lock();
		}
	}

	// server pushes message to a client
	// client is specified within the message itself (??????????????????????????????????????????????????????????????????????????????????????????????????)
	public void pushClientOutput(StCMsg message) {
		int clientID = -1; // FALTA ISTO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		ClientData clientInfo = getClient(clientID);
		try {
			clientInfo.serverPushLock.lock(); // precisamos da lock para alterar a variavel n_currentJobs

			clientInfo.n_currentJobs --;
			clientInfo.permissionToPush.signal(); // acordar 1 thread que esteja a esperar

			clientInfo.serverPushLock.unlock(); // deixar isto num finally ???????

			clientInfo.outputBuffer.push(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// clientInfo.n_currentJobs --;
		// wakeup a thread
		// mandar para output buffer
	}

	/////////////////////////////////////// WORKER ///////////////////////////////////////
	public class WorkerData {
		// .......
	}

	public static final int PortToWorker = 12346;
	public static final int inputBufferWorkerSize = 100;
	public static final int localOutputBufferWorkerSize = 10; // local porque ha 1 por worker

	private ServerSocket socketToWorkers;
	private BoundedBuffer<ClientMessage<StCMsg>> inputBufferWorker; // buffer onde workers colocam resultados de queries
	private Map<InetAddress, WorkerData> workerInfoMap;
	private ReentrantReadWriteLock workerInfoMapLock;
	private Map<InetAddress, BoundedBuffer<ClientMessage<StWMsg>>> workerOutputBufferMap; // buffers onde se coloca requests para worker processar
	private ReentrantReadWriteLock workerOutputBufferMapLock;

	// returns a worker's ouput buffer, you can use it freely
	public BoundedBuffer<ClientMessage<StWMsg>> getWorkerOutputBuffer(InetAddress address) {
		try {
			workerOutputBufferMapLock.readLock().lock();

			return workerOutputBufferMap.get(address);
		} finally {
			workerOutputBufferMapLock.readLock().unlock();
		}
	}

	// assign an output buffer to a worker (it does not create it)
	public void putWorkerOutputBuffer(InetAddress address, BoundedBuffer<ClientMessage<StWMsg>> buff) {
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
	public void pushInputBufferWorker(ClientMessage<StCMsg> message) {
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
