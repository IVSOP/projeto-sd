package grupo49;

import java.io.DataOutputStream;

// nunca fecha a socket
public class AnswerClientOutput implements Runnable {

    private DataOutputStream out;
	private BoundedBuffer<ClientMessage<IMessage>> outputBuffer;

    public AnswerClientOutput(DataOutputStream out, BoundedBuffer<ClientMessage<IMessage>> outputBuffer) {
		this.out = out;
		this.outputBuffer = outputBuffer;
	}

    @Override
    public void run() {
        try {
            while (true) {
				ClientMessage<IMessage> message = outputBuffer.pop();

				message.deserialize(out);
				// out.flush();
			}
            
        // } catch (IOException e) {
            // e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
        } finally {

		}
    }
}
