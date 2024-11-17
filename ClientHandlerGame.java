
package skribbl_clone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;


public class ClientHandlerGame implements Runnable, Comparable<ClientHandlerGame>{

    private Socket socket;
    private String username;
    private boolean isHost;
    private int playerID;
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
            this.playerID = createID();
            writer.println(this.playerID);
            // add the client to the arraylist
            addClient();
        }catch(IOException e){
            closeEverything(socket, reader, writer);
        }
    }
    
    
    @Override
    public void run() {
       String messageFromClient;
       try{
           while((messageFromClient = reader.readLine()) != null){
                processMessage(messageFromClient); 
           }
       }catch(IOException e){
           closeEverything(socket, reader, writer);
       }
    }
    
    // this will evaluate what clients sent to server
    private void processMessage(String messageFromClient){
        String[] message = messageFromClient.split(",");
        
        switch(message[0]){
            case "DRAWING":
                gameServer.broadcastMessage(messageFromClient, this);
                break;
            case "GUESS":
                gameServer.broadcastMessage(messageFromClient);
                break;
            case "ANNOUNCEMENT":
                gameServer.broadcastMessage(messageFromClient);
                break;
            case "CLEAR-DRAWING":
                gameServer.broadcastMessage(messageFromClient, this);
                break;
            case "UNDO-DRAWING":
                gameServer.broadcastMessage(messageFromClient, this);
                break;
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
    
    // create simple unique id for each player
    private int createID(){
        return gameServer.returnPlayerListSize() + 1;
    }
    
    // getters and setters
    
    public String getUsername (){
        return username;
    }
    
    public int getScore(){
        return score;
    }
    
    public int getPlayerID(){
        return playerID;
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
    
    private void addClient(){
        gameServer.addClientHandler(this);
    }
    
    // send the players data in string format 
    public void sendPlayerList(ArrayList<String> playerData){
        writer.println("PLAYER-LIST:"+ String.join(";",playerData));
    }

    @Override
    public int compareTo(ClientHandlerGame otherPlayer) {
        return Integer.compare(otherPlayer.score, this.score);
    }
    
}
