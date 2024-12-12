
package skribbl_clone;


public class App {
    public static void main(String[] args) {
        GameMenu menu = new GameMenu();
        menu.setTitle("DoodleDuel - Menu");
        menu.setResizable(true);
        menu.setLocationRelativeTo(null);
        menu.pack();
        menu.setVisible(true);
        
    }
}