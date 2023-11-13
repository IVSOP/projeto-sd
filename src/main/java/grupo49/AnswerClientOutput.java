package grupo49;

import java.io.DataOutputStream;
import java.io.IOException;

// nunca fecha a socket
public class AnswerClientOutput implements Runnable {

    private DataOutputStream out;
	private BoundedBuffer<StCMsg> outputBuffer;

    public AnswerClientOutput(DataOutputStream out, BoundedBuffer<StCMsg> outputBuffer) {
		this.out = out;
		this.outputBuffer = outputBuffer;
	}

    @Override
    public void run() {
        try {
            while (true) {
				StCMsg message = outputBuffer.pop();

				message.serialize(out);
				// out.flush();
			}
            
        } catch (IOException e) {
            e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
        } finally {

		}
    }
}
