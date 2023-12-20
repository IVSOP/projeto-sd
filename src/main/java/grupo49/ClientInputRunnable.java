package grupo49;

import java.io.DataInputStream;
import java.io.IOException;

// le da socket, mete no input buffer
public class ClientInputRunnable implements Runnable {
	public DataInputStream in;
	public BoundedBuffer<StCMsg> inputBuffer;

	public ClientInputRunnable(DataInputStream in, BoundedBuffer<StCMsg> inputBuffer) {
		this.in = in;
		this.inputBuffer = inputBuffer;
	}

	@Override
	public void run() {
		try {
			while (true) {
				StCMsg message = null;
				byte opcode = in.readByte();
				switch(opcode){
					case 0:
						message = new StCExecMsg();
						message.deserialize(in);
						break;
					case 1:
						message = new StCErrorMsg();
						message.deserialize(in);
						break;
					case 2:
						message = new StCStatusMsg();
						message.deserialize(in);
						break;
				}
				this.inputBuffer.push(message);
			}
            
		} catch (IOException e) {
			System.out.println("Is server closed? Shutting down");
			System.exit(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
}
