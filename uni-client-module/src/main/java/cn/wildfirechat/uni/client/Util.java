package cn.wildfirechat.uni.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.UserOnlineState;
import cn.wildfirechat.uni.client.jsmodel.JSConversationInfo;
import cn.wildfirechat.uni.client.jsmodel.JSMessage;

public class Util {
    public static JSONArray strLongMap2Array(Map<String, Long> map) {
        JSONArray array = new JSONArray();
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            JSONObject object = new JSONObject();
            object.put("key", entry.getKey());
            object.put("value", entry.getValue());
            array.add(object);
        }

        return array;
    }

    public static JSONArray strStrMap2Array(Map<String, String> map) {
        JSONArray array = new JSONArray();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            JSONObject object = new JSONObject();
            object.put("key", entry.getKey());
            object.put("value", entry.getValue());
            array.add(object);
        }

        return array;
    }

    public static JSONArray convertUserOnlineMap(Map<String, UserOnlineState> map) {

        JSONArray array = new JSONArray();
        for (Map.Entry<String, UserOnlineState> entry : map.entrySet()) {
            array.add(entry.getValue());
        }
        return array;
    }

    public static List<JSMessage> messagesToJSMessages(List<Message> messages) {
        List<JSMessage> jsMessages = new ArrayList<>();
        for (Message msg : messages) {
            jsMessages.add(JSMessage.fromMessage(msg));
        }
        return jsMessages;
    }

    public static List<JSConversationInfo> conversationInfosToJSConversationInfos(List<ConversationInfo> infos){
        List<JSConversationInfo> jsConversationInfos = new ArrayList<>();
        for (ConversationInfo info : infos) {
            jsConversationInfos.add(JSConversationInfo.fromConversationInfo(info));
        }
        return jsConversationInfos;

    }
}
