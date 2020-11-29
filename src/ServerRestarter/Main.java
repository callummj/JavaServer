package ServerRestarter;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Map;


import Server.*;

public class Main {


    static Socket socket;
    private static final String serverLocation = "C:\\Users\\callu\\IdeaProjects\\CE303_Assignment\\JavaServer\\src\\Server\\Main.java";

    public static void main(String[] args) {
        System.out.println("Starting...");
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream output = null;
        try {
            output = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        OutputStream os = null;
        try {
            os = socket.getOutputStream();
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
                System.out.println("error");

                System.out.println("Server is down, restarting server");
                try {
                    restartServer();
                    connect();
                } catch (IOException error) {
                    System.out.println("Cannot restart server");
                }

            }
        }
    }


    protected static void restartServer() throws IOException {

        System.out.println("Starting server...");


        try {
            int result = compile("Server/Main.java");
            System.out.println("javac returned " + result);
            run("Server.Main");
        }catch (IOException | InterruptedException e){
            System.out.println("error");
        }

        System.out.println("server restarted");

    }

    protected static void run(String mainLoc) throws IOException, InterruptedException {
        System.out.println("in run");
        ProcessBuilder pb = new ProcessBuilder("java", mainLoc);
        System.out.println("made process");
        pb.redirectError();
        pb.directory(new File("src"));

        Process p = pb.start();
        System.out.println("Displaying server output:");
        new Thread(new ServerOutput(p)).start();
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

    }

}
