



import java.awt.Toolkit;


public class App {
   
    
    public static void main(String[] args) {
        
        // Set the UI scale property before the application starts
        double scalingFactor = getScalingFactor();
        if (scalingFactor < 1.2) {
            System.setProperty("sun.java2d.uiScale", "1.0");
        } else if (scalingFactor >= 1.2 && scalingFactor < 2.0) {
            System.setProperty("sun.java2d.uiScale", "1.5");
        } else {
            System.setProperty("sun.java2d.uiScale", "2.0");
        }
        System.out.println("UI Scale Set to: " + System.getProperty("sun.java2d.uiScale"));
        
       
        javax.swing.SwingUtilities.invokeLater(() -> {
            GameMenu menu = new GameMenu();
            menu.setTitle("DoodleDuel - Menu");
            menu.setResizable(true);
            menu.setLocationRelativeTo(null);
            menu.pack();
            menu.setVisible(true);
        });
       
       
        
    }
    // Method to retrieve the current scaling factor (DPI)
    public static double getScalingFactor() {
        
        double dpi = Toolkit.getDefaultToolkit().getScreenResolution();

        // Return the scaling factor as a value based on the DPI
        // For example, scaling factors could be based on common DPI values
        if (dpi < 96) {
            return 1.0; // Low DPI screens (Standard)
        } else if (dpi < 144) {
            return 1.5; // Medium DPI screens (Retina HD)
        } else {
            return 2.0; // High DPI screens (4K)
        }
    }
}
