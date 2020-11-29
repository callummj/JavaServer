package Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Market implements Runnable{

    //public static HashMap<Integer, ClientHandler> clients = new HashMap<Integer, ClientHandler>();
    public static LinkedHashMap<String, ClientHandler> clients = new LinkedHashMap<String, ClientHandler>();
    private static ArrayList<Stock> stock = new ArrayList<Stock>();
    private UpdateThread updateThread;


    //Where the last used ID will be saved
    private static final String idDir = "./src/lastID.txt"; //Directory to look for ID file if server is started from Server.Main
    private static final String secondIdDir = "lastID.txt"; //Directory to look for ID file if server is started from ServerRestarter.Main (ProecessBuilder).


    public Market() {
        Stock item = new Stock("sample stock");
        stock.add(item);
    }

    public static ClientHandler checkClient(Integer ID) {
        return clients.get(ID);
    }

    public static void disconnectClient(ClientHandler disconnectingClient){
        clients.remove(disconnectingClient.getID());

        if ((stock.get(0).getOwner() == disconnectingClient)){
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

    public synchronized static void updateIDFile(String ID){
        System.out.println("update id file");
        try {
            FileWriter idFile = new FileWriter(idDir);
            ID = ID.replace("([ID])|\\s+", "");
            idFile.write(ID);
            idFile.close();
        } catch (IOException e) {

                try {
                    FileWriter myWriter = new FileWriter(secondIdDir);
                    ID = ID.replace("([ID])|\\s+", "");
                    myWriter.write(ID);
                    myWriter.close();
                } catch (IOException ioException) {
                    System.out.println("second dir not work");
                    ioException.printStackTrace();
                }


        }
    }

    private synchronized static String createIDFile() {
        System.out.println("create id file");
        try {
            FileWriter idFile = new FileWriter(idDir);
            idFile.write("1");
            idFile.close();
        } catch (IOException e) {
            try{
                FileWriter idFile = new FileWriter(secondIdDir);
                idFile.write("1");
                idFile.close();
            }catch (IOException e2){
                System.out.println("Error creating save data creatIDFile func");
            }
        }
        return "1"; //1 is the first ID generated
    }

    
    public synchronized static String generateID(){
        System.out.println("---Generating ID funciton---");
        String ID = null;
        File userData;
        Scanner userDataReader = null;
        //Try opening using first directory: if the server is running in its own process
        
        userData = new File(idDir);
        try{
            userDataReader = new Scanner(userData);
        }catch (FileNotFoundException e){
            //If above fails, try opening using second directory for if the server is running via the Server restarter process.
            userData = new File(secondIdDir);
            try{
                userDataReader = new Scanner(userData);
            }catch (FileNotFoundException e2){
                //ID file does not exist in this project so will need to be created and initiated.
                System.out.println("ID File does not exist. Creating file.");
                ID = createIDFile();
                return ID;
            }
        }

        ID = userDataReader.nextLine();
        ID = ID.replace("([ID])|\\s+", "");
        System.out.println("ID before idint: " + ID);
        int IDint = Integer.parseInt(ID); //Convert to int to increment
        IDint++;

        return String.valueOf(IDint);
    }


    public static ClientHandler getClient(String ID) {
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



    //TODO doesnt work: may have to sync threads.
    public static synchronized boolean trade(ClientHandler newOwner, Stock stock) {

        System.out.println(newOwner.getID() + " is in trade");

        // UNCOMMENT FOR TESTING IF A TRADER DISCONNECTS MID-TRADE

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        ClientHandler oldOwner = stock.getOwner();
        //Check if the owner is also the buyer
        if (stock.getOwner() == newOwner) {
            System.out.println("stock owner: " + stock.getOwner().getID());
            System.out.println("new owner: " + newOwner.getID());
            newOwner.sendMessage("Buy/Sell failed: cannot sell to owner.");
            System.out.println("There was an attempted trade of: " + stock.getName() + " but failed, because the owner tried to trade with themselves.");
            return false;
        } else {
            stock.setOwner(newOwner);
            System.out.println("new owner: " + stock.getOwner().getID());
            //Check that new owner has been assigned
            if (stock.getOwner() == newOwner) {

                if ((oldOwner.isConnected()) && (newOwner.isConnected())) { //Check if traders are still connected before finalisation of trade.
                    return true;
                } else {
                    stock.setOwner(oldOwner);
                    System.out.println("new owner offline");
                    return false;
                }
            }else{
                System.out.println("Stock owner was unable to be changed.");
                return false;
            }
        }

    }

    public static synchronized void userDisconnectionUpdate(String message){

    }

    //Updates the users of the current market
    public static synchronized void updateMarket(String message){
        /*
        if (!(message.startsWith("[UPDATE]"))){
            if (!(message.startsWith("[CONN]"))){
                message = "[UPDATE] " + message;
            }
        }*/
        System.out.println("sending update to clients. Message: " + message);
        Iterator iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            ClientHandler client = (ClientHandler) pair.getValue();
            System.out.println("Sending to client: "+client.getID());
            client.sendMessage(message);
        }

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

    @Override
    public void run() {
    }
}
