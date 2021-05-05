package com.techrocking.order.repository;

import com.techrocking.order.entity.MessageInfo;
import org.springframework.data.repository.CrudRepository;

public interface MessageInfoRepository extends CrudRepository<MessageInfo,Long> {

    MessageInfo findByKey(String key);
}
