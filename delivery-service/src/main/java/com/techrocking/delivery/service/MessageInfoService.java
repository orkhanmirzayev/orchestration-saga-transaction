package com.techrocking.delivery.service;

import com.techrocking.delivery.model.MessageInfo;
import com.techrocking.delivery.repository.MessageInfoRepository;
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
