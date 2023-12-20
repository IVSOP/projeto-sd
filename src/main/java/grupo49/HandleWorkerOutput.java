package grupo49;

import java.io.DataOutputStream;
import java.io.IOException;

public class HandleWorkerOutput implements Runnable {
	private DataOutputStream out;
	private BoundedBuffer<ClientMessage<StWMsg>> outputBuffer;

	public HandleWorkerOutput(DataOutputStream out, BoundedBuffer<ClientMessage<StWMsg>> outputBuffer) {
		this.out = out;
		this.outputBuffer = outputBuffer;
	}

	@Override
	public void run() {
		try {
			while(true) {
				ClientMessage<StWMsg> msg = outputBuffer.pop();
				msg.serialize(out);
			}
		} catch (InterruptedException e) {
			// do nothing, got terminated from parent thread, just exit
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
