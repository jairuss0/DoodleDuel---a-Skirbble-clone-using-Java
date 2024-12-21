


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
    private boolean isTurn;
    private boolean guessCorrectly;
    

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
            this.isTurn = false;
            this.guessCorrectly = false;
            this.playerID = gameServer.getNextPlayerID();
            writer.println(this.playerID); // send the id to the corresponding client thread
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
            // this is for drawing points coming from one of the clients
            case "DRAWING": 
                gameServer.broadcastMessage(messageFromClient, this);
                gameServer.addDrawingPointHistory(messageFromClient.trim()); // add drawing history
                break;
            // a guess or the messages coming from the chat
            case "GUESS": 
                gameServer.evaluateGuess(messageFromClient, this);
                break;
            // a command to clear each clients drawing panel
            case "CLEAR-DRAWING":
                gameServer.broadcastMessage(messageFromClient, this);
                break;
            // to undo certain drawing point to each clients
            case "UNDO-DRAWING": 
                gameServer.broadcastMessage(messageFromClient, this);
                break;
            // a command sent by the client that is host
            case "START-GAME":
                gameServer.startGame(message[1], this);
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
            System.err.println(e);
        }
    }
    
    // this is for individual broadcast message
    public void notifyPlayer(String message){
        writer.println(message);
    }
    
    // server would decide the score for each client who got the correct answer
    public void incrementScore(int plusScore){
        score += plusScore;
    }
    
    
    
    private void addClient(){
        gameServer.addClientHandler(this);
    }
    
    // send the players data in string format 
    public void sendPlayerList(String typeOfList,ArrayList<String> playerData){
        writer.println(typeOfList + String.join(";",playerData));
    }
    
    // this is for sorting the array in descending order
    @Override
    public int compareTo(ClientHandlerGame otherPlayer) {
        return Integer.compare(otherPlayer.score, this.score);
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
    
    public boolean getIsTurnStatus(){
        return isTurn;
    }
    public boolean getGuessCorrectlyStatus(){
        return guessCorrectly;
    }
    public void setUsername(String username){
        this.username = username;
    }
    
    public void setScore(int score){
        this.score = score;
    }
    
    public void setGuessedCorrectly(boolean status){
        this.guessCorrectly = status;
    
    }
    public void setHostStatus(boolean status){
        this.isHost = status;
    }
    
    public void setTurnStatus(boolean status){
        this.isTurn = status;
    }
    

    
    
    
}
