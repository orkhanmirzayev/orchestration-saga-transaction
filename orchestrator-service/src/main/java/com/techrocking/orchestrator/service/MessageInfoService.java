package com.techrocking.orchestrator.service;

import com.techrocking.orchestrator.entity.MessageInfo;
import com.techrocking.orchestrator.repository.MessageInfoRepository;
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
