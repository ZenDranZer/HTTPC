import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Driver {
    public static void main(String[] args) {
        String[] command = new String[]{};
        HTTP http = new HTTP();
        try{
            System.out.println("COMMAND:\n" +
                    "httpc [get/post/help] URL [-v] [-h key:value] [-d dataWithoutSpace] [-f fileName] [-o fileName]");
            System.out.println("Enter your command below:\n");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            command = br.readLine().split(" ");
            System.out.println("do you want redirection information? (T/F): ");
            boolean redirection = false;
            String input = br.readLine();
            if(input.toLowerCase().charAt(0) == 't')
                redirection = true;
            if(command.length <= 1 || (!command[0].toLowerCase().equals("httpc")) || (command[1].toLowerCase().equals("help")))
                throw new IOException("Please check help section.");

            http.processRequest(command,redirection);

        }catch (IOException e){
            http.printHelp(command);
        }

    }
}
