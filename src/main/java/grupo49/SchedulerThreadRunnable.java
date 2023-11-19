package grupo49;

public class SchedulerThreadRunnable implements Runnable {
	private ThreadWorkerInfo workers;
	private BoundedBuffer<ClientMessage<StCMsg>> inputBuffer;

	public SchedulerThreadRunnable(ThreadWorkerInfo workers, BoundedBuffer<ClientMessage<StCMsg>> inputBuffer) {
		this.workers = workers;
		this.inputBuffer = inputBuffer;
	}

	@Override
	public void run() {
		ClientMessage<StCMsg> inputMessage;
		ClientMessage<StWMsg> outputMessage;
		int memory;

		try {
			while (true) {
				inputMessage = inputBuffer.pop();

				memory = inputMessage.getMemory();

				// criar mensagem para enviar......................................

				workers.dispatchToBestWorker(outputMessage, memory);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
