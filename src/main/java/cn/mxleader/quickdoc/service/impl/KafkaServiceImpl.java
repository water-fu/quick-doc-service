package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class KafkaServiceImpl implements KafkaService {

    private KafkaTemplate<String, String> kafkaTemplate;

    private final String defaultTopic;

    @Autowired
    public KafkaServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this(kafkaTemplate, null);
    }

    public KafkaServiceImpl(KafkaTemplate<String, String> kafkaTemplate, String defaultTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.defaultTopic = (defaultTopic != null ? defaultTopic : "quick-doc-stream");
    }

    @Override
    @Async
    public void sendMessage(String message) {
        kafkaTemplate.send(defaultTopic, message);
    }

    @Override
    @Async
    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}