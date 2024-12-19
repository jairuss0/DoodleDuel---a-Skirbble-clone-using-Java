
package skribbl_clone;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.net.*;
import java.util.Stack;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


public class GameClient extends JFrame implements MouseMotionListener {

    private String username;
    private int id;
    private boolean hostStatus = false;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean canDraw = false;
    // List to store drawPoint object 
    private Stack<DrawPoint> drawingPoint;
    private DrawPoint drawPoint;
   
    // for styling the textPane
    StyledDocument chatDoc,scoreBoardDoc,rankPaneDoc,subRoundDoc;
  
    SimpleAttributeSet attrBold = new SimpleAttributeSet();
    SimpleAttributeSet attrNormal = new SimpleAttributeSet();
    SimpleAttributeSet attrsAnnouncement = new SimpleAttributeSet();
    SimpleAttributeSet attrGreenBg = new SimpleAttributeSet();
    SimpleAttributeSet attrCenter = new SimpleAttributeSet();
    
    private int brushSize; // brush size
    private Color brushColor; // brush color
    private int pointsToRemove = 10;
    Point point; 
    Color color;
    StringBuilder rankBoardStringBuilder, subRoundStringBuilder;
    JDialog roundDialog, serverChoosingWordDialog, revealWordDialog, declareFinalRanksDialog;
    JLabel serverLabelText, winnerLabelText, revealWordLabelText;
    private Image backgroundImage;
    JTextPane rankPane, subRoundSummaryPane;
    
    
    
   
    public GameClient(Socket socket, String username) {
        setBackgroundImage("assets/gameplayBg.png");
        
        this.brushSize = 13; // initialize brush size to 13
        this.brushColor = Color.black; // initialize color to black
        this.drawingPoint = new Stack<>(); // use the stacks for the drawing points (LAST IN FIRST OUT)
        this.socket = socket;
        this.username = username;
        try{
           this.writer = new PrintWriter(socket.getOutputStream(),true);
           this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           // send the username in the server
           writer.println(username);
           this.id = Integer.parseInt(reader.readLine()); // reads the id that the client handler sent
        }catch(IOException e){
            closeEverything(socket, reader, writer);
        }
        initDialogComponents();
        initComponents();
        roundTitleLabel.setVisible(false);
        startGameBtn.setVisible(false); // initualize host button as not visible
        customRound.setVisible(false);
        DrawingPanelTools.setVisible(false); // initialize drawing panel tools as not visible
    }
    
    private void initDialogComponents(){
        serverLabelText = new JLabel();
        winnerLabelText = new JLabel();
        revealWordLabelText = new JLabel();
        roundDialog = new JDialog();
        revealWordDialog = new JDialog();
        serverChoosingWordDialog = new JDialog();
        declareFinalRanksDialog = new JDialog();
        rankPane = new JTextPane();
        rankPane.setEditable(false);
        rankPane.setForeground(Color.white);
        rankPane.setBackground(new Color(59, 59, 59));
        rankPane.setFont(new java.awt.Font("Comic sans MS", 0, 23));
        subRoundSummaryPane = new JTextPane();
        subRoundSummaryPane.setEditable(false);
        subRoundSummaryPane.setForeground(Color.white);
        subRoundSummaryPane.setBackground(new Color(59, 59, 59));
        subRoundSummaryPane.setFont(new java.awt.Font("Comic sans MS", 0, 23));
    }
    
    
    // switch the ability to draw if playerID were not met
    private void turnToDraw(int playerID){
        if(id == playerID){
            canDraw = true;
            enableDrawingTools();
            drawingPanelFunctionality();
            // disable the textfield
            chatInpuTextField.setEditable(false);
        }
        else{
            canDraw = false;
            disableDrawingTools();
            drawingPanelFunctionality();
            chatInpuTextField.setEditable(true);
        }
        System.out.println(username + " ability to draw: " + canDraw);
    }
    
    public void setBackgroundImage(String imagePath) {
        backgroundImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
        repaint(); // Repaint the panel to apply the new background image
    }
    // enable and disable method for drawingTools and drawing ability
    private void enableDrawingTools(){
        canDraw = true;
        DrawingPanelTools.setVisible(true);
    }
    private void disableDrawingTools(){
        canDraw = false;
        DrawingPanelTools.setVisible(false);
    }
    
