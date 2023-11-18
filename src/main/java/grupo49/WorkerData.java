package grupo49;

import java.util.concurrent.locks.ReentrantReadWriteLock;

// tudo public, explicacao igual a do ClientData
public class WorkerData {
	public int memory;
	public BoundedBuffer<ClientMessage<StWMsg>> outputBuffer;
	public int ID;
	public ReentrantReadWriteLock memoryLock; // para evitar conflitos, porque varias threads mexem na memory

	WorkerData(int ID, int memory) {
		this.ID = ID;
		this.memory = memory;
		this.outputBuffer = new BoundedBuffer<>(Server.localOutputBufferWorkerSize);
		this.memoryLock = new ReentrantReadWriteLock();
	}
}
