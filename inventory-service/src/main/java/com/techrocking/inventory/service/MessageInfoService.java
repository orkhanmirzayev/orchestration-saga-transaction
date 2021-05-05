package com.techrocking.inventory.service;

import com.techrocking.inventory.entity.MessageInfo;
import com.techrocking.inventory.repository.MessageInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageInfoService {

    private final MessageInfoRepository messageInfoRepository;

    public MessageInfoService(MessageInfoRepository messageInfoRepository) {
        this.messageInfoRepository = messageInfoRepository;
    }

    @Transactional
    public void createMessageInfo(MessageInfo messageInfo) {
        messageInfoRepository.save(messageInfo);
    }

    @Transactional(readOnly = true)
    public boolean isMessageFound(String key) {
        return messageInfoRepository.findByKey(key) !=null;
    }
}
