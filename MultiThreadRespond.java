
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MultiThreadRespond implements Runnable{

    private ServerSocket server;
    private int port ;
    private ObjectOutputStream socketOutput;
    private ObjectInputStream socketInput;

    private int MAX_TRY = 7;

    public MultiThreadRespond(int port){
        this.port = port;
        try{
            server = new ServerSocket(port);
        }catch(Exception e){

        }

    }

    @Override
    public void run(){
        String  [] Word = { "Harrypotter","Friendzone","Spiderman","Alita",
                "Frozen","Batman","Deadpool","Sinderella", "Avatar",
                "Titanic","Avengers","Incredibles","Minions", "Aquaman ",
                "Transformers","Skyfall","Zootopia", "Jumanji"};
        String rand_word;
        char[] hidden_word;
        String user_guess = "";
        int miss_chance = 0;
        char[] missed = new char[7];
        boolean letter_found = false, solved = false;
        rand_word = Word[ (int)(Math.random() * Word.length) ].toLowerCase();
        hidden_word = new char[rand_word.length()];

        int isWin = 0;
        int isLose = 0;

        for (int i = 0; i < rand_word.length(); i++) {
            if (rand_word.charAt(i) == ' ') {
                hidden_word[i] = ' ';
            } else {
                hidden_word[i] = '_';
            }
        }

        StringBuilder res_hidden_word = new StringBuilder();
        int  res_miss_count = miss_chance;
        StringBuilder res_missed = new StringBuilder();


        ////   hidden_word|miss_count|missed|isWin|isLose

        System.out.println("Start Games : " + rand_word);
        while(true){
            try{
                Socket socket = server.accept();
                socketInput = new ObjectInputStream(socket.getInputStream());
                socketOutput = new ObjectOutputStream(socket.getOutputStream());
                boolean running = true;
                while (running){


                    System.out.print("\nHidden Word: ");
                    res_hidden_word.delete(0,  res_hidden_word.length());
                    for (int i = 0; i < rand_word.length(); i++) {
                        System.out.print(hidden_word[i] + " ");
                        res_hidden_word.append(hidden_word[i]).append(" ");
                    }
                    System.out.print("\nMisses: ");
                    res_missed.delete(0,  res_missed.length());
                    for (int i = 0; i < missed.length; i++) {
                        System.out.print(missed[i]);
                        res_missed.append(missed[i]).append(" ");
                    }


                    /// check action which Clint do
                    String action =  (String) socketInput.readObject();
                    System.out.println(action);
                    if (action.equals("start") || action.equals("getStatus")){
                        /// response games status
                        res_miss_count = miss_chance;
                        socketOutput.writeObject(res_hidden_word + "@" + res_miss_count + "@" + res_missed + "@" + isWin + "@" + isLose );

                    }else if (action.equals("getAnswer") && isLose == 1) {
                        socketOutput.writeObject(rand_word);
                    }else if (action.equals("exit")) {
                        socketOutput.close();
                        socketInput.close();
                        socket.close();
                    }else if (action.substring(0,5).equals("send:")){
                        /// get Client input
                        user_guess = action.substring(5,6);
                        System.out.print("\nGuess: " + user_guess);

                        /// Game Logical
                        letter_found = false;
                        for (int i = 0; i < rand_word.length(); i++) {
                            if (user_guess.toLowerCase().charAt(0) == rand_word.toLowerCase().charAt(i)) {
                                hidden_word[i] = rand_word.charAt(i);
                                letter_found = true;
                            }
                        }
                        if (!letter_found) {
                            missed[miss_chance] = user_guess.charAt(0);
                            miss_chance++;
                        }
                        int hidden_count = 0;
                        for (int i = 0; i < rand_word.length(); i++) {
                            if ('_' == hidden_word[i])
                                hidden_count++;
                        }
                        if (hidden_count > 0) {
                            solved = false;
                        } else{
                            solved = true;
                        }
                    }

                    /// check win or lose
                    if (miss_chance >= MAX_TRY){
                        isLose = 1;

                    }
                    if (solved){
                        isWin = 1;
                    }
                }




            }catch(Exception e){
                System.out.println(e);
            }

        }
    }

}
