package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Random;

public class KafkaProducer extends Thread {
    
    private org.apache.kafka.clients.producer.KafkaProducer<String, String> producer;
    
    private String groupId = "my-group";
    
    private String topic = "singleThreadedS";
    
    private TestCallback testCallback;
    
    public KafkaProducer(String brokerIP, Node myNode, ArrayList<Node> routingTable) {
        //run the program with args: producer/consumer broker:port
        String brokers = brokerIP;
        
        Properties props = new Properties();
        
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        
        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
        testCallback = new TestCallback();
    }
    
    public void run(){
        
    }
    public void produce() {
        Random rnd = new Random();
        // So we can generate random sentences
        Random random = new Random();
        String[] sentences = new String[] { "12", "23", "34", "45", "56", "67", "78", "89", "910" };
        for (int i = 0; i < 9; i++) {
            // Pick a sentence at random
            String sentence = sentences[i];
            // Send the sentence to the test topic
            ProducerRecord<String, String> data = new ProducerRecord<String, String>(topic, sentence);
            long startTime = System.currentTimeMillis();
            producer.send(data, testCallback);
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Sent this sentence: " + sentence + " in " + elapsedTime + " ms");
            
        }
        System.out.println("Done");
        producer.flush();
    }
    
    public void closeProducer() {
        
        producer.close();
        
    }
    
    private static class TestConsumerRebalanceListener implements ConsumerRebalanceListener {
        
        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            System.out.println("Called onPartitionsRevoked with partitions:" + partitions);
        }
        
        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            System.out.println("Called onPartitionsAssigned with partitions:" + partitions);
        }
    }
    
    private static class TestCallback implements Callback {
        
        @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
            if (e != null) {
                System.out.println("Error while producing message to topic :" + recordMetadata);
                e.printStackTrace();
            } else {
                String message = String.format("sent message to topic:%s partition:%s  offset:%s", recordMetadata.topic(),
                        recordMetadata.partition(), recordMetadata.offset());
                System.out.println(message);
            }
        }
    }
    
}
