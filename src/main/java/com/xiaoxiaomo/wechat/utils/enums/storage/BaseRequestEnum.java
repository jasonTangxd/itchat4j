package com.xiaoxiaomo.wechat.utils.enums.storage;

/**
 *
 *
 * Created by xiaoxiaomo on 2017/5/7.
 */
public enum BaseRequestEnum {

    Skey("Skey","skey", new String()),
    Sid("Sid","wxsid",new String()),
    Uin("Uin","wxuin",new String()),
    DeviceID("DeviceID","pass_ticket",new String());

    private String key;
    private String storageKey;
    private Object obj;

    BaseRequestEnum(String key, String storageKey , Object obj) {
        this.key = key;
        this.storageKey = storageKey;
        this.obj = obj;
    }

    public String getKey() {
        return key;
    }


    public Object getStorageKey() {
        return storageKey;
    }

}
