package grupo49;

public class SchedulerThreadRunnable implements Runnable {
	private ThreadWorkerInfo workers;
	private BoundedBuffer<ClientMessage<CtSMsg>> inputBuffer;

	public SchedulerThreadRunnable(ThreadWorkerInfo workers, BoundedBuffer<ClientMessage<CtSMsg>> inputBuffer) {
		this.workers = workers;
		this.inputBuffer = inputBuffer;
	}

	@Override
	public void run() {
		ClientMessage<CtSMsg> inputMessage = null;
		ClientMessage<StWMsg> outputMessage = null;

		try {
			while (true) {
				inputMessage = inputBuffer.pop();

				// como mensagens status são processadas noutro lado, aqui já se sabe que innerMsg será CtsExecMsg

				CtSExecMsg execMsg = (CtSExecMsg) inputMessage.getMessage();

				StWMsg outMsg = new StWExecMsg(execMsg.getRequestN(), execMsg.getMem(), execMsg.getData());
				outputMessage = new ClientMessage<StWMsg>(inputMessage.getClient(),outMsg);
				workers.dispatchToBestWorker(outputMessage, execMsg.getMem());
				
				// if (innerMsg instanceof CtSExecMsg) {
				// 	CtSExecMsg execMsg = (CtSExecMsg) innerMsg;

				// 	StWMsg outMsg = new StWExecMsg(execMsg.getRequestN(),execMsg.getData());
				// 	outputMessage = new ClientMessage<StWMsg>(inputMessage.getClient(),outMsg);
				// 	workers.dispatchToBestWorker(outputMessage, execMsg.getMem());
				// }

				// //se mensagem for de status
				// if (innerMsg instanceof CtSStatusMsg) {
				// 	OcupationData ocupation = server.getOcupationData();
				// 	StCStatusMsg statusMsg = new StCStatusMsg(inputMessage.getClient(), ocupation.getMemRemaining(),ocupation.getCurrentJobs());
				// 	// meter no inputBufferserver
					
				// }
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
