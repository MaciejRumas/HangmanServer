package HangmanServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class Controller implements Initializable {

    private static Map<String, ClientThread> clients = new HashMap<>();

    @FXML
    private TextField serverIp;

    @FXML
    private TextField portField;

    @FXML
    private Button startButton;

    @FXML
    private Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String ipAddress = inetAddress.getHostAddress();
            serverIp.setText(ipAddress);
            serverIp.setEditable(false);
            portField.requestFocus();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void handleStartButton(ActionEvent actionEvent) {
        if(portFieldOk()){
            int port = Integer.parseInt(portField.getText());

            new Thread(() -> startServer(port)).start();

            Lookups.getInstance().log("Starting server on port " + port + " " + new Date() + "\n");

            startButton.setText("Running");
            startButton.setDisable(true);
        }
    }

    public void showUsersButton(ActionEvent event) {
        for(Map.Entry<String, ClientThread> entry: clients.entrySet()){
            Lookups.getInstance().log("Client: " + entry.getKey());
        }
    }

    private boolean portFieldOk(){
        try {
            int port = Integer.parseInt(portField.getText());
            if(port >= 0 && port <= 65535){
                errorLabel.setText("");
                return true;
            }
        }catch (NumberFormatException e){
                errorLabel.setText("port must be between 0 and 65535");
        }
        return false;
    }

    public void startServer(int port){
            try{
                ServerSocket serverSocket = new ServerSocket(port);

                while(true){
                    Socket clientSocket = serverSocket.accept();
                    ClientThread t = new ClientThread(clientSocket);
                    t.start();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
    }

    public static Map<String, ClientThread> getClients() {
        return clients;
    }

}
