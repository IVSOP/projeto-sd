package grupo49;

import java.net.UnknownHostException;
import java.util.Scanner;

import javax.sound.midi.SysexMessage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ClientUI {
	private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) throws UnknownHostException, InterruptedException {

		//register/login loop
		Client client = authenticateClient();
		
		Thread receiveThread = new Thread(() -> {
			StCMsg message;
			try {
				while (true) {
					message = client.getNextAnswer();
					System.out.println("Message " + message.getRequestN() + " arrived, writing to file");
					writeToFile(message);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		receiveThread.start();

		// loop para permitir enviar pedidos
		String input = askForInput("Actions available:\n1. Execution request\n2. Service status\n");
		while (true) {

			if (input.equals("1")) { // Pedido de execução
				// pedir nome do ficheiro de input
				// se for para dar submit e buffer de output estiver cheio, vai bloquear
				String filePath = askForInput("Enter the file path for job input: ");
				int memNeeded = readMemFromFile(filePath);
				byte[] requestMsg = readInputBytesFromFile(filePath);
				client.sendExecMsg(memNeeded,requestMsg);
			}
			else if (input.equals("2")) { // Pedido de estado
				client.sendStatusMsg();
			}
			else { // Se user meter outro input qualquer
				System.out.println("Invalid choice. Please enter '1' for \"Execution request\" or '2' for \"Service Status\"");
			}
	}
}

	private static String askForInput(String msg) {
		System.out.print(msg);
		return scanner.nextLine();
	}

	private static Client authenticateClient() {
		String serverAddress = askForInput("Enter server IP: ");
		// receber address do terminal (so apra evitar conflitos como ta tudo em localhost)
		// String localAddress = String.valueOf(InetAddress.getLocalHost()); // cursed
		String localAddress = askForInput("Enter local IP: ");
		// receber nome e password do terminal
		String username = askForInput("Enter your username: ");
		String password = askForInput("Enter your password: ");
		// criar cliente
		Client client = new Client(serverAddress, localAddress, username, password);

		//Escolha entre register e login
		String choice = askForInput("Do you want to (R)egister or (L)ogin? ").toUpperCase();

		boolean authSuccessful = false;
		while (!authSuccessful) {
			try {
				if (choice.equals("R")) { // register
					authSuccessful = client.registerClient();
					if (!authSuccessful) {
						System.out.println("Register failed. Username already in use.");
					}
					else {
						authSuccessful = true;
						System.out.println("Registered client successfully");
					}

				} else if (choice.equals("L")) { //login
					authSuccessful = client.loginClient();
					if (!authSuccessful) {
						System.out.println("Login failed. Client not registered, or password doesn't match");
					} else {
						authSuccessful = true;
						System.out.println("Logged in client successfully");
					}

				} else { // Se user meter outro input qualquer
					System.out.println("Invalid choice. Please enter 'R' for registration or 'L' for login.");
				}
			} catch (InterruptedException e) {
				// o que fazer aqui??
				e.printStackTrace();
			} catch (IOException e) {
				// o que fazer aqui??
				e.printStackTrace();
			}
		}
		return client;
	}

	private static int readMemFromFile(String filePath) {
        int memNeeded = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
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
			// Skip 1ª linha
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
            writer.write(message.toString());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
