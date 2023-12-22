package grupo49;

import java.io.DataOutputStream;
import java.io.IOException;

public class HandleWorkerOutput implements Runnable {
	private DataOutputStream out;
	private BoundedBuffer<ClientMessage<StWMsg>> outputBuffer;
	int id; // debug

	public HandleWorkerOutput(DataOutputStream out, BoundedBuffer<ClientMessage<StWMsg>> outputBuffer, int id) {
		this.out = out;
		this.outputBuffer = outputBuffer;
		this.id = id;
	}

	@Override
	public void run() {
		try {
			while(true) {
				ClientMessage<StWMsg> msg = outputBuffer.pop();
				msg.serialize(out);
				System.out.println("Scheduler-WorkerOutput: sent request " + msg.getMessage().getRequestN() + " from client " + msg.getClient() + " to worker " + id);
			}
		} catch (InterruptedException e) {
			// do nothing, got terminated from parent thread, just exit
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
