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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;

import it.ppu.consts.ConstantsGrid;
import it.ppu.consts.ConstantsNodeDefaults;
import it.ppu.consts.ConstantsSimulator.NodeOutputModes;
import it.ppu.spiking.simulation.model.SimulationInputDataModel;
import it.ppu.utils.grid.GridIgnite;
import it.ppu.spiking.node.model.internode.InternodeSpikeModel;
import it.ppu.utils.statistics.model.CollectorBurnModel;
import it.ppu.utils.statistics.model.CollectorFireModel;
import it.ppu.utils.statistics.model.CollectorMissedFireModel;
import it.ppu.utils.tools.Message;
import it.ppu.utils.tools.Strings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.io.Serializable;
import java.util.ArrayList;

import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

@Slf4j
public class CollectorNOIProducer implements Serializable {

    private static final long serialVersionUID = 3248671994878955042L;
    
    private Long messageCounterFire = 0L;
    private Long messageCounterBurn = 0L;
    
    private Long active = 0l;
    private Long passive = 0l;

    private Long passive2active = 0l;
    private Long active2passive = 0l;

    private Double minMissedAxonalDelay = Double.MAX_VALUE;

    private final SimulationInputDataModel cfg;

    private Producer<String, String> kafkaProducer;
    
    private KinesisProducer kinesisProducer;
    
    private final Integer nodeId;
    
    private final int BUFF_SIZE = (1024 * 64);
    
    private PrintWriter burningPw;
    private PrintWriter burningPwGephi;
    private PrintWriter burningPwMatlab;

    private PrintWriter firingPw;
    private PrintWriter firingPwGephi;
    private PrintWriter firingPwMatlab;

    boolean alsoGephi  = false;
    boolean alsoMatlab = false;
        
    private ArrayList<PutRecordsRequestEntry> records = new ArrayList<PutRecordsRequestEntry>();
    
    private final static int SECONDS_1 = 1000;
    
    public CollectorNOIProducer(Integer nodeId,SimulationInputDataModel cfg) {
        this.cfg = cfg;

        this.nodeId = nodeId;

        messageCounterFire = 0L;
        messageCounterBurn = 0L;

        if (Strings.isEmpty(cfg.getSimulationConfig().getKafkaAddress()) == false) {
            createProducerKafka();
        } else if (cfg.getSimulationConfig().getSimOnKinesis() == true) {
            createProducerKinesis();
        } else if(cfg.getSimulationConfig().getSimOnDB() == true) {
            ;
        } else {
            createProducerFile();
        }
    }

    private void createProducerFile() {
        
        alsoGephi  = cfg.getSimulationConfig().getOutputFormatGephi();
        alsoMatlab = cfg.getSimulationConfig().getOutputFormatMatlab();
        
        String projectOutput = setupOutputFolder(cfg.getSimulationConfig().getProjectFullPath());

        createWriterFileBurn(projectOutput, alsoGephi, alsoMatlab);
        createWriterFileFire(projectOutput, alsoGephi, alsoMatlab);
    }
    
    private String setupOutputFolder(String projectFullPath) {

        String baseFolder = projectFullPath + File.separator + "output_node" + File.separator;
        
        File folder = new File(baseFolder);

        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        return baseFolder;
    }
    
    private String getFileName(String projectFullPath, Integer nodeId, boolean burning, NodeOutputModes mode,Boolean gephi,Boolean matlab) {
        
        String fullPathAndRunFormatted = projectFullPath + "nodeId" + nodeId + "_";
        
        String type = "";
        
        if(gephi  == true) type = "_gephi";
        if(matlab == true) type = "_matlab";
        
        String burningFiring = "burning" + type;
        
        if(burning == false) {
            burningFiring = "firing" + type;
        }
        
        switch (mode) {
            case MINIMAL:
                return fullPathAndRunFormatted + burningFiring + "_R." + ConstantsNodeDefaults.EXT;
            case REDUCED:
                return fullPathAndRunFormatted + burningFiring + "_r." + ConstantsNodeDefaults.EXT;
            default:
                return fullPathAndRunFormatted + burningFiring + "." + ConstantsNodeDefaults.EXT;
        }
    }
    
