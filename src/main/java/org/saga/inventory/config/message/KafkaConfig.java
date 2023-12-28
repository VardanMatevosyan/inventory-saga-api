package org.saga.inventory.config.message;

import static org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES;
import static org.springframework.kafka.support.serializer.JsonDeserializer.USE_TYPE_INFO_HEADERS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableKafka
public class KafkaConfig {

  @Value("${inventory-payment-saga-topic}")
  private String inventoryPaymentSagaTopic;

  @Value("${inventory-order-saga-topic}")
  private String inventoryOrderSagaTopic;

  @Value("${kafka-server-host}")
  private String serverHost;

  @Value("${kafka-inventory-consumer-group-id}")
  private String inventoryConsumerGroupId;

  @Bean
  @Qualifier("inventoryPaymentSagaTopic")
  public NewTopic inventoryPaymentSagaTopic() {
    return TopicBuilder.name(inventoryPaymentSagaTopic)
        .partitions(2)
        .replicas(1)
        .build();
  }

  @Bean
  @Qualifier("inventoryOrderSagaTopic")
  public NewTopic inventoryOrderSagaTopic() {
    return TopicBuilder.name(inventoryOrderSagaTopic)
        .partitions(2)
        .replicas(1)
        .build();
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  public ProducerFactory<String, Object> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfig());
  }

  @Bean
  public Map<String, Object> producerConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverHost);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return config;
  }

  @Bean
  public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>>
  kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConcurrency(3);
    factory.setConsumerFactory(consumerFactory());
    factory.getContainerProperties().setPollTimeout(3000);
    return factory;
  }

  @Bean
  public ConsumerFactory<? super String, ? super Object> consumerFactory() {
    return new DefaultKafkaConsumerFactory<>(consumerConfig());
  }

  @Bean
  public Map<String, Object> consumerConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverHost);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, inventoryConsumerGroupId);
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    config.put(TRUSTED_PACKAGES, "*");
    return config;
  }


  // todo remove this after reading
//  By default, the deserializer will use type information in headers to determine which type to create.
//
//  Caused by: java.lang.ClassNotFoundException: com.freeproxy.parser.model.kafka.KafkaMessage
//
//  Most likely, KafkaMessage is in a different package on the sending side.
//
//  There are a couple of solutions:
//
//  https://docs.spring.io/spring-kafka/docs/current/reference/html/#serdes-json-config
//
//  Set JsonDeserializer.USE_TYPE_INFO_HEADERS to false and JsonDeserializer.VALUE_DEFAULT_TYPE to com.new.package.kafka.KafkaMessage (the fully qualified name of KafkaMessage on the receiving side).
//
//  Use type mapping: https://docs.spring.io/spring-kafka/docs/current/reference/html/#serdes-mapping-types
//
//  I suggest you read this whole section https://docs.spring.io/spring-kafka/docs/current/reference/html/#json-serde


}
