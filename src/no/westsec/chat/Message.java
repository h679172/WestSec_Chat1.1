package no.westsec.chat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msg_id")
    private int msgId;

    @Column(name = "message")
    private String message;

    @Column(name = "msg_timestamp")
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "sender_user_id")
    private Users sender;

    @ManyToOne
    @JoinColumn(name = "recipient_user_id")
    private Users recipient;

    @ManyToOne
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column(name = "file_path")
    private String filePath;

    public Message() {}

    public Message(LocalDateTime timestamp, String message, Users sender, Users recipient, Channel channel) {
        this.timestamp = timestamp;
        this.message = message;
        this.sender = sender;
        this.recipient = recipient;
        this.channel = channel;
        this.filePath = message.endsWith(".enc") ? message : null;
    }

    public int getMsgId() {
        return msgId;
    }
    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }
    public LocalDateTime getMsgTimestamp() {
        return timestamp;
    }
    public void setMsgTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Users getSender() {
        return sender;
    }
    public void setSender(Users sender) {
        this.sender = sender;
    }
    public Users getRecipient() {
        return recipient;
    }
    public void setRecipient(Users recipient) {
        this.recipient = recipient;
    }
    public Channel getChannel() {
        return channel;
    }
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
	@Override
	public String toString() {
		return "Message [msgId=" + msgId + ", message=" + message + ", timestamp=" + timestamp + ", sender=" + sender
				+ ", recipient=" + recipient + ", channel=" + channel + ", filePath=" + filePath + "]";
	}
}
