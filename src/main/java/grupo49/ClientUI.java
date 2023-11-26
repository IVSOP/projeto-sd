package grupo49;

import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientUI {
	private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) throws UnknownHostException, InterruptedException {
		String serverAddress = askForInput("Enter server IP: ");
		// receber address do terminal (so apra evitar conflitos como ta tudo em localhost)
		// String localAddress = String.valueOf(InetAddress.getLocalHost()); // cursed
		String localAddress = askForInput("Enter local IP: ");
		// receber nome e password do terminal
		String username = askForInput("Enter your username: ");
		String password = askForInput("Enter your password: ");
		// criar cliente
		Client client = new Client(serverAddress, localAddress, username, password);

		// falta login???????????????????????????????? ou client ja faz sozinho????????????????????????????????

		Thread receiveThread = new Thread(() -> {
			StCMsg message;
			try {
				while (true) {
					message = client.getNextAnswer();
					writeToFile(message);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		receiveThread.start();

		// loop para permitir enviar pedidos
		while (true) {
			// pedir nome do ficheiro de input
			// se for para dar submit e buffer de output estiver cheio, vai bloquear
			String filePath = askForInput("Enter the file path: ");
			CtSMsg requestMsg = readClientMessageFromFile(filePath);
			client.sendRequest(requestMsg);
		}
	}

	// private static String getUsernameFromTerminal() {
	// 	System.out.print("Enter your username: ");
	// 	return scanner.nextLine();
	// }

	// private static String getPasswordFromTerminal() {
	// 	System.out.print("Enter your password: ");
	// 	return scanner.nextLine();
	// }

	// private static int getUserChoice() {
	// 	System.out.print("Enter your choice: ");
	// 	int choice = scanner.nextInt();
	// 	scanner.nextLine();
	// 	return choice;
	// }

	// private static String promptForFilePath() {
	// 	System.out.print("Enter the file path: ");
	// 	return scanner.nextLine();
	// }

	private static String askForInput(String msg) {
		System.out.print(msg);
		return scanner.nextLine();
	}

	private static CtSMsg readClientMessageFromFile(String filePath) {
		return null;
	}
}
