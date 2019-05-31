package HangmanServer;


import javafx.scene.control.TextArea;

public class Lookups {

    private static Lookups ourInstance = new Lookups();
    private TextArea logTextArea;
    private int voted = 0;
    private char[] passwd;


    public static Lookups getInstance() {
        return ourInstance;
    }

    private Lookups() {
        logTextArea = (TextArea) Main.getMainScene().lookup("#logTextArea");
        logTextArea.setEditable(false);
    }

    public void log(String msg) {
        logTextArea.appendText(msg + "\n");
    }

    public int getVoted() {
        return voted;
    }

    public void setVoted(int voted) {
        this.voted = voted;
    }

    public void incrementVoted() {
        voted++;
    }

    public char[] getPasswd() {
        return passwd;
    }


    public void setPasswd(char[] passwd) {
        this.passwd = passwd;
    }


}
