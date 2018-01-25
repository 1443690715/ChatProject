package com.yzx.chat.network.chat;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactMessageBean;
import com.yzx.chat.database.ContactDao;
import com.yzx.chat.database.ContactMessageDao;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.rong.imlib.model.Message;
import io.rong.message.ContactNotificationMessage;

/**
 * Created by YZX on 2017年12月31日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class ContactManager {

    private Map<String, ContactBean> mContactsMap;
    private IMClient.SubManagerCallback mSubManagerCallback;
    private List<OnContactChangeListener> mContactChangeListeners;
    private List<OnContactMessageListener> mContactMessageListeners;
    private List<OnContactMessageUnreadCountChangeListener> mContactMessageUnreadCountChangeListeners;
    private ContactMessageDao mContactMessageDao;
    private ContactDao mContactDao;

    private int mContactMessageUnreadNumber;
    private final Object mUpdateContactUnreadNumberLock = new Object();

    private String mUserID;

    public ContactManager(IMClient.SubManagerCallback subManagerCallback) {
        if (subManagerCallback == null) {
            throw new NullPointerException("subManagerCallback can't be NULL");
        }
        mSubManagerCallback = subManagerCallback;
        mContactsMap = new HashMap<>(256);
        mContactMessageDao = DBManager.getInstance().getContactMessageDao();
        mContactDao = DBManager.getInstance().getContactDao();
        mContactChangeListeners = Collections.synchronizedList(new LinkedList<OnContactChangeListener>());
        mContactMessageListeners = Collections.synchronizedList(new LinkedList<OnContactMessageListener>());
        mContactMessageUnreadCountChangeListeners = Collections.synchronizedList(new LinkedList<OnContactMessageUnreadCountChangeListener>());
    }

    public void loadAllContact(String userID) {
        List<ContactBean> contacts = mContactDao.loadAllContacts(userID);
        for (ContactBean contact : contacts) {
            mContactsMap.put(contact.getUserID(), contact);
        }
        mUserID = userID;
    }

    public ContactBean getContact(String userID) {
        return mContactsMap.get(userID);
    }

    public boolean updateContact(ContactBean contact){
        return updateContact(contact,true);
    }

    public boolean updateContact(ContactBean contact,boolean isCallListener) {
        boolean result = mContactDao.update(contact);
        if (result) {
            mContactsMap.put(contact.getUserID(), contact);
            if(isCallListener) {
                for (OnContactChangeListener listener : mContactChangeListeners) {
                    listener.onContactUpdate(contact);
                }
            }
        } else {
            LogUtil.e("update contact fail");
        }
        return result;
    }

    public boolean addContact(ContactBean contact){
        return addContact(contact,true);
    }

    public boolean addContact(ContactBean contact,boolean isCallListener) {
        boolean result = mContactDao.insert(contact);
        if (result) {
            mContactsMap.put(contact.getUserID(), contact);
            if(isCallListener) {
                for (OnContactChangeListener listener : mContactChangeListeners) {
                    listener.onContactAdded(contact);
                }
            }
        } else {
            LogUtil.e("update contact fail");
        }
        return result;
    }

    public boolean delectContact(ContactBean contact){
        return delectContact(contact,true);
    }

    public boolean delectContact(ContactBean contact,boolean isCallListener) {
        boolean result = mContactDao.delete(contact);
        if (result) {
            mContactsMap.remove(contact.getUserID());
            if(isCallListener) {
                for (OnContactChangeListener listener : mContactChangeListeners) {
                    listener.onContactDeleted(contact);
                }
            }
        } else {
            LogUtil.e("update contact fail");
        }
        return result;
    }

    public List<ContactBean> getAllContacts() {
        List<ContactBean> contacts = new ArrayList<>(mContactsMap.size() + 16);
        contacts.addAll(mContactsMap.values());
        return contacts;
    }

    public void updateContactUnreadCount() {
        mSubManagerCallback.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (mUpdateContactUnreadNumberLock) {
                    int count = mContactMessageDao.loadRemindCount(mUserID);
                    if (count != mContactMessageUnreadNumber) {
                        mContactMessageUnreadNumber = count;
                        for (OnContactMessageUnreadCountChangeListener listener : mContactMessageUnreadCountChangeListeners) {
                            listener.onContactMessageUnreadCountChange(mContactMessageUnreadNumber);
                        }
                    }
                }
            }
        });
    }

    public void makeAllContactMessageAsRead() {
        mSubManagerCallback.execute(new Runnable() {
            @Override
            public void run() {
                mContactMessageDao.makeAllRemindAsRemind(mUserID);
                updateContactUnreadCount();
            }
        });
    }

    public void removeContactMessageAsync(final ContactMessageBean contactMessage) {
        mSubManagerCallback.execute(new Runnable() {
            @Override
            public void run() {
                if (mContactMessageDao.delete(contactMessage)) {
                    for (OnContactMessageListener contactListener : mContactMessageListeners) {
                        contactListener.onContactMessageDelete(contactMessage);
                    }
                } else {
                    LogUtil.e("delete ContactMessageFail from DB");
                }
            }
        });
    }

    public void removeContactMessage(final ContactMessageBean contactMessage) {
        if (mContactMessageDao.delete(contactMessage)) {
            for (OnContactMessageListener contactListener : mContactMessageListeners) {
                contactListener.onContactMessageDelete(contactMessage);
            }
        } else {
            LogUtil.e("delete ContactMessageFail from DB");
        }
    }


    public List<ContactMessageBean> loadAllContactMessage(){
        return mContactMessageDao.loadAllContactMessage(mUserID);
    }

    public List<ContactMessageBean> loadMoreContactMessage(int startID, int count) {
        return mContactMessageDao.loadMoreContactMessage(mUserID, startID, count);
    }

    public void addContactMessageListener(OnContactMessageListener listener) {
        if (!mContactMessageListeners.contains(listener)) {
            mContactMessageListeners.add(listener);
        }
    }

    public void removeContactMessageListener(OnContactMessageListener listener) {
        mContactMessageListeners.remove(listener);
    }

    public void addContactMessageUnreadCountChangeListener(OnContactMessageUnreadCountChangeListener listener) {
        if (!mContactMessageUnreadCountChangeListeners.contains(listener)) {
            mContactMessageUnreadCountChangeListeners.add(listener);
        }
    }

    public void removeContactMessageUnreadCountChangeListener(OnContactMessageUnreadCountChangeListener listener) {
        mContactMessageUnreadCountChangeListeners.remove(listener);
    }

    public void addContactChangeListener(OnContactChangeListener listener) {
        if (!mContactChangeListeners.contains(listener)) {
            mContactChangeListeners.add(listener);
        }
    }

    public void removeContactChangeListener(OnContactChangeListener listener) {
        mContactChangeListeners.remove(listener);
    }

    void onReceiveContactNotificationMessage(Message message) {
        ContactNotificationMessage contactMessage = (ContactNotificationMessage) message.getContent();
        ContactMessageBean bean = new ContactMessageBean();
        bean.setUserTo(contactMessage.getTargetUserId());
        bean.setUserFrom(contactMessage.getSourceUserId());
        bean.setReason(contactMessage.getMessage());
        bean.setRemind(true);
        bean.setTime((int) (message.getReceivedTime() / 1000));
        switch (contactMessage.getOperation()) {
//            case ContactNotificationMessage.CONTACT_OPERATION_REQUEST:
//                 bean.setType(ContactMessageBean.TYPE_REQUESTING);
//                break;
//            case ContactNotificationMessage.CONTACT_OPERATION_ACCEPT_RESPONSE:
//                   bean.setType(ContactMessageBean.CONTACT_TYPE_ACCEPTED);
//                break;
//            case ContactNotificationMessage.CONTACT_OPERATION_REJECT_RESPONSE:
//                //   bean.setType(ContactMessageBean.CONTACT_TYPE_DECLINED);
//                break;
        }
        mContactMessageDao.delete(bean);
        mContactMessageDao.insert(bean);
        for (OnContactMessageListener contactListener : mContactMessageListeners) {
            contactListener.onContactMessageReceive(bean);
        }
        updateContactUnreadCount();
    }


    public interface OnContactMessageListener {
        void onContactMessageReceive(ContactMessageBean message);

        void onContactMessageDelete(ContactMessageBean message);
    }

    public interface OnContactMessageUnreadCountChangeListener {
        void onContactMessageUnreadCountChange(int count);
    }

    public interface OnContactChangeListener {
        void onContactAdded(ContactBean contact);
        void onContactDeleted(ContactBean contact);
        void onContactUpdate(ContactBean contact);
    }

}
