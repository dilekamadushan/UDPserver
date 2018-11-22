package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

public class KafkaProducer extends Thread {
    
    private boolean running;
    
    private org.apache.kafka.clients.producer.KafkaProducer<String, String> producer;
    
    private String groupId = "my-group";
    
    private String topic = "topology";
    
    private TestCallback testCallback;
    
    private CopyOnWriteArrayList<Node> routingTable;
    
    private Node myNode;
    
    public KafkaProducer(Node myNode, CopyOnWriteArrayList<Node> routingTable, boolean running) {
        //run the program with args: producer/consumer broker:port
        String brokers = "139.59.77.98:9092";
        
        Properties props = new Properties();
        
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        
        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
        testCallback = new TestCallback();
        this.routingTable = routingTable;
        this.myNode = myNode;
        this.running = running;
    }
    
    public void run() {
        while (running) {
            try {
                System.out.println("Kafka Producer:Kafka producer sleeps  for 120 seconds ");
                Thread.sleep(1000 * 15);
                System.out.println("Kafka Producer:Kafka producer wakes ");
                produce();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        closeProducer();
        
    }
    
    private void produce() {
    
        for (Node node : routingTable) {
            if (node.isStatus()) {
                String edge = "" + myNode.getIdForDisplay() + node.getIdForDisplay();
            
                // Send the sentence to the test topic
                ProducerRecord<String, String> data = new ProducerRecord<>(topic, edge);
                long startTime = System.currentTimeMillis();
                producer.send(data, testCallback);
                long elapsedTime = System.currentTimeMillis() - startTime;
                System.out.println("Sent this edge: " + edge + " in " + elapsedTime + " ms");
            }
        }
        System.out.println("Done");
        producer.flush();
    }
    
    private void closeProducer() {
        
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
