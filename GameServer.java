
package skribbl_clone;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;


public class GameServer {
    
    private ServerSocket server;
    private WordDictionary wordDictionary;
    private ArrayList<ClientHandlerGame> players;
    private ArrayList<String> drawHistory;
    private ArrayList<String> playerDetails;
    private ArrayList<ClientHandlerGame> sortedPlayers;
    private int currentPlayerIndexTurn;
    private final int MAX_ROUND = 3;
    private final int MAX_PLAYERS = 10;
    private int currentRound = 1;
    private String currentSecretWord;
    // to manage timer in each thread, server we use  the ScheduledExecutorService class 
    // to handle the synchronization of time update to all clients in a single thread so it will not blocks the server thread
    private ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> timerTask; // reference to cancel the timer
    private final int DRAWING_TIME = 20;
    private final int CHOOSING_WORD_TIME = 5;
    private final int REVEAL_WORD_TIME = 3;
    private final int ROUND_ANNOUNCING_TIME = 3;
    private int correct_guesses = 0;
    private boolean gameStarted = false;
 
    
    

    public GameServer() {
        players = new ArrayList<>();
        wordDictionary = new WordDictionary();
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
            startAnnouncingRoundPhase();
        }
        else{
            // send the message to specific client
            host.writer.println("ANNOUNCEMENT,SERVER,Minimum of 2 players to start!");
        }
    }
    
    private void startAnnouncingRoundPhase(){
        gameStarted = true;
        System.out.println("Game started : " + gameStarted);
        if(onePlayerLeft()){
            System.out.println("Game stopped before taking turns");
            // broadcast a message to clients to update their interface that the game stopped
            broadcastMessage("GAME-STOPPED");
            resetGame(); 
            if (timerTask != null) {
                timerTask.cancel(false); // Ensure any ongoing timer is stopped
            }
            return; // exit this method
        }
        int[] remainingTime = {ROUND_ANNOUNCING_TIME};
        System.out.println("Round : " + currentRound + "Starting");
        // broadcast to all clients what round it is
        broadcastMessage("ROUND-STATE,ROUND "+currentRound+",Round "+currentRound+" out of 3");
        // announce it for three seconds
        timerTask = timer.scheduleAtFixedRate(() -> {
            // check if only one player left each second of the timer 
            if(onePlayerLeft()){
                System.out.println("Game stopped! mid-turn, one player left");
                // broadcast an update to clients
                broadcastMessage("GAME-STOPPED");
                timerTask.cancel(false);
                resetGame();
                return; // exit this method
            }
            
            if(remainingTime[0] > 0){
                // broadcast time update
                broadcastMessage("TIMER-ROUND-STATE," + remainingTime[0]);
                System.out.println("Timer round state: " + remainingTime[0]);
                remainingTime[0]--;
            }
            else{
                timerTask.cancel(false);
                broadcastMessage("REMOVE-DIALOG-ROUND");
                startTurn(players.size() - 1); // start turn after timer
            }
        
        }, 0,1, TimeUnit.SECONDS);
        
        
    }
    
    private void startTurn(int turns){
        // check if more than one player currently in the game before turns
        if(onePlayerLeft()){
            System.out.println("Game stopped before taking turns");
            // broadcast a message to clients to update their interface that the game stopped
            broadcastMessage("GAME-STOPPED");
            resetGame();
            if (timerTask != null) {
                timerTask.cancel(false); // Ensure any ongoing timer is stopped
            }
            return; // exit this method
        }
        
        System.out.println("Start turn..");
        // the turns is in descending
        currentPlayerIndexTurn = turns;
        
        // get the client object
        ClientHandlerGame player = players.get(currentPlayerIndexTurn);
        System.out.println(player.getUsername() + " turn!  INDEX: " + currentPlayerIndexTurn);
        player.setTurnStatus(true); // turn status to true
        // set to 0 so that the drawing tools is not visible to anyone before word choosing
        broadcastMessage("TURN-DRAW," + 0);
        // start the word choosing phase
        startWordChoosingPhase(player);
        
    }
    
    private void startWordChoosingPhase(ClientHandlerGame player){
        // check if more than one player currently in the game before turns
        if(onePlayerLeft()){
            System.out.println("Game stopped before taking turns");
            // broadcast a message to clients to update their interface that the game stopped
            broadcastMessage("GAME-STOPPED");
            resetGame();
            if (timerTask != null) {
                timerTask.cancel(false); // Ensure any ongoing timer is stopped
            }
            return; // exit this method
        }
        System.out.println("word choosing phase starts..");
        
        int[] remainingTime = {CHOOSING_WORD_TIME};
        // broadcast to all cleints whose turn it is
        broadcastMessage("ANNOUNCEMENT,TURN,Server is now choosing a word for "+ player.getUsername());
        broadcastMessage("WORD-CHOOSING-STATE,Server is now choosing a word for "+ player.getUsername(),player);
        player.notifyPlayer("SERVER-CHOOSING-WORD,Server is now choosing a word for You");
        broadcastClientList(); // update the player list to display who is turn to draw
        // schedule a task to broadcast the remaining time every second
        timerTask = timer.scheduleAtFixedRate(() -> {
            // check if only one player left each second of the timer 
            if(onePlayerLeft()){
                System.out.println("Game stopped! mid-turn, one player left");
                // broadcast an update to clients
                broadcastMessage("GAME-STOPPED");
                timerTask.cancel(false);
                resetGame();
                return; // exit this method
            }
            
            if (remainingTime[0] > 0) {
                // broadcast the timer updates 
                broadcastMessage("TIMER-WORD-CHOOSING," + remainingTime[0]);
                System.out.println("Timer word choosing state: " + remainingTime[0]);
                remainingTime[0]--; // decrement the element value 

            } else {
                timerTask.cancel(false); // Stop this task to prevent task overlapping
                currentSecretWord = wordDictionary.serverGetRandomWord(); // fetch word for players to guess
                broadcastMessage("REMOVE-DIALOG-WORD");
                System.out.println("Random word is: " + currentSecretWord);
                broadcastMessage("SECRET-WORD,"+maskSecretWord(currentSecretWord), player); // send the masked word to player
                player.notifyPlayer("CHOSEN-WORD,"+currentSecretWord); // send the word to current turn player
                // invoke drawing phase if user does not choose a word
                startDrawingPhase(player);
            }

        }, 0, 1, TimeUnit.SECONDS); // Initial delay 0, repeat every 1 second

    
    }
    
    
    public void startDrawingPhase(ClientHandlerGame player){
        if(onePlayerLeft()){
            System.out.println("Game stopped before taking turns");
            // broadcast a message to clients to update their interface that the game stopped
            broadcastMessage("GAME-STOPPED");
            resetGame();
            if (timerTask != null) {
                timerTask.cancel(false); // Ensure any ongoing timer is stopped
            }
            return; // exit this method
        }
        int[] remainingTime = {DRAWING_TIME};
        System.out.println("Drawing phase starts...");
        broadcastMessage("TURN-DRAW," + player.getPlayerID()); // broadcast to all clients whose player id turn
        broadcastMessage("ANNOUNCEMENT,TURN," + player.getUsername() + " is now drawing!");
       
        // schedule a task to broadcast the remaining time every second
        timerTask = timer.scheduleAtFixedRate(() -> {
            // check if only one player left each second of the timer 
            if(onePlayerLeft()){
                System.out.println("Game stopped! mid-turn, one player left");
                // broadcast an update to clients
                broadcastMessage("GAME-STOPPED");
                timerTask.cancel(false);
                resetGame();
                return; // exit this method
            }
            
            if (remainingTime[0] > 0) {
                // broadcast the timer updates 
                broadcastMessage("TIMER-DRAW," + remainingTime[0]);
                System.out.println("Timer drawing state: " + remainingTime[0]);
                remainingTime[0]--; // decrement the element value 

            } else {
                timerTask.cancel(false); // Stop this task to prevent task overlapping 
                broadcastMessage("CLEAR-DRAWING"); // clear panel after timer ended
                player.setTurnStatus(false); // set the turn status to false;
                broadcastClientList(); // send the updated player list
                revealSecretWordPhase(); // invoke next turn if timer ended
            }

        }, 0, 1, TimeUnit.SECONDS); // Initial delay 0, repeat every 1 second
    
    }
    
    private void revealSecretWordPhase(){
        if(onePlayerLeft()){
            System.out.println("Game stopped before taking turns");
            // broadcast a message to clients to update their interface that the game stopped
            broadcastMessage("GAME-STOPPED");
            resetGame();
            if (timerTask != null) {
                timerTask.cancel(false); // Ensure any ongoing timer is stopped
            }
            return; // exit this method
        }
        int[] remainingTime = {REVEAL_WORD_TIME};
        System.out.println("Revealing word phase..");
        broadcastMessage("TURN-DRAW," + 0); // hide the drawing tools to all players
        broadcastMessage("REVEAL-WORD-STATE,The word was '"+currentSecretWord+"',"+currentSecretWord);
        // schedule a task to broadcast the remaining time every second
        timerTask = timer.scheduleAtFixedRate(() -> {
            // check if only one player left each second of the timer 
            if(onePlayerLeft()){
                System.out.println("Game stopped! mid-turn, one player left");
                // broadcast an update to clients
                broadcastMessage("GAME-STOPPED");
                timerTask.cancel(false);
                resetGame();
                return; // exit this method
            }
            
            if (remainingTime[0] > 0) {
                // broadcast the timer updates 
                broadcastMessage("TIMER-REVEAL-WORD," + remainingTime[0]);
                System.out.println("Timer reveal word state: " + remainingTime[0]);
                remainingTime[0]--; // decrement the element value 

            } else {
                timerTask.cancel(false); // Stop this task to prevent task overlapping 
                broadcastMessage("REMOVE-REVEAL-DIALOG");
                endPlayerTurn(); // invoke next turn if timer ended
            }

        }, 0, 1, TimeUnit.SECONDS); // Initial delay 0, repeat every 1 second
        
    }
    
    private void endPlayerTurn(){
        System.out.println("End turn.. resetting turn");
        correct_guesses = 0; // reset the correctGuesses;
        // this would announce the scoring and then invoke the start Turn again
        if (currentPlayerIndexTurn > 0) {
            currentPlayerIndexTurn -= 1; // decrement the player index to 1
            // start new turns
            startTurn(currentPlayerIndexTurn);
        } else {
            System.out.println("New round");
            currentRound++;
            timerTask.cancel(false); // ensure to stop any timer task 
            
            startAnnouncingRoundPhase(); // announce the round
        }
        
       
    }
    
    private String maskSecretWord(String word){
        String mask = "";
        for(int i = 0; i < word.length(); i++){
            mask += "_ ";
        }
        
        return mask;
        
    }
    // evaluate guess from players
    public void evaluateGuess(String guessFromClient, ClientHandlerGame player){
        String[] message = guessFromClient.split(",");
            
        if(checkIfCorrectGuess(message[2])){
            // increment the score of the player and the player who is drawing
            ClientHandlerGame drawer = returnPlayerDrawer();
            player.incrementScore(scoreWithinTimeFrame(message[3]));
            drawer.incrementScore(50); // 55 for drawer for each player guessed correctly
            // add a scoring based system after testing
            broadcastMessage("ANNOUNCEMENT,GUESS,"+player.getUsername()+" Guessed the Word!");
            player.notifyPlayer("GUESSED,"+currentSecretWord); // reveal the word to the player
            correct_guesses++;
            
            // end the turn if all players guessed the secret word
            if(correct_guesses == (players.size() - 1)){
                System.out.println("all players guessed the word!");
                timerTask.cancel(false);
                drawer.setTurnStatus(false);
                broadcastClientList(); // broadcast updated player list
                revealSecretWordPhase();
            }
            
        }
        else{
            broadcastMessage(guessFromClient);
            System.out.println("test secretword: " + currentSecretWord);
            System.out.println("test guess: " + message[2]);
        }
    
    }
    // return drawer object
    private ClientHandlerGame returnPlayerDrawer(){
        for(ClientHandlerGame player : players){
            if(player.getIsTurnStatus() == true){
                return player;
            }
        }
        return null;
    }
    // check if the player guesse is correct
    private boolean checkIfCorrectGuess(String wordGuess){
        return wordGuess.trim().equalsIgnoreCase(currentSecretWord.trim());
    }
    
    // implement scoring system based on the player guess timeframe
    private int scoreWithinTimeFrame(String timeFrame){
        int time = Integer.parseInt(timeFrame);
        
        if(time <= 80 && time >= 70){
            return 150;
        }
        else if(time < 70 && time > 60){
            return 100;
        }
        else if(time < 60 && time > 50){
            return 60;
        }
        else if(time < 50 && time > 40){
            return 50;
        }
        else if(time < 40 && time > 20){
            return 40;
        }
        else{
            return 25;
        }
            
    
    }
    
    // assign a player host 
    private void assignPlayerHost(){
        // assign a host if there is no host
        if(!players.isEmpty() && noHostAvailable() && !gameStarted){
            ClientHandlerGame host = players.get(0); // set player as host the current first player in the list
            host.setHostStatus(true);
            host.notifyPlayer("HOST-STATUS,OUT-GAME");
            broadcastMessage("ANNOUNCEMENT,SERVER,"+host.getUsername()+" is the Host! "); // broadcast who is the host
        }
        else if(!players.isEmpty() && noHostAvailable() && gameStarted){
            ClientHandlerGame host = players.get(0); // set player as host the current first player in the list
            host.setHostStatus(true);
            host.notifyPlayer("HOST-STATUS,IN-GAME");
            broadcastMessage("ANNOUNCEMENT,SERVER,"+host.getUsername()+" is the Host! "); // broadcast who is the host
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
    // reset game state values
    public void resetGame(){
        //drawHistory.clear();
        currentRound = 1;
        correct_guesses = 0;
        broadcastMessage("CLEAR-DRAWING");
        gameStarted = false;
        removeTurnStatus();
        broadcastClientList();
    }
    
    // check if one player only
    private boolean onePlayerLeft(){
       
        return players.size() <= 1;
    }
    
    private void removeTurnStatus() {
        for (ClientHandlerGame player : players) {
            player.setTurnStatus(false);
            player.setScore(0); // reset the score also
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
        System.out.println(player.getUsername() + " left the game");
        if(player.getIsTurnStatus()){
            // disregard what happend to current round and next the turn
            timerTask.cancel(false);
            broadcastMessage("CLEAR-DRAWING");
            endPlayerTurn();
        }
        players.remove(player);
        // reassign host if the player who left is the host
        assignPlayerHost();
        // update the scoreboard (player list)
        broadcastClientList();
    }
    // broadcast to each player the details of all players
    public void broadcastClientList(){
        playerDetails = new ArrayList<>();
        
        // ensure the thread safety when trying to access the list just like the drawing point
        synchronized (players) {
            sortedPlayers = new ArrayList<>(players); // copy the original arraylist of player objects
            // sort the list of clients first in descinding order before sending it to each clients
            Collections.sort(sortedPlayers);
            int rank = 1;
            for(ClientHandlerGame player: sortedPlayers){
                playerDetails.add("#"+rank+","+player.getUsername()+","+player.getScore()+","+player.getPlayerID()+","+player.getIsTurnStatus());
                rank++;
            }
            
        }
        
        // send the string arraylist of player details in each client
        for(ClientHandlerGame player: players){
            player.sendPlayerList(playerDetails);
        }
    }
    
    public void addDrawingPointHistory(){
        
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
