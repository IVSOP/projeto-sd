package grupo49;

import java.util.concurrent.locks.ReentrantReadWriteLock;

// tudo public, explicacao igual a do ClientData
public class WorkerData {
	public BoundedBuffer<ClientMessage<StWMsg>> outputBuffer;
	public int ID;
	public ReentrantReadWriteLock workerLock; // para evitar conflitos, porque varias threads mexem na memory e nos jobs
		public int memory;
		public int jobs; // jobs que ele esta a fazer atualmente
	public ThreadWorkerInfo ownerThread; // util para query de ocupacao, eu sei que e feio e redundante

	public WorkerData(int ID, int memory, ThreadWorkerInfo ownerThread) {
		this.ID = ID;
		this.outputBuffer = new BoundedBuffer<>(Server.localOutputBufferWorkerSize);
		this.workerLock = new ReentrantReadWriteLock();
		this.memory = memory;
		this.jobs = 0;
		this.ownerThread = ownerThread;
	}

	// ja que a lock e a mesma, fazer tudo de uma vez
	// acabou por nunca ser usada, ver Server::pushInputBufferWorker
	// public void addMemoryAndJobs(int memoryDelta, int jobsDelta) {
	// 	try {
	// 		workerLock.writeLock().lock();
	// 		this.memory += memoryDelta;
	// 		this.jobs += jobsDelta;
	// 	} finally {
	// 		workerLock.writeLock().unlock();
	// 	}
	// }
}
