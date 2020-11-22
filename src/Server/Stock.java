package Server;

public class Stock {
    private String name;
    private ClientHandler owner;

    public Stock(String name){
        this.name = name; owner = null;
    }

    public String getName(){return this.name;}

    public ClientHandler getOwner(){return this.owner;}

    public void setOwner(ClientHandler newOwner){this.owner = newOwner;}


    public boolean hasOwner(){
        if (this.owner == null){
            return false;
        }else{
            return true;
        }
    }
}
