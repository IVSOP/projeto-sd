package grupo49;

import java.io.DataOutputStream;
import java.io.IOException;

// le do buffer e manda para a socket
public class ClientOutputRunnable implements Runnable {
	DataOutputStream out;
	BoundedBuffer<CtSMsg> outputBuffer;

	public ClientOutputRunnable(DataOutputStream out, BoundedBuffer<CtSMsg> outputBuffer) {
		this.out = out;
		this.outputBuffer = outputBuffer;
	}

	@Override
	public void run() {
		try {
            while (true) {
				CtSMsg message = outputBuffer.pop();
				message.serialize(out);
			}
        } catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
