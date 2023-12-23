package grupo49;

import java.io.DataOutputStream;
import java.io.IOException;

// pop do buffer e escrever para a stream
public class WorkerOutputRunnable implements Runnable {
	DataOutputStream out;
	BoundedBuffer<ClientMessage<WtSMsg>> outputBuffer;

	public WorkerOutputRunnable(DataOutputStream out, BoundedBuffer<ClientMessage<WtSMsg>> outputBuffer) {
		this.out = out;
		this.outputBuffer = outputBuffer;
	}

	@Override
	public void run() {
		try {
            while (true) {
				ClientMessage<WtSMsg> message = outputBuffer.pop();
				System.out.println("Sending request " + message.getMessage().getRequestN() + " to client " + message.getClient());
				message.serialize(out);
				System.out.println("Sent request " + message.getMessage().getRequestN() + " to client " + message.getClient());
			}
        } catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
