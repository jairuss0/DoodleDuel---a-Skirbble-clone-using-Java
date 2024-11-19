
package skribbl_clone;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;


public class GameServer {
    
    private ServerSocket server;
    private ArrayList<ClientHandlerGame> players;
    private ArrayList<String> drawHistory;
    private ArrayList<String> messageHistory;
    private ArrayList<String> playerDetails;
    private int currentPlayerIndexTurn;
    private final int MAX_ROUND = 3;
    private final int MAX_PLAYERS = 10;
    private int currentRound = 1;
    // to manage timer in each thread, server we use  the ScheduledExecutorService class 
    // to handle the synchronization of time update to all clients in a single thread so it will not blocks the server thread
    private ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> timerTask; // reference to cancel the timer
    private final int DRAWING_TIME = 10;
    private final int CHOOSING_WORD_TIME = 10;
    private final int ANNOUNCING_TIME = 5;
    
    

    public GameServer() {
        players = new ArrayList<>();
        
    }
    
    private void startServer(){
        try{
            this.server = new ServerSocket(1234);
            // while the server is open wait for the client to connect
            while(!server.isClosed()){
                Socket socket = server.accept();
                System.out.println("a new client connected");
                
                // this class would handle every client that is connected
                // each object implement runnable interface so each instance has separate thread
                
                // Pass server class to each clientHandler
                ClientHandlerGame player = new ClientHandlerGame(socket,this);
                // create threads instances to each client objects
                Thread thread = new Thread(player);
                thread.start();
                
            }
        }catch(IOException e){
            System.err.println(e);
        }

    }
    
    // this is for the host command to start the game
    public void startGame(String command, ClientHandlerGame host){
        if(players.size() > 1 && host.getHostStatus() != false){
            host.notifyPlayer("HOST-COMMAND,GAME-STARTED");
            // method to start the game
            startTurn(players.size() - 1);
        }
        else{
            // send the message to specific client
            host.writer.println("ANNOUNCEMENT,SERVER,Minimum of 2 players to start!");
        }
    }
    
    private void startTurn(int turns){
        // the turns is in descending
        currentPlayerIndexTurn = turns;
        int[] remainingTime = {DRAWING_TIME}; // Mutable array to track remaining time
        // get the client object
        ClientHandlerGame player = players.get(currentPlayerIndexTurn);
        
        System.out.println(player.getUsername()+ " turn!  INDEX: " +currentPlayerIndexTurn);
        
        broadcastMessage("TURN-DRAW,"+player.getPlayerID());
        // broadcast to all cleints whose turn it is
        broadcastMessage("ANNOUNCEMENT,TURN,"+player.getUsername()+" is now drawing!",player);
        
        // schedule a task to broadcast the remaining time every second
        timerTask =  timer.scheduleAtFixedRate(() -> {
           
            if(remainingTime[0] > 0){
                // broadcast the timer updates 
                broadcastMessage("TIMER,"+remainingTime[0]);
                remainingTime[0]--; // decrement the element value 
            }
            else{
                timerTask.cancel(false); // Stop this task to prevent task overlapping 
                endPlayerTurn(); // invoke next turn if timer ended
            }
            
        },0,1,TimeUnit.SECONDS); // Initial delay 0, repeat every 1 second
    }
    
    private void endPlayerTurn(){
       // this would announce the scoring and then invoke the start Turn again
       if(currentPlayerIndexTurn > 0){
           currentPlayerIndexTurn -= 1; // decrement the player index to 1
       }
       else{
           System.out.println("new round");
           currentPlayerIndexTurn = players.size() - 1; // reset the turns
       }
       
       startTurn(currentPlayerIndexTurn);
    }
    
    private void assignPlayerHost(){
        // assign a host if there is no host
        if(!players.isEmpty() && noHostAvailable()){
            ClientHandlerGame host = players.get(0); // set player as host the current first player in the list
            host.setHostStatus(true);
            host.notifyPlayer("HOST-STATUS");
            broadcastMessage("ANNOUNCEMENT,SERVER,"+host.getUsername()+" is the Host! "+host.getHostStatus()); // broadcast who is the host
        }
        
    }
    // check if there is no host in the moment
    private boolean noHostAvailable(){
        for(ClientHandlerGame player : players){
            if(player.getHostStatus() == true){
                return false;
            }
        }
        return true;
    }
    
    
    
    // broadcast message from other clients
    public void broadcastMessage(String messageFromClient,ClientHandlerGame client){
        for(ClientHandlerGame player : players){
            // exclude the sender
            if(player != client){
                player.writer.println(messageFromClient);
            }
        }
    }
    // for announcement broadcast method
    public void broadcastMessage(String message){
        for(ClientHandlerGame player : players){
            player.writer.println(message);
        }
    }
       
    public int returnPlayerListSize(){
        return players.size();
    }

    // add player to the list
    public void addClientHandler(ClientHandlerGame player){
        
        players.add(player);
        broadcastMessage("ANNOUNCEMENT,JOIN,"+player.getUsername()+" joined the game!",player);
        // assign player host
        assignPlayerHost();
        
        // update the scoreboard (player list)
        broadcastClientList();
       
        
    }
    
    // remove player from the list
    public void removeClientHandler(ClientHandlerGame player){
        broadcastMessage("ANNOUNCEMENT,LEFT,"+player.getUsername()+" left the game!");
       
        players.remove(player);
        // reassign host if the player who left is the host
        assignPlayerHost();
        // update the scoreboard (player list)
        broadcastClientList();
    }
    
    public void broadcastClientList(){
        playerDetails = new ArrayList<>();
        // ensure the thread safety when trying to access the list just like the drawing point
        synchronized (players) {
            // sort the list of clients first in descinding order before sending it to each clients
            Collections.sort(players);
            int rank = 1;
            for(ClientHandlerGame player: players){
                playerDetails.add("#"+rank+","+player.getUsername()+","+player.getScore()+","+player.getPlayerID());
                rank++;
            }
        }
        // send the string arraylist of player details in each client
        for(ClientHandlerGame player: players){
            player.sendPlayerList(playerDetails);
        }
    }
    
    
    // close server socket
    public void closeServer(){
        try{
            if(server != null){
                server.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }
    
    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.startServer();
    }
    
    
   
    
}
