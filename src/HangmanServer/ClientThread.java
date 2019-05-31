package HangmanServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ClientThread extends Thread {

    private final Socket clientSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedReader bufferedReader;
    private String name;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException {
        this.outputStream = clientSocket.getOutputStream();
        this.inputStream = clientSocket.getInputStream();
        this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String in = bufferedReader.readLine();
        System.out.println("<in>" + in + "<in>");
        String[] input = in.split(" ", 2);
        this.name = input[1];
        Lookups.getInstance().log(name + " has successfully connected from " + clientSocket.getInetAddress().toString());

        new Thread(() -> {
            Controller.getClients().put(name, this);
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println("<in>" + line + "<in>");
                    String[] tokens = line.split(" ", 2);
                    if (tokens.length == 2) {
                        String cmd = tokens[0];
                        if ("disconnect".equalsIgnoreCase(cmd)) {
                            Lookups.getInstance().log(tokens[1] + " disconnected");
                            Controller.getClients().remove(tokens[1]);
                            break;
                        } else if ("msg".equals(cmd)) {
                            handleMessageReceived(tokens[1]);
                        } else if ("try".equals(cmd)) {
                            handleHitAttempt(tokens[1]);
                        } else if ("vote".equals(cmd)) {
                            handleVoteStart(tokens[1]);
                        } else if ("finished".equals(cmd)) {
                            handleFinishedGame(tokens[1]);
                        } else if ("passwd".equals(cmd)) {
                            sendPassword(tokens[1]);
                        }
                    }
                }
                clientSocket.close();
                Lookups.getInstance().log("Closed client socket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void handleFinishedGame(String token) throws IOException {
        String msg = token + " has finished their game";
        Lookups.getInstance().log(msg);
        //Lookups.getInstance().decrementVoted();
        for (Map.Entry<String, ClientThread> entry : Controller.getClients().entrySet()) {
            entry.getValue().getOutputStream().write(("msg " + msg + "\n").getBytes());
        }
    }

    private void handleVoteStart(String token) throws IOException {
        Lookups.getInstance().incrementVoted();
        int numberOfClients = Controller.getClients().size();
        String msg = token + " voted to start (" + Lookups.getInstance().getVoted() + "/" + numberOfClients + ")";
        Lookups.getInstance().log(msg);

        for (Map.Entry<String, ClientThread> entry : Controller.getClients().entrySet()) {
            entry.getValue().getOutputStream().write(("msg " + msg + "\n").getBytes());
        }

        if (Lookups.getInstance().getVoted() == numberOfClients) {
            ClientThread drew = drawUserToPickWord();
            for (Map.Entry<String, ClientThread> entry : Controller.getClients().entrySet()) {
                entry.getValue().getOutputStream().write(("msg " + drew.name + " is choosing the word\n").getBytes());
            }
            Lookups.getInstance().log(drew.name + " is choosing the word");
            drew.getOutputStream().write("draw You are choosing the word \n".getBytes());
            Lookups.getInstance().setVoted(0);
        }
    }

    private ClientThread drawUserToPickWord() {
        Random random = new Random();
        int usersAmount = Controller.getClients().size();
        int pickedIndex = random.nextInt(usersAmount);
        System.out.println("Picked index: " + pickedIndex);


        int i = 0;
        for (Map.Entry<String, ClientThread> entry : Controller.getClients().entrySet()) {
            if (i == pickedIndex) {
                return entry.getValue();
            }
            i++;
        }
        return this;
    }

    private void handleMessageReceived(String token) throws IOException {
        Lookups.getInstance().log(token);
        for (Map.Entry<String, ClientThread> entry : Controller.getClients().entrySet()) {
            entry.getValue().getOutputStream().write(("msg " + token + "\n").getBytes());
        }
    }

    private void handleHitAttempt(String letter) throws IOException {
        char[] password = Lookups.getInstance().getPasswd();
        List<Integer> hitIndexes = new ArrayList<>();
        for (int i = 0; i < password.length; i++) {
            String p = ("" + password[i]).toUpperCase();
            if (letter.toUpperCase().equals(p)) {
                hitIndexes.add(i);
            }
        }

        if (hitIndexes.size() == 0) {
            outputStream.write(("missed " + letter.toUpperCase() + ",\n").getBytes());
            System.out.println("missed " + letter.toUpperCase() + ",\n");
        } else {
            StringBuilder listOfIndexes = new StringBuilder();
            listOfIndexes.append(letter.toUpperCase());
            listOfIndexes.append(",");
            listOfIndexes.append(password.length);
            for (int i = 0; i < hitIndexes.size(); i++) {
                listOfIndexes.append(",");
                listOfIndexes.append(hitIndexes.get(i));
            }
            System.out.println("correctLetter " + listOfIndexes.toString() + "\n");
            outputStream.write(("correctLetter " + listOfIndexes.toString() + "\n").getBytes());
        }
    }

    private void sendPassword(String token) throws IOException {
        String[] tokens = token.split(",",2);
        String category = tokens[0];
        String password = tokens[1];

        char[] pswd = password.toCharArray();
        Lookups.getInstance().setPasswd(pswd);

        for (Map.Entry<String, ClientThread> entry : Controller.getClients().entrySet()) {
            entry.getValue().getOutputStream().write(("passwd " + category + "," + password + "\n").getBytes());
        }
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

}
