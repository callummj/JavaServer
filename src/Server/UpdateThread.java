package Server;

import java.util.Iterator;
import java.util.Map;

public class UpdateThread implements Runnable{


    private String updateMessage;


    public UpdateThread(){}

    public void setMessage(String updateMessage){
        this.updateMessage = updateMessage;
    }


    @Override
    public void run() {

        Iterator iterator = Market.clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            ClientHandler client = (ClientHandler) pair.getValue();
            client.sendMessage("[UPDATE]" + updateMessage);
        }

    }
}
