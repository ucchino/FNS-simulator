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

import java.nio.ByteBuffer;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;

import java.io.IOException;
import java.util.concurrent.Future;

public class KinesisWRITE_ASYNC {

    public static void main(String[] args) throws IOException {
        // Credenziali AWS
        AWSCredentials awsCredentials = new BasicAWSCredentials("KEY", "PRIVATE");

//        // Costruisce un oggetto AmazonKinesis a partire dal builder
//        AmazonKinesis kinesisClient = kinesisClientBuilder.build();
//        
//        // Dati da inviare
//        List<PutRecordsRequestEntry> records = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            String data = "Record " + i;
//            String partitionKey = "partition-" + i;
//            PutRecordsRequestEntry entry = new PutRecordsRequestEntry();
//            entry.setData(ByteBuffer.wrap(data.getBytes()));
//            entry.setPartitionKey(partitionKey);
//            records.add(entry);
//        }
//
//        // Richiesta di invio
//        PutRecordsRequest putRecordsRequest = new PutRecordsRequest();
//        putRecordsRequest.setStreamName("fns-kinesis-stream");
//        putRecordsRequest.setRecords(records);
//
//        // Invio dei dati
//        PutRecordsResult putRecordsResult = kinesisClient.putRecords(putRecordsRequest);
//
//        // Stampa dei risultati
//        List<PutRecordsResultEntry> resultEntries = putRecordsResult.getRecords();
//        for (int i = 0; i < resultEntries.size(); i++) {
//            PutRecordsRequestEntry requestEntry = records.get(i);
//            PutRecordsResultEntry resultEntry = resultEntries.get(i);
//            if (resultEntry.getErrorCode() == null) {
//                System.out.println("Record sent. ShardID: " + resultEntry.getShardId() + ", SequenceNumber: " + resultEntry.getSequenceNumber());
//            } else {
//                System.out.println("Error sending record. ErrorCode: " + resultEntry.getErrorCode() + ", ErrorMessage: " + resultEntry.getErrorMessage());
//            }
//        }
//
//        // Chiusura della connessione
//        kinesisClient.shutdown();
        AmazonKinesisAsync kinesisClient = AmazonKinesisAsyncClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
            .withRegion(Regions.US_EAST_1)
            .build();

        for(int i=0;i < 1000;i++) {
            PutRecordRequest putRecordRequest = new PutRecordRequest();
            putRecordRequest.setStreamName("fns-kinesis-stream");
            putRecordRequest.setPartitionKey("PIPPO" + i);
            putRecordRequest.setData(ByteBuffer.wrap(("PIPPO" + i + "\n").getBytes()));

            // Invia i dati nel flusso Kinesis in modo asincrono
            Future<PutRecordResult> futureResult = kinesisClient.putRecordAsync(putRecordRequest);
        
            PutRecordResult putRecordResult;
            try {
                putRecordResult = futureResult.get();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            System.out.println("Record " + i + " : " + putRecordResult.getSequenceNumber() + " " + putRecordResult.getShardId());
        }
        kinesisClient.shutdown();
    }
}
