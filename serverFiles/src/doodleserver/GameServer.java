
package doodleserver;

import doodleserver.WordDictionary;
import doodleserver.ClientHandlerGame;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.*;



public class GameServer {
    
    private ServerSocket server;
    private WordDictionary wordDictionary;
    private ArrayList<ClientHandlerGame> players; 
    private ArrayList<String> drawHistory; // drawing point history to player who joined in the middle of the game
    private ArrayList<String> playerDetails; // Stores the each player details in string format 
    private HashMap<String,Integer> playerSubRoundScores; // stores each player sub-round points
    private ArrayList<ClientHandlerGame> sortedPlayers; // stores the sorted players details for the player board 
    private int currentPlayerIndexTurn;
    private int MAX_ROUND = 3; // 3 is the default
    private final int MAX_PLAYERS = 8;
    private int currentRound = 1;
    private String currentSecretWord = "";
    // to manage timer in each thread, server we use  the ScheduledExecutorService class 
    // to handle the synchronization of time update to all clients in a single thread so it will not block the server thread
    private ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> timerTask; // reference to cancel the timer
    private final int DRAWING_TIME = 80;
    private final int CHOOSING_WORD_TIME = 3;
    private final int REVEAL_WORD_TIME = 5;
    private final int ROUND_ANNOUNCING_TIME = 3;
    private final int RANK_ANNOUNCING_TIME = 15;
    private int correct_guesses = 0;
    private boolean gameStarted = false;
    private static int playerIDCounter = 0; // Static counter for unique IDs
    
