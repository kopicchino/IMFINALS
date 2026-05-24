import ui.LoginFrame;
import util.UIHelper;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Apply modern global UI styles before any frame is created
        UIHelper.applyGlobalStyles();

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}