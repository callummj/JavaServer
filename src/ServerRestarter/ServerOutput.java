package ServerRestarter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;



//Outputs the server stream, as otherwise it would not be accessable.
public class ServerOutput implements Runnable {

    private Process serverProcess;

    public ServerOutput(Process server){
        this.serverProcess = server;
    }
    @Override
    public void run() {

        BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));


        while (true) { 
            try {
                if (reader.readLine()!= null){
                    System.out.println(reader.readLine());
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }
}
