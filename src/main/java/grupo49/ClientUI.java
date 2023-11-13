package grupo49;

public class ClientUI {
	public static void main(String args[]) {
		// receber address do terminal (so apra evitar conflitos como ta tudo em localhost)
		// receber email e password do terminal
		// criar cliente
		// loop para permitir enviar pedidos
			// perguntar se quer dar submit request ou ler uma resposta
			// se client.inputBuffer.size == 0 (vazio) entao nao ha respostas, voltar a perguntar
			// se houverem respostas, mostrar todas
			
			// se for para dar submit e buffer de output estiver cheio, vai bloquear
			// para evitar deadlock, nao deixamos que mensagem seja enviada? ainda que depois nao seja facil de reenviar
	}
}
