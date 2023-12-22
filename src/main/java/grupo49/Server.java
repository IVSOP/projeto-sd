package grupo49;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
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
	public static final int ClientDispatcherThreads = 5;
	
	private ServerSocket socketToClients;
	private BoundedBuffer<ClientMessage<CtSMsg>> inputBufferClient; // client requests are all written to this buffer
	
	private ReentrantReadWriteLock clientMapLock;
		private Map<Integer, ClientData> clientMap; // ID -> dados
		private int clientID_counter; // counter to assign new client ID. does not have lock since the lock for clientMap is used
	private ReentrantReadWriteLock clientNameToIDMapLock;
		private Map<String, Integer> clientNameToIDMap;
	// private Map<Integer, BoundedBuffer<StCMsg>> clientOutputBufferMap; // buffers de output para os clientes (passou para o ClientData em si)
	// private ReentrantReadWriteLock clientOutputBufferMapLock;

	public class OcupationData {
		int memory_remaining;
		int current_jobs;

		public OcupationData(int memory_remaining, int current_jobs) {
			this.memory_remaining = memory_remaining;
			this.current_jobs = current_jobs;
		}

		public int getMemRemaining() {
			return this.memory_remaining;
		}

		public int getCurrentJobs() {
			return this.current_jobs;
		}
	}

	public static final int MaxJobsPerClient = 5;

	public Server() {
		this.clientID_counter = 0;
		this.clientMap = new HashMap<>();
		this.clientMapLock = new ReentrantReadWriteLock();
		// this.clientOutputBufferMap = new HashMap<Integer, BoundedBuffer<StCMsg>>();
		// this.clientOutputBufferMapLock = new ReentrantReadWriteLock();
		this.inputBufferClient = new BoundedBuffer<ClientMessage<CtSMsg>>(Server.inputBufferClientSize);
		this.clientNameToIDMap = new HashMap<>();
		this.clientNameToIDMapLock = new ReentrantReadWriteLock();

		this.threadWorkerInfo = new ThreadWorkerInfo[Server.SchedulerThreadPoolSize];
		for (int i = 0; i < threadWorkerInfo.length; i++) { // isto e preciso???? ou o construtor vazio ja e chamado
			threadWorkerInfo[i] = new ThreadWorkerInfo();
		}
		this.workerIDLock = new ReentrantReadWriteLock();
		this.inputBufferWorker = new BoundedBuffer<>(Server.inputBufferWorkerSize);
		this.workerID_counter = 0;

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

			clientMapLock.readLock().lock();
			ClientData data = clientMap.get(clientID);

			if (data != null && data.password.equals(password)) {
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
	// ClientData is passed for efficiency, avoiding a loopkup in the map of all clients
	public void pushInputBufferClient(ClientMessage<CtSMsg> message, ClientData clientInfo) {
		try {
			clientInfo.serverPushLock.lock();

			while (clientInfo.n_currentJobs >= MaxJobsPerClient) {
				// client sent too many requests too quick, will have to stay blocked until other jobs finish
				clientInfo.permissionToPush.await();
			}

			inputBufferClient.push(message.clone());
			clientInfo.n_currentJobs ++;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			clientInfo.serverPushLock.unlock();
		}
	}

	// server pushes message to a client
	public void pushClientOutput(int clientId,  StCMsg message) {
		ClientData clientInfo = getClient(clientId); // NAO USEI TWO PHASE LOCKING PORQUE CLIENTES NUNCA SAO REMOVIDOS
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
			clientInfo.serverPushLock.lock(); // precisamos da lock para alterar a variavel n_currentJobs e para garantir que buffer nao e 'apagado'
			if (clientInfo.outputBuffer != null) { // se client estiver logged out, vai ser null
				clientInfo.outputBuffer.push(message.clone());
				clientInfo.n_currentJobs --;
				clientInfo.permissionToPush.signal(); // acordar 1 thread que esteja a esperar
			} else { // por seguranca, reset completo de tudo. isto nao e muito bom, mas acho que previne crashar tudo com logouts inesperados
				clientInfo.n_currentJobs --; // fingimos que 1 job foi concluido
				clientInfo.permissionToPush.signal();
				System.out.println("ERROR: client " + clientId + " does not have a buffer");
			}
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
	public static final int SchedulerThreadPoolSize = 10; // threads que mandam coisas para workers

	private ServerSocket socketToWorkers;
	private BoundedBuffer<ClientMessage<StCMsg>> inputBufferWorker; // buffer global de input para workers
	private ThreadWorkerInfo[] threadWorkerInfo; // thread ID -> information about workers for that thread, and a lock associated with it.
	// NOTA: threadWorkerInfo nao tem nenhuma lock pois a threadPool tem tamanho estatico e nada disto vai ser alterado
	// mudei de list para [] para ser mais claro como nunca vai mudar,
	// facil de mudar para dinamico no futuro se for preciso, e a lock podia simplesmente ser a de baixo
	private ReentrantReadWriteLock workerIDLock; // usar lock normal?, nunca se faz so um read
		private int workerID_counter;

	// assigns a worker to its designated thread on the pool, based on its ID
	public WorkerData registerWorker(int memory) {
		try {
			// workerInfoLock.writeLock().lock();
			workerIDLock.writeLock().lock();
			int threadID = workerID_counter % threadWorkerInfo.length;
			WorkerData data = new WorkerData(this.workerID_counter, memory, threadWorkerInfo[threadID]);
			this.threadWorkerInfo[threadID].addWorker(data);
			this.workerID_counter ++;
			System.out.println("New worker connection, ID: " + data.ID);
			return data;
		} finally {
			// workerInfoLock.writeLock().unlock();
			// acho que unlock podia ficar noutro sitio antes, mas assim fica seguro
			workerIDLock.writeLock().unlock();
		}
	}

	// push message into global input buffer for workers
	// idk why a worker's memory and job count is changed here and not as soon as memory is received, I forgor
	// will also change the values on the thread that controls this worker
	public void pushInputBufferWorker(ClientMessage<StCMsg> message, WorkerData data, int memUsed) {
		try {
			data.workerLock.writeLock().lock();

			// nao vou usar isto porque ja temos a lock, podemos alterar diretamente
			// data.addMemoryAndJobs(message.getMemory(), - 1);

			data.memory += memUsed;
			data.jobs --;

			// ja que mudamos no worker individual, aproveita-se tbm para mudar o acumulador nas threads
			// nao sei se faz diferenca ser aqui ou fazer um unlock depois, nao pensei muito bem nisto mas vai dar ao mesmo acho eu
			data.ownerThread.addMemoryAndJobs(new OcupationData(memUsed, -1));
			
			data.workerLock.writeLock().unlock(); // unlocked here since no other changes will be made and the push itself will block
			System.out.println("Client " + message.getClient() + " message " + message.getMessage().getRequestN() + " returned from worker " + data.ID + ", pushing");
			inputBufferWorker.push(message.clone());

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public OcupationData getOcupationData() {
		OcupationData data = new OcupationData(0, 0);

		for (ThreadWorkerInfo thread : threadWorkerInfo) {
			thread.readMemoryAndJobs(data);
		}

		return data;
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
					// mas a de input vai criar a de output
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

		// criar threadpool de distribuir trabalho para workers
		Thread schedulerThread;
		for (int i = 0; i < threadWorkerInfo.length; i++) {
			schedulerThread = new Thread(new SchedulerThreadRunnable(threadWorkerInfo[i], this.inputBufferClient));
			schedulerThread.start();
		}

		Thread clientDispatcherThread;
		for (int i = 0; i < ClientDispatcherThreads; i++) {
			// algo de errado não está certo, ambas as duas funções recebem o mesmo buffer?????
			clientDispatcherThread = new Thread(new ClientDispatcherThread(this, this.inputBufferWorker));
			clientDispatcherThread.start();
		}

		// vou correr workers aqui em vez de numa thread, assim servidor bloqueia em vez de ficar no background
		WorkerLoop workerloop = new WorkerLoop(socketToWorkers, this);
		workerloop.run();
	}

    public static void main( String[] args ) {
		Server server = new Server();
		System.out.println("Starting server");
		server.mainLoop();
    }
}
