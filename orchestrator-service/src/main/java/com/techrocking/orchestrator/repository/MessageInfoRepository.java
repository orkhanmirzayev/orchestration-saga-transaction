package com.techrocking.orchestrator.repository;

import com.techrocking.orchestrator.entity.MessageInfo;
import org.springframework.data.repository.CrudRepository;

public interface MessageInfoRepository extends CrudRepository<MessageInfo, Long> {

    MessageInfo findByKey(String key);
}
