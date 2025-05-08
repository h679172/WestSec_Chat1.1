package no.westsec.chat;

import javax.swing.*;
import java.awt.*;

public class Main {
	// This is the main class for the chat application.
	private static final String VERSION = "1.1";
	@SuppressWarnings({ "unused"})
	public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("WestSec Chat " + VERSION + " - Login");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(350, 125);
            frame.setLayout(new BorderLayout());

            JPanel panel = new JPanel(new GridLayout(3, 2));
            JPanel panel2 = new JPanel(new GridLayout(1, 2));
            JTextField usernameField = new JTextField();
            JPasswordField passwordField = new JPasswordField();
            JPasswordField secretkeyField = new JPasswordField();
            JButton loginBtn = new JButton("Login");

            panel.add(new JLabel("Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("Password:"));
            panel.add(passwordField);
            panel.add(new JLabel("Secret Key:"));
            panel.add(secretkeyField);
            panel2.add(loginBtn);

            frame.add(panel, BorderLayout.CENTER);
            frame.add(panel2, BorderLayout.SOUTH);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.getRootPane().setDefaultButton(loginBtn);

            loginBtn.addActionListener(e -> {
                String username = usernameField.getText().trim();
                String pass = new String(passwordField.getPassword());
                String secretKey = new String(secretkeyField.getPassword());
                String password = Password.createSHA256Hash(pass);

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Fields cannot be empty");
                    return;
                }
                Login login = new Login();
                if (login.loggedIn(username, password)) {
                    UsersDAO dao = new UsersDAO();
                    Users user = dao.getUserFromUsername(username);
                    dao.close();
                    frame.dispose();
                    // Assuming secretkeyField is a JPasswordField
					if (secretKey.isEmpty()) {
						JOptionPane.showMessageDialog(frame, "Secret key cannot be empty");
						// logout the user
						login.loggedOut(username);
						return;
					}
                    new ChatFrame(user, secretKey, VERSION);
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid login");
                }
            });
        });
    }
	public static String getVersion() {
		return VERSION;
	}
}
