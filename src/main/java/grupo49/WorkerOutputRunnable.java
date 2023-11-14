package grupo49;

import java.io.DataOutputStream;
import java.io.IOException;

// pop do buffer e escrever para a stream
public class WorkerOutputRunnable implements Runnable {
	DataOutputStream out;
	BoundedBuffer<ClientMessage<StCMsg>> outputBuffer;

	public WorkerOutputRunnable(DataOutputStream out, BoundedBuffer<ClientMessage<StCMsg>> outputBuffer) {
		this.out = out;
		this.outputBuffer = outputBuffer;
	}

	@Override
	public void run() {
		try {
            while (true) {
				ClientMessage<StCMsg> message = outputBuffer.pop();
				message.serialize(out);
			}
        } catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
