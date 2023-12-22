package grupo49;

import java.io.DataOutputStream;
import java.io.IOException;

// nunca fecha a socket
public class AnswerClientOutput implements Runnable {

    private DataOutputStream out;
	private BoundedBuffer<StCMsg> outputBuffer;
    private int clientId;

    public AnswerClientOutput(DataOutputStream out, BoundedBuffer<StCMsg> outputBuffer, int clientID) {
		this.out = out;
		this.outputBuffer = outputBuffer;
        this.clientId = clientID;
	}

    @Override
    public void run() {
        try {
            while (true) {
				StCMsg message = outputBuffer.pop();
				message.serialize(out);
                System.out.println("Message " + message.getRequestN() + " sent to " + clientId + " socket");
			}
            
        } catch (IOException e) {
            e.printStackTrace();
			System.out.println("ERROR");
			// System.exit(1);
		} catch (InterruptedException e) {
			// do nothing, got terminated from parent thread, just exit
        }
    }
}
