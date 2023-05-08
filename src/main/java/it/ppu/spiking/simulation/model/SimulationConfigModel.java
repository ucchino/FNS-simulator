/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.spiking.simulation.model;

import it.ppu.consts.ConstantsSimulator;
import it.ppu.consts.ConstantsSimulator.NodeOutputModes;
import it.ppu.consts.ConstantsSimulatorLauncherCLI;
import it.ppu.utils.grid.model.AwsIdentity;
import it.ppu.utils.tools.Strings;

import org.apache.commons.cli.CommandLine;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SimulationConfigModel implements Serializable {

    private static final long serialVersionUID = 324867199487895584L;

    private String kafkaAddress;
    
    private String projectFullPath = null;

    private String noi;
    
    private Boolean simOnDB      = false;
    private Boolean simOnKinesis = false;
    private Boolean simFast      = false;
    private Boolean simNoRandom  = true;

    private Boolean outputFormatMatlab = false;
    private Boolean outputFormatGephi = false;

    private NodeOutputModes outputNodesModes = NodeOutputModes.NORMAL;

    private AwsIdentity aws = null;
    
    private Long startTime;
    
    private SimulationConfigModel() {
        //Col costruttore privato evito che la classe
        //sia istanziabile -> Obbligo all'uso del builder
    }
    
    public static SimulationConfigModel builder() {
        return new SimulationConfigModel();
    }
    
    public String getKafkaAddress() {
        return kafkaAddress;
    }


    public String getProjectFullPath() {
        return projectFullPath;
    }

    public String getNoi() {
        return noi;
    }

    public Boolean getSimFast() {
        return simFast;
    }

    public Boolean getSimNoRandom() {
        return simNoRandom;
    }

    public Boolean getSimOnDB() {
        return simOnDB;
    }

    public Boolean getOutputFormatMatlab() {
        return outputFormatMatlab;
    }

    public Boolean getOutputFormatGephi() {
        return outputFormatGephi;
    }

    public NodeOutputModes getOutputNodesModes() {
        return outputNodesModes;
    }
   
    public static SimulationConfigModel builder(CommandLine commandLine, boolean calledByMain) throws Exception {

        SimulationConfigModel parsed = new SimulationConfigModel();
        
        parsed.simFast      = commandLine.hasOption(ConstantsSimulatorLauncherCLI.OPT_SIM_FAST);
        parsed.simOnKinesis = commandLine.hasOption(ConstantsSimulatorLauncherCLI.OPT_SIM_ON_KINESIS);
        parsed.simNoRandom  = commandLine.hasOption(ConstantsSimulatorLauncherCLI.OPT_SIM_NO_RANDOM);
        parsed.simOnDB      = commandLine.hasOption(ConstantsSimulatorLauncherCLI.OPT_SIM_ON_DB);
        
        parsed.outputFormatMatlab = commandLine.hasOption(ConstantsSimulatorLauncherCLI.OPT_OUTPUT_MATLAB);
        parsed.outputFormatGephi  = commandLine.hasOption(ConstantsSimulatorLauncherCLI.OPT_OUTPUT_GEPHI);

        parsed.kafkaAddress = commandLine.getOptionValue(ConstantsSimulatorLauncherCLI.OPT_KAFKA, "");

        if(calledByMain == true) {
            if(commandLine.hasOption(ConstantsSimulatorLauncherCLI.OPT_PROJECT) == false) {
                throw new Exception("PROJECT NAME NOT PROVIDED");
            }
        }

        parsed.projectFullPath = ConstantsSimulator.PROJECTS_BASE + commandLine.getOptionValue(ConstantsSimulatorLauncherCLI.OPT_PROJECT, "");
        
        parsed.noi = commandLine.getOptionValue(ConstantsSimulatorLauncherCLI.OPT_NODES_LIST, "");

        boolean reduced = commandLine.hasOption(ConstantsSimulatorLauncherCLI.OPT_REDUCED_OUTPUT);
        boolean minimal = commandLine.hasOption(ConstantsSimulatorLauncherCLI.OPT_ULTRA_REDUCED_OUTPUT);
        
        if(reduced) parsed.outputNodesModes = NodeOutputModes.REDUCED;
        if(minimal) parsed.outputNodesModes = NodeOutputModes.MINIMAL;
        
        parsed.aws = getAwsIdentity(commandLine);
        
        parsed.startTime = System.currentTimeMillis();
        
        return parsed;
    }

    public AwsIdentity getAws() {
        return aws;
    }

    private static String cleanCmd(String from) {
        return from.replaceAll("\"","").replaceAll("'","").replaceAll(" ","");
    }
    
    private static AwsIdentity getAwsIdentity(CommandLine cmd) {
        
        AwsIdentity aws = new AwsIdentity();
        
        log.info("aws cmd parameters has access :" + cmd.hasOption(ConstantsSimulatorLauncherCLI.OPT_IDENTITY));
        log.info("aws cmd parameters has secret :" + cmd.hasOption(ConstantsSimulatorLauncherCLI.OPT_CREDENTIAL));
        log.info("aws cmd parameters has region :" + cmd.hasOption(ConstantsSimulatorLauncherCLI.OPT_REGIONS));
        log.info("aws cmd parameters has zones  :" + cmd.hasOption(ConstantsSimulatorLauncherCLI.OPT_ZONES));
        
        aws.setCredentialSecretKey(cmd.getOptionValue(ConstantsSimulatorLauncherCLI.OPT_CREDENTIAL, ""));
        aws.setIdentityAccessKey(cmd.getOptionValue(ConstantsSimulatorLauncherCLI.OPT_IDENTITY,     ""));
         
        String regions = cmd.getOptionValue(ConstantsSimulatorLauncherCLI.OPT_REGIONS, "");
        String zones   = cmd.getOptionValue(ConstantsSimulatorLauncherCLI.OPT_ZONES,   "");

        if(regions == null) regions = "";
        if(zones   == null) zones   = "";
        
        String[] regionArray = cleanCmd(regions).split(",");
        String[] zonesArray  = cleanCmd(zones).split(",");
            
        if(regionArray.length > 0) {
            aws.setRegions(new ArrayList<String>(Arrays.asList(regionArray)));
        }
            
        if(zonesArray.length > 0) {
            aws.setZones(new ArrayList<String>(Arrays.asList(zonesArray)));
        }

        log.info(ConstantsSimulator.BAR_LOGGER);
        log.info("command line AWS using");
        log.info(ConstantsSimulator.BAR_LOGGER);
        log.info("access--: " + aws.getIdentityAccessKey());
        log.info("secret--: " + aws.getCredentialSecretKey());
        log.info("regions-: " + Arrays.toString(aws.getRegions().toArray()));
        log.info("zones---: " + Arrays.toString(aws.getZones().toArray()));
        log.info(ConstantsSimulator.BAR_LOGGER);
        
        boolean hasIdentity = true;
        
        hasIdentity &= !Strings.isEmpty(aws.getCredentialSecretKey());
        hasIdentity &= !Strings.isEmpty(aws.getIdentityAccessKey());
        hasIdentity &= (aws.getRegions().size() > 0);

        if(hasIdentity == false) {
            return null;
        }
        
        return aws;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Boolean getSimOnKinesis() {
        return simOnKinesis;
    }
}
