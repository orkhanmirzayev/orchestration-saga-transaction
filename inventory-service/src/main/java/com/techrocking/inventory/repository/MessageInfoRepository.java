package com.techrocking.inventory.repository;

import com.techrocking.inventory.entity.MessageInfo;
import org.springframework.data.repository.CrudRepository;

public interface MessageInfoRepository extends CrudRepository<MessageInfo,Long> {

    MessageInfo findByKey(String key);
}
