package grupo49;

public class WorkerWorkRunnable implements Runnable {
	BoundedBuffer<ClientMessage<StWMsg>> inputBuffer;
	BoundedBuffer<ClientMessage<WtSMsg>> outputBuffer;

	public WorkerWorkRunnable(BoundedBuffer<ClientMessage<StWMsg>> inputBuffer, BoundedBuffer<ClientMessage<WtSMsg>> outputBuffer) {
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
