
package skribbl_clone;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Stack;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


public class GameClient extends JFrame implements MouseMotionListener {

    private String username;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean canDraw = false;
    private int timerCount;
    // List to store points where the user draws
    private Stack<Point> points;

    // for styling the textPane
    StyledDocument doc;
    SimpleAttributeSet attrUsernameBold = new SimpleAttributeSet();
    SimpleAttributeSet attrMessage = new SimpleAttributeSet();
    SimpleAttributeSet attrsAnnouncement = new SimpleAttributeSet();
   
    
    public GameClient(Socket socket, String username) {
        this.points = new Stack<>();
        this.socket = socket;
        this.username = username;
        try{
           this.writer = new PrintWriter(socket.getOutputStream(),true);
           this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           // send the username in the server
           writer.println(username);
        }catch(IOException e){
            closeEverything(socket, reader, writer);
        }
        initComponents();
        
        // initialize draw ability on drawing panel
        drawingPanelFunctionality();
        
        
    }
   
    
    private void drawingPanelFunctionality(){
        // add mouse listener to drawing panel 
        drawingPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Use synchronized block to avoid concurrent modification errors
                synchronized (points) {
                    points.push(e.getPoint());   // push the dragged point to the stack list
                }
                
                drawingPanel.repaint();     // this will call the paintComponent method to refresh and show updated drawing points
                // send the mouse point to server;
                sendMouseDrawingPoint(e.getPoint());
            }

        });
    }
    // send the drawing coordinates to server 
    private void sendMouseDrawingPoint(Point point){
        writer.println("DRAWING,"+ point.x+","+point.y);
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
                   }
                }catch(IOException e){
                    closeEverything(socket, reader, writer);
                }
            }
        }).start();
    }
    
    // in this method we would evaluate what type of message the client or server sent
    private void processMessage(String receivedMessage){
        
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
        }
        
    }
    
    private void evaluateClientDrawingPoints(String[] message){
        // parse the string point coordinates to integer
        int x = Integer.parseInt(message[1]);
        int y = Integer.parseInt(message[2]);
        
        // add the point to the stack point
        synchronized (points) {
                points.push(new Point(x,y));
        }
       
        // refresh the panel to see the update
        drawingPanel.repaint();
    }
    
    
    private void evaluateClientMessage(String[] message){
        doc = chatPane.getStyledDocument();
        
        StyleConstants.setBold(attrUsernameBold, true);
        StyleConstants.setForeground(attrUsernameBold, Color.black);
        StyleConstants.setForeground(attrMessage, Color.BLACK);
        
        // append the message to text content with the style configure
        try {
            doc.insertString(doc.getLength(), message[1], attrUsernameBold);
            doc.insertString(doc.getLength(), message[2] + "\n", attrMessage);
        } catch (BadLocationException e) {
            System.err.println(e);
        }
    }
    
    // append messages from clients or server to textPane
    private void evaluateAnnouncementMessage(String[] message){
        doc = chatPane.getStyledDocument();
        
        switch (message[1]) {
            case "RED" -> StyleConstants.setForeground(attrsAnnouncement, Color.RED);
            case "GREEN" -> StyleConstants.setForeground(attrsAnnouncement, Color.GREEN);
            default -> StyleConstants.setForeground(attrsAnnouncement, Color.black);
        }
        StyleConstants.setBold(attrsAnnouncement, true);
        
        // append the message to text content with the style configure
        try {
            doc.insertString(doc.getLength(), message[2] + "\n", attrsAnnouncement);
        } catch (BadLocationException e) {
            System.err.println(e);
        }
    }
    
    
    // INIT COMPONENTS CODE HERE ->
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        drawingPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                // Use synchronized block to avoid concurrent modification errors
                synchronized (points){
                    for (Point p : points) {
                        g.fillOval(p.x, p.y, 10, 10); // Draw each point in the list
                    }
                }

            }

        };
        //  -- added paintComponent method for the drawing functionality);
    jTextField1 = new javax.swing.JTextField();
    gameStatusPanel = new javax.swing.JPanel();
    timerLabel = new javax.swing.JLabel();
    roundLabel = new javax.swing.JLabel();
    titleStatusLabel = new javax.swing.JLabel();
    secretWordLabel = new javax.swing.JLabel();
    jButton1 = new javax.swing.JButton();
    jScrollPane2 = new javax.swing.JScrollPane();
    chatPane = new javax.swing.JTextPane();
    jScrollPane3 = new javax.swing.JScrollPane();
    scoreBoardPane = new javax.swing.JTextPane();
    jSlider1 = new javax.swing.JSlider();
    clearBtn = new javax.swing.JButton();
    UndoBtn = new javax.swing.JButton();
    brushSizeLabel = new javax.swing.JLabel();
    colorsBtnPanel = new javax.swing.JPanel();
    jButton2 = new javax.swing.JButton();
    jButton3 = new javax.swing.JButton();
    jButton4 = new javax.swing.JButton();
    jButton5 = new javax.swing.JButton();
    jButton6 = new javax.swing.JButton();
    jButton7 = new javax.swing.JButton();
    jButton8 = new javax.swing.JButton();
    jButton9 = new javax.swing.JButton();
    jButton10 = new javax.swing.JButton();
    jButton11 = new javax.swing.JButton();
    jButton12 = new javax.swing.JButton();
    jButton13 = new javax.swing.JButton();
    jButton14 = new javax.swing.JButton();
    jButton15 = new javax.swing.JButton();
    jButton16 = new javax.swing.JButton();
    jButton17 = new javax.swing.JButton();
    jButton18 = new javax.swing.JButton();
    jButton19 = new javax.swing.JButton();
    jButton20 = new javax.swing.JButton();
    jButton21 = new javax.swing.JButton();
    jButton22 = new javax.swing.JButton();
    jButton23 = new javax.swing.JButton();
    jButton24 = new javax.swing.JButton();
    jButton25 = new javax.swing.JButton();
    jButton26 = new javax.swing.JButton();
    jButton27 = new javax.swing.JButton();

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

    gameStatusPanel.setBackground(new java.awt.Color(255, 255, 255));

    timerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    timerLabel.setText("0");
    timerLabel.setToolTipText("");

    roundLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    roundLabel.setText("Round 1 out of 3");
    roundLabel.setToolTipText("");

    titleStatusLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    titleStatusLabel.setText("WAITING");
    titleStatusLabel.setToolTipText("");

    secretWordLabel.setBackground(new java.awt.Color(255, 255, 255));
    secretWordLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    secretWordLabel.setText("WORD");

    javax.swing.GroupLayout gameStatusPanelLayout = new javax.swing.GroupLayout(gameStatusPanel);
    gameStatusPanel.setLayout(gameStatusPanelLayout);
    gameStatusPanelLayout.setHorizontalGroup(
        gameStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(gameStatusPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(timerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 154, Short.MAX_VALUE)
            .addGroup(gameStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(secretWordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(titleStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE))
            .addGap(92, 92, 92)
            .addComponent(roundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );
    gameStatusPanelLayout.setVerticalGroup(
        gameStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(gameStatusPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(gameStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(gameStatusPanelLayout.createSequentialGroup()
                    .addComponent(titleStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(secretWordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE))
                .addComponent(roundLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(timerLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
    );

    timerLabel.getAccessibleContext().setAccessibleName("");
    roundLabel.getAccessibleContext().setAccessibleName("");
    titleStatusLabel.getAccessibleContext().setAccessibleName("");
    secretWordLabel.getAccessibleContext().setAccessibleName("");

    jButton1.setText("jButton1");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton1ActionPerformed(evt);
        }
    });

    chatPane.setEditable(false);
    chatPane.setBackground(new java.awt.Color(255, 255, 255));
    chatPane.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
    jScrollPane2.setViewportView(chatPane);

    scoreBoardPane.setEditable(false);
    scoreBoardPane.setBackground(new java.awt.Color(255, 255, 255));
    jScrollPane3.setViewportView(scoreBoardPane);

    jSlider1.setForeground(new java.awt.Color(255, 255, 255));
    jSlider1.setMaximum(20);
    jSlider1.setMinimum(8);
    jSlider1.setToolTipText("Brush Size");
    jSlider1.setValue(10);
    jSlider1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
    jSlider1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

    clearBtn.setText("Clear");

    UndoBtn.setText("Undo");
    UndoBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            UndoBtnActionPerformed(evt);
        }
    });

    brushSizeLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
    brushSizeLabel.setForeground(new java.awt.Color(255, 255, 255));
    brushSizeLabel.setText("Brush size: ");
    brushSizeLabel.setToolTipText("");

    // Code for adding buttons or other components
    colorsBtnPanel.setBackground(new java.awt.Color(0, 65, 108));

    jButton2.setText("jButton2");

    jButton3.setText("jButton2");

    jButton4.setText("jButton2");

    jButton5.setText("jButton2");

    jButton6.setText("jButton2");

    jButton7.setText("jButton2");

    jButton8.setText("jButton2");

    jButton9.setText("jButton2");

    jButton10.setText("jButton2");

    jButton11.setText("jButton2");

    jButton12.setText("jButton2");

    jButton13.setText("jButton2");

    jButton14.setText("jButton2");

    jButton15.setText("jButton2");

    jButton16.setText("jButton2");

    jButton17.setText("jButton2");

    jButton18.setText("jButton2");

    jButton19.setText("jButton2");

    jButton20.setText("jButton2");

    jButton21.setText("jButton2");

    jButton22.setText("jButton2");

    jButton23.setText("jButton2");

    jButton24.setText("jButton2");

    jButton25.setText("jButton2");

    jButton26.setText("jButton2");

    jButton27.setText("jButton2");

    javax.swing.GroupLayout colorsBtnPanelLayout = new javax.swing.GroupLayout(colorsBtnPanel);
    colorsBtnPanel.setLayout(colorsBtnPanelLayout);
    colorsBtnPanelLayout.setHorizontalGroup(
        colorsBtnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(colorsBtnPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(colorsBtnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(colorsBtnPanelLayout.createSequentialGroup()
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(colorsBtnPanelLayout.createSequentialGroup()
                    .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton19, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton21, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton22, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton23, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton24, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton25, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton26, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton27, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(0, 16, Short.MAX_VALUE))
    );
    colorsBtnPanelLayout.setVerticalGroup(
        colorsBtnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(colorsBtnPanelLayout.createSequentialGroup()
            .addGroup(colorsBtnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton2)
                .addComponent(jButton3)
                .addComponent(jButton4)
                .addComponent(jButton5)
                .addComponent(jButton6)
                .addComponent(jButton7)
                .addComponent(jButton8)
                .addComponent(jButton9)
                .addComponent(jButton10)
                .addComponent(jButton11)
                .addComponent(jButton12)
                .addComponent(jButton13)
                .addComponent(jButton14))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(colorsBtnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton15)
                .addComponent(jButton16)
                .addComponent(jButton17)
                .addComponent(jButton18)
                .addComponent(jButton19)
                .addComponent(jButton20)
                .addComponent(jButton21)
                .addComponent(jButton22)
                .addComponent(jButton23)
                .addComponent(jButton24)
                .addComponent(jButton25)
                .addComponent(jButton26)
                .addComponent(jButton27))
            .addContainerGap(22, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(33, 33, 33)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(gameStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(drawingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 769, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(brushSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(colorsBtnPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(UndoBtn)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(clearBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addContainerGap(37, Short.MAX_VALUE))
    );
    jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(56, 56, 56)
            .addComponent(gameStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(89, 89, 89)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton1))
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(25, 25, 25)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(drawingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 506, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(clearBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                        .addComponent(jSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(UndoBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(brushSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(colorsBtnPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap(51, Short.MAX_VALUE))
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
    
    // send message using the event from the button
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        sendMessage();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void UndoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UndoBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UndoBtnActionPerformed
    
    
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
            e.printStackTrace();
        }
    }
    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton UndoBtn;
    private javax.swing.JLabel brushSizeLabel;
    private javax.swing.JTextPane chatPane;
    private javax.swing.JButton clearBtn;
    private javax.swing.JPanel colorsBtnPanel;
    private javax.swing.JPanel drawingPanel;
    private javax.swing.JPanel gameStatusPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel roundLabel;
    private javax.swing.JTextPane scoreBoardPane;
    private javax.swing.JLabel secretWordLabel;
    private javax.swing.JLabel timerLabel;
    private javax.swing.JLabel titleStatusLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
