package no.westsec.chat;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "channel")
public class Channel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "channel_id")
	private int channelId;

	@Column(name = "channel_name")
	private String channelName;

	@ManyToOne
	@JoinColumn(name = "creator")
	private Users creator; // User who created the channel

	@Column(name = "creation_timestamp")
	private LocalDateTime creationTimestamp;

    @ManyToMany
    @JoinTable(
            name = "channel_users", 
            joinColumns = @JoinColumn(name = "channel_id"), 
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<Users> users = new ArrayList<>(); // List of users in the channel

    // Default constructor
    public Channel() {}

    // Constructor with all fields (including creationTimestamp)
    public Channel(String channelName, Users creator) {
        this.channelName = channelName;
        this.creator = creator;
        this.creationTimestamp = LocalDateTime.now(ZoneId.of("Europe/Oslo")); // Set creation timestamp
    }

    // Getters and Setters
    public int getChannelId() {
        return channelId;
    }
    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }
    public String getChannelName() {
        return channelName;
    }
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
    public Users getCreator() {
        return creator;
    }
    public void setCreator(Users creator) {
        this.creator = creator;
    }
    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }
    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }
    public List<Users> getUsers() {
        return users;
    }
    public void setUsers(List<Users> users) {
        this.users = users;
    }
    // Helper method to add a user to the channel
    public void addUser(Users user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }
    // Helper method to remove a user from the channel
    public void removeUser(Users user) {
        users.remove(user);
    }
}
