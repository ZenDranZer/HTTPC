import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class HTTP {

    private int port = 80;
    final String HTTPVERSION = "HTTP/1.1";
    final String CRLF = "\r\n";

    public void printHelp(String[] command){

        if(command.length <=2 && !command[0].toLowerCase().equals("httpc")){
            printGeneralHelp();
            return;
        }

        if(command[1].toLowerCase().equals("help") && command[2].toLowerCase().equals("get")){
            printGetHelp();
            return;
        }

        if(command[1].toLowerCase().equals("help") && command[2].toLowerCase().equals("post")){
            printPostHelp();
            return;
        }

        if(!command[1].toLowerCase().equals("help") && !command[1].toLowerCase().equals("get") && !command[1].toLowerCase().equals("post")){
            printGeneralHelp();
            return;
        }


        if((command[1].toLowerCase().equals("get") || command[1].toLowerCase().equals("post")) && command[2].isEmpty())
            printGeneralHelp();
    }

    public void printGeneralHelp(){
        String help = "\n" +
                "httpc is a curl-like application but supports HTTP protocol only.\n" +
                "Usage: \n    httpc [get/post/help] URL [-v] [-h key:value] [-d dataWithoutSpace] [-f fileName] [-o fileName]\n" +
                "    get     executes a HTTP GET request and prints the response.\n" +
                "    post    executes a HTTP POST request and prints the response.\n" +
                "    help    prints this screen.\n\n";
        System.out.println(help);
        printGetHelp();
        printPostHelp();
    }

    public void printGetHelp(){
        System.out.println("httpc help get\n" +
                "usage: httpc get [-v] [-h key:value] URL\n"
                + "Get executes a HTTP GET request for a given URL.\n"
                + "-v\tPrints the detail of the response such as protocol, status, and headers.\n"
                + "-h key:value\tAssociates headers to HTTP Request with the format 'key:value'.");
    }

    public void printPostHelp(){
        System.out.println("httpc help post\n"
                + "usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n"
                + "Post executes a HTTP POST request for a given URL with inline data or from file.\n"
                + "-v\tPrints the detail of the response such as protocol, status, and headers.\n"
                + "-h key:value\tAssociates headers to HTTP Request with the format 'key:value'.\n"
                + "-d string\tAssociates an inline data to the body HTTP POST request.\n"
                + "-f file\tAssociates the content of a file to the body HTTP POST request.\n\n"
                + "Either [-d] or [-f] can be used but not both.");
    }

    public void processRequest(String[] command,boolean redirectInfo){
        BufferedWriter writer, fileWriter =  null;
        BufferedReader reader;
        try {
            ArrayList<String> commandArray = new ArrayList<>(Arrays.asList(command));
            commandArray.remove(0);
            String method = command[1].toLowerCase().equals("get") ? "GET" : "POST";
            commandArray.remove(0);
            ArrayList<String> headers = new ArrayList<>();
            String host = "",url = "",location = "/",data = "";
            boolean isF = commandArray.contains("-f") || commandArray.contains("-F");
            boolean isO = false;
            boolean isV = false;
            boolean redirect = false;

            while (commandArray.size()>=1) {
                String s = commandArray.get(0);
                switch (s.toLowerCase()) {
                    case "-v":
                        isV = true;
                        commandArray.remove(s);
                        break;
                    case "-h":
                        commandArray.remove(s);
                        if (commandArray.size() < 1) {
                            System.out.println("A key:value pair is required.");
                            return;
                        }
                        String headerString = commandArray.get(0);
                        commandArray.remove(headerString);
                        if (headerString.contains(":")) {
                            headers.add(headerString);
                        } else {
                            System.out.println("A key:value pair is required.");
                            return;
                        }
                        break;
                    case "-d":
                        if (method.equals("GET") || isF) {
                            System.out.println("GET do not work with -d option. Please try again without -d.\n -d and -f do not work together.");
                            return;
                        }
                        commandArray.remove(s);
                        if (commandArray.size() < 1) {
                            System.out.println("An Inline Data without space is required.");
                            return;
                        }
                        data = commandArray.get(0) + CRLF;
                        commandArray.remove(0);
                        break;
                    case "-f": {
                        if (method.equals("GET")) {
                            System.out.println("GET do not work with -f option. Please try again without -f.");
                            return;
                        }
                        commandArray.remove(s);
                        if (commandArray.size() < 1) {
                            System.out.println("An input file name is required.");
                            return;
                        }
                        String fileName = commandArray.get(0);
                        commandArray.remove(fileName);
                        File file = new File("src/"+fileName);
                        if (file.exists()) {
                            Scanner sc = new Scanner(file);
                            String line;
                            while (sc.hasNextLine()) {
                                line = sc.nextLine();
                                data += line;
                            }
                            data += CRLF;
                        } else {
                            System.out.println("File not found.");
                            System.out.println(fileName);
                            return;
                        }
                        break;
                    }
                    case "-o": {
                        commandArray.remove(s);
                        if (commandArray.size() < 1) {
                            System.out.println("An output file name is required.");
                            return;
                        }
                        String fileName = commandArray.get(0);
                        if (!fileName.contains(".")) {
                            fileName += ".txt";
                        }
                        File file = new File(fileName);
                        fileWriter = new BufferedWriter(new FileWriter(file,true));
                        isO = true;
                        commandArray.remove(fileName);
                        break;
                    }
                    default: {
                        url = s;
                        commandArray.remove(0);
                    }
                }
            }

            if(!url.contains(".")){
                System.out.println("URL is missing");
                System.out.println(url);
                return;
            }

            int separator;
            if(url.contains("https://")) {
                separator = url.indexOf("/", 8);
                if(separator == -1)
                    host = url.substring(8);
                else{
                    host = url.substring(8,separator);
                    location = url.substring(separator);
                }
            }else if(url.contains("http://")){
                separator = url.indexOf("/", 7);
                if(separator == -1)
                    host = url.substring(7);
                else{
                    host = url.substring(7,separator);
                    location = url.substring(separator);
                }
            } else{
                separator = url.indexOf("/", 8);
                if(separator == -1)
                    host = url;
                else{
                    host = url.substring(0,separator);
                    location = url.substring(separator);
                }
            }

            Socket socket = new Socket(InetAddress.getByName(host), port);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            writer.write(method + " " + location + " " + HTTPVERSION + CRLF);
            writer.write("Host: " + host + CRLF);

            if (!headers.isEmpty()) {
                for (String head : headers) {
                    writer.write(head + CRLF);
                }
            }
            if (!data.isEmpty()) {
                writer.write("Content-Length: " + data.length() + CRLF);
            }

            writer.write(CRLF);
            writer.write(data);
            writer.flush();

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response;
            System.out.println("Socket connection: " + socket.isConnected());
            boolean content = false;

            while((response = reader.readLine())!=null) {
                if(response.trim().isEmpty()){
                    content = true;
                    continue;
                }

                if (response.contains("301") || response.contains("302") || response.contains("303") || response.contains("304")) {
                    redirect = true;
                }

                if (redirect && response.length() > 10 && response.substring(0, 10).equals("Location: ")) {
                    String newLocation = response.substring(10);
                    if (redirectInfo) {
                        System.out.println("302: redirect to \"" + newLocation + "\"");
                        if(isO){
                            fileWriter.write("302: redirect to \"" + newLocation + "\"");
                            fileWriter.write("\n---------------------Redirect---------------------\n");
                            fileWriter.flush();
                        }
                        reader.close();
                        writer.close();
                        socket.close();
                        System.out.println("---------------------Redirect---------------------");
                        redirect(method,newLocation, 1,isO,fileWriter);
                    }
                    break;
                }

                if (content) {
                    if (isV) {
                        if (isO) {
                            fileWriter.write(response+"\n");
                            fileWriter.flush();
                        } else
                            System.out.println(response);
                    }
                } else{
                    if(isO) {
                        fileWriter.write(response+"\n");
                        fileWriter.flush();
                    }else
                        System.out.println(response);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void redirect(String method,String url,int redIndex,boolean isO, BufferedWriter fileWriter){
        if(redIndex==6)
            return;
        BufferedWriter writer;
        BufferedReader reader;
        String host ,location="/";
        int separator;
        if(url.contains("https://")) {
            separator = url.indexOf("/", 8);
            if(separator == -1)
                host = url.substring(8);
            else{
                host = url.substring(8,separator);
                location = url.substring(separator);
            }
        }else if(url.contains("http://")){
            separator = url.indexOf("/", 7);
            if(separator == -1)
                host = url.substring(7);
            else{
                host = url.substring(7,separator);
                location = url.substring(separator);
            }
        } else{
            separator = url.indexOf("/", 8);
            if(separator == -1)
                host = url;
            else{
                host = url.substring(0,separator);
                location = url.substring(separator);
            }
        }
        try {
            Socket socket = new Socket(InetAddress.getByName(host), port);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            writer.write(method + " " + location + " " + HTTPVERSION + CRLF);
            writer.write("Host: " + host + CRLF);
            writer.write(CRLF);
            writer.flush();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response;
            boolean redirect = false;
            while((response = reader.readLine())!=null) {
                if(isO){
                    fileWriter.write(response + "\n");
                    fileWriter.flush();
                } else
                    System.out.println(response);
                if (response.contains("301") || response.contains("302") || response.contains("303") || response.contains("304")) {
                    redirect = true;
                }
                if (redirect && response.length() > 10 && response.substring(0, 10).equals("Location: ")) {
                    String newLocation = response.substring(10);
                    System.out.println("302: redirect to \"" + newLocation + "\"");
                    if(isO){
                        fileWriter.write("302: redirect to \"" + newLocation + "\"");
                        fileWriter.flush();
                    }
                    reader.close();
                    writer.close();
                    socket.close();
                    redirect(method,newLocation,++redIndex,isO,fileWriter);
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
