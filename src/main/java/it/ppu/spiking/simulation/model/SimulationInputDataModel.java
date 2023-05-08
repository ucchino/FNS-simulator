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

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;

import it.ppu.spiking.node.model.internode.InternodeConnectionModel;
import it.ppu.utils.configuration.model.XmlConfigModel;
import it.ppu.utils.configuration.model.XmlNodeModel;
import it.ppu.utils.tools.Files;
import it.ppu.spiking.simulation.controller.SimulationInternodeBuilder;
import it.ppu.utils.tools.Strings;

import java.io.Serializable;

public final class SimulationInputDataModel implements Serializable {

    private static final long serialVersionUID = 324867199487895585L;
    
    private SimulationConfigModel simulationConfig;

    private Integer nodesNum;
    
    private Double minNeXnRatio;
    private Double maxNeXnRatio;
    
    private ArrayList<Integer> NOI;
    
    private XmlConfigModel xmlConfig;
    
    private HashMap<Integer, XmlNodeModel> xmlNodes;
    
    private ArrayList<InternodeConnectionModel> internodeConnections;
    
    private final Long runName = System.nanoTime();
    
    private Double timeSimulationCycle;
    private Double timeSimulationTotal;
    
    private SimulationInputDataModel() {
        //Col costruttore privato evito che la classe
        //sia istanziabile -> Obbligo all'uso del builder
    }
    
    public static SimulationInputDataModel builder(SimulationConfigModel simConfig) throws IOException {
        SimulationInputDataModel simData = new SimulationInputDataModel();

        simData.simulationConfig = simConfig;
        
        String configFile = (new File(simConfig.getProjectFullPath()+ "/config.xml")).getAbsolutePath();
        String dataFolder = (new File(simConfig.getProjectFullPath()+ "/connectivity")).getAbsolutePath();
        
        simData.xmlConfig = Files.readConfigXmlFile(configFile);
        simData.xmlNodes  = Files.loadAndGetXmlNodes(simData.getXmlConfig().getNode());
        
        SimulationInternodeBuilder packageInternodeBuilder = new SimulationInternodeBuilder(dataFolder);
        
        simData.nodesNum             = packageInternodeBuilder.getNodesNum();
        simData.minNeXnRatio         = packageInternodeBuilder.getMinNeXnRatio();
        simData.maxNeXnRatio         = packageInternodeBuilder.getMaxNeXnRatio();
        simData.internodeConnections = packageInternodeBuilder.getInternodeConnections();
        
        simData.NOI = fromNOIStringToList(simData.nodesNum, simConfig.getNoi());
        
        simData.timeSimulationTotal = simData.getXmlConfig().getStop().doubleValue();
                
        return simData;
    }
    
    public String getRunName() {
        return "" + runName;
    }
    
    public SimulationConfigModel getSimulationConfig() {
        return simulationConfig;
    }

    public ArrayList<Integer> getNOI() {
        return NOI;
    }
    
    public XmlConfigModel getXmlConfig() {
        return xmlConfig;
    }
    
    public HashMap<Integer, XmlNodeModel> getXmlNodes() {
        return xmlNodes;
    }
    
    public Integer getNodesNum() {
        return nodesNum;
    }

    public Double getMinNeXnRatio() {
        return minNeXnRatio;
    }

    public Double getMaxNeXnRatio() {
        return maxNeXnRatio;
    }

    public ArrayList<InternodeConnectionModel> getInternodeConnections() {
        return internodeConnections;
    }

    private static ArrayList<Integer> fromNOIStringToList(int totalNodes,String nodesOfInterestList) {
        nodesOfInterestList = clean(nodesOfInterestList);

        ArrayList<Integer> nodesOfInterest = new ArrayList<Integer>();
        
        if (nodesOfInterestList.length() > 0) {

            String[] nodesStr = nodesOfInterestList.split(",");

            for (int i = 0; i < nodesStr.length; ++i) {
                nodesOfInterest.add(Integer.parseInt(nodesStr[i].trim()));
            }
        } else {
            for (int i = 0; i < totalNodes; ++i) {
                nodesOfInterest.add(i);
            }
        }

        return nodesOfInterest;
    }
    
    private static String clean(String nodesOfInterestList) {
        
        if(Strings.isEmpty(nodesOfInterestList)) return "";
        
        return nodesOfInterestList.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "").trim();
    }

    public Double getTimeSimulationCycle() {
        return timeSimulationCycle;
    }

    public Double getTimeSimulationTotal() {
        return timeSimulationTotal;
    }

    public void setTimeSimulationCycle(Double timeSimulationCycle) {
        this.timeSimulationCycle = timeSimulationCycle;
    }

}
