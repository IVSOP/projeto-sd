package grupo49;

import sd23.*; // n dá depois

public class WorkerWorkRunnable implements Runnable {
	BoundedBuffer<ClientMessage<StWMsg>> inputBuffer;
	BoundedBuffer<ClientMessage<WtSMsg>> outputBuffer;

	public WorkerWorkRunnable(BoundedBuffer<ClientMessage<StWMsg>> inputBuffer, BoundedBuffer<ClientMessage<WtSMsg>> outputBuffer) {
		this.inputBuffer = inputBuffer;
		this.outputBuffer = outputBuffer;
	}

	@Override
	public void run() {
		try {
			while (true) {

				ClientMessage<StWMsg> inputMsg = inputBuffer.pop();

				// não há mais mensagens StW por isso não é preciso switch com opcode
				StWExecMsg msg = (StWExecMsg) inputMsg.getMessage(); 

				WtSMsg innerMsg = null;

				// tarefa do ficheiro
				byte[] job = msg.getData(); 

				try {

					// executar a tarefa
					byte[] output = JobFunction.execute(job);

					//System.err.println("success, returned "+output.length+" bytes");
					innerMsg = new WtSExecMsg(msg.getRequestN(), msg.getMemUsed(), output); //se resultado correu bem
					System.out.println("Succcessfully processed task number " + msg.getRequestN() + " for client " + inputMsg.getClient());

				} catch (JobFunctionException e) { //se resultado correu mal
					//System.err.println("job failed: code="+e.getCode()+" message="+e.getMessage());
					innerMsg = new WtSErrorMsg(msg.getRequestN(), msg.getMemUsed(), "job failed: code="+e.getCode()+" message="+e.getMessage());

					System.out.println("Error processing task number "  + msg.getRequestN() + " for client " + inputMsg.getClient());
				}

				ClientMessage<WtSMsg> finalMsg = new ClientMessage<>(inputMsg.getClient(), innerMsg);
				System.out.println("Trying to push task " + msg.getRequestN() + " for client " + inputMsg.getClient());
				this.outputBuffer.push(finalMsg.clone());
				System.out.println("Pushed task " + msg.getRequestN() + " for client " + inputMsg.getClient());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}