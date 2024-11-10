
package skribbl_clone;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class GameServer {
    
    private ServerSocket server;
    private ArrayList<ClientHandlerGame> players;
    private ArrayList<String> drawHistory;
    private ArrayList<String> messageHistory;

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
                
                // Pass clientHandlers to each ClientHandleGame instance
                // Pass server class to each clientHandler
                ClientHandlerGame player = new ClientHandlerGame(socket,this);
                Thread thread = new Thread(player);
                thread.start();
                
            }
        }catch(IOException e){
            e.printStackTrace();
        }
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
    

    // add player to the list
    public void addClientHandler(ClientHandlerGame player){
        players.add(player);
        broadcastMessage("ANNOUNCEMENT,GREEN,"+player.getUsername()+" joined the game!",player);
    }
    
    // remove player from the list
    public void removeClientHandler(ClientHandlerGame player){
        broadcastMessage("ANNOUNCEMENT,RED,"+player.getUsername()+" left the game!");
        players.remove(player);
        
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
