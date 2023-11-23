package grupo49;

public class WorkerWorkRunnable implements Runnable {
	BoundedBuffer<ClientMessage<StWMsg>> inputBuffer;
	BoundedBuffer<ClientMessage<StCMsg>> outputBuffer;

	public WorkerWorkRunnable(BoundedBuffer<ClientMessage<StWMsg>> inputBuffer, BoundedBuffer<ClientMessage<StCMsg>> outputBuffer) {
		this.inputBuffer = inputBuffer;
		this.outputBuffer = outputBuffer;
	}

	@Override
	public void run() {
		try {
			while (true) {

				o que e que se esta a passar aqui? esta thread devia simplemente fazer trabalho e enviar a resposta, porque e que no fim tamos a usar um StCMsg????
				ClientMessage<StWMsg> inputMsg = inputBuffer.pop();

				// não há mais mensagens StW por isso não é preciso switch com opcode, para já pelo menos
				StWExecMsg msg = (StWExecMsg) inputMsg.getMessage(); 
				byte[] data = msg.getData();
				int requestN = msg.getRequestN();

				// do work with previous values
				
				StCMsg innerMsg = null;
				
				//se work result deu erro
				// finalMsg = new StCErrorMsg(requestN, errorMsg);
				// //se work não deu erro
				// finalMsg = new StCExecMsg(requestN, newData);

				ClientMessage<StCMsg> finalMsg = new ClientMessage<>(inputMsg.getClient(), innerMsg);
				this.outputBuffer.push(finalMsg);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
