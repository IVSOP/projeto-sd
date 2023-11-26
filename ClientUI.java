package grupo49;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientUI {
	public static void main(String[] args) throws UnknownHostException, InterruptedException {
		String serverAddress = "localhost";
		// receber address do terminal (so apra evitar conflitos como ta tudo em localhost)
		String localAddress = String.valueOf(InetAddress.getLocalHost());
		// receber nome e password do terminal
		String username = getUsernameFromTerminal();
		String password = getPasswordFromTerminal();
		// criar cliente
		Client client = new Client(serverAddress, localAddress, username, password);
		// loop para permitir enviar pedidos
		while (true) {
			// perguntar se quer dar submit request ou ler uma resposta
			displayUserOptions();
			int choice = getUserChoice();

			switch (choice) {
				case 1:
					// Submit request
					// se for para dar submit e buffer de output estiver cheio, vai bloquear
					if(client.outputBuffer.size == maxSize()){ //????
						System.out.print("Buffer is full.");
						continue;
					}
					else{
						String filePath = promptForFilePath();
						ClientMessage<StWMsg> requestMsg = readClientMessageFromFile(filePath);
						client.outputBuffer.push((CtSMsg) requestMsg);
					}
					break;
				case 2:
					// se client.inputBuffer.size == 0 (vazio) entao nao ha respostas, voltar a perguntar
					if (client.inputBuffer.size == 0){
						System.out.print("No responses available");
						continue;
					}
					else{
						// se houverem respostas, mostrar todas
						client.inputBuffer.printBufferContents(); //????
					}
					break;
				default:
					System.out.println("Invalid choice. Please try again.");
			}
		}
			// para evitar deadlock, nao deixamos que mensagem seja enviada? ainda que depois nao seja facil de reenviar
	}

	private static String getUsernameFromTerminal() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter your username: ");
		return scanner.nextLine();
	}

	private static String getPasswordFromTerminal() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter your password: ");
		return scanner.nextLine();
	}

	private static void displayUserOptions() {
		System.out.println("Choose an option:");
		System.out.println("1. Submit Request");
		System.out.println("2. Read Responses");
	}

	private static int getUserChoice() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter your choice: ");
		int choice = scanner.nextInt();
		scanner.nextLine();
		return choice;
	}

	private static String promptForFilePath() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter the file path: ");
		return scanner.nextLine();
	}
	private static ClientMessage<StWMsg> readClientMessageFromFile(String filePath){

	}
}
