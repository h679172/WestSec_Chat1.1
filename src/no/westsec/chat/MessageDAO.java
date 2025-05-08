package no.westsec.chat;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class MessageDAO {
    private final EntityManagerFactory emf;
    private static final Logger logger = Logger.getLogger(MessageDAO.class.getName());

    public MessageDAO() {
        emf = Persistence.createEntityManagerFactory("WestsecChatPU");
    }
    public void newMessage(Message m) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(m);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.severe("Error saving message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public String getFilePathForMessage(int msgId) {
        try (EntityManager em = emf.createEntityManager()) {
            Message m = em.find(Message.class, msgId);
            return m != null ? m.getFilePath() : null;
        }
    }
    public void deleteAllMessagesWithRecipient(Users recipient) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Message m WHERE m.recipient = :recipient")
                .setParameter("recipient", recipient)
                .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.severe("Error deleting messages: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public List<Message> getAllMessages() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT m FROM Message m", Message.class).getResultList();
        }
    }
    public List<Message> getAllMessagesOutsideChannel(Users user) {  
		try (EntityManager em = emf.createEntityManager()) {
			return em.createQuery(
					"SELECT m FROM Message m WHERE m.channel IS NULL AND (m.sender = :user OR m.recipient = :user)",
					Message.class).setParameter("user", user).getResultList();
		}
    }
	public List<Message> getAllMessagesForUser(Users user) {
		try (EntityManager em = emf.createEntityManager()) {
			return em
					.createQuery("SELECT m FROM Message m WHERE m.sender = :user OR m.recipient = :user", Message.class)
					.setParameter("user", user).getResultList();
		}
	}
    public Message getMsgFromId(int msgId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Message.class, msgId);
        }
    }
    public void deleteMsg(int msgId) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Message m = em.find(Message.class, msgId);
            if (m != null) {
                em.remove(m);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.severe("Error deleting message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public List<Message> getMessagesFromSender(Users sender) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT m FROM Message m WHERE m.sender = :sender", Message.class)
                     .setParameter("sender", sender)
                     .getResultList();
        }
    }
    public List<Message> getMessagesFromRecipient(Users recipient) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT m FROM Message m WHERE m.recipient = :recipient", Message.class)
                     .setParameter("recipient", recipient)
                     .getResultList();
        }
    }
    public List<Message> getMessagesBetweenUsers(Users sender, Users recipient) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                "SELECT m FROM Message m WHERE (m.sender = :sender AND m.recipient = :recipient) " +
                "OR (m.sender = :recipient AND m.recipient = :sender)", Message.class)
                .setParameter("sender", sender)
                .setParameter("recipient", recipient)
                .getResultList();
        }
    }
    public List<Message> getMessagesForChannel(Channel channel) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT m FROM Message m WHERE m.channel = :channel", Message.class)
                     .setParameter("channel", channel)
                     .getResultList();
        }
    }
    public List<Message> getMessagesFromDate(LocalDate date) {
        try (EntityManager em = emf.createEntityManager()) {
            Date start = Date.from(date.atStartOfDay(ZoneId.of("Europe/Oslo")).toInstant());
            Date end = Date.from(date.plusDays(1).atStartOfDay(ZoneId.of("Europe/Oslo")).toInstant());
            TypedQuery<Message> query = em.createQuery(
                "SELECT m FROM Message m WHERE m.msg_timestamp >= :start AND m.msg_timestamp < :end",
                Message.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getResultList();
        }
    }
    public List<Message> getNewMessagesForChannel(Channel channel, LocalDateTime since) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Message> query = em.createQuery(
                "SELECT m FROM Message m WHERE m.channel = :channel AND m.msg_timestamp > :since ORDER BY m.msg_timestamp ASC",
                Message.class
            );
            query.setParameter("channel", channel);
            query.setParameter("since", since);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
