

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.net.Socket;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class GameMenu extends JFrame {
    private Image backgroundImage;
    
    public GameMenu() {
        setBackgroundImage("assets/menuBg.png");
        initComponents();
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 =  new JPanel(){
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
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        ipInput = new javax.swing.JTextField();
        portInput = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setMinimumSize(new java.awt.Dimension(653, 446));

        jPanel1.setBackground(new java.awt.Color(0, 65, 108));
        jPanel1.setMaximumSize(new java.awt.Dimension(1000, 1000));
        jPanel1.setMinimumSize(new java.awt.Dimension(653, 446));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBackground(new java.awt.Color(255, 102, 51));
        jPanel2.setMaximumSize(new java.awt.Dimension(1200, 700));
        jPanel2.setOpaque(false);
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTextField2.setToolTipText("Enter your name");
        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField2KeyPressed(evt);
            }
        });
        jPanel2.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(72, 146, 325, 31));
        jTextField2.getAccessibleContext().setAccessibleDescription("");

        jButton1.setBackground(new java.awt.Color(115, 239, 115));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("PLAY!");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jButton1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jButton1KeyPressed(evt);
            }
        });
        jPanel2.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(72, 183, 325, 40));

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setFont(new java.awt.Font("Comic Sans MS", 1, 55)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("DoodleDuel");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(72, 6, 325, 122));

        ipInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipInputActionPerformed(evt);
            }
        });
        jPanel2.add(ipInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(72, 295, 168, -1));

        portInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portInputActionPerformed(evt);
            }
        });
        jPanel2.add(portInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(247, 294, 150, -1));

        jLabel2.setFont(new java.awt.Font("Comic Sans MS", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Server IP");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(72, 271, 168, -1));

        jLabel3.setFont(new java.awt.Font("Comic Sans MS", 0, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Port:");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(247, 270, 97, -1));

        jButton2.setBackground(new java.awt.Color(204, 0, 51));
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("HOW TO PLAY?");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 230, 130, 30));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 82;
        gridBagConstraints.ipady = 41;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(51, 90, 37, 84);
        jPanel1.add(jPanel2, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        
        if (!jTextField2.getText().isEmpty() && !ipInput.getText().isEmpty() && !portInput.getText().isEmpty() 
                && !validatePort() && jTextField2.getText().length() < 7  && isValidIPAddress() ) {

            String username = jTextField2.getText();
            String ip = ipInput.getText().trim();
            String port = portInput.getText().trim();

            configSocket(username, ip,port);
        }  else if (jTextField2.getText().isEmpty() && !ipInput.getText().isEmpty() && !portInput.getText().isEmpty()) {
            // add some jOption warning here
            JOptionPane.showMessageDialog(this, "Enter a Username!", "Alert", JOptionPane.WARNING_MESSAGE);

        }
        else if(ipInput.getText().isEmpty() || portInput.getText().isEmpty()){
             JOptionPane.showMessageDialog(this, "Enter Server IP Address and its Port!", "Alert", JOptionPane.WARNING_MESSAGE);
        }
        else if(jTextField2.getText().isEmpty() && ipInput.getText().isEmpty() && portInput.getText().isEmpty()){
        
            JOptionPane.showMessageDialog(this, "Inputs are required!", "Alert", JOptionPane.WARNING_MESSAGE);
        }
        else if(validatePort()){
            JOptionPane.showMessageDialog(this, "Port must be numbers!", "Alert", JOptionPane.WARNING_MESSAGE);
        }
        else if(!isValidIPAddress()){
            JOptionPane.showMessageDialog(this, "IP address is invalid!", "Alert", JOptionPane.WARNING_MESSAGE);
        }
        else{
            JOptionPane.showMessageDialog(this, "Username must be below 6 characters!", "Alert", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    // method to validate port number
    private boolean validatePort(){
        String port = portInput.getText();
        // Check if the input contains any characters (letters)
        return port.matches(".*[a-zA-Z]+.*");
         
    }
    // Method to validate IPv4 addresses using regex
    private  boolean isValidIPAddress() {
        String ip = ipInput.getText();
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$";
        if(ip.equals("localhost")){
            return true;
        }
        return ip.matches(ipPattern);
    }
    // Method to set the background image
    public void setBackgroundImage(String imagePath) {
        backgroundImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
        repaint(); // Repaint the panel to apply the new background image
    }

    
    
    
    private void jButton1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButton1KeyPressed
        
       
    }//GEN-LAST:event_jButton1KeyPressed
    private void configSocket(String username,String ip, String port){
        // Adjust UI scale for high-DPI displays 
        
        int portNo = Integer.parseInt(port);
        try {
            
            // create socket object with the server ip and the server port that is listening to
            Socket socket = new Socket(ip, portNo);
           
            GameClient gameClient = new GameClient(socket, username);
            gameClient.setLocationRelativeTo(null);
            gameClient.pack();
            // method to listen for the incoming messages
            gameClient.listenForMessage();
            gameClient.setVisible(true);
            
            
            this.dispose();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,"Connection failed: No server available, Check if IP Address and Port is correct!","Error",JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    private void jTextField2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyPressed
    }//GEN-LAST:event_jTextField2KeyPressed

    private void ipInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipInputActionPerformed

    private void portInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_portInputActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        
        Instructions instructions = new Instructions();
        instructions.setResizable(false);
        instructions.pack();
        instructions.setLocationRelativeTo(null);
        instructions.setVisible(true);
      
        
    }//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField ipInput;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField portInput;
    // End of variables declaration//GEN-END:variables
}
