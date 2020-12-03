package Server;

import java.io.*;
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
        StringBuilder result = new StringBuilder("[CONN]");
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
        Main.gui.removeConnection(this.getID());
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

    public Socket getSocket(){
        return this.socket;
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
            System.out.println("reconection: " + reconnection);
            if (reconnection.equals("reconnection")){


                /*

                if (Market.getStock().getOwner() == null){ //if the owner is null, but the user is telling the server that it is a reconnection, it means the server has just restarted and so the previous owner of the stock should exist.

                    File lastOwnerData = new File("lastOwner.txt");
                    String lastOwnerID;
                    Scanner lastOwnerReader = null;
                    boolean lastOwnerFound = true;
                    try {
                        lastOwnerReader = new Scanner(lastOwnerData);
                    }catch(FileNotFoundException e){
                        try{
                            lastOwnerData = new File("./src/lastOwner.txt");
                            lastOwnerReader = new Scanner(lastOwnerData);
                        }catch(FileNotFoundException error){
                            lastOwnerFound = false;
                            System.out.println("last owner file doesnt exist.");
                        }
                    }
                    if (lastOwnerFound){
                        String lastOwnerIDStr = lastOwnerReader.nextLine();
                        if (Market.clients.containsValue(lastOwnerIDStr)){ //if the last owner is connected otherwise will set owner to random connection.
                            ClientHandler client = Market.clients.get(lastOwnerIDStr);
                            Stock stock = Market.getStock();
                            stock.setOwner(client);
                        }
                    }else{
                        //last owner file does not exist.
                        Market.createLastIDFile();
                    }

                }*/



                this.ID = reader.nextLine();
            }else if (reconnection.equals("new connection")){
                System.out.println("Reconnection");
                this.ID = Market.generateID().replace("([ID])|\\s+", "");

                sendMessage("[ID] " + this.ID);
            }



            Market.updateIDFile(this.ID);
            Main.gui.newConnection(this.ID);

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
            Market.updateMarket("[NEW_CONN]User: " + this.getID() + " has connected to the server");

            String connectionsResponse; //Used to send connections status to client
            Stock stock = Market.getStock();
            while (connected) {
                try{
                    String input = reader.nextLine();
                    input = input.replace("@", ""); //@ = ping signal and is not a valid character for any commands.
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

                            boolean success = Market.trade(stock.getOwner(), this, stock);
                            if (success){
                                sendMessage("[WARNING]Trade successful");

                            }else{
                                sendMessage("[WARNING]Trade unsucessful");

                            }
                            break;


                        case "sell":
                            String IDtoSellTo = reader.nextLine();
                            IDtoSellTo = IDtoSellTo.replace("@", ""); //if any ping requests got mixed with the stream
                            IDtoSellTo = IDtoSellTo.replace(" ", ""); //if any ping requests got mixed with the stream
                            ClientHandler clientToSellTo = Market.getClient(IDtoSellTo);
                            System.out.println("Client to sell to: " + clientToSellTo);
                            Boolean sellSuccess = false;
                            if (clientToSellTo != null){
                                if (stock.getOwner() != clientToSellTo){
                                    sellSuccess = Market.trade(this, clientToSellTo, stock);
                                }else{
                                    System.out.println("Trade unsucessful");
                                    sendMessage("[WARNING] Trade unsucessful");
                                }
                            }else{
                                System.out.println("ID to sell to: " + IDtoSellTo);
                                System.out.println("Invalid client ID");
                                sendMessage("[WARNING]Invalid client ID to sell to");

                            }
                            if (sellSuccess){
                                System.out.println("sending successul");
                                sendMessage("[UPDATE]Sell successful");

                            }
                            break;
                        case "status":
                            String message = "[UPDATE]Stock owned by trader: " + Market.getStock().getOwner().getID();
                            System.out.println("sending status message: " + message);
                            sendMessage(message);
                            break;
                        case "connections":
                            connectionsResponse = connectionsToString();
                            sendMessage(connectionsResponse);

                            break;
                        case "quit":
                            connected = false; // break out of while loop to catch statement: setConnected() runs the  quit() function, so should not be used here.
                            break;
                        case "@": //Single character sent from the client every 5 seconds to see if the connection is still alive.
                            System.out.println("ping detected.");
                            break;
                        case "": //Single character which acts as a ping when connection has been established.
                            break;
                        default:
                            System.out.println("error input");
                            System.out.println("input: "  + input);
                            break;
                    }

                    //Handles client disconnecting
                }catch (NoSuchElementException e){
                    this.setConnected(false);
                }

            }
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("Error establishing I/O stream");
        }

    }


}
