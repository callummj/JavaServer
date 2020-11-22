package Server;

import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class Market {

    //public static HashMap<Integer, ClientHandler> clients = new HashMap<Integer, ClientHandler>();
    public static LinkedHashMap<Integer, ClientHandler> clients = new LinkedHashMap<Integer, ClientHandler>();
    private static ArrayList<Stock> stock = new ArrayList<Stock>();

    public Market() {
        Stock item = new Stock("sample stock");
        stock.add(item);
    }

    public static ClientHandler checkClient(Integer ID) {
        return clients.get(ID);
    }

    public static void disconnectClient(Integer ID){
        clients.remove(ID);
        if (stock.get(0).getOwner().getID() == ID){
            resetStock();
            System.out.println("Stock is now unowned, waiting for next connection");
        }
    }

    //Returns stock based on stock name, returns null if stock doesnt exist
    public static Stock getStock(String name){
        for (int i = 0; i < stock.size(); i++){
            if (stock.get(i).getName() == name){
                return stock.get(i);
            }
        }
        return null;
    }

    public static void newConnection(ClientHandler newClient) {
        clients.put(newClient.getID(), newClient);
        Stock sampleStock = stock.get(0);
        if ((clients.size() == 1)&&(sampleStock.getOwner()==null)){
            sampleStock.setOwner(newClient);
        }
    }


    public static int generateID() {
        return clients.size() + 1;
    }

    public static ClientHandler getClient(int ID) {
        return clients.get(ID);
    }


    //Returns a total of stock owned by client.
    public int getBalance(ClientHandler client) {
        int total = 0;
        for (int i = 0; i < stock.size(); i++) {
            if ((stock.get(i).getOwner()) == client) {
                total++;
            }
        }
        return total;
    }

    public static boolean trade(ClientHandler newOwner, Stock stock) {

        /* UNCOMMENT FOR TESTING IF A TRADER DISCONNECTS MID-TRADE
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */


        ClientHandler oldOwner = stock.getOwner();
        //Check if the owner is also the buyer
        if (stock.getOwner() == newOwner) {
            newOwner.sendMessage("Buy failed: cannot sell to owner.");
            System.out.println("Trader: " + newOwner.getID() + " tried to buy stock, but failed as they are already the owner.");
            return false;
        } else {
            stock.setOwner(newOwner);

            //Check that new owner has been assigned
            if (stock.getOwner() == newOwner) {
                if ((oldOwner.isConnected()) && (newOwner.isConnected())) { //Check if traders are still connected before finalisation of trade.
                    return true;
                } else {
                    stock.setOwner(oldOwner);
                    return false;
                }
            }
        }
        return false;
    }


    public static void resetStock(){
        Stock stock = getStock("sample stock");
        if (clients.size() == 0){
            stock.setOwner(null);
        }else{
            clients.values();

            boolean getClient=true;

            Iterator iterator = clients.entrySet().iterator();
            while (iterator.hasNext() && getClient) {
                Map.Entry pair = (Map.Entry)iterator.next();
                ClientHandler client = (ClientHandler) pair.getValue();
                stock.setOwner(client);
                getClient = false;
            }

        }
    }

}
