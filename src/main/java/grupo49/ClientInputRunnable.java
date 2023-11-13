package grupo49;

import java.io.DataInputStream;

// le da socket, mete no input buffer
public class ClientInputRunnable implements Runnable {
	public DataInputStream in;
	public BoundedBuffer<...> inputBuffer;

	public ClientInputRunnable(DataInputStream in, BoundedBuffer<...> inputBuffer) {
		this.in = in;
		this.inputBuffer = inputBuffer;
	}

	@Override
	public void run() {
		
	}
}
