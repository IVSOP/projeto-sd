package grupo49;

// le do buffer e manda para a socket
public class ClientOutputRunnable implements Runnable {
	DataOutputStream out;
	BoundedBuffer<...> outputBuffer;

	public ClientOutputRunnable(DataOutputStream out, BoundedBuffer<...> outputBuffer) {
		this.out = out;
		this.outputBuffer = outputBuffer;
	}

	@Override
	public void run() {
		
	}
}
