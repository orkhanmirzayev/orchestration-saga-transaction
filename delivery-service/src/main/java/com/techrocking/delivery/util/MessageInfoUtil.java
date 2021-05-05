package com.techrocking.delivery.util;

import com.techrocking.delivery.model.MessageInfo;
import com.techrocking.delivery.service.MessageInfoService;
import org.springframework.stereotype.Component;

@Component
public class MessageInfoUtil {

    private final MessageInfoService messageInfoService;

    public MessageInfoUtil(MessageInfoService messageInfoService) {
        this.messageInfoService = messageInfoService;
    }

    public void createInfo(String label,String key){
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setKey(key);
        messageInfo.setLabel(label);
        messageInfoService.createMessageInfo(messageInfo);
    }


}
