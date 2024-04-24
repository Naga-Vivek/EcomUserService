package com.scaler.EcomUserService.config;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerConfig {
    private KafkaTemplate<String,String> kafkaTemplate;

    public KafkaProducerConfig(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    // sendMessage will push the message to the given event in Kafka
    public void sendMessage(String topic , String message){
        kafkaTemplate.send(topic,message);
    }
    /*
    Why message is being sent as String to Kafka

    -> message is sent in a serialized way
    -> We will have to serialize the message before sending to Kafka
    -> serialize the message using ObjectMapper
     */
}
