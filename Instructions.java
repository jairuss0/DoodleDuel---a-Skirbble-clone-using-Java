
package skribbl_clone;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;


public class Instructions extends javax.swing.JFrame {
    
    private Image image;
    private final String[] instructions = {"When it's your turn, The game will select a random word for you to draw!",
    "Try and draw the word the game has selected! No spelling!",
    "Try to guess what other players are drawing when it's not your turn!",
    "Get the most points and win the victory at the end!"};
    private final String[] imagePath =  {"assets/instruction-1-min.png","assets/instruction-2-min.png","assets/instruction-3-min.png"
            ,"assets/instruction-4-min.png"};
    private int currentIndex = 0;
    private Image backgroundImage;
   
    public Instructions() {
        setBackgroundImage("assets/menuBg.png");
        initComponents();
        initializeInstruction(currentIndex);
        prevButton.setVisible(false);
    }
    
    // Method to set the background image
    public void setBackgroundImage(String imagePath) {
        backgroundImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
        revalidate();
        repaint();
    }
    
    public void initializeInstruction(int index){
        image = new ImageIcon(getClass().getResource(imagePath[index])).getImage();
        instructionsLabel.setText(instructions[index]);
        revalidate();
        repaint();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw the background image if it's set
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        jPanel1 = new javax.swing.JPanel();
        instructionsLabel = new javax.swing.JLabel();
        prevButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        imagePanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw the background image if it's set
                // Scale the image to fit the imagePanel

                if (image != null) {
                    g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                }
            }

        };

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("How to Play");
        setResizable(false);

        mainPanel.setBackground(new java.awt.Color(0, 51, 102));

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        instructionsLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 24)); // NOI18N
        instructionsLabel.setForeground(new java.awt.Color(255, 255, 255));
        instructionsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        instructionsLabel.setText("jLabel1");
        instructionsLabel.setToolTipText("");
        jPanel1.add(instructionsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 527, 910, 80));

        prevButton.setBackground(new java.awt.Color(102, 204, 0));
        prevButton.setFont(new java.awt.Font("Segoe UI Emoji", 1, 18)); // NOI18N
        prevButton.setForeground(new java.awt.Color(255, 255, 255));
        prevButton.setText("←");
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });
        jPanel1.add(prevButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 230, 60, 45));

        nextButton.setBackground(new java.awt.Color(102, 204, 0));
        nextButton.setFont(new java.awt.Font("Segoe UI Emoji", 1, 18)); // NOI18N
        nextButton.setForeground(new java.awt.Color(255, 255, 255));
        nextButton.setText("→");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        jPanel1.add(nextButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 240, 60, 47));

        imagePanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 0), 4, true));

        javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout(imagePanel);
        imagePanel.setLayout(imagePanelLayout);
        imagePanelLayout.setHorizontalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 799, Short.MAX_VALUE)
        );
        imagePanelLayout.setVerticalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 465, Short.MAX_VALUE)
        );

        jPanel1.add(imagePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 50, -1, -1));

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(62, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 620, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevButtonActionPerformed
        // TODO add your handling code here:
        if(currentIndex > 0){
            currentIndex--;
            initializeInstruction(currentIndex);
        }
        prevButton.setVisible(currentIndex > 0);
        nextButton.setVisible(true); // Ensure the next button is visible when moving backward
    }//GEN-LAST:event_prevButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        // TODO add your handling code here:
        if(currentIndex < imagePath.length - 1){
            currentIndex++;
            initializeInstruction(currentIndex);
        }
        nextButton.setVisible(currentIndex < imagePath.length - 1);
        prevButton.setVisible(true); // Ensure the prev button is visible when moving forward
    }//GEN-LAST:event_nextButtonActionPerformed

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel imagePanel;
    private javax.swing.JLabel instructionsLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton prevButton;
    // End of variables declaration//GEN-END:variables
}
