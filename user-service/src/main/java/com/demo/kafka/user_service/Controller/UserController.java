package com.demo.kafka.user_service.Controller;

import com.demo.kafka.user_service.Service.UserService;
import com.demo.kafka.user_service.dto.CreateUserRequestDto;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
//import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    @Value("${kafka.topic.user-random-topic}")
    private String KAFKA_RANDOM_USER_TOPIC;

    private final UserService userService;
    private final KafkaTemplate<String,String> kafkaTemplate;

    @PostMapping("/{message}")
    public ResponseEntity<String> getmessage(@PathVariable String message){
       for(int i=0;i<1000;i++){
           kafkaTemplate.send(KAFKA_RANDOM_USER_TOPIC,""+i%2,message+i);

       }
      // userService.getmessage(message);
       return ResponseEntity.ok("Message send");
    }

    @PostMapping("/createuser")
    public ResponseEntity<String>  createuser(@RequestBody CreateUserRequestDto createUserRequestDto){
        userService.createuser(createUserRequestDto);
     return  ResponseEntity.ok("User created Succesfully");
    }

}
