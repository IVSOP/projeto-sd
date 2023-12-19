package grupo49;

import java.io.DataInputStream;

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
            
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
