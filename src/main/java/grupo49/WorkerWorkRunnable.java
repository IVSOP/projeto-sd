package grupo49;

public class WorkerWorkRunnable implements Runnable {
	BoundedBuffer<...> inputBuffer;
	BoundedBuffer<...> outputBuffer;

	public WorkerWorkRunnable(BoundedBuffer<...> inputBuffer, BoundedBuffer<...> outputBuffer) {
		this.inputBuffer = inputBuffer;
		this.outputBuffer = outputBuffer;
	}

	@Override
	public void run() {
		while (true) {
			// pop do input buffer e fazer trabalho. meter resposta no outputBuffer
		}
	}
}
