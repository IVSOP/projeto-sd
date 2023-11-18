package grupo49;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import java.util.concurrent.locks.Condition;
// import java.util.concurrent.locks.ReentrantLock;
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
	private Map<String, Integer> clientNameToIDMap;
	private ReentrantReadWriteLock clientNameToIDMapLock;
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
		this.clientNameToIDMap = new HashMap<>();
		this.clientNameToIDMapLock = new ReentrantReadWriteLock();


		this.workerInfo = new ArrayList<>();
		this.workerInfoLock = new ReentrantReadWriteLock();
		this.inputBufferWorker = new BoundedBuffer<>(Server.inputBufferWorkerSize);
		this.workerID_counter = 0;

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
	// output buffer is created for the client
	// carefull, if client already exists, will get replaced and will most likely crash in 3 or 5 threads
	// no error checking performed
	public ClientData registerClient(String name, String password) {
		try {
			clientNameToIDMapLock.writeLock().lock();
			clientMapLock.writeLock().lock();
			
			
			ClientData data = new ClientData(name, password, clientID_counter);
			// adicionar infos
			clientMap.put(clientID_counter, data);
			clientNameToIDMap.put(name, clientID_counter);
			clientID_counter++;
			
			return data;
		} finally {
			clientMapLock.writeLock().unlock();
			clientNameToIDMapLock.writeLock().unlock();
		}
	}

	public boolean clientExists(String name) {
		try {
			clientNameToIDMapLock.readLock().lock();
			return clientNameToIDMap.containsKey(name);
		} finally {
			clientNameToIDMapLock.readLock().unlock();
		}
	}

	// output buffer is created for the client
	// can return null if client does not exist
	// clients can only login once, no error checking is performed
	// multiple logins will crash 3 or 4 different threads at least
	public ClientData loginClient(String name, String password) {
		try {
			// temos um 'furo' entre as locks porque sabemos que ID e nome de um cliente nunca mudam,
			// e que nada de significante vai ser alterado por outras threads
			clientNameToIDMapLock.readLock().lock();
			int clientID = clientNameToIDMap.get(name);
			clientNameToIDMapLock.readLock().unlock();

			clientMapLock.readLock().unlock();
			ClientData data = clientMap.get(clientID);
			if (data != null && data.password == password) {
				data.createOutputBuffer(); // !!!!!!!!!!!!!!!!!
				return data;
			} else {
				return null;
			}
		} finally {
			clientMapLock.readLock().unlock();
		}
	}

	// for now logging means to just 'delete' the outputBuffer of the client 
	public void logoutClient(ClientData clientInfo) {
		clientInfo.removeOutputBuffer();
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
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			clientInfo.serverPushLock.unlock();
		}
	}

	// server pushes message to a client
	// client is specified within the message itself (??????????????????????????????????????????????????????????????????????????????????????????????????)
	public void pushClientOutput(StCMsg message) {
		int clientID = -1; // FALTA ISTO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		ClientData clientInfo = getClient(clientID); // NAO USEI TWO PHASE LOCKING PORQUE CLIENTES NUNCA SAO REMOVIDOS
		try {
			// order of operations is important
			// this way, job counts as finished once it enters output buffer
			// if buffer is full, this will block, and not release the lock
			// if lock is not released, new jobs can't be added anyway,
			// effectively limiting a client in 2 ways:
			//		job limit
			// 		output buffer limit
			// this way if client simply refuses to read from socket etc
			// it cannot use many requests
			clientInfo.serverPushLock.lock(); // precisamos da lock para alterar a variavel n_currentJobs
			clientInfo.outputBuffer.push(message);
			clientInfo.n_currentJobs --;
			clientInfo.permissionToPush.signal(); // acordar 1 thread que esteja a esperar			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			clientInfo.serverPushLock.unlock(); // deixar isto num finally ???????
		}
	}

	/////////////////////////////////////// WORKER ///////////////////////////////////////

	public static final int PortToWorker = 12346;
	public static final int inputBufferWorkerSize = 100;
	public static final int localOutputBufferWorkerSize = 10; // local porque ha 1 por worker

	private ServerSocket socketToWorkers;
	private BoundedBuffer<ClientMessage<StCMsg>> inputBufferWorker; // buffer onde workers colocam resultados de queries
	private ArrayList<WorkerData> workerInfo; // worker ID -> worker info. ArrayList para ser facil de iterar
	private ReentrantReadWriteLock workerInfoLock;
	private int workerID_counter;


	// // returns a worker's ouput buffer, you can use it freely
	// public BoundedBuffer<ClientMessage<StWMsg>> getWorkerOutputBuffer(InetAddress address) {
	// 	try {
	// 		workerOutputBufferMapLock.readLock().lock();

	// 		return workerOutputBufferMap.get(address);
	// 	} finally {
	// 		workerOutputBufferMapLock.readLock().unlock();
	// 	}
	// }

	// no error checking
	public WorkerData registerWorker(int memory) {
		try {
			workerInfoLock.writeLock().lock();
			WorkerData data = new WorkerData(this.workerID_counter, memory);
			this.workerInfo.add(workerID_counter, data); // manually add at this index
			this.workerID_counter ++;
			return data;
		} finally {
			workerInfoLock.writeLock().unlock();
		}
	}

	// push message into global input buffer for workers
	// 'releases' memory in the worker
	public void pushInputBufferWorker(ClientMessage<StCMsg> message, WorkerData data) {
		try {
			data.memoryLock.writeLock().lock();
			data.memory -= message.getMemory();
			data.memoryLock.writeLock().unlock();
			inputBufferWorker.push(message);
		} catch (InterruptedException e) {
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
