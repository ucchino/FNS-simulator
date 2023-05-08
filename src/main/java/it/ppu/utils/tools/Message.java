/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.tools;

import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.ppu.consts.ConstantsSimulator;
import it.ppu.utils.statistics.CollectorCallbackKafka;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
public class Message implements Serializable {

    private static final long serialVersionUID = 3248671994878955052L;
    
    public static void sendKafkaMessage(Producer<String, String> kafkaProducer, String topic, String key, String message) {

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);

        if (ConstantsSimulator.KAFKA_MESSAGES_ON_FILE == true) {
            try {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(key + ".txt"))) {
                    writer.write(record.value());
                }
            } catch (IOException reason) {
                log.error("error in send message", reason);
            }
        } else {
            kafkaProducer.send(record, new CollectorCallbackKafka());
        }
    }

    public static void sendKinesisMessage(KinesisProducer kinesisProducer, String stream, String partitionKey, String message) {
        
        try {
           kinesisProducer.addUserRecord(stream, partitionKey, ByteBuffer.wrap(message.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException reason) {
            log.error("error writing on kinesis using stream: " + stream + " - partition key: " + partitionKey,reason);
        }
    }
    
    public static String toJsonString(Object obj) {
        //--------------------------------------------/
        ObjectMapper mapper = new ObjectMapper();
        //--------------------------------------------/
        try {
            //--------------------------------------------/
            //Object to JSON in String
            //--------------------------------------------/
            return mapper.writeValueAsString(obj);
            //--------------------------------------------/
        } catch (JsonProcessingException ex) {
            log.error("serialization error", ex);
        }
        //--------------------------------------------/
        return "";
        //--------------------------------------------/
    }
}
