package grupo49;

import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ClientUI {
	private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) throws UnknownHostException, InterruptedException {
		//Escolha entre register e login
		String choice = askForInput("Do you want to (R)egister or (L)ogin? ").toUpperCase();

		String serverAddress = askForInput("Enter server IP: ");
		// receber address do terminal (so apra evitar conflitos como ta tudo em localhost)
		// String localAddress = String.valueOf(InetAddress.getLocalHost()); // cursed
		String localAddress = askForInput("Enter local IP: ");
		// receber nome e password do terminal
		String username = askForInput("Enter your username: ");
		String password = askForInput("Enter your password: ");
		// criar cliente
		Client client = new Client(serverAddress, localAddress, username, password);


		//???? é para colocar isto dentro do loop de pedidos e criar sendLoginMsg???

		eu nao vou mexer no codigo aqui, vou so explicar depois muda-se
		tu aqui nao tens server
		o que tavamos a pensar e sempre que o cliente liga manda pedido com o nome e password
		se ja existe, simplesmente fica interpretado como login
		nao sei bem como e que isso ficou feito na pratica, nao fiz as mensagens

		if (choice.equals("R")) {
			if (!server.clientExists(username)) {
				// Register
				server.registerClient(username, password);
			} else {
				System.out.println("Username already in use.")
			}
		} else if (choice.equals("L")) {
			if (server.clientExists(username)) {
				// Login
				server.loginClient(username, password);
			} else {
				System.out.println("User doesn't exist.")
			}
		} else {
			System.out.println("Invalid choice. Please enter 'R' for registration or 'L' for login.");
		}
		
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

			String messageType = readMessageTypeFromFile(filePath);

			if ("EXEC".equals(messageType)) {
				int memNeeded = readMemFromFile(filePath);
				byte[] requestMsg = readInputBytesFromFile(filePath);
				client.sendExecMsg(memNeeded,requestMsg);
			}

			else if ("STATUS".equals(messageType)) {
				client.sendExecMsg();
			}
		}
	}

	private static String askForInput(String msg) {
		System.out.print(msg);
		return scanner.nextLine();
	}

	//Supondo que a primeira linha indica o tipo de mensagem
	private static String readMessageTypeFromFile(String filePath) {
        String messageType = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            messageType = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messageType;
    }

	private static int readMemFromFile(String filePath) {
        int memNeeded = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip 1ª linha
            reader.readLine();
            memNeeded = (int) reader.lines()
                .mapToInt(String::length)
                .sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return memNeeded;
    }

	private static byte[] readInputBytesFromFile(String filePath) {
        byte[] fileBytes = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip 1linha
            reader.readLine();
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            fileBytes = content.toString().getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileBytes;
    }

	private static void writeToFile(StCMsg message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt", true))) {
            writer.write(message.getData);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
