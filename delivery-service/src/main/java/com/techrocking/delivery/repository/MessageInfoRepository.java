package com.techrocking.delivery.repository;

import com.techrocking.delivery.model.MessageInfo;
import org.springframework.data.repository.CrudRepository;

public interface MessageInfoRepository extends CrudRepository<MessageInfo,Long> {

    MessageInfo findByKey(String key);
}
