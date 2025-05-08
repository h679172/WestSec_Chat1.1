package no.westsec.chat;
import java.util.logging.Logger;

public class Login {
    private static final Logger logger = Logger.getLogger(Login.class.getName());
    public static final String SUCCESS = "Login successful";
    public static final String FAILURE = "Login failed";
    public static final String LOGOUT = "Logout successful";
    public static final String INVALID = "Invalid username or password";
    public static final String ALREADY_LOGGED_IN = "User already logged in";
    
	public boolean loggedIn(String username, String password) {
		UsersDAO ud = new UsersDAO();
		Users user = ud.getUserFromUsername(username);
		if (user != null) {
			if (user.getPassword().equals(password)) {
				if (!user.isLoggedIn()) {
					user.setLoggedIn(true);
					ud.updateUser(user);
					logger.info("User " + username + " logged in successfully.");
					ud.close();
					return true;
				} else {
					logger.warning("User " + username + " is already logged in.");
					ud.close();
					return false;
				}
			} else {
				logger.warning("Invalid password for user: " + username);
				ud.close();
				return false;
			}
		} else {
			logger.warning("User not found: " + username);
			ud.close();
			return false;
		}
	}
	public boolean loggedOut(String username) {
		UsersDAO ud = new UsersDAO();
		Users user = ud.getUserFromUsername(username);
		if (user != null) {
			if (user.isLoggedIn()) {
				user.setLoggedIn(false);
				ud.updateUser(user);
				logger.info("User " + username + " logged out successfully.");
				ud.close();
				return true;
			} else {
				logger.warning("User " + username + " is not logged in.");
				ud.close();
				return false;
			}
		} else {
			logger.warning("User not found: " + username);
			ud.close();
			return false;
		}
	}
}
