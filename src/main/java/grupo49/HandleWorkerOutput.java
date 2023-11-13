package grupo49;

import java.io.DataOutputStream;

public class HandleWorkerOutput implements Runnable {
	private DataOutputStream out;
	private BoundedBuffer<...> outputBuffer;

	public HandleWorkerOutput(DataOutputStream out, BoundedBuffer<...> outputBuffer) {
		this.out = out;
		this.outputBuffer = outputBuffer;
	}

	@Override
	public void run() {

	}
}
