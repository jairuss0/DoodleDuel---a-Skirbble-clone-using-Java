
package skribbl_clone;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.List;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
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
    private ArrayList<Point> points;

    // for styling the textPane
    StyledDocument doc;
    SimpleAttributeSet attrUsernameBold = new SimpleAttributeSet();
    SimpleAttributeSet attrMessage = new SimpleAttributeSet();
    SimpleAttributeSet attrsAnnouncement = new SimpleAttributeSet();
   
    
    public GameClient(Socket socket, String username) {
        this.points = new ArrayList<>();
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
                points.add(e.getPoint());   // Add the dragged point to the list
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
        
        // add the the point to the arraylist point
        points.add(new Point(x,y));
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
                for (Point p : points) {
                    g.fillOval(p.x, p.y, 10, 10); // Draw each point in the list
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

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("GuessMyDrawing");
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
    jScrollPane2.setViewportView(chatPane);

    scoreBoardPane.setEditable(false);
    scoreBoardPane.setBackground(new java.awt.Color(255, 255, 255));
    jScrollPane3.setViewportView(scoreBoardPane);

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
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(drawingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 769, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
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
            .addGap(18, 18, 18)
            .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(100, Short.MAX_VALUE))
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
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE)
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
    private javax.swing.JTextPane chatPane;
    private javax.swing.JPanel drawingPanel;
    private javax.swing.JPanel gameStatusPanel;
    private javax.swing.JButton jButton1;
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
