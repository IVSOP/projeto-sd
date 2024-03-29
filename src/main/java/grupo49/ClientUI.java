package grupo49;

import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ClientUI {
	public static class MemoryAndBytes { // wrapper para ler tudo de uma vez dum ficheiro
		int memNeeded;
		byte[] requestMsg;

		MemoryAndBytes(int memNeeded, byte[] requestMsg) {
			this.memNeeded = memNeeded;
			this.requestMsg = requestMsg;
		}
	}
	private static Scanner scanner = new Scanner(System.in);
	public static void main(String[] args) throws UnknownHostException, InterruptedException {

		//register/login loop
		Client client = authenticateClient();
		String outputPath = getOutputPath();

		client.startInput();

		Thread receiveThread = new Thread(() -> {
			StCMsg message;
			try {
				while (true) {
					message = client.getNextAnswer();
					System.out.println("Message " + message.getRequestN() + " arrived, writing to file");
					writeToFile(client, message, outputPath);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		receiveThread.start();

		// loop para permitir enviar pedidos
		String input;
		while (true) {
			System.out.println("Actions available:\n1. Execution request\n2. Service status\n");
			input = askForInput("Input:\n");
			if (input.equals("1")) { // Pedido de execução
				// pedir nome do ficheiro de input
				// se for para dar submit e buffer de output estiver cheio, vai bloquear
				String filePath = askForInput("Enter the file path for job input: ");
				MemoryAndBytes fileInfo = readExecFile(filePath);
				// int memNeeded = readMemFromFile(filePath);
				// byte[] requestMsg = readInputBytesFromFile(filePath);
				System.out.println("Sending exec msg from path:" + filePath + " mem: " + fileInfo.memNeeded);
				client.sendExecMsg(fileInfo.memNeeded, fileInfo.requestMsg);
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
		try {
			return scanner.nextLine();
		} catch (NoSuchElementException e) {
			System.out.println("Nothing to read, exiting");
			System.exit(1);
			return null; // so para isto poder compilar
		}
	}

	private static Client authenticateClient() {
		String serverAddress = askForInput("Enter server IP: ");
		// receber address do terminal (so apra evitar conflitos como ta tudo em localhost)
		//String localAddress = askForInput("Enter local IP: ");
		// receber nome e password do terminal
		String username = askForInput("Enter your username: ");
		String password = askForInput("Enter your password: ");

		// criar cliente
		Client client = null;
		try {
			client = new Client(serverAddress, username, password);
			//client = new Client(serverAddress, localAddress, username, password);
		} catch (IOException e) {
			System.out.println("Bad host, exiting");
			System.exit(1);
		}

		//Escolha entre register e login
		String choice;
		System.out.println("Do you want to (R)egister or (L)ogin?");
		boolean authSuccessful = false;
		while (!authSuccessful) {
			choice = askForInput("Input:").toUpperCase();
			try {
				if (choice.equals("R")) { // register
					authSuccessful = client.registerClient();
					if (!authSuccessful) {
						System.out.println("Register failed. Username already in use.");
					}
					else {
						System.out.println("Registered client successfully");
					}

				} else if (choice.equals("L")) { //login
					authSuccessful = client.loginClient();
					if (!authSuccessful) {
						System.out.println("Login failed. Client not registered, or password doesn't match");
					} else {
						System.out.println("Logged in client successfully");
					}

				} else { // Se user meter outro input qualquer
					System.out.println("Invalid choice. Please enter 'R' for registration or 'L' for login.");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-2);
			}
		}
		System.out.println("Client authenticated");
		return client;
	}

	private static String getOutputPath() {
		String path = askForInput("Output folder for exec msgs: ");
		if (!path.endsWith("/")) {
			path += "/";
		}
		try {
			Files.createDirectories(Paths.get(path));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return path;
	}

	// private static int readMemFromFile(String filePath) {
    //     int memNeeded = 0;
    //     try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
    //         memNeeded = Integer.parseInt(reader.readLine());
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    //     return memNeeded;
    // }

	// private static byte[] readInputBytesFromFile(String filePath) {
    //     byte[] fileBytes = null;
    //     try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
	// 		// // Skip 1ª linha
	// 		reader.readLine();
    //         StringBuilder content = new StringBuilder();
    //         String line;
    //         while ((line = reader.readLine()) != null) {
    //             content.append(line).append(System.lineSeparator());
    //         }
	// 		//System.out.println("Read from file " + line);
    //         fileBytes = content.toString().getBytes();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    //     return fileBytes;
    // }

	// estou a assumir 1 so linha de texto, mas e facil de mudar
	private static MemoryAndBytes readExecFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			int memNeeded = Integer.parseInt(reader.readLine());
			byte[] requestMsg = reader.readLine().getBytes();

			reader.close();
			return new MemoryAndBytes(memNeeded, requestMsg);
        } catch (IOException e) {
            e.printStackTrace();
			System.out.println("ERROR");
			System.exit(1);
        }
        return null; // nunca deve correr
	}

	private static void writeToFile(Client client, StCMsg message, String outputPath) {
        String outputFile = outputPath +  client.getName() + "-" + message.getRequestN() + ".txt";
		try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            writer.write(message.getResultInBytes());
            //writer.newLine();
			writer.close();
        } catch (IOException e) {
            e.printStackTrace();
			System.out.println("ERROR");
			System.exit(1);
        }
    }
}
