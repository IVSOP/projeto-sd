package grupo49;

import java.io.DataOutputStream;

public class WorkerOutputRunnable implements Runnable {
	DataOutputStream out;
	BoundedBuffer<ClientMessage<WtSMsg>> outputBuffer;

	public WorkerOutputRunnable(DataOutputStream out, BoundedBuffer<ClientMessage<WtSMsg>> outputBuffer) {
		this.out = out;
		this.outputBuffer = outputBuffer;
	}

	@Override
	public void run() {
		// pop do buffer e escrever para a stream..................................
	}
}
