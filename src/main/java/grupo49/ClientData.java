package grupo49;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// esta classe age apenas como forma de guardar dados que nunca serao alterados
// nao consideramos necessario meter tudo private e com getters e setters

// contem buffer de output do cliente, bem como as locks e conditions para o servidor as aceder
// podemos pensar como se fosse uma struct partilhada pelo servidor e os runnables de atender clientes
public class ClientData {
	// meter isto como final????
	public String password;
	public int ID;
	// ID continua a ser preciso nuns casos especificos
	public String name;

	// lock e condition para termos permissao para dar push no input buffer do servidor
	public ReentrantLock serverPushLock;
		public Condition permissionToPush;
		public int n_currentJobs; // numero de jobs que o cliente tem:
		// assim que entram para input buffer para SV, ++
		// quando entram para output buffer para Cliente, --
		// NOTA IMPORTANTE: n_currentJobs nao precisa de locks para ler, pois pequenos desvios sao aceitaveis
		// para escrever, usar a serverPushLock

		// OUTRA NOTA: buffer fica == null quando client esta logged out
		// aproveitamos a lock que existe acima para nao acontecer nada de mal noutra thread quando fazemos isso
		public BoundedBuffer<StCMsg> outputBuffer;

	public ClientData(String name, String password, int ID) {
		this.password = password;
		this.ID = ID;
		this.name = name;
		this.serverPushLock = new ReentrantLock();
		this.permissionToPush = serverPushLock.newCondition();
		this.n_currentJobs = 0;
		this.outputBuffer = new BoundedBuffer<>(Server.localOutputBufferClientSize);
	}
	
	// para poupar memoria
	public void createOutputBuffer() {
		this.outputBuffer = new BoundedBuffer<>(Server.localOutputBufferClientSize);
	}

	// para poupar memoria
	// just sets it to null
	public void removeOutputBuffer() {
		this.serverPushLock.lock();
		this.outputBuffer = null;
		this.serverPushLock.unlock();
	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id " + this.ID);
        sb.append("name: +" + this.name);
		sb.append("password" + this.password);
        return sb.toString();
    }
}
