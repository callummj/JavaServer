package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientHandler implements Runnable{

    private Socket socket;
    private Market market;
    private PrintWriter writer;
    private Scanner reader;
    private String ID;
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

    public void sendMessage(String message){
        writer.println(message);
    }

    public String getID(){
        return this.ID;
    }

    private int getBalance(){
        int items = market.getBalance(this);
        return items;
    }

    public String connectionsToString(){
        StringBuilder result = new StringBuilder("[UPDATE]");
        Iterator iterator = Market.clients.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            i++;
            System.out.println("i: " + i);

            Map.Entry pair = (Map.Entry)iterator.next();
            ClientHandler client = (ClientHandler) pair.getValue();
            System.out.println("client: " + client.getID());
            result.append(" " + client.getID());
        }
        System.out.println("result: " + result);
        return result.toString();
    }



    public void quit(){
        System.out.println("User: " + this.getID() + " disconnected from server.");
        Market.disconnectClient(this);
        try {
            this.socket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket");
        }
        this.connected = false;
        Thread.currentThread().interrupt(); //Closes current thread.
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
                this.ID = reconnection;
                System.out.println("Client reconnected with ID: " + this.ID);
            }else{ //First time connection for user
                this.ID = Market.generateID();
                System.out.println("Client connected with ID: " + this.ID);
                sendMessage(this.ID);
            }

            //Add user to the Hashmap of connected traders.
            Market.newConnection(this);


            this.setConnected(true);

            String connectionsResponse; //Used to send connections status to client
            while (connected) {
                try{
                    String input = reader.nextLine();
                    if (!(input.equals("o"))){
                        System.out.println("User  " + this.ID + " inputs: " + input);
                    }

                    switch (String.valueOf(input.toLowerCase())) {
                        case "balance":
                            if (getBalance() !=0){
                                sendMessage(String.valueOf(getBalance()));
                            }else{
                                sendMessage("You do not own any stock.");
                            }
                            break;
                        case "buy":
                            //TODO Might have to go back to Market.trade as error is client side rather than serverside.
                            boolean success = Market.trade(this, Market.getStock("sample stock"));
                            if (success){
                                sendMessage("Trade successful");
                            }else{
                                sendMessage("Trade unsucessful");
                            }
                            break;


                        case "sell":
                            String IDtoSellTo = reader.nextLine();
                            ClientHandler clientToSellTo = Market.getClient(IDtoSellTo);
                            Stock stock = Market.getStock("sample stock");
                            if (clientToSellTo != null){
                                if (stock.getOwner() == clientToSellTo){
                                    Market.trade(clientToSellTo, stock);
                                }else{
                                    System.out.println("Trade unsucessful");
                                    sendMessage("Trade unsucessful");
                                }
                            }else{
                                System.out.println("Invalid client ID");
                                sendMessage("Invalid client ID");
                            }
                            break;
                        case "status":
                            sendMessage("Stock owned by trader: " + Market.getStock("sample stock").getOwner().getID());
                            break;
                        case "connections":
                            System.out.println("connections");
                            connectionsResponse = connectionsToString();
                            sendMessage(connectionsResponse);
                            break;
                        case "quit":
                            quit();
                            connected = false;
                            break;
                        case "o": //Single character sent from the client every 5 seconds to see if the connection is still alive.
                            connectionsResponse = connectionsToString();
                            sendMessage(connectionsResponse);
                            break;
                        default:
                            System.out.println("error");System.out.println(input);
                    }
                }catch (NoSuchElementException e){
                    this.setConnected(false);
                    quit();
                }

            }
        }catch (IOException e){
            System.out.println("Error establishing I/O stream");
        }

    }


}
