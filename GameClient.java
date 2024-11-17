
package skribbl_clone;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.net.*;
import java.util.Stack;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


public class GameClient extends JFrame implements MouseMotionListener {

    private String username;
    private int id;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean canDraw = false;
    private int timerCount;
    // List to store drawPoint object 
    private Stack<DrawPoint> drawingPoint;
    private DrawPoint drawPoint;
    

    // for styling the textPane
    StyledDocument chatDoc;
    StyledDocument scoreboardDoc;
    SimpleAttributeSet attrBold = new SimpleAttributeSet();
    SimpleAttributeSet attrNormal = new SimpleAttributeSet();
    SimpleAttributeSet attrsAnnouncement = new SimpleAttributeSet();
    
    private int brushSize; // brush size
    private Color brushColor; // brush color
    private int pointsToRemove = 10;
    Point point; 
    Color color;
    StringBuilder scoreboardStringBuilder;
   
    
    public GameClient(Socket socket, String username) {
       
        this.brushSize = 10; // initialize brush size to 10
        this.brushColor = Color.black; // initialize color to 10
        this.drawingPoint = new Stack<>();
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
        initComponents();
        
        // initialize draw ability on drawing panel
        drawingPanelFunctionality();
        
        
    }
   
    private void drawingPanelFunctionality(){
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
    
    // send client's message to server
    public void sendMessage(){
        String message = jTextField1.getText().trim();
        if (!message.isEmpty()) {
            jTextField1.setText("");  // Clear the text field
            writer.println("GUESS,"+username + ":, " + message);  // Send message to the server
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
                       messageFromServer.trim();
                       processMessage(messageFromServer);
                       checkIfScoreboardMessage(messageFromServer);
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
        scoreboardDoc = scoreBoardPane.getStyledDocument();
        String[] message = receivedMessage.split(",");
       
        switch(message[0]){
            case "ANNOUNCEMENT":
                // listens to game state
                evaluateAnnouncementMessage(message);
                break;
            case "GUESS":
                // listens to other players chats (their guesses)
                evaluateClientMessage(message);
                break;
            case "DRAWING":
                // listens to the player who is drawing
                evaluateClientDrawingPoints(message);
                break;
            case "CLEAR-DRAWING":
                // clear client-side drawing panel
                clearDrawingPanel();
                break;
            case "UNDO-DRAWING":
                // undo drawing
                undoDrawingPanel();
                break;
                
        }
        
    }
    // check if messages received is player list (this is for scoreboard)
    private void checkIfScoreboardMessage(String message){
        if(message.startsWith("PLAYER-LIST:")){
            // extract the message that contains the player lists
            // using the substring as the begin index of the last index of the prefix message
            System.out.println(message.substring("PLAYER-LIST:".length()));
            evaluateScoreboardMessage(message.substring("PLAYER-LIST:".length()));
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
    }
    
    // append messages from clients or server to textPane
    private void evaluateAnnouncementMessage(String[] message){
        
        switch (message[1]) {
            case "LEFT" -> StyleConstants.setForeground(attrsAnnouncement, Color.RED);
            case "JOIN" -> StyleConstants.setForeground(attrsAnnouncement, Color.GREEN);
            default -> StyleConstants.setForeground(attrsAnnouncement, Color.black);
        }
        StyleConstants.setBold(attrsAnnouncement, true);
        
        // append the message to text content with the style configure
        try {
            chatDoc.insertString(chatDoc.getLength(), message[2] + "\n", attrsAnnouncement);
        } catch (BadLocationException e) {
            System.err.println(e);
        }
    }
    
    private void evaluateScoreboardMessage(String message){
        scoreboardStringBuilder = new StringBuilder(); // initialize new StringBuilder
        // split the message using ; as delimiter to split each players details
        String[] players = message.split(";");
        // loop thru the splitted players string
        for(String player : players){
            // split each player using , as delimiter 
            // append each player details to one string using string builder 
            String[] playerInfo = player.split(",");
            scoreboardStringBuilder.append(playerInfo[0]).append("  ").append(playerInfo[1])
                    .append(": ")
                    .append(playerInfo[2])
                    .append(" points").append("\n");
        }
        // set the stringbuilder text to the textPane
        scoreBoardPane.setText(scoreboardStringBuilder.toString());
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
        drawingPoint.clear();
        
        // refresh the panel to see the update
        drawingPanel.repaint();
    }
    // INIT COMPONENTS CODE HERE ->
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
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
    jTextField1 = new javax.swing.JTextField();
    gameStatusPanel = new javax.swing.JPanel();
    timerLabel = new javax.swing.JLabel();
    roundLabel = new javax.swing.JLabel();
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
    brushSizeSlider = new javax.swing.JSlider();
    brushSizeLabel = new javax.swing.JLabel();
    clearBtn = new javax.swing.JButton();
    UndoBtn = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Doodle Me This");
    setBackground(new java.awt.Color(255, 255, 255));
    setResizable(false);

    jPanel1.setBackground(new java.awt.Color(0, 65, 108));
    jPanel1.setPreferredSize(new java.awt.Dimension(1200, 700));

    drawingPanel.setBackground(new java.awt.Color(255, 255, 255));
    drawingPanel.setLayout(null);

    jTextField1.setToolTipText("Type here...");
    jTextField1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jTextField1ActionPerformed(evt);
        }
    });
    jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyPressed(java.awt.event.KeyEvent evt) {
            jTextField1KeyPressed(evt);
        }
    });

    gameStatusPanel.setBackground(new java.awt.Color(255, 255, 255));

    timerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    timerLabel.setText("0");
    timerLabel.setToolTipText("");

    roundLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    roundLabel.setText("Round 1 out of 3");
    roundLabel.setToolTipText("");

    secretWordLabel.setBackground(new java.awt.Color(255, 255, 255));
    secretWordLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    secretWordLabel.setText("WORD");

    javax.swing.GroupLayout gameStatusPanelLayout = new javax.swing.GroupLayout(gameStatusPanel);
    gameStatusPanel.setLayout(gameStatusPanelLayout);
    gameStatusPanelLayout.setHorizontalGroup(
        gameStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(gameStatusPanelLayout.createSequentialGroup()
            .addGap(63, 63, 63)
            .addComponent(timerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 145, Short.MAX_VALUE)
            .addComponent(secretWordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 618, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(101, 101, 101)
            .addComponent(roundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );
    gameStatusPanelLayout.setVerticalGroup(
        gameStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(gameStatusPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(gameStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gameStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(roundLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                    .addComponent(secretWordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(timerLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
    );

    timerLabel.getAccessibleContext().setAccessibleName("");
    roundLabel.getAccessibleContext().setAccessibleName("");
    secretWordLabel.getAccessibleContext().setAccessibleName("");

    chatPane.setEditable(false);
    chatPane.setBackground(new java.awt.Color(255, 255, 255));
    chatPane.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
    jScrollPane2.setViewportView(chatPane);

    scoreBoardPane.setEditable(false);
    scoreBoardPane.setBackground(new java.awt.Color(255, 255, 255));
    scoreBoardPane.setFont(new java.awt.Font("Segoe UI Emoji", 0, 14)); // NOI18N
    jScrollPane3.setViewportView(scoreBoardPane);

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
            .addGap(0, 12, Short.MAX_VALUE))
    );
    colorsBtnPanelLayout.setVerticalGroup(
        colorsBtnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(colorsBtnPanelLayout.createSequentialGroup()
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
                .addComponent(orangeBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );

    brushSizeSlider.setForeground(new java.awt.Color(255, 255, 255));
    brushSizeSlider.setMaximum(20);
    brushSizeSlider.setMinimum(8);
    brushSizeSlider.setPaintLabels(true);
    brushSizeSlider.setPaintTicks(true);
    brushSizeSlider.setSnapToTicks(true);
    brushSizeSlider.setToolTipText("Brush Size");
    brushSizeSlider.setValue(10);
    brushSizeSlider.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
    brushSizeSlider.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    brushSizeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            brushSizeSliderStateChanged(evt);
        }
    });

    brushSizeLabel.setFont(new java.awt.Font("Comic Sans MS", 0, 14)); // NOI18N
    brushSizeLabel.setForeground(new java.awt.Color(255, 255, 255));
    brushSizeLabel.setText("Brush Size: 10");
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
                    .addComponent(brushSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE))
                .addGroup(DrawingPanelToolsLayout.createSequentialGroup()
                    .addComponent(brushSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(colorsBtnPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                    .addComponent(UndoBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(clearBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
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
                    .addGroup(DrawingPanelToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(brushSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(colorsBtnPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(brushSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(12, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(33, 33, 33)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(gameStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(DrawingPanelTools, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(drawingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addContainerGap(37, Short.MAX_VALUE))
    );
    jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(56, 56, 56)
            .addComponent(gameStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(55, 55, 55)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(drawingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 506, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(DrawingPanelTools, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(39, Short.MAX_VALUE))
    );

    drawingPanel.getAccessibleContext().setAccessibleName("");
    jTextField1.getAccessibleContext().setAccessibleName("");
    gameStatusPanel.getAccessibleContext().setAccessibleName("gameDetailsPanel");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1300, javax.swing.GroupLayout.PREFERRED_SIZE)
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 780, javax.swing.GroupLayout.PREFERRED_SIZE)
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed
    
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

    private void whiteBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_whiteBtnActionPerformed

        brushColor = Color.white;
    }//GEN-LAST:event_whiteBtnActionPerformed

    private void redBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redBtnActionPerformed
       
        brushColor = Color.red;
    }//GEN-LAST:event_redBtnActionPerformed

    private void pinkBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pinkBtnActionPerformed
       
        brushColor = Color.pink;
    }//GEN-LAST:event_pinkBtnActionPerformed

    private void magentaBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_magentaBtnActionPerformed
        
        brushColor = Color.magenta;
    }//GEN-LAST:event_magentaBtnActionPerformed

    private void orangeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orangeBtnActionPerformed
        
        brushColor = Color.orange;
    }//GEN-LAST:event_orangeBtnActionPerformed

    private void yellowBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yellowBtnActionPerformed
     
        brushColor = Color.yellow;
    }//GEN-LAST:event_yellowBtnActionPerformed

    private void greyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_greyBtnActionPerformed
       
        brushColor = Color.darkGray;
    }//GEN-LAST:event_greyBtnActionPerformed

    private void greenBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_greenBtnActionPerformed
        
        brushColor = Color.green;
    }//GEN-LAST:event_greenBtnActionPerformed

    private void blueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blueBtnActionPerformed
     
        brushColor = Color.blue;
    }//GEN-LAST:event_blueBtnActionPerformed

    private void cyanBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cyanBtnActionPerformed
       
        brushColor = Color.cyan;
    }//GEN-LAST:event_cyanBtnActionPerformed

    private void brownBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_brownBtnActionPerformed
    
        brushColor = new Color(102, 51, 0);
    }//GEN-LAST:event_brownBtnActionPerformed

    private void brushSizeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_brushSizeSliderStateChanged
        
        brushSize = brushSizeSlider.getValue(); // update the brush size based on slider;
        brushSizeLabel.setText("Brush Size: " + brushSize); // reflect from the interface the changes
    }//GEN-LAST:event_brushSizeSliderStateChanged
    // send message if user pressed enter
    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed
       
        // invoke the send message function if user press enter key
        if(evt.getKeyCode() == 10){
            sendMessage();
        }
    }//GEN-LAST:event_jTextField1KeyPressed
    
    
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
    private javax.swing.JTextPane chatPane;
    private javax.swing.JButton clearBtn;
    private javax.swing.JPanel colorsBtnPanel;
    private javax.swing.JButton cyanBtn;
    private javax.swing.JPanel drawingPanel;
    private javax.swing.JPanel gameStatusPanel;
    private javax.swing.JButton greenBtn;
    private javax.swing.JButton greyBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton magentaBtn;
    private javax.swing.JButton orangeBtn;
    private javax.swing.JButton pinkBtn;
    private javax.swing.JButton redBtn;
    private javax.swing.JLabel roundLabel;
    private javax.swing.JTextPane scoreBoardPane;
    private javax.swing.JLabel secretWordLabel;
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
