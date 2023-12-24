package grupo49;

import java.util.Arrays;

public class SchedulerThreadRunnable implements Runnable {
	private ThreadWorkerInfo workers;
	private BoundedBuffer<ClientMessage<CtSMsg>> inputBuffer;
	int i; // debug

	public SchedulerThreadRunnable(ThreadWorkerInfo workers, BoundedBuffer<ClientMessage<CtSMsg>> inputBuffer, int i) {
		this.workers = workers;
		this.inputBuffer = inputBuffer;
		this.i = i;
	}

	public static final int SchedulerMessageBufferSize = 10;
	public static final int MaxPriority = 5;

	public static class PriorityMessage {
		int priority;
		ClientMessage<StWMsg> message;
		int memory; // para evitar confusoes

		PriorityMessage(int priority, ClientMessage<StWMsg> message, int memory) {
			this.priority = priority;
			this.message = message;
			this.memory = memory;
		}
	}

	// os tipos de mensagens qnao fui eu que fiz, nao conseguia meter isto mais confuso se tentasse de proposito
	// literalmente tive de guardar a memoria a parte porque e impossivel ir busca la a mensagem de output
	// que confusao desnecessaria
	@Override
	public void run() {
		ClientMessage<CtSMsg> inputMessage = null;
		ClientMessage<StWMsg> outputMessage = null;
		
		// como mensagens status são processadas noutro lado, aqui já se sabe que innerMsg será CtsExecMsg
		PriorityMessage messageBuffer[] = new PriorityMessage[SchedulerMessageBufferSize];
		int size = 0;

		// usa o pop especial quando a queue tem elementos, para se livrar das coisas que ja tem
		// adicionar novo elemento a queue, prioridade 0
		// sort da queue por prioridade
		// atribuir, pela prioridade, ao worker menos ocupado em que couber
		// se nao coube, prioridade ++
		// se prioridade > N, mandar de qualquer das formas ao melhor worker (ele em principio nao vai estar entupido e continua tudo a dar)

		try {
			while (true) {
				if (size == 0) { // queue nao tem nada, temos de esperar no pop

					inputMessage = inputBuffer.pop();
					System.out.println("Scheduler " + i + ": received request " + inputMessage.getMessage().getRequestN() + " from client " + inputMessage.getClient());

					// converter para mensagem de output
					CtSExecMsg execMsg = (CtSExecMsg) inputMessage.getMessage();
					StWMsg outMsg = new StWExecMsg(execMsg.getRequestN(), execMsg.getMem(), execMsg.getData());
					outputMessage = new ClientMessage<StWMsg>(inputMessage.getClient(), outMsg);

					messageBuffer[size] = new PriorityMessage(0, outputMessage.clone(), execMsg.getMem());
					size++;
				} else if (size >= SchedulerMessageBufferSize) { // queue cheia, nao fazemos pop sequer

					// nao se faz nada.....
				} else { // temos coisas mas nao temos a queue cheia. tentamos dar pop, se nao der imediatamente entao processamos o que temos

					inputMessage = inputBuffer.pop_noBlock();
					if (inputMessage != null) {
						System.out.println("Scheduler " + i + ": received request " + inputMessage.getMessage().getRequestN() + " from client " + inputMessage.getClient());
	
						// converter para mensagem de output
						CtSExecMsg execMsg = (CtSExecMsg) inputMessage.getMessage();
						StWMsg outMsg = new StWExecMsg(execMsg.getRequestN(), execMsg.getMem(), execMsg.getData());
						outputMessage = new ClientMessage<StWMsg>(inputMessage.getClient(), outMsg);
	
						messageBuffer[size] = new PriorityMessage(0, outputMessage.clone(), execMsg.getMem());
						size++;
					}
				}

				// sort por prioridade, depois por memoria ao contrario. menos prioridade e menos memoria fica primeiro
				Arrays.sort(messageBuffer, 0, size, (a,b) -> { 
					int res;
					res = b.priority - a.priority;
					if (res == 0) {
						res = b.memory - a.memory;
					}
					return res;
				}); // pesado o sort ficar aqui mas funcemina


				
				try {
					workers.arrayLock.lock();
					
					int j;
					PriorityMessage message;
					for (j = 0; j < size; j++) {
						message = messageBuffer[j];
						workers.arr.sort((wa,wb) -> wb.memory - wa.memory); // sort por memoria livre. mais memoria livre fica primeiro

						if (message.priority >= MaxPriority) {
							// forcar a mandar para o melhor worker
							// workers.arr[0].sendJob(message.message);
							// pode fazer com que memoria fique negativa mas nao faz mal
							workers.forceDispatchToBestWorker(message.message, message.memory);
							// shift para a esquerda de todos os elementos, tive preguica de inverter todos os sorts e loops
							size--;
							for (int k = j; k < size; k++) {
								messageBuffer[k] = messageBuffer[k + 1];
							}
							j--;

						} else {
							// encontrar first match e mandar
							Boolean sent = workers.tryDispatchToBestWorker(message.message, message.memory); // decrementa dados sobre este scheduler, se for preciso
							if (sent == false) {
								// nao encontramos, aumentar prioridade
								message.priority++;
							} else {
								// shift para a esquerda de todos os elementos, tive preguica de inverter todos os sorts e loops
								size--;
								for (int k = j; k < size; k++) {
									messageBuffer[k] = messageBuffer[k + 1];
								}
								j--;
							}
						}
					}
				} finally {
					workers.arrayLock.unlock(); // cuidado com isto para nao empancar tudo quando quero dar push e o push bloqueia!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				}

			}
		} catch (InterruptedException e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
}
