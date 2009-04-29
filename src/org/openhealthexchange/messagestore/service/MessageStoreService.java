package org.openhealthexchange.messagestore.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.openhealthexchange.messagestore.dao.MessageStoreDAO;
import org.openhealthexchange.messagestore.dao.MessageStoreDAOImpl;
import org.openhealthexchange.openpixpdq.ihe.log.IMessageStoreLogger;
import org.openhealthexchange.openpixpdq.ihe.log.MessageStore;

/**
 * 
 * @author Anil kumar
 * @date Nov 25, 2008
 */

public class MessageStoreService implements IMessageStoreLogger {

	private static Logger log = Logger.getLogger(MessageStoreService.class);

	private MessageStoreDAO messagelogdao = new MessageStoreDAOImpl();
	
	public void saveLog(MessageStore messageLog)  {
		if (messageLog.getInMessage() != null) {
			String messagein=messageLog.getInMessage().replace("\r", "<br>");;
			messageLog.setInMessage(messagein);
		}
		if (messageLog.getOutMessage() != null) {
			String messageout=messageLog.getOutMessage().replace("\r", "<br>");
			messageLog.setOutMessage(messageout);
		}
		log.info("Service saveLog method called");
		messagelogdao.saveLog(messageLog);
	}

	public List<MessageStore> searchLog(MessageStore messageLog)
		 {
		log.info("Service seachLog method called");
		return messagelogdao.searchLog(messageLog);

	}
}
