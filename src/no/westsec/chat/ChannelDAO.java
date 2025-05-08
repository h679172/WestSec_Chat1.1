package no.westsec.chat;

import jakarta.persistence.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChannelDAO {
    private static final Logger logger = Logger.getLogger(ChannelDAO.class.getName());
    private final EntityManagerFactory emf;

    public ChannelDAO() {
        emf = Persistence.createEntityManagerFactory("WestsecChatPU");
    }

    // Create a new channel
    public void createChannel(Channel channel) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(channel);
            tx.commit();
            logger.info("Channel created: " + channel);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.log(Level.SEVERE, "Error creating channel", e);
        } finally {
            em.close();
        }
    }

    // Get a list of all channels
    public List<Channel> getAllChannels() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Channel c", Channel.class).getResultList();
        } finally {
            em.close();
        }
    }
    public List<Channel> getChannelsForUser(Users user) {
    	EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<Channel> query = em
					.createQuery("SELECT c FROM Channel c JOIN c.users u WHERE u.userId = :userId", Channel.class);
			query.setParameter("userId", user.getUserId());
			return query.getResultList();
		} finally {
			em.close();
		}
    }

    // Get a channel by its name
    public Channel getChannelByName(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Channel> q = em.createQuery(
                "SELECT c FROM Channel c WHERE c.channelName = :name", Channel.class
            );
            q.setParameter("name", name);
            return q.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    // Update an existing channel
    public void updateChannel(Channel channel) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(channel);
            tx.commit();
            logger.info("Channel updated: " + channel);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.log(Level.SEVERE, "Error updating channel", e);
        } finally {
            em.close();
        }
    }

    // Delete a channel
    public void deleteChannel(Channel channel) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Channel managed = em.merge(channel);
            em.remove(managed);
            tx.commit();
            logger.info("Channel deleted: " + channel);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.log(Level.SEVERE, "Error deleting channel", e);
        } finally {
            em.close();
        }
    }

    /** 
     * Add a user to the channel's membership.
     * This assumes you have a many-to-many relationship set up between Channel and Users entities
     */
    public void addUserToChannel(String channelName, Users user) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Channel channel = getChannelByName(channelName);
            if (channel == null) {
                logger.warning("Channel not found: " + channelName);
                return;
            }
            if (channel.getUsers().contains(user)) {
                logger.warning("User already in channel: " + user.getUsername());
                return;
            }
            tx.begin();
            channel = em.merge(channel);
            user = em.merge(user);
            channel.getUsers().add(user);
            em.merge(channel);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.log(Level.SEVERE, "Error adding user to channel", e);
        } finally {
            em.close();
        }
    }
    /** 
     * Remove a user from the channel's membership.
     */
    public void removeUserFromChannel(String channelName, Users user) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Channel channel = getChannelByName(channelName);
            if (channel == null) {
                logger.warning("Channel not found: " + channelName);
                return;
            }
            tx.begin();
            channel = em.merge(channel);
            user = em.merge(user);
            channel.getUsers().remove(user);  // Remove the user from the channel
            em.merge(channel); // Update the channel entity
            tx.commit();
            logger.info("Removed user " + user.getUsername() + " from channel " + channelName);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            logger.log(Level.SEVERE, "Error removing user from channel", e);
        } finally {
            em.close();
        }
    }
    // Channel creator can remove users from the channel
	public void removeUserFromChannel(String channelName, Users user, Users creator) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			Channel channel = getChannelByName(channelName);
			if (channel == null) {
				logger.warning("Channel not found: " + channelName);
				return;
			}
			if (!channel.getCreator().equals(creator)) {
				logger.warning("Only the channel creator can remove users.");
				return;
			}
			tx.begin();
			channel = em.merge(channel);
			user = em.merge(user);
			channel.getUsers().remove(user); // Remove the user from the channel
			em.merge(channel); // Update the channel entity
			tx.commit();
			logger.info("Removed user " + user.getUsername() + " from channel " + channelName + " by creator "
					+ creator.getUsername());
		} catch (Exception e) {
			if (tx.isActive())
				tx.rollback();
			logger.log(Level.SEVERE, "Error removing user from channel", e);
		} finally {
			em.close();
		}
	}
    // Close the EntityManagerFactory to free resources
    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
    // Get all users in a specific channel
	public List<Users> getUsersInChannel(Channel currentChannel) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<Users> query = em
					.createQuery("SELECT u FROM Users u JOIN u.channels c WHERE c.channelId = :channelId", Users.class);
			query.setParameter("channelId", currentChannel.getChannelId());
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	public List<Channel> getAllChannelsForUser(Users currentUser) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<Channel> query = em
					.createQuery("SELECT c FROM Channel c JOIN c.users u WHERE u.userId = :userId", Channel.class);
			query.setParameter("userId", currentUser.getUserId());
			return query.getResultList();
		} finally {
			em.close();
		}
	}
}
