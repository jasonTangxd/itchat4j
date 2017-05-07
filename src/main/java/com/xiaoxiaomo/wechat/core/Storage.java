package com.xiaoxiaomo.wechat.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

/**
 * 核心存储类，全局只保存一份，单例模式
 * 
 * @author https://github.com/yaphone
 * @date 创建时间：2017年4月23日 下午2:33:56
 * @version 1.0
 *
 */
public class Storage {

	private static Storage instance;
	private Storage() {}
	public static Storage getInstance() {
		if (instance == null) {
			synchronized (Storage.class) {
				instance = new Storage();
			}
		}
		return instance;
	}

	boolean alive = false;
	private int memberCount = 0;
	String uuid = null;
	private String userName;
	private String nickName;
	private String lastInputUserName;

	boolean useHotReload = false;
	String hotReloadDir = "itchat.pkl";
	int receivingRetryCount = 5;

	private List<JSONObject> msgList = new ArrayList<JSONObject>();
	private List<JSONObject> userSelfList = new ArrayList<JSONObject>(); // 登陆账号自身信息
	private List<JSONObject> memberList = new ArrayList<JSONObject>(); // 好友+群聊+公众号+特殊账号
	private List<JSONObject> contactList = new ArrayList<JSONObject>();// 好友
	private List<String> groupIdList = new ArrayList<String>(); // 群聊，以String格式保存群的userName，如@@37da24fee2114e9475729b942d130190ffddb669411228651da3e8a8933603c8
	private List<JSONObject> groupList = new ArrayList<JSONObject>(); // 群
	private List<JSONObject> groupMemberList = new ArrayList<JSONObject>(); // 群聊成员字典
	private List<JSONObject> publicUsersList = new ArrayList<JSONObject>();// 公众号／服务号

	private List<JSONObject> specialUsersList = new ArrayList<JSONObject>();// 特殊账号
	public Map<String, Object> loginInfo = new HashMap<String, Object>();

	Map<String, Object> functionDict = new HashMap<String, Object>() {
		{
			put("FriendChat", new HashMap<Object, Object>());
			put("GroupChat", new HashMap<Object, Object>());
			put("MpChat", new HashMap<Object, Object>());
		}
	};






	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public List<JSONObject> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<JSONObject> memberList) {
		this.memberList = memberList;
	}

	public Map<String, Object> getLoginInfo() {
		return loginInfo;
	}

	public void setLoginInfo(Map<String, Object> loginInfo) {
		this.loginInfo = loginInfo;
	}

	public List<String> getGroupIdList() {
		return groupIdList;
	}

	public void setGroupIdList(List<String> groupIdList) {
		this.groupIdList = groupIdList;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Map<String, Object> getFunctionDict() {
		return functionDict;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public void setFunctionDict(Map<String, Object> functionDict) {
		this.functionDict = functionDict;
	}

	public boolean isUseHotReload() {
		return useHotReload;
	}

	public void setUseHotReload(boolean useHotReload) {
		this.useHotReload = useHotReload;
	}

	public String getHotReloadDir() {
		return hotReloadDir;
	}

	public void setHotReloadDir(String hotReloadDir) {
		this.hotReloadDir = hotReloadDir;
	}

	public int getReceivingRetryCount() {
		return receivingRetryCount;
	}

	public void setReceivingRetryCount(int receivingRetryCount) {
		this.receivingRetryCount = receivingRetryCount;
	}

	public List<JSONObject> getMsgList() {
		return msgList;
	}

	public void setMsgList(List<JSONObject> msgList) {
		this.msgList = msgList;
	}

	public List<JSONObject> getUserSelfList() {
		return userSelfList;
	}

	public void setUserSelfList(List<JSONObject> userSelfList) {
		this.userSelfList = userSelfList;
	}

	public List<JSONObject> getContactList() {
		return contactList;
	}

	public void setContactList(List<JSONObject> contactList) {
		this.contactList = contactList;
	}

	public List<JSONObject> getGroupList() {
		return groupList;
	}

	public void setGroupList(List<JSONObject> groupList) {
		this.groupList = groupList;
	}

	public List<JSONObject> getPublicUsersList() {
		return publicUsersList;
	}

	public void setPublicUsersList(List<JSONObject> publicUsersList) {
		this.publicUsersList = publicUsersList;
	}

	public List<JSONObject> getSpecialUsersList() {
		return specialUsersList;
	}

	public void setSpecialUsersList(List<JSONObject> specialUsersList) {
		this.specialUsersList = specialUsersList;
	}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getLastInputUserName() {
        return lastInputUserName;
    }

    public void setLastInputUserName(String lastInputUserName) {
        this.lastInputUserName = lastInputUserName;
    }

    public List<JSONObject> getGroupMemberList() {
        return groupMemberList;
    }

    public void setGroupMemberList(List<JSONObject> groupMemberList) {
        this.groupMemberList = groupMemberList;
    }

}