    private PrintWriter getFile(String fullpath) throws IOException {
        File burningTowritefile = new File(fullpath);
            
        burningTowritefile.delete();
        burningTowritefile.createNewFile();

        FileWriter     fw = new FileWriter(burningTowritefile);
        BufferedWriter bw = new BufferedWriter(fw, BUFF_SIZE);
        PrintWriter    pw = new PrintWriter(bw);
        
        return pw;
    }
        
    private void createWriterFileBurn(String projectOutput, boolean alsoGephi, boolean alsoMatlab) {

        try {
            String fileNameBase = getFileName(projectOutput, nodeId, true, cfg.getSimulationConfig().getOutputNodesModes(), false, false);
            
            burningPw = getFile(fileNameBase);

            boolean fullLog = cfg.getSimulationConfig().getOutputNodesModes().equals(NodeOutputModes.NORMAL);

            if (fullLog == true) {
                storeMessage(burningPw,""
                    + "Burning Time, "
                    + "Firing Node, "
                    + "Firing Neuron, "
                    + "Burning Node, "
                    + "Burning Neuron, "
                    + "External Source, "
                    + "From Internal State, "
                    + "To Internal State, "
                    + "Step in State, "
                    + "Post Synaptic Weight, "
                    + "Pre Synaptic Weight, "
                    + "Instant to Fire, "
                    + "(Afferent) Firing Time", null);
            } else {
                storeMessage(burningPw,""
                    + "Burning Node, "
                    + "Burning Neuron", null);
            }
            
            //----------
            //matlab
            //----------
            if (alsoMatlab) {
                String fileNameMatlab = getFileName(projectOutput, nodeId, true, cfg.getSimulationConfig().getOutputNodesModes(), false, true);
                
                burningPwMatlab = getFile(fileNameMatlab);

                storeMessage(burningPwMatlab,"Firing, Burning", null);
            }

            //----------
            //gephi 
            //----------
            if (alsoGephi) {
                String fileNameGephi = getFileName(projectOutput, nodeId, true, cfg.getSimulationConfig().getOutputNodesModes(), true, false);
                
                burningPwGephi = getFile(fileNameGephi);

                storeMessage(burningPwGephi,"Firing, Burning", null);
            }
        } catch (IOException e) {
            log.error("error in statsCollector_InitBurningWriters", e);
        }
    }

    private void createWriterFileFire(String projectOutput, boolean alsoGephi, boolean alsoMatlab) {

        try {
            String fileNameBase = getFileName(projectOutput, nodeId, false, cfg.getSimulationConfig().getOutputNodesModes(), false, false);
                
            firingPw = getFile(fileNameBase);

            boolean fullLog = cfg.getSimulationConfig().getOutputNodesModes().equals(NodeOutputModes.NORMAL);

            if (fullLog == true) {
                storeMessage(firingPw,""
                    + "Firing Time, "
                    + "Firing Node, "
                    + "Firing Neuron, "
                    + "Neuron Type, "
                    + "External Source", null);
            } else {
                storeMessage(firingPw,""
                    + "Firing Node, "
                    + "Firing Neuron", null);
            }
            
            //----------
            //matlab
            //----------
            if (alsoMatlab) {
                String fileNameMatlab = getFileName(projectOutput, nodeId, false, cfg.getSimulationConfig().getOutputNodesModes(), false, true);
                
                firingPwMatlab = getFile(fileNameMatlab);
                
                storeMessage(firingPwMatlab,"Firing, Burning", null);
            }

            //----------
            //gephi 
            //----------
            if (alsoGephi) {
                String fileNameGephi = getFileName(projectOutput, nodeId, false, cfg.getSimulationConfig().getOutputNodesModes(), true, false);
                
                firingPwGephi = getFile(fileNameGephi);
                
                storeMessage(firingPwGephi,"Firing, Burning", null);
            }
        } catch (IOException e) {
            log.error("error in statsCollector_InitBurningWriters", e);
        }
    }
 
    private void createProducerKinesis() {

        String KINESIS_CONFIG = "default_config.properties";
        
        boolean fromFile = false; // new File(KINESIS_CONFIG).exists();
        
        String region = cfg.getSimulationConfig().getAws().getRegions().get(0);
        
        AWSCredentials credentials = new BasicAWSCredentials(
            cfg.getSimulationConfig().getAws().getIdentityAccessKey(),
            cfg.getSimulationConfig().getAws().getCredentialSecretKey());

        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
   
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
        
        if(fromFile == true) {
            config = KinesisProducerConfiguration.fromPropertiesFile(KINESIS_CONFIG);
        }

        kinesisProducer = new KinesisProducer(config);
    }
    
