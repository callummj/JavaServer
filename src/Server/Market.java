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
    private static int lastID;

    //Where the last used ID will be saved
    private static final String idDir = "./src/lastID.txt"; //Directory to look for ID file if server is started from Server.Main
    private static final String secondIdDir = "lastID.txt"; //Directory to look for ID file if server is started from ServerRestarter.Main (ProecessBuilder).
    private static final String lastOwnerDir = "./src/lastOwner.txt";
    private static final String secondLastOwnerDir = "lastOwner.txt";

    public static boolean checkConn = false; //used in the connectionEnsurer

    public Market() {
        Stock item = new Stock("sample stock");
        stock.add(item);
        this.lastID = initID();
    }

    public static ClientHandler checkClient(Integer ID) {
        return clients.get(ID);
    }

    public static void disconnectClient(ClientHandler disconnectingClient){
        clients.remove(disconnectingClient.getID());
        updateMarket("[DISCONN]"+disconnectingClient.getID());//for the GUI
        if ((stock.get(0).getOwner() == disconnectingClient)){
            resetStock();
            System.out.println("Stock is now unowned, waiting for next connection");

        }
    }

    //Returns stock based on stock name, returns null if stock doesnt exist
    public static Stock getStock(){
        return stock.get(0);

    }

    public static void newConnection(ClientHandler newClient) {
        clients.put(newClient.getID(), newClient);
        Stock sampleStock = stock.get(0);
        if ((clients.size() == 1)&&(sampleStock.getOwner()==null)){
            sampleStock.setOwner(newClient);
            Main.gui.updateStockOwner(newClient.getID());
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

    //When the program is ran, the server gets the last saved id
    private static int initID(){
        File userData = new File(idDir);
        String ID;
        Scanner userDataReader = null;
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
                return Integer.valueOf(ID);
            }
        }
        try{
            ID = userDataReader.nextLine();
        }catch (NoSuchElementException e){
            ID = createIDFile();
        }
        userDataReader.close();
        ID = ID.replace("([ID])|\\s+", "");
        return Integer.valueOf(ID);
    }

    public synchronized static String generateID(){
        lastID++;
        String idToUpdate = String.valueOf(lastID);
        idToUpdate = idToUpdate.replace(" ", ""); //in case of any
        boolean idValid = false;

        //Make sure that the id hasn't already been assigned to an online client (could occur if server restarted.
        while (!idValid){
            Iterator iterator = clients.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry pair = (Map.Entry)iterator.next();
                ClientHandler client = (ClientHandler) pair.getValue();
                if (client.getID().equals(idToUpdate)){
                    lastID++;
                    idToUpdate = String.valueOf(lastID);
                }
            }
            idValid = true;
        }
        updateIDFile(idToUpdate);
        return String.valueOf(lastID);
    }
 /*
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

        try{
            ID = userDataReader.nextLine();
        }catch (NoSuchElementException e){
            //Corrupted file
            ID = createIDFile();
        }
        userDataReader.close();
        ID = ID.replace("([ID])|\\s+", "");
        System.out.println("ID before idint: " + ID);
        int IDint = Integer.parseInt(ID); //Convert to int to increment
        IDint++;

        return String.valueOf(IDint);
    }
*/

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
    public static synchronized boolean trade(ClientHandler oldOwner, ClientHandler newOwner, Stock stock) {




        System.out.println("in trade started");




        Thread t1 = new Thread(new connectionEnsurer(oldOwner));
        Thread t2 = new Thread(new connectionEnsurer(newOwner));
        System.out.println("starting threads");
        t1.start();
        t2.start();

        if (oldOwner == stock.getOwner()){
            //Check if the owner is also the buyer
            if (stock.getOwner() == newOwner) {


                System.out.println("There was an attempted trade of: " + stock.getName() + " but failed, because the owner tried to trade with themselves.");
                t1.interrupt();
                t2.interrupt();

            } else {
                stock.setOwner(newOwner);
                System.out.println("new owner: " + stock.getOwner().getID());
                //Check that new owner has been assigned
                if (stock.getOwner() == newOwner) {

                    if ((oldOwner.isConnected()) && (newOwner.isConnected())) { //Check if traders are still connected before finalisation of trade.
                        String updateMsg = "[UPDATE]Stock is now owned by trader: " + Market.getStock().getOwner().getID();
                        updateMarket(updateMsg);
                        updateMsg = updateMsg.replace("[UPDATE]", "");
                        Main.gui.updateConsole(updateMsg);
                        Main.gui.updateStockOwner(String.valueOf(newOwner.getID()));
                        updateLastOwnerFile(newOwner.getID());
                        t1.interrupt();
                        t2.interrupt();
                        return true;
                    } else {
                        stock.setOwner(oldOwner);
                        System.out.println("new owner offline");
                        t1.interrupt();
                        t2.interrupt();
                        return false;
                    }
                }else{
                    System.out.println("Stock owner was unable to be changed.");
                    t1.interrupt();
                    t2.interrupt();

                    return false;
                }
            }
        }else{
            System.out.println("client: " + oldOwner.getID() + " tried to sell the stock, but are not the owner.");
            t1.interrupt();
            t2.interrupt();
            return false;
        }

        t1.interrupt();
        t2.interrupt();
    return false;
    }



    //Updates the users of the current market
    public static synchronized void updateMarket(String message){

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
        Stock stock = getStock();
        if (clients.size() == 0){
            stock.setOwner(null);
            Main.gui.updateStockOwner("None");
        }else{
            clients.values();

            boolean getClient=true;

            String previousowner = stock.getOwner().getID();
            updateMarket("[DISCONN]" + previousowner);

            Iterator iterator = clients.entrySet().iterator();
            while (iterator.hasNext() && getClient) {
                Map.Entry pair = (Map.Entry)iterator.next();
                ClientHandler client = (ClientHandler) pair.getValue();
                stock.setOwner(client);
                getClient = false;
            }
            Main.gui.updateStockOwner(Market.getStock().getOwner().getID());
            String updateMsg = "[UPDATE]Previous owner disconnected, stock is now owned by trader: " + Market.getStock().getOwner().getID();
            updateMarket(updateMsg);

        }

    }

    public static void createLastOwnerFile(){
        try {
            FileWriter lastOwnerFile = new FileWriter(lastOwnerDir);
            Stock stock = getStock();
            if (stock.hasOwner()){
                lastOwnerFile.write(stock.getOwner().getID());
            }
            else{
                lastOwnerFile.write("");
            }
            lastOwnerFile.close();
        } catch (IOException e) {
            try{
                FileWriter lastOwnerFile = new FileWriter(secondLastOwnerDir);
                Stock stock = getStock();
                if (stock.hasOwner()){
                    lastOwnerFile.write(stock.getOwner().getID());
                }
                else{
                    lastOwnerFile.write("");
                }
                lastOwnerFile.close();
            }catch (IOException e2){
                System.out.println("Error creating save data creatIDFile func");
            }
        }
    }

    public static void updateLastOwnerFile(String newOwnerID){
        System.out.println("update lastOwner file");
        System.out.println("new owner id: "+ newOwnerID);
        try {
            FileWriter lastOwnerFile = new FileWriter(idDir);
            lastOwnerFile.write(newOwnerID);
            lastOwnerFile.close();
        } catch (IOException e) {

            try {
                FileWriter lastOwnerFile = new FileWriter(secondIdDir);
                lastOwnerFile.write(newOwnerID);
                lastOwnerFile.close();
            } catch (IOException ioException) {
                System.out.println("last owner id update failed.");
                ioException.printStackTrace();
            }


        }
    }

    @Override
    public void run() {
    }
}
