package no.westsec.chat;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class Users {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int userId;

	@Column(name = "username", unique = true, nullable = false)
	private String username;

	@Column(name = "password")
	private String password;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "surname", nullable = false)
	private String surname;

	@Column(name = "birthdate", nullable = false)
	private Date birthdate;

	@Column(name = "address", nullable = false)
	private String address;

	@Column(name = "zip_code", nullable = false)
	private String zipCode;

	@Column(name = "city", nullable = false)
	private String city;

	@Column(name = "country", nullable = false)
	private String country;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "phone", nullable = false)
	private String phone;

	@Column(name = "secret_key")
	private String secretKey;

	@Column(name = "active_status")
	private Integer activeStatus;
	
	@Column(name = "salt")
	private String salt;
	        
	@ManyToMany
	@JoinTable(name = "channel_users", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "channel_id"))
	private List<Channel> channels = new ArrayList<>();
	        
	// No-argument constructor
	public Users() {
	}
	
	// Constructor with parameters
	public Users(String username, String password, String name, String surname, Date birthdate, String address,
			String zipCode, String city, String country, String email, String phone) {
		this.username = username;
		this.password = Password.createSHA256Hash(password);
		this.name = name;
		this.surname = surname;
		this.birthdate = birthdate;
		this.address = address;
		this.zipCode = zipCode;
		this.city = city;
		this.country = country;
		this.email = email;
		this.phone = phone;
	}
	// Constructor with parameters including active status
	public Users(String username, String password, String name, String surname, Date birthdate, String address,
			String zipCode, String city, String country, String email, String phone, Integer activeStatus) {
		this.username = username;
		this.password = Password.createSHA256Hash(password);
		this.name = name;
		this.surname = surname;
		this.birthdate = birthdate;
		this.address = address;
		this.zipCode = zipCode;
		this.city = city;
		this.country = country;
		this.email = email;
		this.phone = phone;
		this.activeStatus = activeStatus;
	}
	// Getter and Setter methods
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = Password.createSHA256Hash(password);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public Date getBirthdate() {
		return birthdate;
	}
	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	public Integer getActiveStatus() {
		return activeStatus;
	}
	public void setActiveStatus(Integer activeStatus) {
		// set active for 5 minutes.
		// if active status is 1, set active status to 1
		// if active status is 0 and current time is after 5 minutes, set active status to 0
        // else set active status to active status
		Date now = new Date();
		Date fiveMinutesLater = new Date(now.getTime() + 5 * 60 * 1000);
		if (activeStatus == 1) {
			this.activeStatus = 1;
		} else if (activeStatus == 0 && now.after(fiveMinutesLater)) {
			this.activeStatus = 0;
		} else {
			this.activeStatus = activeStatus;
		}
	}
	public String getSalt() {
	    return salt;
	}
	public void setSalt(String salt) {
	    this.salt = salt;
	}
	public void setLoggedIn(boolean loggedIn) {
		if (loggedIn) {
			this.activeStatus = 1;
		} else {
			this.activeStatus = 0;
		}
	}
	public boolean isLoggedIn() {
		return activeStatus == 1;
	}
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}
	public List<Channel> getChannels() {
		return channels;
	}

	public void addChannel(Channel channel) {
		this.channels.add(channel);
	}

	public void removeChannel(Channel channel) {
		this.channels.remove(channel);
	}

	public void clearChannels() {
		this.channels.clear();
	}

	public boolean isChannelMember(Channel channel) {
		return this.channels.contains(channel);
	}

	public boolean isChannelMember(int channelId) {
		for (Channel channel : channels) {
			if (channel.getChannelId() == channelId) {
				return true;
			}
		}
		return false;
	}
	public boolean isChannelMember(String channelName) {
		for (Channel channel : channels) {
			if (channel.getChannelName().equals(channelName)) {
				return true;
			}
		}
		return false;
	}
	public boolean isChannelMember(int channelId, String channelName) {
		for (Channel channel : channels) {
			if (channel.getChannelId() == channelId && channel.getChannelName().equals(channelName)) {
				return true;
			}
		}
		return false;
	}
	@Override
	public String toString() {
		return "Users [userId=" + userId + ", username=" + username + ", name=" + name
				+ ", surname=" + surname + ", birthdate=" + birthdate + ", address=" + address + ", zipCode=" + zipCode
				+ ", city=" + city + ", country=" + country + ", email=" + email + ", phone=" + phone + "]";
	}
}