    // the mouse listener event for the ability to draw
    private void drawingPanelFunctionality(){
        // Remove all existing mouse motion listeners first to avoid duplication
        for (MouseMotionListener listener : drawingPanel.getMouseMotionListeners()) {
            drawingPanel.removeMouseMotionListener(listener);
        }
        // check if the user boolean canDraw is true to be able to draw in the panel
        if(canDraw){
            // add mouse listener to drawing panel
            drawingPanel.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    // Use synchronized block to avoid concurrent modification errors
                    synchronized (drawingPoint) {
                        drawingPoint.push(new DrawPoint(e.getPoint(), brushColor, brushSize));   // push the dragged point to the stack list
                    }
                    drawingPanel.repaint();     // this will call the paintComponent method to refresh and show updated drawing points
                    // send the mouse point to server;
                    sendMouseDrawingPoint(e.getPoint());
                }

            });
        }
        
    }
    // send the drawing coordinates to server 
    private void sendMouseDrawingPoint(Point point){
        // send the point coordinate along with rgb code and brush size
        int red = brushColor.getRed();
        int blue = brushColor.getBlue();
        int green = brushColor.getGreen();
        writer.println("DRAWING,"+ point.x+","+point.y+","+red+","+green+","+blue+","+brushSize);
        
    }
    // determine to hide or unhide the start button
    private void hideHostBtn(){
        startGameBtn.setVisible(false);
        customRound.setVisible(false);
        roundTitleLabel.setVisible(false);
    }
    private void showHostBtn(){
        if(hostStatus){
            startGameBtn.setVisible(true);
            customRound.setVisible(true);
            roundTitleLabel.setVisible(true);
        }
        
    }
    
    // send client's message to server
    public void sendMessage(){
        String message = chatInpuTextField.getText().trim();
        if (!message.isEmpty()) {
            chatInpuTextField.setText("");  // Clear the text field
            writer.println("GUESS,"+username + ":, " + message+","+timerLabel.getText());  // Send message to the server
        }
            
    }
    
    // function to listen for message or updates from the server
    // use thread so that user can still perform actions without need to 
    // wait for the message to be received
    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                   String messageFromServer = "";
                   while(socket.isConnected() && (messageFromServer = reader.readLine()) != null){
                       processMessage(messageFromServer.trim());
                       checkClientListMessage(messageFromServer.trim());
                   }
                }catch(IOException e){
                    closeEverything(socket, reader, writer);
                }
            }
        }).start();
    }
    
    // in this method we would evaluate what type of message the client or server sent
    private void processMessage(String receivedMessage){
        // initialize the document style for the chat and scoreboard text pane
        chatDoc = chatPane.getStyledDocument();
        scoreBoardDoc = scoreBoardPane.getStyledDocument();
        rankPaneDoc = rankPane.getStyledDocument();
        subRoundDoc = subRoundSummaryPane.getStyledDocument();
        
        String[] message = receivedMessage.split(",");
       
        switch(message[0]){
            case "ANNOUNCEMENT": // listens to game state
                evaluateAnnouncementMessage(message);
                break;
            case "GUESS": // listens to other players chats (their guesses)
                evaluateClientMessage(message);
                break;
            case "DRAWING": // listens to the player who is drawing
                evaluateClientDrawingPoints(message);
                break;
            case "CLEAR-DRAWING": // clear client-side drawing panel
                clearDrawingPanel();
                break;
            case "UNDO-DRAWING": // undo drawing
                undoDrawingPanel();
                break;
            case "TURN-DRAW" : // check if the player's turn to draw to enable the draw tools otherwise disable it
                turnToDraw(Integer.parseInt(message[1]));
                break;
            case "TIMER-DRAW" : // update timer coming from the server
                updateDrawingTimer(message[1]);
                break;
            case "HOST-STATUS" :// make the player host and able the host start button
                hostStatus = true;
                if (message[1].equals("OUT-GAME")) {
                    showHostBtn();
                } else if (message[1].equals("IN-GAME")) {
                    hideHostBtn();
                }
                break;
            
            case "HOST-COMMAND": // a command coming from the server triggered if the host started the game
                System.out.println(message[1]);
                if(message[1].equals("GAME-STARTED")){
                    hideHostBtn(); // hide the start button to not disrupt the game state
                }
                break;
            case "ROUND-STATE":
                secretWordLabel.setText("WORD");
                showRoundDialog(message); // display the details about what round it is
                break;
            case "TIMER-ROUND-STATE":
                updateTimer(message[1]);
                break;// to update the timer
            case "TIMER-WORD-CHOOSING":
                updateTimer(message[1]); // to update the timer
                break;
            case "WORD-CHOOSING-STATE": // display the dialog where it informs players that the server were choosing a word
                
                 secretWordLabel.setText("WORD");
                 showGameStateDialog(message);
                 break;
            case "SERVER-CHOOSING-WORD": // same as word-choosing-state but this is for the one that is going to draw
                secretWordLabel.setText("WORD");
                showGameStateDialog(message);
                break;
            case "TIMER-REVEAL-WORD":
                updateTimer(message[1]); // update timer
                break;
            case "REVEAL-WORD-STATE": // reveal word dialog
                
                secretWordLabel.setText(message[2]);
                showTheSecretWordDialog(message);
                playAudio("assets/reveal.wav");
                break;
            case "REMOVE-REVEAL-DIALOG":
                revealWordDialog.dispose();
                break; // remove dialog
            case "SECRET-WORD":
                secretWordLabel.setText(message[1]); // set the label as the secret word
                break;
            case "CHOSEN-WORD": 
                secretWordLabel.setText(message[1]); // set the label as the word to guess
                break;
            case "REMOVE-DIALOG-ROUND":
                roundDialog.dispose(); // remove dialog
                break;
            case "REMOVE-DIALOG-WORD":
                serverChoosingWordDialog.dispose(); // remove dialog
                break;
            case "GAME-STOPPED": // reset the game state
                updateTimer("0");
                showHostBtn();
                secretWordLabel.setText("WORD");
                roundLabel.setText("Round 1 out of ?");
                turnToDraw(0);
                serverChoosingWordDialog.dispose();
                roundDialog.dispose();
                revealWordDialog.dispose();
                break;
            case "GUESSED": // if the player got the right guess
                secretWordLabel.setText(message[1]);
                chatInpuTextField.setEditable(false); // disable the input to prevent cheating
                
                break;
            case "ON-GOING-JOINED":  // if the player joined in on-going game
                roundLabel.setText(message[1]);
                break;
            case "TIMER-RANK-ANNOUNCE":
                updateTimer(message[1]); // update timer
                break;
            case "WINNER": // display the rank and the winner
                secretWordLabel.setText("WORD");
                showRankListDialog(message);
                break;
            case "REMOVE-RANK-ANNOUNCE-DIALOG":
                declareFinalRanksDialog.dispose();
                break;
            case "PLAYERS-MAXED":
                JOptionPane.showMessageDialog(this, "Cannot join: The game is full!", "Error", JOptionPane.ERROR_MESSAGE);
                goBackToMenu();
                break;
            case "SERVER-DOWN":
                JOptionPane.showMessageDialog(this, "Disconnected: Server has shut down!", "Error", JOptionPane.ERROR_MESSAGE);
                break;
            case "PLAYER-GUESSED-CORRECTLY": 
                playAudio("assets/guess.wav");
                break;
            case "DUPLICATED-USERNAME":
                JOptionPane.showMessageDialog(this, "Cannot join: Username Already Exists!", "Error", JOptionPane.ERROR_MESSAGE);
                goBackToMenu();
                break;
                
        }
        
    }
    
    // go back to main menu
    private void goBackToMenu(){
        GameMenu menu = new GameMenu();
        menu.setTitle("DoodleDuel - Menu");
        menu.setResizable(false);
        menu.setVisible(true);
        menu.setLocationRelativeTo(null);
        this.dispose();
    
    }
    
    // check if messages received is player list (this is for scoreboard)
    private void checkClientListMessage(String message){
        if(message.startsWith("PLAYER-LIST:")){
            // extract the message that contains the player lists
            // using the substring as the begin index of the last index of the prefix message
            System.out.println("PLAYER-LIST " + message.substring("PLAYER-LIST:".length()));
            evaluateScoreboardMessage(message.substring("PLAYER-LIST:".length()));
        }
        if(message.startsWith("RANK-LIST:")){
            System.out.println("RANK-LIST " +message.substring("RANK-LIST:".length()));
            evaluateRankMessage(message.substring("RANK-LIST:".length()));
        }
        if(message.startsWith("SUB-ROUND-LIST:")){
            System.out.println("SUB-ROUND-LIST " + message.substring("SUB-ROUND-LIST:".length()));
            evalauteSubRoundMessage(message.substring("SUB-ROUND-LIST:".length()));
        }
    }
    
    private void evaluateClientDrawingPoints(String[] message){
        // parse the string point, rgb, size  to integer
        int x = Integer.parseInt(message[1]);
        int y = Integer.parseInt(message[2]);
        int red = Integer.parseInt(message[3]);
        int green = Integer.parseInt(message[4]);
        int blue = Integer.parseInt(message[5]);
        int size = Integer.parseInt(message[6]);
        
        color = new Color(red,green,blue); // create the color with rgb
        drawPoint = new DrawPoint(new Point(x,y), color, size); // create drawPoint object
        // push the drawPoint object to the stack 
        synchronized (drawingPoint) {
                drawingPoint.push(drawPoint);
        }
       
        // refresh the panel to see the update
        drawingPanel.repaint();
    }
   
    private void evaluateClientMessage(String[] message){
         
        StyleConstants.setBold(attrBold, true);
        StyleConstants.setForeground(attrBold, Color.black);
        StyleConstants.setForeground(attrNormal, Color.BLACK);
        
        // append the message to text content with the style configure
        try {
            chatDoc.insertString(chatDoc.getLength(), message[1], attrBold);
            chatDoc.insertString(chatDoc.getLength(), message[2] + "\n", attrNormal);
        } catch (BadLocationException e) {
            System.err.println(e);
        }
        scrollToBottom();
    }
    
    // append messages from clients or server to textPane
    private void evaluateAnnouncementMessage(String[] message){
        
        switch (message[1]) {
            case "LEFT":
                StyleConstants.setForeground(attrsAnnouncement, Color.RED);
                playAudio("assets/left.wav");
                break;
            case "JOIN":
                StyleConstants.setForeground(attrsAnnouncement, Color.GREEN);
                playAudio("assets/joined.wav");
                break;
            case "TURN":
                StyleConstants.setForeground(attrsAnnouncement, Color.BLUE);
                playAudio("assets/start.wav");
                break;
            case "SERVER":
                StyleConstants.setForeground(attrsAnnouncement, Color.orange);
                break;
            case "GUESS":
                StyleConstants.setForeground(attrsAnnouncement, Color.green);
                break;
            default:
                StyleConstants.setForeground(attrsAnnouncement, Color.black);
                break;
        }
        StyleConstants.setBold(attrsAnnouncement, true);
        
        // append the message to text content with the style configure
        try {
            chatDoc.insertString(chatDoc.getLength(), message[2] + "\n", attrsAnnouncement);
        } catch (BadLocationException e) {
            System.err.println(e);
        }
        scrollToBottom(); // scroll to bottom every message received
    }
    
    // this one will process any updates within the players list and their details
    private void evaluateScoreboardMessage(String message){
        scoreBoardPane.setText(""); // clear any existing text
        StyleConstants.setBackground(attrGreenBg, Color.green);
        // split the message using ; as delimiter to split each players details
        String[] players = message.split(";");
        // loop thru the splitted players string
        for(String player : players){
            // split each player using , as delimiter 
            String[] playerInfo = player.split(",");
            String drawerEmoji = Boolean.parseBoolean(playerInfo[4]) == true ? "🖍️" : "";
            String playerDetails = playerInfo[0] + " " + playerInfo[1] + ": "
                    + playerInfo[2] + " points" +  " " + drawerEmoji + "\n";
            
            try{
                // apply green background if the player guessed correctly
                if(Boolean.parseBoolean(playerInfo[5])){
                    scoreBoardDoc.insertString(scoreBoardDoc.getLength(), playerDetails, attrGreenBg);
                }
                else{
                    scoreBoardDoc.insertString(scoreBoardDoc.getLength(), playerDetails, null);
                }
            }catch (BadLocationException e) {
                
                System.err.println(e);
            }
        }
       
    }
    
    
    
    // evalute the final scores of the player in the end
    private void evaluateRankMessage(String message){
        rankBoardStringBuilder = new StringBuilder(); // initialize new StringBuilder
        // split the message using ; as delimiter to split each players details
        String[] players = message.split(";");
        // loop thru the splitted players string
        for(String player : players){
            // split each player using , as delimiter 
            // append each player details to one string using string builder 
            String[] playerInfo = player.split(",");
            String winnerEmoji = playerInfo[0].equalsIgnoreCase("#1") ? "🎖️️" : "";
            rankBoardStringBuilder.append(playerInfo[0]).append("  ").append(playerInfo[1])
                    .append(": ")
                    .append(playerInfo[2])
                    .append(" points").append(" ").append(winnerEmoji).append("\n");
        }
       
        StyleConstants.setAlignment(attrCenter, StyleConstants.ALIGN_CENTER);
        rankPaneDoc.setParagraphAttributes(0, rankPaneDoc.getLength(), attrCenter, false);
        // set the stringbuilder text to the textPane
        rankPane.setText(rankBoardStringBuilder.toString());
    
    }
    
    private void evalauteSubRoundMessage(String message){
        // repeat the process from rank message
        subRoundStringBuilder = new StringBuilder();
        String[] players = message.split(";");
        for(String player : players){
            // split each player using , as delimiter 
            // append each player details to one string using string builder 
            String[] playerInfo = player.split(",");
            String status = Integer.parseInt(playerInfo[1]) != 0 ? "+" : ""; // add sign to indicate if the players gain or not
            subRoundStringBuilder.append(playerInfo[0]).append(" : ").append(status).append(playerInfo[1]).append("\n");
        }
       
        StyleConstants.setAlignment(attrCenter, StyleConstants.ALIGN_CENTER);
        subRoundDoc.setParagraphAttributes(0, subRoundDoc.getLength(), attrCenter, false);
        // set the stringbuilder text to the textPane
        subRoundSummaryPane.setText(subRoundStringBuilder.toString());
    }
    
    private void updateTimer(String seconds){
        // update the timer label
        SwingUtilities.invokeLater(() -> 
                timerLabel.setText(seconds));
    }
    // separate timer for the drawing time to only play audio of the countdown without playing it on other timers
    
    private void updateDrawingTimer(String time){
        int seconds = Integer.parseInt(time);
        SwingUtilities.invokeLater(() -> {
             if(seconds <= 10){
                 playAudio("assets/timer.wav");
             }
             timerLabel.setText(time);
        });
    }
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            chatPane.setCaretPosition(chatPane.getDocument().getLength());
        });
    }
    
    // undo drawing
    private void undoDrawingPanel(){
        // make sure the stack is not empty
        // use for loop to remove number of points per undo
        for (int i = 0; i < pointsToRemove && !drawingPoint.isEmpty(); i++) {
            drawingPoint.pop(); // Remove one point at a time
        }
        drawingPanel.repaint();
        
    }
    // clear drawing panel
    private void clearDrawingPanel(){
        // clear the drawing panel and send the update to server
        if(!drawingPoint.empty()){
            drawingPoint.clear();
        }
        
        // refresh the panel to see the update
        drawingPanel.repaint();
    }
     private void showRoundDialog(String[] message){
        // use the swingUtilities.invokeLater to prevent the GUI freeze and
        // update the gui on separate thread 
        SwingUtilities.invokeLater(() -> {
           
            serverLabelText.setForeground(Color.white);
            serverLabelText.setText(message[1].toUpperCase());
            serverLabelText.setFont(new Font("Comic sans MS", Font.BOLD, 24));
            serverLabelText.setHorizontalAlignment(SwingConstants.CENTER);
            roundDialog.add(serverLabelText);
            // remove the window buttons and bars
            roundDialog.setUndecorated(true);
            roundDialog.getContentPane().setBackground(new Color(59,59,59));
            roundDialog.setModal(true);
            roundDialog.setSize(500, 250);
            
            roundDialog.setLocationRelativeTo(drawingPanel); // Centers it on drawing panel
            roundDialog.setVisible(true);
            roundLabel.setText(message[2]);
        });
        
    }
    
    private void showGameStateDialog(String[] message){
        // use the swingUtilities.invokeLater to prevent the GUI freeze and
        // update the gui on separate thread 
        SwingUtilities.invokeLater(() -> {
           
            serverLabelText.setForeground(Color.white);
            serverLabelText.setFont(new Font("Comic sans MS", Font.BOLD, 24));
            serverLabelText.setText(message[1]);
            serverLabelText.setHorizontalAlignment(SwingConstants.CENTER);
            serverChoosingWordDialog.add(serverLabelText);
            serverChoosingWordDialog.setUndecorated(true);
            serverChoosingWordDialog.getContentPane().setBackground(new Color(59,59,59));
            serverChoosingWordDialog.setModal(true);
            serverChoosingWordDialog.setSize(600, 300);
            serverChoosingWordDialog.setLocationRelativeTo(drawingPanel); // Centers it on drawing panel
            serverChoosingWordDialog.setVisible(true);
        });
    
    }
    
    private void showTheSecretWordDialog(String[] message){
        // use the swingUtilities.invokeLater to prevent the GUI freeze and
        // update the gui on separate thread 
        SwingUtilities.invokeLater(() -> {
            revealWordLabelText.setForeground(Color.white);
            revealWordLabelText.setFont(new Font("Comic sans MS", Font.BOLD, 24));
            revealWordLabelText.setText(message[1]);
            revealWordLabelText.setHorizontalAlignment(SwingConstants.CENTER);
            revealWordDialog.setLayout(new BorderLayout(10,10));
            revealWordLabelText.setBorder(new EmptyBorder(25, 0, 0, 0));
            revealWordDialog.add(revealWordLabelText,BorderLayout.NORTH);
            revealWordDialog.add(subRoundSummaryPane,BorderLayout.CENTER);
            revealWordDialog.setUndecorated(true);
            revealWordDialog.getContentPane().setBackground(new Color(59,59,59));
            revealWordDialog.setModal(true);
            revealWordDialog.setSize(600, 300);
            revealWordDialog.setLocationRelativeTo(drawingPanel); // Centers it on drawing panel
            revealWordDialog.setVisible(true);
        });
    
    }
    
    private void showRankListDialog(String[] message){
        SwingUtilities.invokeLater(() -> {
            winnerLabelText.setForeground(Color.white);
            winnerLabelText.setFont(new Font("Segoe UI Emoji", Font.BOLD, 26));
            winnerLabelText.setText(message[1].replace("#1", ""));
            winnerLabelText.setHorizontalAlignment(SwingConstants.CENTER);
            declareFinalRanksDialog.setLayout(new BorderLayout(10,10));
            winnerLabelText.setBorder(new EmptyBorder(25, 0, 0, 0));
            declareFinalRanksDialog.add(winnerLabelText,BorderLayout.NORTH);
            declareFinalRanksDialog.add(rankPane,BorderLayout.CENTER);
            declareFinalRanksDialog.setUndecorated(true);
            declareFinalRanksDialog.getContentPane().setBackground(new Color(59, 59, 59));
            declareFinalRanksDialog.setModal(true);
            declareFinalRanksDialog.setSize(650, 350);
            declareFinalRanksDialog.setLocationRelativeTo(drawingPanel); // Centers it on drawing panel
            declareFinalRanksDialog.setVisible(true);
        });
        
    }
    
    // audio for client
    private void playAudio(String audioName) {
        
        try{
            // Load the audio resource as a stream
            InputStream audioStream = getClass().getResourceAsStream(audioName);
            if (audioStream == null) {
                System.err.println("Audio resource not found: " + audioName);
                return;
            }
            
            // Convert the InputStream to a BufferedInputStream
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(audioStream));
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            // Add a LineListener to stop the clip to avoid bulking resource 
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    System.out.println("Playback finished.");
                    clip.close(); // Close the clip when playback is done
                }
            });
            clip.start();
            
        }catch(UnsupportedAudioFileException | IOException | LineUnavailableException e){
            System.err.println(e);
        }
      
    }
    // INIT COMPONENTS CODE HERE ->
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw the background image if it's set
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        jPanel2 = new javax.swing.JPanel();
        drawingPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Use synchronized block to avoid concurrent modification errors
                synchronized (drawingPoint){
                    for (DrawPoint drawPoint : drawingPoint) {
                        g.setColor(drawPoint.getColor()); // set the color for this point
                        //int size = drawPoint.getSize();
                        point = drawPoint.getPoint();
                        g.fillOval(point.x, point.y, drawPoint.getSize(), drawPoint.getSize()); // Draw each point in the list
                    }
                }

            }

        };
        //  -- added paintComponent method for the drawing functionality);
    chatInpuTextField = new javax.swing.JTextField();
    gameStatusPanel = new javax.swing.JPanel();
    roundLabel = new javax.swing.JLabel();
    timerLabel = new javax.swing.JLabel();
    clockImageHolder = new javax.swing.JLabel();
    secretWordLabel = new javax.swing.JLabel();
    jScrollPane2 = new javax.swing.JScrollPane();
    chatPane = new javax.swing.JTextPane();
    jScrollPane3 = new javax.swing.JScrollPane();
    scoreBoardPane = new javax.swing.JTextPane();
    DrawingPanelTools = new javax.swing.JPanel();
    colorsBtnPanel = new javax.swing.JPanel();
    whiteBtn = new javax.swing.JButton();
    blackBtn = new javax.swing.JButton();
    redBtn = new javax.swing.JButton();
    orangeBtn = new javax.swing.JButton();
    yellowBtn = new javax.swing.JButton();
    greyBtn = new javax.swing.JButton();
    greenBtn = new javax.swing.JButton();
    blueBtn = new javax.swing.JButton();
    pinkBtn = new javax.swing.JButton();
    magentaBtn = new javax.swing.JButton();
    cyanBtn = new javax.swing.JButton();
    brownBtn = new javax.swing.JButton();
    skinColorbtn = new javax.swing.JButton();
    brushSizeSlider = new javax.swing.JSlider();
    brushSizeLabel = new javax.swing.JLabel();
    clearBtn = new javax.swing.JButton();
    UndoBtn = new javax.swing.JButton();
    startGameBtn = new javax.swing.JButton();
    panelDialog = new javax.swing.JPanel();
    customRound = new javax.swing.JComboBox<>();
    roundTitleLabel = new javax.swing.JLabel();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("DoodleDuel");
    setBackground(new java.awt.Color(255, 255, 255));
    setMinimumSize(new java.awt.Dimension(1200, 700));

    jPanel1.setBackground(new java.awt.Color(0, 65, 108));
    jPanel1.setMinimumSize(new java.awt.Dimension(1200, 700));
    jPanel1.setPreferredSize(new java.awt.Dimension(1200, 700));
    jPanel1.setLayout(new java.awt.GridBagLayout());

    jPanel2.setBackground(new java.awt.Color(0, 204, 204));
    jPanel2.setOpaque(false);
    jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    drawingPanel.setBackground(new java.awt.Color(255, 255, 255));

    javax.swing.GroupLayout drawingPanelLayout = new javax.swing.GroupLayout(drawingPanel);
    drawingPanel.setLayout(drawingPanelLayout);
    drawingPanelLayout.setHorizontalGroup(
        drawingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 792, Short.MAX_VALUE)
    );
    drawingPanelLayout.setVerticalGroup(
        drawingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 493, Short.MAX_VALUE)
    );

    jPanel2.add(drawingPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(243, 125, -1, 493));
    drawingPanel.getAccessibleContext().setAccessibleName("");

    chatInpuTextField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
    chatInpuTextField.setToolTipText("Type here...");
    chatInpuTextField.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            chatInpuTextFieldActionPerformed(evt);
        }
    });
    chatInpuTextField.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyPressed(java.awt.event.KeyEvent evt) {
            chatInpuTextFieldKeyPressed(evt);
        }
    });
    jPanel2.add(chatInpuTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(1058, 481, 302, 36));
    chatInpuTextField.getAccessibleContext().setAccessibleName("");

    gameStatusPanel.setBackground(new java.awt.Color(255, 255, 255));

    roundLabel.setFont(new java.awt.Font("Segoe UI Emoji", 0, 21)); // NOI18N
    roundLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    roundLabel.setText("Round 1 out of ?");
    roundLabel.setToolTipText("");

    timerLabel.setFont(new java.awt.Font("Comic Sans MS", 3, 20)); // NOI18N
    timerLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    timerLabel.setText("0");
    timerLabel.setToolTipText("");

    // load the image
    ImageIcon originalClockIcon = new ImageIcon(getClass().getResource("assets/alarmClock.png"));

    int labelWidth = 70;
    int labelHeight = 70;
    Image scaledImage = originalClockIcon.getImage().getScaledInstance(labelWidth, labelHeight, Image.SCALE_SMOOTH);
    ImageIcon scaledIcon = new ImageIcon(scaledImage);
    clockImageHolder.setIcon(scaledIcon);

    secretWordLabel.setBackground(new java.awt.Color(255, 255, 255));
    secretWordLabel.setFont(new java.awt.Font("Comic Sans MS", 0, 24)); // NOI18N
    secretWordLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    secretWordLabel.setText("WORD");

    javax.swing.GroupLayout gameStatusPanelLayout = new javax.swing.GroupLayout(gameStatusPanel);
    gameStatusPanel.setLayout(gameStatusPanelLayout);
    gameStatusPanelLayout.setHorizontalGroup(
        gameStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gameStatusPanelLayout.createSequentialGroup()
            .addGap(14, 14, 14)
            .addComponent(clockImageHolder, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(30, 30, 30)
            .addComponent(timerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 183, Short.MAX_VALUE)
            .addComponent(secretWordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(153, 153, 153)
            .addComponent(roundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );
    gameStatusPanelLayout.setVerticalGroup(
        gameStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(gameStatusPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(gameStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(gameStatusPanelLayout.createSequentialGroup()
                    .addGap(4, 4, 4)
                    .addGroup(gameStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(roundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(secretWordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(timerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addComponent(clockImageHolder, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    roundLabel.getAccessibleContext().setAccessibleName("");
    timerLabel.getAccessibleContext().setAccessibleName("");
    secretWordLabel.getAccessibleContext().setAccessibleName("");

    jPanel2.add(gameStatusPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 14, 1354, -1));
    gameStatusPanel.getAccessibleContext().setAccessibleName("gameDetailsPanel");

    chatPane.setEditable(false);
    chatPane.setBackground(new java.awt.Color(255, 255, 255));
    chatPane.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
    chatPane.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
    jScrollPane2.setViewportView(chatPane);

    jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1058, 125, 300, 344));

    scoreBoardPane.setEditable(false);
    scoreBoardPane.setBackground(new java.awt.Color(255, 255, 255));
    scoreBoardPane.setFont(new java.awt.Font("Segoe UI Emoji", 0, 18)); // NOI18N
    jScrollPane3.setViewportView(scoreBoardPane);

    jPanel2.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 125, 218, 456));

    DrawingPanelTools.setBackground(new java.awt.Color(0, 65, 108));
    DrawingPanelTools.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));

    // Code for adding buttons or other components
    colorsBtnPanel.setBackground(new java.awt.Color(0, 65, 108));

    whiteBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            whiteBtnActionPerformed(evt);
        }
    });

    blackBtn.setBackground(new java.awt.Color(0, 0, 0));
    blackBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            blackBtnActionPerformed(evt);
        }
    });

    redBtn.setBackground(java.awt.Color.red);
    redBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            redBtnActionPerformed(evt);
        }
    });

    orangeBtn.setBackground(new java.awt.Color(255, 102, 0));
    orangeBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            orangeBtnActionPerformed(evt);
        }
    });

    yellowBtn.setBackground(new java.awt.Color(255, 255, 0));
    yellowBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            yellowBtnActionPerformed(evt);
        }
    });

    greyBtn.setBackground(java.awt.Color.gray);
    greyBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            greyBtnActionPerformed(evt);
        }
    });

    greenBtn.setBackground(java.awt.Color.green);
    greenBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            greenBtnActionPerformed(evt);
        }
    });

    blueBtn.setBackground(java.awt.Color.blue);
    blueBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            blueBtnActionPerformed(evt);
        }
    });

    pinkBtn.setBackground(java.awt.Color.pink);
    pinkBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            pinkBtnActionPerformed(evt);
        }
    });

    magentaBtn.setBackground(java.awt.Color.magenta);
    magentaBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            magentaBtnActionPerformed(evt);
        }
    });

    cyanBtn.setBackground(java.awt.Color.cyan);
    cyanBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            cyanBtnActionPerformed(evt);
        }
    });

    brownBtn.setBackground(new java.awt.Color(153, 102, 0));
    brownBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            brownBtnActionPerformed(evt);
        }
    });

    skinColorbtn.setBackground(new java.awt.Color(241, 194, 125));
    skinColorbtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            skinColorbtnActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout colorsBtnPanelLayout = new javax.swing.GroupLayout(colorsBtnPanel);
    colorsBtnPanel.setLayout(colorsBtnPanelLayout);
    colorsBtnPanelLayout.setHorizontalGroup(
        colorsBtnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(colorsBtnPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(whiteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(blackBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(redBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(orangeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(yellowBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(greyBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(greenBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(blueBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(pinkBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(magentaBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(cyanBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(brownBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(skinColorbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 26, Short.MAX_VALUE))
    );
    colorsBtnPanelLayout.setVerticalGroup(
        colorsBtnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, colorsBtnPanelLayout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(colorsBtnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(brownBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                .addComponent(cyanBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(magentaBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pinkBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(blueBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(greenBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(greyBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(yellowBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(redBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(blackBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(whiteBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(orangeBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(skinColorbtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );

    brushSizeSlider.setForeground(new java.awt.Color(255, 255, 255));
    brushSizeSlider.setMaximum(20);
    brushSizeSlider.setMinimum(8);
    brushSizeSlider.setPaintLabels(true);
    brushSizeSlider.setPaintTicks(true);
    brushSizeSlider.setSnapToTicks(true);
    brushSizeSlider.setToolTipText("Brush Size");
    brushSizeSlider.setValue(13);
    brushSizeSlider.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
    brushSizeSlider.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    brushSizeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            brushSizeSliderStateChanged(evt);
        }
    });

    brushSizeLabel.setFont(new java.awt.Font("Comic Sans MS", 0, 14)); // NOI18N
    brushSizeLabel.setForeground(new java.awt.Color(255, 255, 255));
    brushSizeLabel.setText("Brush Size: 13");
    brushSizeLabel.setToolTipText("");

    clearBtn.setText("Clear");
    clearBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            clearBtnActionPerformed(evt);
        }
    });

    UndoBtn.setText("Undo");
    UndoBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            UndoBtnActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout DrawingPanelToolsLayout = new javax.swing.GroupLayout(DrawingPanelTools);
    DrawingPanelTools.setLayout(DrawingPanelToolsLayout);
    DrawingPanelToolsLayout.setHorizontalGroup(
        DrawingPanelToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(DrawingPanelToolsLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(DrawingPanelToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(DrawingPanelToolsLayout.createSequentialGroup()
                    .addComponent(brushSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(colorsBtnPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(UndoBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(clearBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(brushSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    DrawingPanelToolsLayout.setVerticalGroup(
        DrawingPanelToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(DrawingPanelToolsLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(DrawingPanelToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(DrawingPanelToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clearBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(UndoBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(DrawingPanelToolsLayout.createSequentialGroup()
                    .addComponent(brushSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(brushSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(colorsBtnPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jPanel2.add(DrawingPanelTools, new org.netbeans.lib.awtextra.AbsoluteConstraints(242, 630, -1, -1));

    startGameBtn.setBackground(new java.awt.Color(153, 255, 102));
    startGameBtn.setText("START");
    startGameBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            startGameBtnActionPerformed(evt);
        }
    });
    jPanel2.add(startGameBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 587, 218, 31));

    panelDialog.setLayout(new javax.swing.OverlayLayout(panelDialog));
    jPanel2.add(panelDialog, new org.netbeans.lib.awtextra.AbsoluteConstraints(683, 360, -1, -1));

    customRound.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
    customRound.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2", "3", "4", "6", "8", "10", " " }));
    customRound.setSelectedIndex(1);
    customRound.setSelectedItem(1);
    customRound.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            customRoundActionPerformed(evt);
        }
    });
    jPanel2.add(customRound, new org.netbeans.lib.awtextra.AbsoluteConstraints(112, 630, 110, -1));

    roundTitleLabel.setFont(new java.awt.Font("Comic Sans MS", 0, 18)); // NOI18N
    roundTitleLabel.setForeground(new java.awt.Color(255, 255, 255));
    roundTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    roundTitleLabel.setText("Rounds:");
    jPanel2.add(roundTitleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 630, 90, -1));

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(17, 7, 78, 12);
    jPanel1.add(jPanel2, gridBagConstraints);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1379, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 805, Short.MAX_VALUE)
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents
   
    
    private void chatInpuTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chatInpuTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chatInpuTextFieldActionPerformed
        // send message if user pressed enter
    private void chatInpuTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_chatInpuTextFieldKeyPressed
       
        // invoke the send message function if user press enter key
        if(evt.getKeyCode() == 10){
            sendMessage();
            
        }
    }//GEN-LAST:event_chatInpuTextFieldKeyPressed

    private void UndoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UndoBtnActionPerformed
        undoDrawingPanel();
        // send a signal to server to inform other clients
        writer.println("UNDO-DRAWING");
    }//GEN-LAST:event_UndoBtnActionPerformed

    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnActionPerformed
        clearDrawingPanel();
        // send a signal to server to inform other clients
        writer.println("CLEAR-DRAWING");

    }//GEN-LAST:event_clearBtnActionPerformed

    private void brushSizeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_brushSizeSliderStateChanged

        brushSize = brushSizeSlider.getValue(); // update the brush size based on slider;
        brushSizeLabel.setText("Brush Size: " + brushSize); // reflect from the interface the changes
    }//GEN-LAST:event_brushSizeSliderStateChanged

    private void brownBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_brownBtnActionPerformed

        brushColor = new Color(102, 51, 0);
    }//GEN-LAST:event_brownBtnActionPerformed

    private void cyanBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cyanBtnActionPerformed

        brushColor = Color.cyan;
    }//GEN-LAST:event_cyanBtnActionPerformed

    private void magentaBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_magentaBtnActionPerformed

        brushColor = Color.magenta;
    }//GEN-LAST:event_magentaBtnActionPerformed

    private void pinkBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pinkBtnActionPerformed

        brushColor = Color.pink;
    }//GEN-LAST:event_pinkBtnActionPerformed

    private void blueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blueBtnActionPerformed

        brushColor = Color.blue;
    }//GEN-LAST:event_blueBtnActionPerformed

    private void greenBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_greenBtnActionPerformed

        brushColor = Color.green;
    }//GEN-LAST:event_greenBtnActionPerformed

    private void greyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_greyBtnActionPerformed

        brushColor = Color.darkGray;
    }//GEN-LAST:event_greyBtnActionPerformed

    private void yellowBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yellowBtnActionPerformed

        brushColor = Color.yellow;
    }//GEN-LAST:event_yellowBtnActionPerformed

    private void orangeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orangeBtnActionPerformed

        brushColor = Color.orange;
    }//GEN-LAST:event_orangeBtnActionPerformed

    private void redBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redBtnActionPerformed

        brushColor = Color.red;
    }//GEN-LAST:event_redBtnActionPerformed

    private void whiteBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_whiteBtnActionPerformed

        brushColor = Color.white;
    }//GEN-LAST:event_whiteBtnActionPerformed

    private void startGameBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startGameBtnActionPerformed
        // TODO add your handling code here:
        if(hostStatus){
             writer.println("START-GAME," + customRound.getSelectedItem());
             writer.println();
        }
       
    }//GEN-LAST:event_startGameBtnActionPerformed

    private void blackBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blackBtnActionPerformed
        // TODO add your handling code here:
        brushColor = Color.black;
    }//GEN-LAST:event_blackBtnActionPerformed

    private void skinColorbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skinColorbtnActionPerformed
        // TODO add your handling code here:
        brushColor = new Color(241,194,125);
    }//GEN-LAST:event_skinColorbtnActionPerformed

    private void customRoundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customRoundActionPerformed
        // TODO add your handling code here:
        roundLabel.setText("Round 1 out of " + customRound.getSelectedItem());
    }//GEN-LAST:event_customRoundActionPerformed
    
    
    
    // close streams
    public void closeEverything(Socket socket, BufferedReader bufferedReader, PrintWriter writer){
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
    
    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel DrawingPanelTools;
    private javax.swing.JButton UndoBtn;
    private javax.swing.JButton blackBtn;
    private javax.swing.JButton blueBtn;
    private javax.swing.JButton brownBtn;
    private javax.swing.JLabel brushSizeLabel;
    private javax.swing.JSlider brushSizeSlider;
    private javax.swing.JTextField chatInpuTextField;
    private javax.swing.JTextPane chatPane;
    private javax.swing.JButton clearBtn;
    private javax.swing.JLabel clockImageHolder;
    private javax.swing.JPanel colorsBtnPanel;
    private javax.swing.JComboBox<String> customRound;
    private javax.swing.JButton cyanBtn;
    private javax.swing.JPanel drawingPanel;
    private javax.swing.JPanel gameStatusPanel;
    private javax.swing.JButton greenBtn;
    private javax.swing.JButton greyBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton magentaBtn;
    private javax.swing.JButton orangeBtn;
    private javax.swing.JPanel panelDialog;
    private javax.swing.JButton pinkBtn;
    private javax.swing.JButton redBtn;
    private javax.swing.JLabel roundLabel;
    private javax.swing.JLabel roundTitleLabel;
    private javax.swing.JTextPane scoreBoardPane;
    private javax.swing.JLabel secretWordLabel;
    private javax.swing.JButton skinColorbtn;
    private javax.swing.JButton startGameBtn;
    private javax.swing.JLabel timerLabel;
    private javax.swing.JButton whiteBtn;
    private javax.swing.JButton yellowBtn;
    // End of variables declaration//GEN-END:variables

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
