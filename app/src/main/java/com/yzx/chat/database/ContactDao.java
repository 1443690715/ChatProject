package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactRemarkBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by YZX on 2017年11月24日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ContactDao extends AbstractDao<ContactBean> {

    private static final String TABLE_NAME = "Contact";

    private static final String COLUMN_NAME_UserID = "UserID";
    private static final String COLUMN_NAME_RemarkName = "RemarkName";
    private static final String COLUMN_NAME_Description = "Description";
    private static final String COLUMN_NAME_Telephone = "Telephone";
    private static final String COLUMN_NAME_Tags = "Tags";
    private static final String COLUMN_NAME_UploadFlag = "UploadFlag";

    private static final int COLUMN_INDEX_UserID = 0;
    private static final int COLUMN_INDEX_RemarkName = 1;
    private static final int COLUMN_INDEX_Description = 2;
    private static final int COLUMN_INDEX_Telephone = 3;
    private static final int COLUMN_INDEX_Tags = 4;
    private static final int COLUMN_INDEX_UploadFlag = 5;


    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_UserID + " TEXT NOT NULL , "
                    + COLUMN_NAME_RemarkName + " TEXT,"
                    + COLUMN_NAME_Description + " TEXT,"
                    + COLUMN_NAME_Telephone + " TEXT,"
                    + COLUMN_NAME_Tags + " TEXT,"
                    + COLUMN_NAME_UploadFlag + " INTEGER,"
                    + "PRIMARY KEY (" + COLUMN_NAME_UserID + ")"
                    + ")";


    public List<ContactBean> loadAllContacts() {
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " LEFT OUTER JOIN " + UserDao.TABLE_NAME + " ON " + TABLE_NAME + "." + COLUMN_NAME_UserID + "=" + UserDao.TABLE_NAME + "." + UserDao.COLUMN_NAME_UserID, null);
        List<ContactBean> contactList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            contactList.add(toEntity(cursor));
        }
        cursor.close();
        mHelper.closeReadableDatabase();
        return contactList;
    }


    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getWhereClauseOfKey() {
        return COLUMN_NAME_UserID + "=?";
    }

    @Override
    protected String[] toWhereArgsOfKey(ContactBean entity) {
        return new String[]{entity.getUserProfile().getUserID()};
    }

    @Override
    protected ContentValues toContentValues(ContactBean entity, ContentValues values) {
        ContactRemarkBean remark = entity.getRemark();
        values.put(COLUMN_NAME_UserID, entity.getUserProfile().getUserID());
        values.put(COLUMN_NAME_RemarkName, remark.getRemarkName());
        values.put(COLUMN_NAME_Description, remark.getDescription());
        values.put(COLUMN_NAME_UploadFlag, remark.getUploadFlag());

        List<String> telephone = remark.getTelephone();
        if (telephone != null && telephone.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0, size = telephone.size(); i < size; i++) {
                stringBuilder.append(telephone.get(i));
                if (i != size - 1) {
                    stringBuilder.append(";");
                }
            }
            values.put(COLUMN_NAME_Telephone, stringBuilder.toString());
        }

        List<String> tags = remark.getTags();
        if (tags != null && tags.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0, size = tags.size(); i < size; i++) {
                stringBuilder.append(tags.get(i));
                if (i != size - 1) {
                    stringBuilder.append(";");
                }
            }
            values.put(COLUMN_NAME_Tags, stringBuilder.toString());
        }
        return values;
    }

    @Override
    protected ContactBean toEntity(Cursor cursor) {
        ContactBean contact = new ContactBean();
        ContactRemarkBean remark = new ContactRemarkBean();
        remark.setRemarkName(cursor.getString(COLUMN_INDEX_RemarkName));
        remark.setDescription(cursor.getString(COLUMN_INDEX_Description));
        remark.setUploadFlag(cursor.getInt(COLUMN_INDEX_UploadFlag));

        String telephone = cursor.getString(COLUMN_INDEX_Telephone);
        if (!TextUtils.isEmpty(telephone)) {
            String[] telephones = telephone.split(";");
            remark.setTelephone(new ArrayList<>(Arrays.asList(telephones)));
        }

        String tag = cursor.getString(COLUMN_INDEX_Tags);
        if (!TextUtils.isEmpty(tag)) {
            String[] tags = tag.split(";");
            remark.setTags(new ArrayList<>(Arrays.asList(tags)));
        }
        contact.setRemark(remark);
        contact.setUserProfile(UserDao.toEntityFromCursor(cursor));
        return contact;
    }
}
