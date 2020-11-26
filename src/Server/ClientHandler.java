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

            Map.Entry pair = (Map.Entry)iterator.next();
            ClientHandler client = (ClientHandler) pair.getValue();
            result.append(" " + client.getID());
        }

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
        Market.updateMarket("[CONN] " + this.getID() + " disconnected");
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


            //If server restarted, client will send message reconnection with their previous ID, otherwise they're sent a new ID.
            System.out.println("reconeection: " + reconnection);
            if (reconnection.equals("reconnection")){
                this.ID = reader.nextLine();
            }else if (reconnection.equals("new connection")){
                System.out.println("here");
                this.ID = Market.generateID();
                sendMessage("[ID] " + this.ID);
            }


            Market.updateIDFile(this.ID);

            /*
            if (!(reconnection.equals("new connection"))){ //user has reconnected
                this.ID = reconnection;
                System.out.println("Client reconnected with ID: " + this.ID);
            }else{ //First time connection for user
                this.ID = Market.generateID();
                System.out.println("Client connected with ID: " + this.ID);
                sendMessage(this.ID);
            }*/

            //Add user to the Hashmap of connected traders.
            Market.newConnection(this);


            this.setConnected(true);

            System.out.println("User: " + this.getID() + " has connected to the server");
            Market.updateMarket("[CONN] User: " + this.getID() + " has connected to the server");
            String connectionsResponse; //Used to send connections status to client
            while (connected) {
                try{
                    String input = reader.nextLine();

                    switch (String.valueOf(input.toLowerCase())) {
                        case "balance":
                            if (getBalance() !=0){
                                sendMessage("[UPDATE]" + String.valueOf(getBalance()));
                            }else{
                                sendMessage("[WARNING] You do not own any stock.");
                            }
                            break;
                        case "buy":
                            //TODO Might have to go back to Market.trade as error is client side rather than serverside.
                            boolean success = Market.trade(this, Market.getStock("sample stock"));
                            if (success){
                                sendMessage("[WARNING] Trade successful");
                            }else{
                                sendMessage("[WARNING] Trade unsucessful");
                            }
                            break;


                        case "sell":
                            String IDtoSellTo = reader.nextLine();
                            ClientHandler clientToSellTo = Market.getClient(IDtoSellTo);
                            Stock stock = Market.getStock("sample stock");
                            if (clientToSellTo != null){
                                if (stock.getOwner() != clientToSellTo){
                                    Market.trade(clientToSellTo, stock);
                                }else{
                                    System.out.println("Trade unsucessful");
                                    sendMessage("[WARNING] Trade unsucessful");
                                }
                            }else{
                                System.out.println("Invalid client ID");
                                sendMessage("[WARNING] Invalid client ID");
                            }
                            break;
                        case "status":

                            String message = "[UPDATE]Stock owned by trader: " + Market.getStock("sample stock").getOwner().getID();
                            System.out.println("sending status message: " + message);
                            sendMessage(message);
                            break;
                        case "connections":
                            connectionsResponse = "[CONN]" + connectionsToString();
                            sendMessage(connectionsResponse);
                            break;
                        case "quit":
                            connected = false; // break out of while loop to catch statement: setConnected() runs the  quit() function, so should not be used here.
                            break;
                        case "o": //Single character sent from the client every 5 seconds to see if the connection is still alive.
                            break;
                        default:
                            System.out.println("error");System.out.println(input);
                    }

                    //Handles client disconnecting
                }catch (NoSuchElementException e){
                    this.setConnected(false);
                }

            }
        }catch (IOException e){
            System.out.println("Error establishing I/O stream");
        }

    }


}
