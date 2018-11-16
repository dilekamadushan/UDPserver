package kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;

public class ProducerConsumer {
    
    public static void main(String[] args) {
        //run the program with args: producer/consumer broker:port
        String groupId = "my-group", brokers = "", topic = "singleThreadedS", type = "";
        if (args.length == 2) {
            type = args[0];
            brokers = args[1];
        } else {
            type = "producer";
            brokers = "broker1:9093,broker2:9093,broker3:9093";
        }
        
        Properties props = new Properties();
        
        if (type.equals("consumer")) {
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.StringDeserializer");
            KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(props);
            TestConsumerRebalanceListener rebalanceListener = new TestConsumerRebalanceListener();
            consumer.subscribe(Collections.singletonList(topic), rebalanceListener);
            final int giveUp = 100;
            int noRecordsCount = 0;
            ArrayList<String> edges = new ArrayList<>();
            while (true) {
                final ConsumerRecords<byte[], byte[]> consumerRecords = consumer.poll(250);
                
                if (consumerRecords.count() == 0) {
                    noRecordsCount++;
                    if (noRecordsCount > giveUp)
                        break;
                    else
                        continue;
                }
                
                consumerRecords.forEach(record -> {
                    System.out.printf("Consumer Record:(%d, %s, %d, %d)\n", record.key(), record.value(), record.partition(),
                            record.offset());
                    edges.add(String.valueOf(record.value()));
                    System.out.println(String.valueOf("aaaaaaaaaaaaaaaa"+record.value())+edges.size());
                });
                
                consumer.commitAsync();
            }
            consumer.close();
    
            System.out.println("11111111111111111111111111");
            try {
                Writer fileWriter = new FileWriter("/home/dileka/Desktop/vis-4.21.0/examples/network/kafkaTest.html");
                String st1 =
                        "<!doctype html>\n" + "<html>\n" + "<head>\n" + "  <title>Network | Basic usage</title>\n" + "\n"
                                + "  <script type=\"text/javascript\" src=\"../../dist/vis.js\"></script>\n"
                                + "  <link href=\"../../dist/vis-network.min.css\" rel=\"stylesheet\" type=\"text/css\" />\n"
                                + "\n" + "  <style type=\"text/css\">\n" + "    #mynetwork {\n" + "      width: 1200px;\n"
                                + "      height: 500px;\n" + "      border: 1px solid lightgray;\n" + "    }\n"
                                + "  </style>\n" + "</head>\n" + "<body>\n" + "\n" + "\n"
                                + "  <h>Current Network Topology.<h>\n" + "\n" + "\n"
                                + "<div id=\"mynetwork\"></div>\n" + "\n" + "<script type=\"text/javascript\">\n"
                                + "  // create an array with nodes";
        
                String st2 = "// create an array with nodes\n" + "  var nodes = new vis.DataSet([\n"
                        + "    {id: 1, label: 'Node 1'},\n" + "    {id: 2, label: 'Node 2'},\n"+ "    {id: 7, label: 'Node 7'},\n"
                        + "    {id: 3, label: 'Node 3'},\n" + "    {id: 4, label: 'Node 4'},\n"+ "    {id: 6, label: 'Node 6'},\n"
                        + "    {id: 5, label: 'Node 5'}\n";
                String st3 = "  ]);\n" + "\n" + "  // create an array with edges\n" + "  var edges = new vis.DataSet([\n";
    
                String edgeString ="";
                int size = edges.size();
                int i = 0;
                String temp =null;
                for (String edge : edges) {
                    System.out.println(edge);
                    if(i == (size-1)){
                         temp = "     {from:"+ edge.charAt(0)+", to:"+ edge.charAt(1)+"}\n"; 
                    }
                    else{
                        temp = "     {from:"+ edge.charAt(0)+", to:"+ edge.charAt(1)+"},\n"; 
                    }
        
                    edgeString+=temp;
                    i++;
                }
                String st4 = "    {from: 1, to: 3},\n" + "    {from: 1, to: 1},\n" + "    {from: 2, to: 4},\n"+ "    {from: 6, to: 7},\n"
                        + "    {from: 2, to: 5},\n" + "    {from: 3, to: 5}\n";
        
                String st5 = "  ]);";
                String st6 = " // create a network\n" + "  var container = document.getElementById('mynetwork');\n"
                        + "  var data = {\n" + "    nodes: nodes,\n" + "    edges: edges\n" + "  };\n"
                        + "  var options = {};\n" + "  var network = new vis.Network(container, data, options);\n"
                        + "</script>\n" + "\n" + "\n" + "</body>\n" + "</html>";
                fileWriter.write(st1 + st2 + st3 + edgeString + st5 + st6);
                fileWriter.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            
            
            
            System.out.println("DONE222222222222222222");
            
        } else {
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.RETRIES_CONFIG, 0);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.StringSerializer");
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            
            KafkaProducer<String, String> producer = new KafkaProducer<>(props);
            TestCallback callback = new TestCallback();
            Random rnd = new Random();
            // So we can generate random sentences
            Random random = new Random();
            String[] sentences = new String[] { "12", "13",
                    "25", "35", "45",
                    "15","67" };
            for (int i = 0; i < 7; i++) {
                // Pick a sentence at random
                String sentence = sentences[i];
                // Send the sentence to the test topic
                ProducerRecord<String, String> data = new ProducerRecord<String, String>(topic, sentence);
                long startTime = System.currentTimeMillis();
                producer.send(data, callback);
                long elapsedTime = System.currentTimeMillis() - startTime;
                System.out.println("Sent this sentence: " + sentence + " in " + elapsedTime + " ms");
                
            }
            System.out.println("Done");
            producer.flush();
            producer.close();
        }
        
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
//to run-./target/kafka-producer-consumer-1.0-SNAPSHOT.jar producer localhost:9092
