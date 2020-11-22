package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientHandler implements Runnable{

    private Socket socket;
    private Market market;
    private PrintWriter writer;
    private Scanner reader;
    private int ID;
    private boolean connected;


    public ClientHandler(Socket socket, Market market){
        this.socket = socket;
        this.market = market;
    }

    public boolean isConnected(){
        return this.connected;
    }

    public void setConnected(boolean status){
        this.connected = status;
        if (status == false){ //If incoming status is setting connection to false
            quit();
        }
    }

    /*
    public void buyStock(){
        Stock stock = Market.getStock("sample stock");
        if (stock.getOwner() !=this){
            stock.setOwner(this);
            //check
            if (stock.getOwner() == this){
                System.out.println("Stock: " + stock.getName() + " now owned by trader ID: " + this.getID());
            }
            sendMessage("You now own the stock");
        }else{
            sendMessage("You are the owner of the stock already.");
        }

    }*/

    public void sendMessage(String message){
        writer.println(message);
    }

    public int getID(){
        return this.ID;
    }

    private int getBalance(){
        int items = market.getBalance(this);
        return items;
    }
    @Override
    public void run() {
        try{
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.reader =  new Scanner(socket.getInputStream());
            String reconnection = "new connection"; //initiated to new connection
            try {
                reconnection = reader.nextLine();
            }catch(NoSuchElementException e){
                System.out.println("Error");
            }
            if (!(reconnection.equals("new connection"))){ //user has reconnected
                this.ID = Integer.valueOf(reconnection);
                System.out.println("Client reconnected with ID: " + this.ID);
            }else{ //First time connection for user
                this.ID = Market.generateID();
                System.out.println("Client connected with ID: " + this.ID);
                sendMessage(String.valueOf(this.ID));
            }

            //Add user to the Hashmap of connected traders.
            Market.newConnection(this);


            boolean connected = true;
            while (connected) {
                try{
                    String input = reader.nextLine();
                    switch (input.toLowerCase()) {
                        case "balance":
                            if (getBalance() !=0){
                                sendMessage(String.valueOf(getBalance()));
                            }else{
                                sendMessage("You do not own any stock.");
                            }
                            break;
                        case "buy":
                            Market.trade(this, Market.getStock("sample stock"));
                            break;
                        case "sell":
                            sendMessage("sell");
                            break;
                        case "status":
                            sendMessage("Stock owned by trader: " + Market.getStock("sample stock").getOwner().getID());
                            break;
                        case "quit":
                            quit();
                            connected = false;
                            break;
                        default:
                            System.out.println("error");
                    }
                }catch (NoSuchElementException e){
                    connected = false;
                    quit();
                }

            }
        }catch (IOException e){
            System.out.println("Error establishing I/O stream");
        }

    }

    public void quit(){
        System.out.println("User: " + this.getID() + " disconnected from server.");
        Market.disconnectClient(this.ID);
        try {
            this.socket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket");
        }
        this.connected = false;
        Thread.currentThread().interrupt(); //Closes current thread.
    }
}
