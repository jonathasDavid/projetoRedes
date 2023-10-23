package quizgame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteGUI extends Application {
    private static final String SERVER_ADDRESS = "26.48.28.14";
    private static final int SERVER_PORT = 12345;
    private PrintWriter out;
    private BufferedReader in;
    private TextArea chatArea;
    private TextField answerField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Quiz Game Client");

        chatArea = new TextArea();
        chatArea.setEditable(false);

        answerField = new TextField();
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> sendAnswer());

        VBox layout = new VBox(10);
        layout.getChildren().addAll(chatArea, answerField, submitButton);

        Scene scene = new Scene(layout, 800, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Thread readerThread = new Thread(this::readFromServer);
            readerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFromServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                chatArea.appendText(message + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAnswer() {
        String answer = answerField.getText().trim();
        if (!answer.isEmpty()) {
            out.println(answer);
            answerField.clear();
        }
    }
}

