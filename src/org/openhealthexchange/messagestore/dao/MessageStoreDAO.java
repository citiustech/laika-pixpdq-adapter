package org.openhealthexchange.messagestore.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.openhealthexchange.openpixpdq.ihe.log.MessageStore;
/**
 * This class processes MessageStore.
 * 
 * @author Anil kumar
 * @date Nov 25, 2008
 */
public interface MessageStoreDAO {
	
	/**
	 * Method to save the MessageStore object.
	 * @param messageLog
	 * @throws HibernateException
	 */
	 public void saveLog(MessageStore messageLog);
	 
	/**
	 * Method to search the log data based on the search criteria.
	 * @param messageLog
	 * @return List<MessageStore>
	 * @throws HibernateException
	 */	
	 public List<MessageStore> searchLog(MessageStore messageLog);

}
