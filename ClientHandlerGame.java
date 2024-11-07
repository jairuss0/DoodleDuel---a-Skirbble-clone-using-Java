
package skribbl_clone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ClientHandlerGame implements Runnable{

    private Socket socket;
    private String username;
    private boolean isHost;
    private boolean isDrawing;
    private int score;
    public PrintWriter writer;
    public BufferedReader reader;
    private GameServer gameServer;

    public ClientHandlerGame(Socket socket, GameServer gameServer) {
        try{
            this.gameServer = gameServer;
            this.socket = socket;
            this.writer = new PrintWriter(socket.getOutputStream(),true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // read the username input by the client itself
            this.username = reader.readLine();
            this.score = 0;
            this.isHost = false;
            this.isDrawing = false;
            // add the client to the arraylist
            init();
        }catch(IOException e){
            closeEverything(socket, reader, writer);
        }
    }
    
    
    @Override
    public void run() {
       String messageFromClient;
       try{
           while((messageFromClient = reader.readLine()) != null){
                gameServer.broadcastMessage(messageFromClient);
           }
       }catch(IOException e){
           closeEverything(socket, reader, writer);
       }
    }
    
    private void closeEverything(Socket socket, BufferedReader bufferedReader, PrintWriter writer){
        // remove from the list if client left or disconnected
        gameServer.removeClientHandler(this);
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(writer != null){
                writer.close();
            }
            if(socket != null){
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
   
    
    // server would decide the score for each client who got the correct answer
    public void incrementScore(int plusScore){
        score += plusScore;
    }
    
    
    // getters and setters
    
    public String getUsername (){
        return username;
    }
    
    public int getScore(){
        return score;
    }
    
    public boolean getDrawAbility(){
        return isDrawing;
    }
    
    public boolean getHostStatus(){
        return isHost;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    
    public void setScore(int score){
        this.score = score;
    }
    
    public void setHostStatus(boolean status){
        this.isHost = status;
    }
    
    public void setDrawAbility(boolean status){
        this.isDrawing = status;
    }
    
    private void init(){
        gameServer.addClientHandler(this);
    }
    
}
