package no.westsec.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.TimerTask;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class ChatFrame extends JFrame {
    // This is the main class for the chat application.
	private static final long serialVersionUID = -5299243330480521183L;
	private final Users currentUser;
    private final String secretKey;
    private final MessageDAO messageDAO = new MessageDAO();
    private final ChannelDAO channelDAO = new ChannelDAO();
    private final UsersDAO usersDAO = new UsersDAO();
    private final Logger logger = System.getLogger("ChatFrame");
    private JComboBox<String> channelComboBox;
    private JTextArea usersInChannelList;
    private JEditorPane chatArea;
    private JTextArea userArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton fileButton;
    private JButton voiceButton; // New button for voice message
    private Channel currentChannel;
    private JScrollPane userScrollPane;
    private final String fileServer = "http://westsec.no:5000";

    public ChatFrame(Users user, String secretKey, String VERSION) {
        super("WestSec Chat " + VERSION + " – " + user.getUsername());
        this.currentUser = user;
        this.secretKey = secretKey;
        initComponents();
        loadChannels();
        loadMessages();
        startAutoRefresh();
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        autoLogoutWhenClosed();
    }
    @SuppressWarnings("unused")
	private void initComponents() {
        // Top panel with channel dropdown and private username input
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Channel:"));
        channelComboBox = new JComboBox<>();
        channelComboBox.addActionListener(e -> selectChannel());
        channelComboBox.setPreferredSize(new Dimension(200, 20));
        top.add(channelComboBox);

        JButton createBtn = new JButton("New Channel");
        createBtn.addActionListener(e -> createChannel());
        top.add(createBtn);

        top.add(new JLabel("User:"));
        userArea = new JTextArea();
        userArea.setPreferredSize(new Dimension(100, 20));
        top.add(userArea);

        add(top, BorderLayout.NORTH);

     // Chat area – with HTML support and clickable links
        chatArea = new JEditorPane();
        chatArea.setContentType("text/html");
        chatArea.setEditable(false);
        chatArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        chatArea.setBackground(Color.WHITE);

        // Listener for clickable encrypted files
        chatArea.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String filePath = e.getDescription(); // full file path
                    decryptAndOpenFile(filePath);
                }
            }
        });
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // User list (right panel)
        usersInChannelList = new JTextArea();
        usersInChannelList.setEditable(false);
        usersInChannelList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        usersInChannelList.setBackground(Color.LIGHT_GRAY);
        userScrollPane = new JScrollPane(usersInChannelList);
        userScrollPane.setPreferredSize(new Dimension(150, 0));
        add(userScrollPane, BorderLayout.EAST);

        // Bottom panel with message field and buttons
        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        messageField = new JTextField();
        bottom.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        bottom.add(sendButton, BorderLayout.EAST);

        fileButton = new JButton("📎");
        fileButton.setToolTipText("Send file to selected user");
        fileButton.addActionListener(e -> {
            if (!userArea.getText().trim().isEmpty()) {
                sendFileToUser(userArea.getText().trim());
            } else {
                JOptionPane.showMessageDialog(this, "No private user selected for file transfer.");
            }
        });
        bottom.add(fileButton, BorderLayout.WEST);
        
        // NEW BUTTON FOR VOICE MESSAGE
        // should be added next to the send button
        voiceButton = new JButton("🎤");
        voiceButton.setToolTipText("Record and send voice message");
        voiceButton.addActionListener(e -> {
            if (!userArea.getText().trim().isEmpty()) {
                recordAndSendVoice(userArea.getText().trim());
            } else {
                JOptionPane.showMessageDialog(this, "No private user selected for voice message.");
            }
        });
        bottom.add(voiceButton, BorderLayout.SOUTH);
        // END OF NEW BUTTON  
        add(bottom, BorderLayout.SOUTH);
        
        // Set default button for Enter key to send message
        getRootPane().setDefaultButton(sendButton);
    }
    private void recordAndSendVoice(String username) {
        new Thread(() -> {
            try {
                String voiceName = currentUser.getUsername() + "_voice.wav";
                File voiceFile = new File("files/" + voiceName);
                voiceFile.getParentFile().mkdirs();

                // Record audio
                AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                if (!AudioSystem.isLineSupported(info)) {
                    JOptionPane.showMessageDialog(this, "Audio format not supported.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(info);
                mic.open(format);
                mic.start();

                AudioInputStream stream = new AudioInputStream(mic);
                File temp = new File("files/temp_voice.wav");

                JOptionPane.showMessageDialog(this, "Recording 5 seconds of voice...");
                Thread stopper = new Thread(() -> {
                    try {
                        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, temp);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                stopper.start();

                // Wait 5 seconds, then stop mic
                Thread.sleep(5000);
                mic.stop();
                mic.close();
                stopper.join(); // ✅ Wait for writing to finish
                
                JOptionPane.showMessageDialog(this, "Recording finished. Encrypting and sending...");

                // Encrypt and upload
                File encFile = new File("files/" + currentUser.getUsername() + "_voice.wav.enc");
                AESGCMUtil.encryptFile(temp, encFile, secretKey);
                temp.delete();

                String serverUrl = fileServer + "/upload_voice";
                FileHandler.uploadFile(serverUrl, encFile, currentUser.getUsername());

                String filePath = fileServer + "/uploaded_files/" + encFile.getName();
                String encryptedPath = AESGCMUtil.AESMain(0, filePath, secretKey);

                Users recipient = usersDAO.getUserFromUsername(username.trim());
                if (recipient == null) {
                    JOptionPane.showMessageDialog(this, "User not found: " + username, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Message msg = new Message(LocalDateTime.now(ZoneId.of("Europe/Oslo")), encryptedPath, currentUser, recipient, null);
                messageDAO.newMessage(msg);
                SwingUtilities.invokeLater(() -> loadMessages());

            } catch (Exception ex) {
                logger.log(Level.ERROR, "Voice message failed", ex);
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, "Voice message error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }
	private void selectChannel() {
        String selectedChannelName = (String) channelComboBox.getSelectedItem();
        if (selectedChannelName != null && !selectedChannelName.isEmpty()) {
            currentChannel = channelDAO.getChannelByName(selectedChannelName);
            userArea.setText("");
            loadMessages();
            loadUsersInChannel();
            userScrollPane.setVisible(true); // vis brukerliste
        } else {
            currentChannel = null;
            loadMessages();
            usersInChannelList.setText(""); // tømmes
            userScrollPane.setVisible(false); // skjul brukerliste
        }
        revalidate();
        repaint();
    }
    private void loadUsersInChannel() {
        SwingUtilities.invokeLater(() -> {
            usersInChannelList.setText("");
            if (currentChannel != null) {
                List<Users> users = channelDAO.getUsersInChannel(currentChannel);
                for (Users u : users) {
                    usersInChannelList.append(u.getUsername() + "\n");
                }
            }
        });
    }
    private void loadMessages() {
        SwingUtilities.invokeLater(() -> {
            chatArea.setText(""); // Tøm chat
            List<Message> messages;
            try {
                if (currentChannel == null) {
                    // Private meldinger
                    messages = messageDAO.getAllMessagesOutsideChannel(currentUser);
                } else {
                    // Kanal-meldinger
                    messages = messageDAO.getMessagesForChannel(currentChannel);
                }
                String html = "<html><body>";
                for (Message m : messages) {
                    String plain = AESGCMUtil.AESMain(1, m.getMessage(), secretKey);
                    String sender = m.getSender().getUsername();
                    String recipient = m.getRecipient() != null ? m.getRecipient().getUsername() : "";
                    if (plain.endsWith(".enc")) {
                    	// Recipient cannot be empty here
                    	html += "[" + sender + "]->[" + recipient + "]: <a href='" + plain + "'>" + plain.substring(plain.lastIndexOf("/") + 1) + "</a><br>";
                    } else {
                    	// From sender to recipient
                    	if (recipient != null && !recipient.isEmpty()) {
                    		html += "[" + sender + "]->[" + recipient + "]: " + plain + "<br>";
                    	} else {
                    		// This is for chat inside a channel
                    		html += "[" + sender + "]: " + plain + "<br>";
                    	}
                    }
                }
                html += "</body></html>";
                chatArea.setText(html);
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error loading messages", e);
				JOptionPane.showMessageDialog(this, "Error sending message: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    private void sendFileToUser(String username) {
    	new File("files").mkdirs();
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = fileChooser.getSelectedFile();
        File encryptedFile = new File("files/" + currentUser.getUsername() + "_" + selectedFile.getName() + ".enc");
		if (encryptedFile.exists()) {
			int overwrite = JOptionPane.showConfirmDialog(this, "File already exists. Overwrite?", "Overwrite",
					JOptionPane.YES_NO_OPTION);
			if (overwrite != JOptionPane.YES_OPTION)
				return;
		}
        
        String sender = currentUser.getUsername();
        String serverUrl = fileServer + "/upload";
        String filePath = fileServer + "/uploaded_files/" + currentUser.getUsername() + "_" + selectedFile.getName() + ".enc";
        // Krypter filen
        try {
            AESGCMUtil.encryptFile(selectedFile, encryptedFile, secretKey);
            FileHandler.uploadFile(serverUrl, encryptedFile, sender);
        } catch (Exception e) {
            logger.log(Level.ERROR, "Feil ved kryptering", e);
            JOptionPane.showMessageDialog(this, "Error with encryption: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Encrypted file path
        String encryptedPath = AESGCMUtil.AESMain(0, filePath, secretKey);
        Users recipient = new UsersDAO().getUserFromUsername(username.trim());
        if (recipient == null) {
            JOptionPane.showMessageDialog(this, "User not found: " + username, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Send message with encrypted file path
        Message msg = new Message(LocalDateTime.now(ZoneId.of("Europe/Oslo")), encryptedPath, currentUser, recipient, null);
        messageDAO.newMessage(msg);
        loadMessages();
    }
    private File downloadEncFile(String encryptedFilePath) {
        File dir = new File("files");
		if (!dir.exists()) {
			dir.mkdirs();
		}
    	String filename = encryptedFilePath.substring(encryptedFilePath.lastIndexOf("/") + 1);
        try {
			FileHandler.downloadFile(encryptedFilePath, "files/" + filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // Sjekk om filen eksisterer etter nedlasting
        // opprett fil-mappe hvis den ikke eksisterer
        File encryptedFile = new File("files/" + filename);
        if (!encryptedFile.exists()) {
            JOptionPane.showMessageDialog(this, "Download failed. Please try again later.", "Download Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        // Slett meldingen fra databasen
		List<Message> messages = messageDAO.getAllMessagesOutsideChannel(currentUser);
		for (Message m : messages) {
			String plain = AESGCMUtil.AESMain(1, m.getMessage(), secretKey);
			if (plain.equals(encryptedFilePath)) {
				messageDAO.deleteMsg(m.getMsgId());
				break;
			}
		}
		// Update chat area
		loadMessages();
		// Return the downloaded file
        
		return encryptedFile;
    }
    private void decryptAndOpenFile(String encryptedFilePath) {
        try {
        	File encryptedFile = downloadEncFile(encryptedFilePath);
            if (!encryptedFile.exists()) {
                JOptionPane.showMessageDialog(this, "File not found: " + encryptedFilePath, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Dekryptert fil lagres i ./files/decrypted/
            File decryptedDir = new File("files/decrypted");
            if (!decryptedDir.exists()) {
                decryptedDir.mkdirs();
            }

            File decryptedFile = new File(decryptedDir, encryptedFile.getName().replace(".enc", ""));
            AESGCMUtil.decryptFile(encryptedFile, decryptedFile, secretKey);

            if (decryptedFile.getName().endsWith(".wav")) {
                playDecryptedAudio(decryptedFile);
            } else if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(decryptedFile);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Cannot open file automatically. File is saved here:\n" + decryptedFile.getAbsolutePath(),
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
        	logger.log(Level.ERROR, "Error opening file", ex);
        	            JOptionPane.showMessageDialog(this, "Error opening file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void playDecryptedAudio(File decryptedFile) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(decryptedFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();

            // Auto-delete the file after playback
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    boolean deleted = decryptedFile.delete();
                    if (!deleted) {
                        System.err.println("Warning: Failed to delete temporary voice file: " + decryptedFile.getAbsolutePath());
                    }
                }
            });

        } catch (Exception e) {
            logger.log(Level.ERROR, "Playback failed", e);
            JOptionPane.showMessageDialog(this, "Audio playback failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadChannels() {
        channelComboBox.removeAllItems();
        channelComboBox.addItem(""); // Global chat (valgfritt)

        List<Channel> userChannels = channelDAO.getChannelsForUser(currentUser);
        for (Channel channel : userChannels) {
            channelComboBox.addItem(channel.getChannelName());
        }
    }
    private void startAutoRefresh() {
        java.util.Timer timer = new java.util.Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> loadMessages());
            }
        }, 3000, 3000); // hvert 3. sekund
    }
    private void autoLogoutWhenClosed() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                new Login().loggedOut(currentUser.getUsername());
            }
        });
    }
    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) return;

        // Handle commands
        if (text.startsWith("/")) {
            handleCommands(text);
            return;
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Oslo"));
        String encrypted = AESGCMUtil.AESMain(0, text, secretKey);
        Message message;
        try {
            if (!userArea.getText().trim().isEmpty()) {
                Users recipient = usersDAO.getUserFromUsername(userArea.getText().trim());
                if (recipient == null) {
                    JOptionPane.showMessageDialog(this, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                message = new Message(now, encrypted, currentUser, recipient, null);
            } else if (currentChannel != null) {
                message = new Message(now, encrypted, currentUser, null, currentChannel);
            } else {
                JOptionPane.showMessageDialog(this, "Choose a channel or username.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            messageDAO.newMessage(message);
            messageField.setText("");
            loadMessages();
        } catch (Exception e) {
            logger.log(Level.ERROR, "Error sending file", e);
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void handleCommands(String command) {
    	if (command.startsWith("/join ")) {
    	    String name = command.substring(6).trim();
    	    Channel ch = channelDAO.getChannelByName(name);
    	    if (ch == null) {
    	        JOptionPane.showMessageDialog(this, "No such channel: " + name, "Error", JOptionPane.ERROR_MESSAGE);
    	        return;
    	    }

    	    // ❗ Hindre duplikatinnsetting
    	    if (ch.getUsers().stream().anyMatch(u -> u.getUserId() == currentUser.getUserId())) {
    	        JOptionPane.showMessageDialog(this, "You're already member of this channel: " + name, "Info", JOptionPane.INFORMATION_MESSAGE);
    	        return;
    	    }

    	    channelDAO.addUserToChannel(name, currentUser);
    	    loadChannels();
    	    loadUsersInChannel();
    	    channelComboBox.setSelectedItem(name);
        	    loadMessages();
        } else if (command.startsWith("/delete_channel ")) {
            String name = command.substring(16).trim();
            Channel ch = channelDAO.getChannelByName(name);
            if (ch == null) {
                JOptionPane.showMessageDialog(this, "Channel not found: " + name, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ch.getCreator().equals(currentUser)) {
                JOptionPane.showMessageDialog(this, "Only the owner of the channel can delete it.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            channelDAO.deleteChannel(ch);
            loadChannels();
            channelComboBox.setSelectedIndex(0); // Tilbake til global
        } else if (command.equalsIgnoreCase("/logout")) {
            logOut();
        } else if (command.startsWith("/leave")) {
            if (currentChannel == null) {
                JOptionPane.showMessageDialog(this, "You are not inside a channel.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            channelDAO.removeUserFromChannel(currentChannel.getChannelName(), currentUser);
            JOptionPane.showMessageDialog(this, "You have left the channel: " + currentChannel.getChannelName(), "Info", JOptionPane.INFORMATION_MESSAGE);
                
            currentChannel = null;
            loadChannels(); // Oppdater kanal-listen
            channelComboBox.setSelectedIndex(0); // Tilbake til global
            loadMessages(); // Vis globale/private meldinger
        } else {
            JOptionPane.showMessageDialog(this, "Unknown command: " + command, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void logOut() {
		int choice = JOptionPane.showConfirmDialog(this, "Do you want to log out?", "Log out", JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION) {
			new Login().loggedOut(currentUser.getUsername());
			dispose();
		}
	}
	private void createChannel() {
        String name = JOptionPane.showInputDialog(this, "Type channel name:");
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Channel name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Trim whitespace
        String trimmedName = name.trim();

        // Sjekk om kanal allerede finnes
        Channel existing = channelDAO.getChannelByName(trimmedName);
        if (existing != null) {
            JOptionPane.showMessageDialog(this, "Channel alrready exists: " + trimmedName, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Channel newChannel = new Channel(trimmedName, currentUser);
            channelDAO.createChannel(newChannel);
            channelDAO.addUserToChannel(trimmedName, currentUser);
            loadChannels();
            channelComboBox.setSelectedItem(trimmedName);
            JOptionPane.showMessageDialog(this, "Channel created: " + trimmedName, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            logger.log(Level.ERROR, "Error creating channel", e);
            JOptionPane.showMessageDialog(this, "Could not create channel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}