package com.yzx.chat.view.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;

import com.yzx.chat.base.BaseHttpCallback;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactMessageBean;
import com.yzx.chat.database.ContactDao;
import com.yzx.chat.database.ContactMessageDao;
import com.yzx.chat.network.api.JsonResponse;
import com.yzx.chat.network.api.user.UserApi;
import com.yzx.chat.network.framework.Call;
import com.yzx.chat.network.framework.NetworkExecutor;
import com.yzx.chat.tool.ApiManager;
import com.yzx.chat.tool.DBManager;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.util.RSAUtil;
import com.yzx.chat.widget.view.Alerter;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;


public class TestActivity extends BaseCompatActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init() {

    }

    @Override
    protected void setup() {

        ContactMessageDao dao = DBManager.getInstance().getContactMessageDao();
        ContactMessageBean a1 = new ContactMessageBean();
        a1.setReason("爱你");
        a1.setRemind(true);
        a1.setTime(1);
        a1.setType(ContactMessageBean.TYPE_ADDED);
        a1.setUserFrom("2");
        a1.setUserTo("1");
        a1.setNickname("Touch美");

        dao.insert(a1);
        a1.setUserFrom("10");
        a1.setNickname("戴一个表");
        a1.setType(ContactMessageBean.TYPE_REQUESTING);
        dao.insert(a1);
        a1.setUserFrom("3");
        a1.setNickname("Unlimited");
        a1.setType(ContactMessageBean.TYPE_DISAGREE);
        dao.insert(a1);
        a1.setUserFrom("4");
        dao.insert(a1);
        a1.setUserFrom("5");
        a1.setNickname("PlaydonJJ");
        a1.setType(ContactMessageBean.TYPE_REFUSED);
        dao.insert(a1);
        a1.setUserFrom("6");
        a1.setNickname("叶智星");
        a1.setType(ContactMessageBean.TYPE_VERIFYING);
        dao.insert(a1);

        for(int i =11;i<80;i++){
            a1.setUserFrom(String.valueOf(i));
            dao.insert(a1);
            a1.setNickname(String.valueOf(i));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        UserApi api = (UserApi) ApiManager.getProxyInstance(UserApi.class);
//        Call<JsonResponse<Void>> task = api.addFriend("5a3789ff2889040e1822fe88");
//        task.setCallback(new BaseHttpCallback<Void>() {
//            @Override
//            protected void onSuccess(Void response) {
//                LogUtil.e("onSuccess");
//
//            }
//
//            @Override
//            protected void onFailure(String message) {
//                LogUtil.e("onFailure");
//            }
//        });
//        NetworkExecutor.getInstance().submit(task);


//        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
//            @Override
//            public boolean queueIdle() {
//                new Alerter(TestActivity.this,R.layout.item_conversation_single).show();
//                return false;
//            }
//        });
    }

    Alerter mAlerter;

    public void onClick(View v) {
        v.setTranslationZ(13);
    }

    public void onClick2(View v) {
        mAlerter.hide();
    }

    private void testRSA() {
        byte[] data = "ddddddddddd||".getBytes();
        KeyPair pair = RSAUtil.generateRSAKeyPairInAndroidKeyStore(this, "nSDanmadkiD");
        PublicKey publicKey = pair.getPublic();
        PrivateKey privateKey = pair.getPrivate();
        try {
            Log.e("dawd", new String(data));
            data = RSAUtil.encryptByPublicKey(data, publicKey);
            data = RSAUtil.decryptByPrivateKey(data, privateKey);
            Log.e("dawd", new String(data));
//            data = RSAUtil.encryptByPrivateKey(data, privateKey);
//            data = RSAUtil.decryptByPublicKey(data, publicKey);
//            Log.e("dawd", new String(data));
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void testHTTP() {

    }


}