    private void createProducerKafka() {
        
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, cfg.getSimulationConfig().getKafkaAddress());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        kafkaProducer = new KafkaProducer<>(props);
    }


    public SimulationInputDataModel getSimInputData() {
        return cfg;
    }

    @SuppressWarnings("unused")
    private synchronized void collectActive() {
        ++active;
    }

    @SuppressWarnings("unused")
    private synchronized void collectPassive2active() {
        ++passive2active;
    }

    @SuppressWarnings("unused")
    private synchronized void collectActive2passive() {
        ++active2passive;
    }

    @SuppressWarnings("unused")
    private synchronized void collectPassive() {
        ++passive;
    }

    public void collectFireSpike(
        Integer firingNodeId, Long firingNeuronId, Double firingTime,
        Long maxNeurons,
        Double compressionFactor,
        Boolean isExcitatory, Boolean isExternal,Long split) {
        
        CollectorFireModel itemToSendFire = new CollectorFireModel(
            firingNodeId,
            firingNeuronId,
            firingTime,
            maxNeurons,
            compressionFactor,
            isExcitatory,
            isExternal,
            messageCounterFire++,
            split);
        
        prepareAndSendMessage(itemToSendFire);
    }

    public void collectBurnSpike(
        Long firingNeuronId, Integer firingNodeId,
        Long burningNeuronId, Integer burningNodeId,
        Double burnTime,
        Boolean fromExternalSource, Double fromState,
        Double stepInState,
        Double postsynapticWeight, Double presynapticWeight,
        Double timeToFire, Double fireTime,Long split,boolean refractPeriod) {

        CollectorBurnModel itemToSendBurn = new CollectorBurnModel(
            firingNeuronId,
            firingNodeId,
            burningNeuronId,
            burningNodeId,
            burnTime,
            fromExternalSource,
            fromState,
            stepInState,
            postsynapticWeight,
            presynapticWeight,
            timeToFire,
            fireTime,
            messageCounterBurn++,
            split,
            Boolean.FALSE,
            refractPeriod);
        
        prepareAndSendMessage(itemToSendBurn);
    }

    public synchronized void collectMissedFire(InternodeSpikeModel internodeSpike,Double currentTime,Long missedFires) {

        if (internodeSpike.getAxonalDelay() < minMissedAxonalDelay) {
            minMissedAxonalDelay = internodeSpike.getAxonalDelay();
        }
        
        CollectorMissedFireModel itemToSendMissedFire = new CollectorMissedFireModel(internodeSpike, currentTime, missedFires, minMissedAxonalDelay);

        prepareAndSendMessage(itemToSendMissedFire);
    }

    private void prepareAndSendMessage(Object data) {
        try {
            if(data instanceof CollectorBurnModel) {
                saveBurnMessage((CollectorBurnModel)data);
            }
            if(data instanceof CollectorFireModel) {
                saveFireMessage((CollectorFireModel)data);
            }
        } catch(IOException reason) {
            log.error("error storing on cache", reason);
        }
    }
    
    public void saveBurnMessage(CollectorBurnModel burnMessage) throws IOException {

        Boolean isReduced = (cfg.getSimulationConfig().getOutputNodesModes().equals(NodeOutputModes.NORMAL) == false);

        //----------------------------------------------------------------------
        //std csv
        //----------------------------------------------------------------------
        Double fromState   = burnMessage.getFromState();
        Double stepInState = burnMessage.getStepInState();

        String stepInStateToPrint;
        String fromStateToPrint;
        String toStateToPrint;
        
        String stepInStateToPrintMatlab;
        String fromStateToPrintMatlab;
        String toStateToPrintMatlab;

        if (fromState == null) {
            fromStateToPrint = isReduced ? "0" : "refr";
            toStateToPrint   = isReduced ? "0" : "refr";
        } else {
            fromStateToPrint = "" + ConstantsNodeDefaults.FORMAT.format(fromState);
            toStateToPrint   = "" + ConstantsNodeDefaults.FORMAT.format(fromState + stepInState);
        }

        if (stepInState == null) {
            stepInStateToPrint = isReduced ? "0" : "refr";
        } else {
            stepInStateToPrint = "" + ConstantsNodeDefaults.FORMAT.format(stepInState);
        }

        if (isReduced) {
            storeMessage(burningPw, ""
                + ConstantsNodeDefaults.FORMAT.format(burnMessage.getBurnTime()) + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getBurningNodeId()   + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getBurningNeuronId() + ConstantsNodeDefaults.CSV_SEPARATOR
                + toStateToPrint, burnMessage);
        } else {
            storeMessage(burningPw, "" 
                //DEBUG + burnMessage.getSplit() + "-" + burnMessage.getRefractPeriod() + "-"
                + ConstantsNodeDefaults.FORMAT.format(burnMessage.getBurnTime()) + ConstantsNodeDefaults.CSV_SEPARATOR                
                + burnMessage.getFiringNodeId()     + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getFiringNeuronId()   + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getBurningNodeId()    + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getBurningNeuronId()  + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getFromExternalInput()+ ConstantsNodeDefaults.CSV_SEPARATOR
                + fromStateToPrint                  + ConstantsNodeDefaults.CSV_SEPARATOR
                + toStateToPrint                    + ConstantsNodeDefaults.CSV_SEPARATOR
                + stepInStateToPrint                + ConstantsNodeDefaults.CSV_SEPARATOR
                + ConstantsNodeDefaults.FORMAT.format(burnMessage.getPostSynapticWeight()) + ConstantsNodeDefaults.CSV_SEPARATOR
                + ConstantsNodeDefaults.FORMAT.format(burnMessage.getPreSynapticWeight())  + ConstantsNodeDefaults.CSV_SEPARATOR
                + ConstantsNodeDefaults.FORMAT.format(burnMessage.getTimeToFire())         + ConstantsNodeDefaults.CSV_SEPARATOR
                + ConstantsNodeDefaults.FORMAT.format((burnMessage.getFireTime() != null) ? burnMessage.getFireTime() : 0), burnMessage);
        }
        
        //----------------------------------------------------------------------
        //paramMatlab csv
        //----------------------------------------------------------------------
        if (cfg.getSimulationConfig().getOutputFormatMatlab()) {
            String refrStringMatlab = "0";

            if (fromState == null) {
                fromStateToPrintMatlab = refrStringMatlab;
                toStateToPrintMatlab = refrStringMatlab;
            } else {
                fromStateToPrintMatlab = fromState.toString();
                toStateToPrintMatlab = "" + (fromState + stepInState);
            }

            if (stepInState == null) {
                stepInStateToPrintMatlab = "0";
            } else {
                stepInStateToPrintMatlab = stepInState.toString();
            }

            storeMessage(burningPwMatlab, ""
                + ConstantsNodeDefaults.FORMAT.format(burnMessage.getBurnTime()) + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getFiringNodeId()             + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getFiringNeuronId()           + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getBurningNodeId()            + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getBurningNeuronId()          + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getFromExternalInputInteger() + ConstantsNodeDefaults.CSV_SEPARATOR
                + fromStateToPrintMatlab                    + ConstantsNodeDefaults.CSV_SEPARATOR
                + toStateToPrintMatlab                      + ConstantsNodeDefaults.CSV_SEPARATOR
                + stepInStateToPrintMatlab                  + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getPostSynapticWeight()       + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getPreSynapticWeight()        + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getTimeToFire()               + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getFireTime(), burnMessage);
        }
        //----------------------------------------------------------------------
        //paramGephi csv
        //----------------------------------------------------------------------
        if (cfg.getSimulationConfig().getOutputFormatGephi() && (!burnMessage.getFromExternalInput())) {
            storeMessage(burningPwGephi, ""
                + burnMessage.getFiringNodeId()  + "-" + burnMessage.getFiringNeuronId() + ConstantsNodeDefaults.CSV_SEPARATOR
                + burnMessage.getBurningNodeId() + "-" + burnMessage.getBurningNeuronId(), burnMessage);
        }
    }

     public void saveFireMessage(CollectorFireModel fireMessage) throws IOException {

        Boolean isReduced = (cfg.getSimulationConfig().getOutputNodesModes().equals(NodeOutputModes.NORMAL) == false);
        
        //----------------------------------------------------------------------
        //std csv
        //----------------------------------------------------------------------
        String excitStr;
        String isExternalStr;

        if (fireMessage.getExcitatory()) {
            excitStr = "excitatory";
        } else {
            excitStr = "inhibitory";
        }

        if (fireMessage.getExternal()) {
            isExternalStr = isReduced ? "1" : "true";
        } else {
            isExternalStr = isReduced ? "0" : "false";
        }

        if (isReduced) {
            storeMessage(firingPw, ""
                + ConstantsNodeDefaults.FORMAT.format(fireMessage.getFiringTime()) + ConstantsNodeDefaults.CSV_SEPARATOR
                + fireMessage.getFiringNodeId()   + ConstantsNodeDefaults.CSV_SEPARATOR
                + fireMessage.getFiringNeuronId() + ConstantsNodeDefaults.CSV_SEPARATOR
                + isExternalStr, fireMessage);
        } else {
            storeMessage(firingPw, ""
                //DEBUG + cf.getSplit() + "---"
                + ConstantsNodeDefaults.FORMAT.format(fireMessage.getFiringTime()) + ConstantsNodeDefaults.CSV_SEPARATOR
                + fireMessage.getFiringNodeId()   + ConstantsNodeDefaults.CSV_SEPARATOR
                + fireMessage.getFiringNeuronId() + ConstantsNodeDefaults.CSV_SEPARATOR
                + excitStr + ConstantsNodeDefaults.CSV_SEPARATOR
                + fireMessage.getExternal(), fireMessage);
        }
        //----------------------------------------------------------------------
        //paramMatlab csv
        //----------------------------------------------------------------------
        if (cfg.getSimulationConfig().getOutputFormatMatlab()) {
            storeMessage(firingPwMatlab, ""
                + ConstantsNodeDefaults.FORMAT.format(fireMessage.getFiringTime()) + ConstantsNodeDefaults.CSV_SEPARATOR
                + fireMessage.getFiringNodeId()             + ConstantsNodeDefaults.CSV_SEPARATOR
                + fireMessage.getFiringNeuronId()           + ConstantsNodeDefaults.CSV_SEPARATOR
                + (fireMessage.getExcitatory() ? '1' : '0') + ConstantsNodeDefaults.CSV_SEPARATOR
                + (fireMessage.getExternal()   ? '1' : '0'), fireMessage);
        }
    }   
     
    private void storeMessage(PrintWriter writer, String content, Object originalContent) throws IOException {
        if (kafkaProducer != null) {
            Message.sendKafkaMessage(kafkaProducer, ConstantsGrid.TOPIC_NAME, ConstantsGrid.TOPIC_KEY + nodeId, content);
        }        
        else if(kinesisProducer != null) {
            Message.sendKinesisMessage(kinesisProducer, ConstantsGrid.TOPIC_NAME, ConstantsGrid.TOPIC_KEY + nodeId, content);
        }
        else if(cfg.getSimulationConfig().getSimOnDB() == true) {
            GridIgnite.updateCacheDatabaseSpikes(originalContent);
        }
        else if(writer != null) {
            writer.println(content);
        } 
        
    }

    public void close() throws IOException {
        flush();
        
        if(burningPw != null) {
            burningPw.close();
        }
        
        if (burningPwMatlab != null) {
            burningPwMatlab.close();
        }
        
        if (burningPwGephi != null) {
            burningPwGephi.close();
        }
        
        if(firingPw != null) {
            firingPw.close();
        }
        
        if (firingPwMatlab != null) {
            firingPwMatlab.close();
        }
        
        if (firingPwGephi != null) {
            firingPwGephi.close();
        }

        if (kafkaProducer != null) {
            kafkaProducer.close();
        }

        if (kinesisProducer != null) {
            kinesisProducer.destroy();
        }
    }

    private void flush() throws IOException {

        if (burningPw != null) {
            burningPw.flush();
        }

        if (burningPwMatlab != null) {
            burningPwMatlab.flush();
        }

        if (burningPwGephi != null) {
            burningPwGephi.flush();
        }
        
        if (firingPw != null) {
            firingPw.flush();
        }

        if (firingPwMatlab != null) {
            firingPwMatlab.flush();
        }

        if (firingPwGephi != null) {
            firingPwGephi.flush();
        }
        
        if (kafkaProducer != null) {
            kafkaProducer.flush();
        }
        
        if (kafkaProducer != null) {
            kinesisProducer.flush();
        }
    }         
}
