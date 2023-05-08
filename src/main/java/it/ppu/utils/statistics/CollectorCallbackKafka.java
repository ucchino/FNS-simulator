/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.statistics;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

@Slf4j
public class CollectorCallbackKafka implements Callback,Serializable {

    private static final long serialVersionUID = 3248671994878955040L;
    
    @Override
    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
        if (e != null) {
            log.info("asynchronousProducer failed with an exception: " + e.getLocalizedMessage());
        } else {
            log.info("asynchronousProducer call Success: " + recordMetadata.toString());
        }
    }
}
