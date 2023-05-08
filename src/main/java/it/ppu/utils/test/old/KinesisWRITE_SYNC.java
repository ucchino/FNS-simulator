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
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesis.model.ResourceNotFoundException;

import java.io.IOException;

public class KinesisWRITE_SYNC {
    public static void main(String[] args) throws IOException {
        // Credenziali AWS
        AWSCredentials awsCredentials = new BasicAWSCredentials("KEY", "PRIVATE");

        // Configurazione Kinesis
        AmazonKinesisClientBuilder kinesisClientBuilder = AmazonKinesisClientBuilder.standard();
        
        // Imposta le credenziali e la regione
        kinesisClientBuilder.withCredentials(new AWSStaticCredentialsProvider(awsCredentials));
        kinesisClientBuilder.withRegion(Regions.US_EAST_1);
        
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

        AmazonKinesis kinesisClient = kinesisClientBuilder.build();
// Costruisce un oggetto PutRecordRequest per scrivere i dati in Kinesis
        PutRecordRequest putRecordRequest = new PutRecordRequest();
        putRecordRequest.setStreamName("fns-kinesis-stream");
        putRecordRequest.setPartitionKey("PIPO");
        putRecordRequest.setData(ByteBuffer.wrap("PIPPO".getBytes()));
        
        try {
            // Invia la richiesta al servizio Kinesis e ottiene la risposta
            PutRecordResult putRecordResult = kinesisClient.putRecord(putRecordRequest);
            System.out.println("Record inviato: " + putRecordResult.getSequenceNumber() + " " + putRecordResult.getShardId());
            
        } catch (ResourceNotFoundException e) {
            System.out.println("Flusso non trovato: " + e.getMessage());
            
        } catch (Exception e) {
            System.out.println("Errore durante l'invio del record: " + e.getMessage());
        }
        
        // Chiude la connessione al servizio Kinesis
        kinesisClient.shutdown();
    }
}
