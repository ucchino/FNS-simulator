/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.test.old;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.kinesis.producer.UserRecordResult;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class KinesisWrite_KPL {

    private static final String STREAM_NAME = "fns-kinesis-stream";
    private static final String REGION_NAME = "us-east-1";

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        
        BasicAWSCredentials credentials = new BasicAWSCredentials("KEY", "PRIVATE");
        
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
                
        KinesisProducerConfiguration config = new KinesisProducerConfiguration();
        config.setRegion(REGION_NAME);
        config.setCredentialsProvider(credentialsProvider);

        KinesisProducer producer = new KinesisProducer(config);

        String data = "Hello, world!";
        ByteBuffer dataBuffer = ByteBuffer.wrap(data.getBytes());

        Future<UserRecordResult> future = producer.addUserRecord(STREAM_NAME, "partition-key", dataBuffer);
        UserRecordResult result = future.get();

        System.out.println("Data ingested to shard " + result.getShardId());

        producer.flushSync();
        producer.destroy();
    }
}
