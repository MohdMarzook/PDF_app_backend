package com.marzook.pdfbackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.marzook.pdfbackend.model.Language;

import java.util.*;

@Service
public class MessageService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public MessageService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public boolean sendMessage(String pdf_key, String from_language, String to_language) {
        try{
            String message = message(from_language,to_language, pdf_key);
            String MESSAGE_QUEUE = "celery";
//            redisTemplate.convertAndSend(MESSAGE_QUEUE, );
            redisTemplate.opsForList().leftPush(MESSAGE_QUEUE, message);
            System.out.println("Sent message: " + message);
            return true;
        } catch (Exception e){
            System.out.println("Cant add link to the message Queue \n" + e.getMessage());
            return false;
        }
    }

    public String message(String fromLang, String toLang, String pdfKey) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Generate task ID
        String taskId = UUID.randomUUID().toString();

        // Create task body
        Map<String, Object> taskBody = new HashMap<>();
        taskBody.put("id", taskId);
        taskBody.put("task", "python_task");
        taskBody.put("args", Arrays.asList(fromLang, toLang, pdfKey));
        taskBody.put("kwargs", new HashMap<>());
        taskBody.put("retries", 0);
        taskBody.put("eta", null);
        taskBody.put("expires", null);

        // Create delivery info
        Map<String, String> deliveryInfo = new HashMap<>();
        deliveryInfo.put("exchange", "celery");
        deliveryInfo.put("routing_key", "celery");

        // Create properties
        Map<String, Object> properties = new HashMap<>();
        properties.put("correlation_id", taskId);
        properties.put("reply_to", "");
        properties.put("delivery_tag", String.valueOf(System.currentTimeMillis() / 1000));
        properties.put("delivery_info", deliveryInfo);
        properties.put("delivery_mode", 2);
        properties.put("priority", 0);

        // Create the full message envelope
        Map<String, Object> message = new HashMap<>();
        message.put("body", mapper.writeValueAsString(taskBody));
        message.put("content-type", "application/json");
        message.put("content-encoding", "utf-8");
        message.put("properties", properties);

        // Return the JSON string representation of the message
        return mapper.writeValueAsString(message);
    }

    public void addlanglist(List<Language> languages){
        try {
            String jsonString = objectMapper.writeValueAsString(languages);
            redisTemplate.opsForValue().set("langlist", jsonString);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Language> getlandlist(){
        String jsonString = redisTemplate.opsForValue().get("langlist");
        if( jsonString == null ||  jsonString.isEmpty() ){
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<List<Language>>() {});
        } catch (Exception e){
            System.out.println(e.getMessage());
            return new ArrayList<>();
        }
    }
}
