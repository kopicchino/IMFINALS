<<<<<<< HEAD
import ui.LoginFrame;
import util.UIHelper;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Apply modern global UI styles before any frame is created
        UIHelper.applyGlobalStyles();

=======
import javax.swing.*;
import ui.shared.LoginFrame;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
>>>>>>> 0bc87d04903327a398d57bc0ad7a11b23bfb99e6
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}