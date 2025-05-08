package no.westsec.chat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import java.util.List;
import java.util.logging.Logger;

public class UsersDAO {
	// Logger for logging purposes
	private static final Logger logger = Logger.getLogger(UsersDAO.class.getName());
    private EntityManagerFactory emf;
    private EntityManager em;

    public UsersDAO() {
        // Create an EntityManagerFactory and EntityManager
        emf = Persistence.createEntityManagerFactory("WestsecChatPU");
        em = emf.createEntityManager();
    }

    // Method to get a user from username
    public Users getUserFromUsername(String username) {
        try {
            Query query = em.createQuery("SELECT u FROM Users u WHERE u.username = :username");
            query.setParameter("username", username);
            return (Users) query.getSingleResult();
        } catch (Exception e) {
			e.printStackTrace();
			// Log the error
			logger.severe("Error fetching user: " + e.getMessage());
		}
        return null;
    }

    // Method to get the password for a given username
    public String getPasswordFromUsername(String username) {
        try {
            Query query = em.createQuery("SELECT u.password FROM Users u WHERE u.username = :username");
            query.setParameter("username", username);
            
            // Check if there are results before calling getSingleResult()
            @SuppressWarnings("unchecked")
			List<String> results = query.getResultList();
            
            if (results.isEmpty()) {
                // No result found, return null or handle accordingly
                return null;
            }
            
            // Return the password if found
            return results.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // Method to create a new user
	public void createUser(Users user) {
		try {
			em.getTransaction().begin();
			em.persist(user); // Save the new user
			em.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			em.getTransaction().rollback();
		}
	}
	    // Method to get a user by ID
	public Users getUserById(int userId) {
        try {
            return em.find(Users.class, userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
	}
    // Method to update the user's logged-in status
    public void updateUser(Users user) {
        try {
            em.getTransaction().begin();
            em.merge(user); // Update the user (loggedIn status will be updated)
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
    }
    // Method to get all users (just in case you need this functionality)
    @SuppressWarnings("unchecked")
	public List<Users> getAllUsers() {
        try {
            Query query = em.createQuery("SELECT u FROM Users u");
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // Closing the EntityManager and EntityManagerFactory
    public void close() {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }
}
