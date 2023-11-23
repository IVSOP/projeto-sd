package grupo49;

public class ClientDispatcherThread implements Runnable {
	private Server server;
	private BoundedBuffer<ClientMessage<StCMsg>> inputBuffer;

							// ugly, but passing server allows to use getClient()
	public ClientDispatcherThread(Server server, BoundedBuffer<ClientMessage<StCMsg>> inputBuffer) {
		this.server = server;
		this.inputBuffer = inputBuffer;
	}

	@Override
	public void run() {
		ClientMessage<StCMsg> message;
		StCMsg outMessage;
		int clientID;
		try {
			while (true) {
				message = inputBuffer.pop();

				// sending information to the scheduler threads that control this worker that it has completed a job is not done here but by the input thread itself, its all the same I guess
				// these threads are dumb, just send messages to clients
				fazer coisas para ter uma outMessage................ ou ja ta feita??

				server.pushClientOutput(outMessage); // trata de tudo
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}