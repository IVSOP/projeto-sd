#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <limits.h> // PIPE_BUF
#include <string.h>
#include <time.h>

void spam_clients(int pipe_array[][2], int total_clients) {
	// int r = rand();
	int i;
	for (i = 0; i < total_clients; i++) {
		const char file[] = "1\ntests/sample_text.txt\n";
		write(pipe_array[i][1], file, strlen(file));
	}
}

void spam_status_clients(int pipe_array[][2], int total_clients) {
	// int r = rand();
	int i;
	for (i = 0; i < total_clients; i++) {
		const char opt[] = "2\n";
		write(pipe_array[i][1], opt, strlen(opt));
	}
}

int main (int argc, char *argv[]) {
	// arg 1 is number of clients
	// their name and password is their number
	int total_clients = atoi(argv[1]);

	printf("Init for %d clients\n", total_clients);

	int pipe_array[total_clients][2]; // array of pipes

	int i;
	for (i = 0; i < total_clients; i++) {
		if (pipe(pipe_array[i]) != 0) {
			perror("Error making pipe");
			exit(1);
		}

		if (fork() == 0) {
			close(pipe_array[i][1]);

			close(STDOUT_FILENO);
			close(STDERR_FILENO);

			if (dup2(pipe_array[i][0], STDIN_FILENO) == -1) {
				perror("dup2");
				exit(EXIT_FAILURE);
			}

			close(pipe_array[i][0]);

			// execlp("cat", "cat", (char *)NULL);
			execlp("bash", "bash", "./client.sh", (char *)NULL);

			//...

			_exit(1);
		} else {
			close(pipe_array[i][0]);

			char message[64];
			snprintf(message, 64, "0.0.0.0\n%d\n%d\nR\noutputs/\n", i, i);
			write(pipe_array[i][1], message, strlen(message));

			printf("Spawned and registered %d\n", i);
		}
	}

	printf("All clients registered.\nEnter y to begin turbo pro max spam\n");
	char confirmation;
	scanf("%c", &confirmation);

	// srand(time(NULL));

	// each client sends 9 requests followed by a status request
	// memory is 50 per request

	if (confirmation == 'y') {
		for (i = 0; i < 9; i++) {
			spam_clients(pipe_array, total_clients);
		}
		spam_status_clients(pipe_array, total_clients);
	}

	printf("Spam finished, enter anything to exit\n");
	char idk;
	scanf("%c", &idk);
	printf("%c\n", idk); // para evitar otimizar isto
	return 0;
}
