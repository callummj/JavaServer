//TODO make a file which stores the amount of conns.

package Server;

import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Main {

    private final static int port = 8888;

    private static Market market;

    public static void main(String[] args) {


        run();
    }

    private static void run(){
        ServerSocket server = startServer();
        System.out.println("Server running and waiting for connections....");System.out.println("Working Directory = " + System.getProperty("user.dir"));
        new Thread(market = new Market()).start();

        //TODO not handling multiple clients.
        while (true){
            try {
                Socket socket = server.accept();
                ClientHandler client;

                new Thread(new ClientHandler(socket, market)).start();

            } catch (IOException e) {
                System.out.println("Error accepting client.");
            }

        }
    }

    private static void saveState(){
        //Save the connections and who owns the stock whenever a change has been made:
    }

    private static void readState(){

    }

    //Starts the server and returns a server socket object.
    private static ServerSocket startServer(){
        ServerSocket socket = null;
        try{
            socket = new ServerSocket(8888);
        }catch (IOException e){
            System.out.println("Error starting server");
            System.exit(100);
        }
        return socket;
    }



}
