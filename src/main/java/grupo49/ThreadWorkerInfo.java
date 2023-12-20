package grupo49;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadWorkerInfo {
	public ReentrantLock arrayLock; // threads do scheduler e threads de assignment mexem nisto, precisa de lock
		public ArrayList<WorkerData> arr;
	// nao e readwrite porque nunca se usa para reads
	// usada para:
	//		sort da lista (muda ordem)
	// 		adicionar novos elementos (pode mudar onde os dados estao?)

	// para a query de ocupacao de servico ser mais rapida
	private ReentrantReadWriteLock memoryAndJobsLock;
		int totalMemoryRemaining; 
		int totalPendingJobs;

	public ThreadWorkerInfo() {
		this.arr = new ArrayList<>();
		this.arrayLock = new ReentrantLock();
		this.totalMemoryRemaining = 0;
		this.memoryAndJobsLock = new ReentrantReadWriteLock();
	}

	public void addWorker(WorkerData data) {
		try {
			arrayLock.lock();
			memoryAndJobsLock.writeLock().lock();
			arr.add(data);
			totalMemoryRemaining += data.memory;
		} finally {
			memoryAndJobsLock.writeLock().unlock();
			arrayLock.unlock();
		}
	}

	// adds to this
	public void addMemoryAndJobs(Server.OcupationData data) {
		try {
			memoryAndJobsLock.writeLock().lock();
			this.totalPendingJobs += data.current_jobs;
			this.totalMemoryRemaining += data.memory_remaining;
		} finally {
			memoryAndJobsLock.writeLock().unlock();
		}
	}

	// adds to the received data
	// o nome disto e mesmo muito mau mas ao menos nao fica parecido com a de cima
	public void readMemoryAndJobs(Server.OcupationData data) {
		try {
			memoryAndJobsLock.readLock().lock();
			data.current_jobs += this.totalPendingJobs;
			data.memory_remaining += this.totalMemoryRemaining;
		} finally {
			memoryAndJobsLock.readLock().unlock();
		}
	}

	// will sort according to number of jobs being executed
	// memory is not considered, since it is not directly related to completion speed of a job
	// this is 100% arbitrary but can easily be changed to only worry about memory
	// NOTA IMPORTANTE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	// sort nao usa locks (sem ser lock global)
	// porque memory e jobs de um worker:
	//		so podem ser incrementados pela mesma thread que deu sort
	// 		podem ser decrementados por outra thread qualquer
	// mas decrementar valor significa que solucao encontrada pelo sort nunca esta 'errada',
	// assim temos uma aproximacao que deve ser razoavel sem ter uma zona critica gigante
	// public void sortWorkers() {
	// 	try {
	// 		lock.lock();
	// 		this.arr.sort((a, b) -> a.jobs - b.jobs);
	// 	} finally {
	// 		lock.unlock();
	// 	}
	// }
	// (acabou por nao ser usado porque assim em baixo fazemos tudo de uma vez e aproveitamos o facto de ja termos a lock)


	// ler explicacao acima para falta de locks
	public void dispatchToBestWorker(ClientMessage<StWMsg> outputMessage, int memory) {
		try {
			arrayLock.lock();
			if (arr.size() == 0) {
				System.out.println("This scheduler has no workers, for now this case is not considered, job will go nowhere");
			}
			this.arr.sort((a, b) -> a.jobs - b.jobs);
			for (WorkerData data : arr) {
				// agora ja usamos locks individuais, temos de ter a certeza da memoria disponivel
				// nao usamos locks de read porque podemos ter de alterar o valor, a performance nao deve ser muito diferente, ia ser confuso usar read e write logo a seguir, nem sei como funcemina
				try {
					data.workerLock.writeLock().lock();
					// nao usei changeMemoryAndJobs(), assim aproveito ja o facto de ter a lock feita (muito confuso mas prontos)

					if (data.memory >= memory) { // >= ou so >??
						// worker has been chosen
						data.memory -= memory;
						data.jobs ++;

							// isto podia ser feito em qualquer sitio acho eu, ficou aqui
							try {
								memoryAndJobsLock.writeLock().lock();
								totalMemoryRemaining -= memory;
								totalPendingJobs ++;
							} finally {
								memoryAndJobsLock.writeLock().unlock();
							}
						
						// NOTA: este push em teoria pode bloquear e, assim, mete todos os outros workers desta thread em espera
						// mas como este e um dos melhores workers e incrementamos aqui o jobs, significa que em qualquer outro woker seria necessario esperar
						// podemos ter azar em que escolher outro daria 'unlock' da sua espera mais rapido, mas nao e possivel prever isso, so se fizesse um select() extremament manhoso ou assim
						data.outputBuffer.push(outputMessage);
						System.out.println("Client " + outputMessage.getClient() + " message " + ((StWExecMsg) outputMessage.getMessage()).getRequestN() + " gone to worker " + data.ID);
						break;
					}
				} finally {
					data.workerLock.writeLock().unlock();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			arrayLock.unlock();
		}
	}
}
