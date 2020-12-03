package ServerRestarter;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Map;


import Server.*;

public class Main {

    static OutputStream os;
    static Process server;
    static Socket socket;


    public static void main(String[] args) {
        System.out.println("Starting...");
        System.out.println("working dir: " + System.getProperty("user.dir"));
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
        }


        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                os.write('o');
            } catch (IOException e) {
                System.out.println("Server is down, restarting server");
                try {
                    restartServer();
                    Thread.sleep(1000); //Wait to give server a chance to restart
                    connect();
                } catch (IOException error) {
                    System.out.println("Cannot restart server");
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }

            }
        }
    }


    protected static void restartServer() throws IOException {

        System.out.println("Starting server...");
        try {
            int result = compile("Server/Main.java");
            System.out.println("compile result: " + result);
            run("Server.Main");
        }catch (IOException | InterruptedException e){
            System.out.println("error");
        }

        System.out.println("server restarted");

    }

    protected static void run(String serverLocation) throws IOException, InterruptedException {
        System.out.println("in run");
        ProcessBuilder pb = new ProcessBuilder("java", serverLocation);
        System.out.println("made process");
        pb.redirectError();
        pb.directory(new File("src"));

        server = pb.start();
        System.out.println("Displaying server output:");
        new Thread(new ServerOutput(server)).start();
        //int result = p.waitFor();

    }

    protected static int compile(String file) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("javac", file);
        pb.redirectError();
        pb.directory(new File("src"));
        Process p = pb.start();
        int result = p.waitFor();

        return result;
    }

    protected static void connect() throws IOException {
        try{
            socket = new Socket("localhost", 8888);
        }catch (ConnectException e){
            restartServer();
            connect();
        }

        try {
            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
