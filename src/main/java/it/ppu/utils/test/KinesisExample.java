/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.test;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;

import it.ppu.consts.ConstantsGrid;

import java.io.IOException;
import java.nio.ByteBuffer;

public class KinesisExample {

    private static KinesisProducer kinesisProducer;
    
    private final static int SECONDS_1 = 1000;
    
    public static void main(String[] args) throws IOException {
        
        AWSCredentials credentials = new BasicAWSCredentials("AKIAZ6U45CQIJMWU4GCJ", "pf9C8mmUhUHNHlBlFRRCyGUI8V3jSn5k2nhOW+Mu");

        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        
        String region = "us-east-1";
   
        KinesisProducerConfiguration config = new KinesisProducerConfiguration()
            .setRecordMaxBufferedTime(SECONDS_1 * 3)
            .setAggregationEnabled(true)
            .setAggregationMaxCount(1000)
            .setMaxConnections(10)
            .setRequestTimeout(SECONDS_1 * 60)
            .setRegion(region)
            .setRecordTtl(SECONDS_1 * 60)
            .setThreadPoolSize(20).setThreadingModel("POOLED")
            .setCredentialsProvider(credentialsProvider);
        
        kinesisProducer = new KinesisProducer(config);
        
        for(int i=0;i < 10_000;i++) {
            kinesisProducer.addUserRecord(ConstantsGrid.TOPIC_NAME, ConstantsGrid.TOPIC_KEY + 1, ByteBuffer.wrap(("TEST" + i).getBytes("UTF-8")));
        }
        
        kinesisProducer.flush();
        kinesisProducer.destroy();
    }
}
