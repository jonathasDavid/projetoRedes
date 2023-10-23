package quizgame;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadedServer {
    private static final int PORT = 12345;
    private static final int MAX_SCORE = 5;
    private static final List<Question> questions = List.of(
            new Question("Em que filme um artista pobre se apaixona por uma jovem aristocrata durante o trágico naufrágio de um famoso navio??", "Titanic"),
            new Question("Qual é o filme sobre um casal que se apaixona durante uma viagem de verão na cidade de Veneza?", "Sob o Sol da Toscana"),
            new Question("Em qual filme um casal se conhece em um cruzeiro e decide ficar juntos por apenas uma noite em uma cidade europeia?", "Antes do Amanhecer"),
            new Question("Qual é o filme em que um homem idoso lê uma história de amor para uma mulher com Alzheimer em um lar de idosos?", "Diário de uma Paixão"),
            new Question("Em que filme um casal se apaixona durante uma viagem de trem pela Europa e, anos depois, tenta se reencontrar?", "Antes do Pôr-do-Sol"),
            new Question("Qual é o filme sobre um homem e uma mulher que se apaixonam em meio aos horrores da Segunda Guerra Mundial?", "O Paciente Inglês"),
            new Question("Em qual filme um músico de jazz e uma atriz se apaixonam em Los Angeles enquanto perseguem seus sonhos?", "La La Land"),

            new Question("Qual é o filme sobre um romance entre uma mulher idosa e um jovem escritor, enquanto ela conta a história de sua vida?", "Notting Hill"),
            new Question("Em que filme um casal se conhece durante um desfile de Nova York e passa uma noite juntos explorando a cidade?", "Encontro Marcado"),
            new Question("Qual é o filme sobre um casal que se apaixona em meio a um surto de doença contagiosa, forçando-os a ficar juntos em quarentena?", "Tudo Acontece em Elizabethtown")

    );

    private static final Object lock = new Object();
    private static int connectedPlayers = 0;
    private static final CountDownLatch startLatch = new CountDownLatch(1);

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress().getHostAddress());
                executorService.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private int score;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                synchronized (lock) {
                    connectedPlayers++;
                    if (connectedPlayers == 2) {
                        startLatch.countDown(); // Inicia o jogo quando ambos os jogadores estão conectados


                    } else {
                        out.println("Waiting for the other player to connect...");
                        startLatch.await(); // Aguarda pelo outro jogador
                    }
                }

                while (score < MAX_SCORE) {
                    Random random = new Random();
                    int questionIndex = random.nextInt(questions.size());
                    Question question = questions.get(questionIndex);

                    out.println("Question: " + question.getText());
                    String clientAnswer = in.readLine();

                    if (clientAnswer != null && clientAnswer.equalsIgnoreCase(question.getAnswer())) {
                        out.println("Correct! Your answer is right.");
                        score++;
                    } else {
                        out.println("Wrong answer. The correct answer is: " + question.getAnswer());
                    }

                    out.println("Your Current Score: " + score);
                }

                out.println("Game Over! You reached the maximum score.");
                clientSocket.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}




