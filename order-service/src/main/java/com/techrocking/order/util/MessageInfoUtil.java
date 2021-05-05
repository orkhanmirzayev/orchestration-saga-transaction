package com.techrocking.order.util;

import com.techrocking.order.entity.MessageInfo;
import com.techrocking.order.service.MessageInfoService;
import org.springframework.stereotype.Component;

@Component
public class MessageInfoUtil {

    private final MessageInfoService messageInfoService;

    public MessageInfoUtil(MessageInfoService messageInfoService) {
        this.messageInfoService = messageInfoService;
    }

    public void createInfo(String label,String key){
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setKey(label);
        messageInfo.setLabel(key);
        messageInfoService.createMessageInfo(messageInfo);
    }


}
