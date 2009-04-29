package org.openhealthexchange.messagestore.dao;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.openhealthexchange.openpixpdq.ihe.log.MessageStore;


/**
 * 
 * @author Anil kumar
 * @date Nov 25, 2008
 */
public class MessageStoreDAOImpl implements MessageStoreDAO {

    private static Logger log = Logger.getLogger(MessageStoreDAOImpl.class);

    public void saveLog(MessageStore messageLog) {
        Session session = null;
        try {
            File file = new File("./conf/hibernate.cfg.xml");
            SessionFactory sessionFactory = new Configuration().configure(file)
                    .buildSessionFactory();
            session = sessionFactory.openSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            //session.saveOrUpdate(messageLog);
            session.save(messageLog);
            tx.commit();
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<MessageStore> searchLog(MessageStore messageLog) {

        Session session = null;
        Criteria crit = null;
        List<MessageStore> messages = null;
        try {
            SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
            session = sessionFactory.openSession();
            crit = session.createCriteria(MessageStore.class);

            if (messageLog.getIp() != null) {
                crit.add(Restrictions.like("ip", messageLog.getIp(),
                        MatchMode.ANYWHERE));
            }
            if (messageLog.getInMessage() != null) {
                crit.add(Restrictions.like("inMessage", messageLog.getInMessage(),
                        MatchMode.ANYWHERE));
            }
            if (messageLog.getOutMessage() != null) {
                crit.add(Restrictions.like("outMessage",
                        messageLog.getOutMessage(), MatchMode.ANYWHERE));
            }
            if (messageLog.getMessageDate() != null) {
                Timestamp ts = new Timestamp(messageLog.getMessageDate().getTime());
                crit.add(Restrictions.ge("messageDate", ts));
            }
            if (messageLog.getErrorMessage() != null) {
                crit.add(Restrictions.like("errorMessage", messageLog.getErrorMessage(), MatchMode.ANYWHERE));
            }
            if (messageLog.getMessageCode() != null) {
                crit.add(Restrictions.like("messageCode", messageLog.getMessageCode(), MatchMode.ANYWHERE));
            }
            if (messageLog.getTriggerEvent() != null) {
                crit.add(Restrictions.like("triggerEvent", messageLog.getTriggerEvent(), MatchMode.ANYWHERE));
            }
            if (messageLog.getMessageId() != null) {
                crit.add(Restrictions.like("messageId", messageLog.getMessageId(),
                        MatchMode.ANYWHERE));
            }
            if (messageLog.getSendingFacility() != null) {
                crit.add(Restrictions.like("sendingFacility", messageLog.getSendingFacility(), MatchMode.ANYWHERE));
            }
            if (messageLog.getSendingApplication() != null) {
                crit.add(Restrictions.like("sendingApplication", messageLog.getSendingApplication(), MatchMode.ANYWHERE));
            }
            if (messageLog.getReceivingFacility() != null) {
                crit.add(Restrictions.like("receivingFacility", messageLog.getReceivingFacility(), MatchMode.ANYWHERE));
            }
            if (messageLog.getReceivingApplication() != null) {
                crit.add(Restrictions.like("receivingApplication", messageLog.getReceivingApplication(), MatchMode.ANYWHERE));
            }
            messages = crit.list();
            session.close();
        } catch (HibernateException e) {
            log.error(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messages;
    }

    public static void main(String[] args) throws ParseException {
        String inmessage = "MSH|^~\\&|anilcpr|anilcpr|||1215429605876||ADT^A08|1215429605876|P|2.4|1215429605876" + "EVN|A08|1215429605891" + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + "PD1||||||||||||1" + "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn" + "PV2|||^reason1|||||||||reason1||||||||||1" + "DG1||||fever";
        String outmessage = "MSH|^~\\&|anilcpr|anilcpr|||1215429605876||ADT^A08|1215429605876|P|2.4|1215429605876" + "EVN|A08|1215429605891" + "PID||raj|raj^^^raj||kk^kk^kk^kk^kk|kk|2007-12-27T10:39:43.375Z|m||^kk|212null^^121^^^^HomeAddress||^^^^^^21121|^^^^^^121212|^kk|^kk|^kk||123556666|kk||^kk" + "PD1||||||||||||1" + "PV1||I|||||^d^dfd^fd^^df||||||||||||v1|||||||||||||||||||||||||2007-12-27T10:39:43.375Z|2007-12-27T10:39:43.375Z" + "PV2|||^reason1|||||||||reason1||||||||||1" + "DG1||||fever";
        String ip = "129.1.33.204T14:08:56.235-0700";
        SimpleDateFormat date = new SimpleDateFormat(
                "MM/dd/yyyy");
        Date date1 = date.parse("05/07/2008");
        MessageStore mess = new MessageStore();
        mess.setIp(ip);
        mess.setInMessage(inmessage);
        mess.setOutMessage(outmessage);
        mess.setMessageDate(date1);
        mess.setErrorMessage("debug");
        mess.setMessageId("10250");
        mess.setReceivingFacility("anil");
        mess.setReceivingApplication("anil");
        mess.setSendingFacility("rasa");
        mess.setSendingApplication("rasa");
        MessageStoreDAOImpl messdao = new MessageStoreDAOImpl();
        //messdao.saveLog(mess);
        //messdao.seachLog(mess);

        String rstr = "<br>";
        String str = "EVN,PID,PD1,PV1";
        String output = "";
        String[] strarr = str.split(",");
        for (int i = 0; i < strarr.length - 1; i++) {
            String[] arr1 = outmessage.split(strarr[i]);
            output += arr1 + rstr;
            System.out.println(arr1.toString());
        }
    }
}
