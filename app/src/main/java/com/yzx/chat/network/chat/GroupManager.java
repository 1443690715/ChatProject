package com.yzx.chat.network.chat;

import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.bean.GroupMemberBean;
import com.yzx.chat.database.AbstractDao;
import com.yzx.chat.database.GroupDao;
import com.yzx.chat.database.GroupMemberDao;
import com.yzx.chat.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YZX on 2018年03月09日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class GroupManager {

    private IMClient.SubManagerCallback mSubManagerCallback;
    private Map<String, GroupBean> mGroupsMap;
    private GroupDao mGroupDao;

    GroupManager(IMClient.SubManagerCallback subManagerCallback, AbstractDao.ReadWriteHelper readWriteHelper) {
        if (subManagerCallback == null) {
            throw new NullPointerException("subManagerCallback can't be NULL");
        }
        mSubManagerCallback = subManagerCallback;
        mGroupsMap = new HashMap<>(24);
        mGroupDao = new GroupDao(readWriteHelper);
        List<GroupBean> groups = mGroupDao.loadAllGroup();
        if (groups != null) {
            for (GroupBean group : groups) {
                mGroupsMap.put(group.getGroupID(), group);
            }
        }
    }

    public GroupBean getGroup(String groupID){
        return mGroupsMap.get(groupID);
    }

    public List<GroupBean> getAllGroup() {
        if (mGroupsMap == null) {
            return null;
        }
        List<GroupBean> group = new ArrayList<>(mGroupsMap.size() + 8);
        group.addAll(mGroupsMap.values());
        return group;
    }

    public GroupMemberBean getGroupMenber(String groupID,String memberID){
        GroupBean group = getGroup(groupID);
        if(group==null){
            return null;
        }

        for (GroupMemberBean groupMember:group.getMembers()){
            if(groupMember.getUserProfile().getUserID().equals(memberID)){
                return groupMember;
            }
        }
        return null;
    }

    void destroy() {
        mGroupsMap.clear();
        mGroupsMap = null;
    }

    static boolean update(ArrayList<GroupBean> groups, AbstractDao.ReadWriteHelper readWriteHelper) {
        GroupDao groupDao = new GroupDao(readWriteHelper);
        GroupMemberDao groupMemberDao = new GroupMemberDao(readWriteHelper);
        groupMemberDao.cleanTable();
        groupDao.cleanTable();
        if (groupDao.insertAll(groups)) {
            return true;
        } else {
            LogUtil.e("updateAllGroups fail");
            return false;
        }
    }

}
