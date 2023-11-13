package grupo49;

public class WorkerOutputRunnable implements Runnable {
	DataOutputStream out;
	BoundedBuffer<...> outputBuffer;

	public WorkerOutputRunnable(DataOutputStream out, BoundedBuffer<...> outputBuffer) {
		this.out = out;
		this.outputBuffer = outputBuffer;
	}

	@Override
	public void run() {
		// pop do buffer e escrever para a stream..................................
	}
}
