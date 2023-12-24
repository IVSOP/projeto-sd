package grupo49;

import java.io.DataOutputStream;
import java.io.IOException;

// tries to send latest message to worker
// if it does not fit (worker out of memory etc, it will block)
// case for worker NEVER having enough memory because job is too big is not considered

public class HandleWorkerOutput implements Runnable {
	private DataOutputStream out;
	private WorkerData data;

	public HandleWorkerOutput(DataOutputStream out, WorkerData data) {
		this.out = out;
		this.data = data;
	}

	@Override
	public void run() {
		try {
			while(true) {
				ClientMessage<StWMsg> msg = data.outputBuffer.pop();
				System.out.println("Scheduler-WorkerOutput: popped request " + msg.getMessage().getRequestN() + " from client " + msg.getClient() + " to worker " + data.ID);

				int memory = ((StWExecMsg) msg.getMessage()).getMemUsed(); // cast manhoso, nao quero saber, OOP moment, em C nao acontecia isto

				System.out.println("Scheduler-WorkerOutput: trying to fit request " + msg.getMessage().getRequestN() + " from client " + msg.getClient() + " to worker " + data.ID);

				// wait until worker can accept this job. waken up everytime it completes one, not perfect but works
				try {
					data.workerLock.lock();
					System.out.println("Scheduler-WorkerOutput: got the lock trying to fit request " + msg.getMessage().getRequestN() + " from client " + msg.getClient() + " to worker " + data.ID);
					while (data.memory <= memory) { // <= ou so < para garantir que cabe no worker??
						System.out.println("Scheduler-WorkerOutput: sleeping trying to fit request " + msg.getMessage().getRequestN() + " from client " + msg.getClient() + " to worker " + data.ID + ". Need " + memory + ", worker has " + data.memory);
						data.finishedJobCondition.await();
					}
					System.out.println("Scheduler-WorkerOutput: got through trying to fit request " + msg.getMessage().getRequestN() + " from client " + msg.getClient() + " to worker " + data.ID);

					data.memory -= memory;
					data.jobs ++;
				} finally {
					data.workerLock.unlock();
				}


				msg.serialize(out);
				System.out.println("Scheduler-WorkerOutput: sent request " + msg.getMessage().getRequestN() + " from client " + msg.getClient() + " to worker " + data.ID);


				System.out.println("Scheduler-WorkerOutput: trying to update owner for request " + msg.getMessage().getRequestN() + " from client " + msg.getClient() + " to worker " + data.ID);
				// updating this worker's owner
				try {
					data.ownerThread.memoryAndJobsLock.writeLock().lock();
					data.ownerThread.totalMemoryRemaining -= memory;
					data.ownerThread.totalPendingJobs ++;
				} finally {
					data.ownerThread.memoryAndJobsLock.writeLock().unlock();
				}

				System.out.println("Scheduler-WorkerOutput: upadted owner for request " + msg.getMessage().getRequestN() + " from client " + msg.getClient() + " to worker " + data.ID);
			}
		} catch (InterruptedException e) {
			// do nothing, got terminated from parent thread, just exit
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
