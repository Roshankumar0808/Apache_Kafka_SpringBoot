package com.demo.kafka.user_service.Service;

import com.demo.kafka.events.UserCreatedEvent;
import com.demo.kafka.user_service.Enitity.UserEntity;
import com.demo.kafka.user_service.Repository.UserRepo;
import com.demo.kafka.user_service.dto.CreateUserRequestDto;
//import com.demo.kafka.user_service.events.UserCreatedEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    @Value("${kafka.topic.user-created-topic}")
    private String KAFKA_USER_CREATED_TOPIC;

    private final UserRepo userRepo;
    private final KafkaTemplate<Long, UserCreatedEvent>kafkaTemplate;

    private final ModelMapper modelMapper;

    public void createuser(CreateUserRequestDto createUserRequestDto) {
      UserEntity user=modelMapper.map(createUserRequestDto,UserEntity.class);
      UserEntity saveduser= userRepo.save(user);
      UserCreatedEvent userCreatedEvents=modelMapper.map(saveduser,UserCreatedEvent.class);
      kafkaTemplate.send(KAFKA_USER_CREATED_TOPIC,userCreatedEvents.getId(),userCreatedEvents);
      log.info("User Created");

    }


//    public void getmessage(String message) {
//       userEntity.setMessage(message);
//       System.out.println(message);
//
//
//    }
}