    public GameServer() {
        players = new ArrayList<>();
        wordDictionary = new WordDictionary();
        drawHistory = new ArrayList<>();
        playerSubRoundScores = new HashMap<>();
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
            System.out.println("Server is down");
            System.err.println(e);
            closeServer();
        }

    }
    
    // this is for the host command to start the game
    public void startGame(String command, ClientHandlerGame host){
        if(players.size() > 1 && host.getHostStatus() != false){
            host.notifyPlayer("HOST-COMMAND,GAME-STARTED");
            // method to start the game
            MAX_ROUND = Integer.parseInt(command);
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
        System.out.println("Round : " + currentRound + " Starting");
        // broadcast to all clients what round it is
        broadcastMessage("ROUND-STATE,ROUND "+currentRound+",Round "+currentRound+" out of "+MAX_ROUND);
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
        broadcastMessage("ANNOUNCEMENT,TURN,A word is being chosen for "+ player.getUsername() + "!");
        broadcastMessage("WORD-CHOOSING-STATE,A word is being chosen for '"+ player.getUsername()+"'",player);
        player.notifyPlayer("SERVER-CHOOSING-WORD,Game is picking a word for YOU");
        broadcastClientList("PLAYER-LIST:"); // update the player list to display who is turn to draw
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
                resetGuessCorrectlyStatus(); // reset the guess correctly status to remove green bg
                broadcastMessage("CLEAR-DRAWING"); // clear panel after timer ended
                player.setTurnStatus(false); // set the turn status to false;
                broadcastClientList("PLAYER-LIST:"); // send the updated player list
                revealSecretWordPhase(); // invoke next turn if timer ended
                drawHistory.clear(); // clear the drawing point history
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
        broadcastSubRoundPlayerList(); // broadcast the sub round player points gain
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
                resetPlayersSubRoundScores();
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
            currentRound++;
            timerTask.cancel(false); // ensure to stop any timer task 
            // check if round hit the max round
            if(currentRound > MAX_ROUND){
                System.out.println("Round maxed out!");
                announceWinner();
            }
            else{
                System.out.println("New round");
                startAnnouncingRoundPhase(); // announce the round
            }
            
        }
        
       
    }
    
    private void announceWinner(){
       
        int[] remainingTime = {RANK_ANNOUNCING_TIME};
        System.out.println("Rank announcing phase...");
        broadcastMessage("TURN-DRAW," + 0); // hide the drawing tools to all players
        broadcastClientList("RANK-LIST:"); // broadcast players details
        System.out.println("WINNER,The Winner is "+ getWinnerUsername()+"!");
        broadcastClientList("WINNER,"+getWinnerUsername()+" is the Winner!");
        // schedule a task to broadcast the remaining time every second
        timerTask = timer.scheduleAtFixedRate(() -> {
            
            if (remainingTime[0] > 0) {
                // broadcast the timer updates 
                broadcastMessage("TIMER-RANK-ANNOUNCE," + remainingTime[0]);
                System.out.println("Timer rank announce state: " + remainingTime[0]);
                remainingTime[0]--; // decrement the element value 

            } else {
                timerTask.cancel(false); // Stop this task to prevent task overlapping 
                broadcastMessage("REMOVE-RANK-ANNOUNCE-DIALOG");
                broadcastMessage("GAME-STOPPED"); // reset the players game state
                System.out.println("GAME ENDED");
                resetGame(); // reset the server game state
            }

        }, 0, 1, TimeUnit.SECONDS); // Initial delay 0, repeat every 1 second
    }
    
   
    // mask the secret word
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
            ClientHandlerGame drawer = returnPlayerDrawer();
            // increment the score of the player and the player who is drawing
            int score = scoreWithinTimeFrame(message[3]);
            player.incrementScore(score);
            drawer.incrementScore(40); // 40 for drawer for each player guessed correctly
            
            player.setGuessedCorrectly(true); // set guess correctly to true to reflect green background in the client side
            // add a scoring based system after testing
            // update the value of Second
            // Using computeIfPresent()
            playerSubRoundScores.computeIfPresent(drawer.getUsername(), (key, oldValue) -> oldValue + 40);
            playerSubRoundScores.put(player.getUsername(), score); // put the point that the player has scored in the current sub-round
            broadcastMessage("ANNOUNCEMENT,GUESS,"+player.getUsername()+" Guessed the Word!");
            player.notifyPlayer("GUESSED,"+currentSecretWord); // reveal the word to the player
            broadcastMessage("PLAYER-GUESSED-CORRECTLY");
            correct_guesses++;
            broadcastClientList("PLAYER-LIST:"); // broadcast updated player list
            // end the turn if all players guessed the secret word
            // the player who is drawing not counted
            if(correct_guesses == (players.size() - 1)){
                broadcastSubRoundPlayerList(); // broadcast to each client the sub round summary
                resetGuessCorrectlyStatus(); // reset the guess correctly status to remove green background
                broadcastMessage("CLEAR-DRAWING"); // clear panel if all players were guessed correctly
                System.out.println("all players guessed the word!");
                timerTask.cancel(false);
                drawer.setTurnStatus(false);
                broadcastMessage("CLEAR-DRAWING"); // clear players drawing interface 
                drawHistory.clear(); // clear draw history
                broadcastClientList("PLAYER-LIST:"); // broadcast updated player list
                revealSecretWordPhase();
            }
            
        }
        else{
            // put the player who guesses wrong and their points as zero
            playerSubRoundScores.put(player.getUsername(),0);
            // if player guess were wrong, it would be broadcasted
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
    
    private String getWinnerUsername(){
        // return the player with the highest score
        // for each player in the list comparator call the getScore and compares it to each player
        ClientHandlerGame playerWinner = Collections.max(players,Comparator.comparingInt(ClientHandlerGame::getScore));
        System.out.println("Player with the highest score: " + playerWinner.getUsername());
        return playerWinner.getUsername();
    }
    
    // assign a player host 
    private void assignPlayerHost(){
        // assign a host if there is no host
        if(!players.isEmpty() && noHostAvailable() && !gameStarted){
            ClientHandlerGame host = players.get(0); // set player as host the current first player in the list
            host.setHostStatus(true);
            host.notifyPlayer("HOST-STATUS,OUT-GAME");
            broadcastMessage("ANNOUNCEMENT,SERVER,"+host.getUsername()+" is the Host! "); // broadcast who is the host
        } // a seperate assigning condition to prevent disrupting the game by not displaying the host button
        // in the middle of the game
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
        drawHistory.clear();
        broadcastMessage("CLEAR-DRAWING");
        currentRound = 1;
        correct_guesses = 0;
        gameStarted = false;
        currentSecretWord = "";
        removeTurnStatus();
        broadcastClientList("PLAYER-LIST:");
        System.out.println("Game-state restarted.");
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
    
    private void resetGuessCorrectlyStatus(){
        for (ClientHandlerGame player : players) {
            player.setGuessedCorrectly(false);
           
        }
    }
    
    private void resetPlayersSubRoundScores(){
        for(String player : playerSubRoundScores.keySet()){
            // update all the  value of  the key in the hashmap to be 0
            playerSubRoundScores.put(player,0);
        }
    
    }
    
    // broadcast message to other clients
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
        // check if total players is less than to max players
        if(players.size() < MAX_PLAYERS && !duplicatedUsername(player)){
            players.add(player);
            broadcastMessage("ANNOUNCEMENT,JOIN," + player.getUsername() + " joined the game!", player);
            // initialize the player in the hashmap to still display if they did not guess
            playerSubRoundScores.put(player.getUsername(), 0);
            if (gameStarted) {
                System.out.println(player.getUsername() + " joined in the middle of the Game!");
                // broadcast the round status and the current secret word
                player.notifyPlayer("ON-GOING-JOINED,Round " + currentRound + " out of "+MAX_ROUND);
                player.notifyPlayer("SECRET-WORD," + maskSecretWord(currentSecretWord));
                // send the drawing history to player
                sendDrawingHistory(player);
            }
            // assign player host
            assignPlayerHost();
            // update the scoreboard (player list)
            broadcastClientList("PLAYER-LIST:");
        }
        else if(duplicatedUsername(player)){
            player.notifyPlayer("DUPLICATED-USERNAME");
            System.out.println(player.getUsername() + " client terminated due to duplicated username");
        }
        else{
            player.notifyPlayer("PLAYERS-MAXED");
            System.out.println(player.getUsername() + " cannot join the game anymore");
        }
        
    
    }
    
    private void sendDrawingHistory(ClientHandlerGame player){
        if(!drawHistory.isEmpty()){
            for (String drawingHistory : drawHistory) {
                player.notifyPlayer(drawingHistory);
            }
        }
     
    }
    
    // Method to get the next unique ID
    // use synchronized since this will be used in multi-threaded environment
    public synchronized int getNextPlayerID() {
        return ++playerIDCounter; // Increment and return the counter
    }
    
    // remove player from the list
    public void removeClientHandler(ClientHandlerGame player){
        // prevent from announcing players who can't join due to players were full
        if(players.contains(player)){
            broadcastMessage("ANNOUNCEMENT,LEFT,"+player.getUsername()+" left the game!");
        }
        System.out.println(player.getUsername() + " left the game");
        // check the player left were their turn
        if(player.getIsTurnStatus()){
            // disregard what happend to current round and next the turn
            timerTask.cancel(false);
            broadcastMessage("CLEAR-DRAWING");
            endPlayerTurn();
        }
        players.remove(player);
        playerSubRoundScores.remove(player.getUsername()); // remove the player from the hashmap
        
        // reassign host if the player who left is the host
        assignPlayerHost();
        // update the scoreboard (player list)
        broadcastClientList("PLAYER-LIST:");
    }
    
    // broadcast to each player the details of all players
    public void broadcastClientList(String typeOfList){
        playerDetails = new ArrayList<>();
        
        // ensure the thread safety when trying to access the list just like the drawing point
        synchronized (players) {
            sortedPlayers = new ArrayList<>(players); // copy the original arraylist of player objects
            // sort the list of clients first in descinding order before sending it to each clients
            Collections.sort(sortedPlayers);
            int rank = 1;
            for(ClientHandlerGame player: sortedPlayers){
                playerDetails.add("#"+rank+","+player.getUsername()+","+player.getScore()+","+player.getPlayerID()+","+player.getIsTurnStatus()+","+
                        player.getGuessCorrectlyStatus());
                rank++;
            }
            
        }
        
        // send the string arraylist of player details in each client
        for(ClientHandlerGame player: players){
            player.sendPlayerList(typeOfList,playerDetails);
        }
    }
    
    public void broadcastSubRoundPlayerList(){
        playerDetails = new ArrayList<>();

        for(String player : playerSubRoundScores.keySet()){
            System.out.println("Key: " + player + "score: " + playerSubRoundScores.get(player));
            playerDetails.add(player+","+playerSubRoundScores.get(player));
            
        }
        // send the sub round summary to all players
        for(ClientHandlerGame player : players){
            player.sendPlayerList("SUB-ROUND-LIST:", playerDetails);
        }
    }
    
    public void addDrawingPointHistory(String drawingPoint){
        drawHistory.add(drawingPoint);
    }
    
    private boolean duplicatedUsername(ClientHandlerGame client){
        for(ClientHandlerGame player: players){
            if(player.getUsername().equals(client.getUsername())){
                return true;
            }
        }
        
        return false;
    }
   
    
    // close server socket
    public void closeServer(){
        broadcastMessage("SERVER-DOWN");
        try{
            if(server != null){
                server.close();
            }
        }catch(IOException e){
            System.err.println(e);
        }
        
    }
    
    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.startServer();
    }
    
    
    
}
