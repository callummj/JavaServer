package Server;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class connectionEnsurer implements Runnable{

    ClientHandler client;
    boolean run;

    public connectionEnsurer(ClientHandler client){
        this.client = client;
        this.run = true;

    }

    //kill the thread once the trade is complete
    public void end(){
        this.run=false;
    }

    @Override
    public void run() {
        System.out.println("Conn ensurer for user: " + client.getID() + " on thread: " + Thread.currentThread().getId());
        Scanner reader = null;
        try {
            reader = new Scanner(client.getSocket().getInputStream());
        } catch (NullPointerException|IOException e) {

        }
        while (Market.checkConn){
            try {
                String checker = reader.nextLine();
            }catch(NoSuchElementException | IndexOutOfBoundsException e){
                //connection with client lost.

                this.run =false;
                client.setConnected(false);

                Market.disconnectClient(client);
                Thread.currentThread().interrupt();
            }
        }
    }
}
