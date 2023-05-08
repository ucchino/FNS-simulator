/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.spiking.simulation.controller;

import it.ppu.consts.ConstantsGrid;
import it.ppu.consts.ConstantsNodeDefaults;
import it.ppu.consts.ConstantsSimulator;
import it.ppu.spiking.simulation.model.SimulationInputDataModel;
import it.ppu.spiking.simulation.model.SimulationOutputDataModel;
import it.ppu.utils.grid.GridIgnite;
import it.ppu.spiking.node.controller.NodeMain;
import it.ppu.utils.exceptions.BadParametersException;
import it.ppu.utils.node.Naming;
import it.ppu.utils.tools.Files;
import it.ppu.utils.tools.Strings;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.cache.affinity.Affinity;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.lang.IgniteFuture;

@Slf4j
public class SimulationMain implements Serializable {

    //----------------------------------------------------------------------
    
    private static final long serialVersionUID = 324867199487895583L;
    
    //----------------------------------------------------------------------
    
    private static ExecutorService EXECUTORS_THREAD_POOL;
    
    //----------------------------------------------------------------------
    
    private Double SIM_START_ABSOLUTE = (double) System.currentTimeMillis();
    private Double SIM_START_THREADS  = (double) System.currentTimeMillis();

    //----------------------------------------------------------------------

    private Long splitsCountExpected = 0L;

    //----------------------------------------------------------------------
    
    private ArrayList<NodeMain> NODES_THREADS;

    //----------------------------------------------------------------------
    
    private SimulationInputDataModel  simulationInputData;
    private SimulationOutputDataModel simulationOutputData;
    
    //----------------------------------------------------------------------
    
    public void setSimulationProcesses(ArrayList<NodeMain> NODES_THREADS) {
        this.NODES_THREADS = NODES_THREADS;
    }
    
    public void setSimulationInputData(SimulationInputDataModel simulationInputData) {
        this.simulationInputData = simulationInputData;
    }
    
    public void setSimulationOutputData(SimulationOutputDataModel simulationOutputData) {
        this.simulationOutputData = simulationOutputData;
    }
    
    public void managementStart() throws BadParametersException {

        //----------------------------------------------------------------------
        
        int threadsNum = NODES_THREADS.size();

        //----------------------------------------------------------------------

        log.info("simulation starting...");
        
        //----------------------------------------------------------------------
        
        Double timeSimulationCycle = simulationInputData.getTimeSimulationCycle();
        Double timeSimulationLimit = simulationInputData.getTimeSimulationTotal();
        
        //----------------------------------------------------------------------
        
        splitsCountExpected = (long)Math.ceil(timeSimulationLimit/timeSimulationCycle);

        //----------------------------------------------------------------------
        
        IgniteCluster cluster = GridIgnite.getIgnite().cluster();

        ClusterGroup executorGroup = cluster.forServers();
        
        //----------------------------------------------------------------------
        
        EXECUTORS_THREAD_POOL = GridIgnite.getIgnite().executorService(executorGroup);
        
        //----------------------------------------------------------------------

        GridIgnite.resetCaches(threadsNum, simulationInputData.getSimulationConfig().getSimOnDB());
        
        //----------------------------------------------------------------------
        //START ALL NODES
        //----------------------------------------------------------------------        
        
        SIM_START_THREADS = (double) System.currentTimeMillis();

        //----------------------------------------------------------------------        

        if(ConstantsGrid.CUSTOMIZE_EXPERIMENTAL_THREAD_MANUAL_MANAGE == true) {
            
            ArrayList<IgniteFuture> futures = new ArrayList<IgniteFuture>();

            log.info(ConstantsSimulator.BAR_LOGGER);
                
            for (int threadCache = 0; threadCache < threadsNum; ++threadCache) {

                String cacheName = Naming.getCacheName(threadCache);
                
                ArrayList<String> caches = new ArrayList<String>();

                caches.add(cacheName);

                Affinity<String> affinity = GridIgnite.getIgnite().affinity(cacheName);

                ClusterNode node = affinity.mapPartitionToNode(0);
                
                if(node == null) {
                    throw new BadParametersException("cannot find node owner of partition 0 for cache " + cacheName);
                }
                
                futures.add(GridIgnite.getIgnite().compute().affinityRunAsync(caches, 0, NODES_THREADS.get(threadCache)));
                
                log.info("thread " + threadCache + " will run on node " + node.id() + String.join(", ", node.addresses()));
            }

            log.info(ConstantsSimulator.BAR_LOGGER);
            
            for (int i = 0; i < threadsNum; ++i) {
                futures.get(i).get();
            }
        } else {
            for (int i=0;i < threadsNum;++i) {

                NodeMain nodeMain = NODES_THREADS.get(i);

                EXECUTORS_THREAD_POOL.submit(nodeMain);
            }

            try {
                if(EXECUTORS_THREAD_POOL.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS) == false) {
                    log.error("compute timeout reached");
                }
            } catch (InterruptedException e) {
                log.error("error waiting for nodes compute termination", e);
            } finally {
                EXECUTORS_THREAD_POOL.shutdownNow();
            }
        }
        
        //----------------------------------------------------------------------        

        cliReport();

        System.exit(ConstantsNodeDefaults.ERROR_NONE);

        //----------------------------------------------------------------------        

    }
	
    private void cliReport() {
        
        final Double SIM_POST_TIME = (double) System.currentTimeMillis();

        String report = "";

        long splitMin = 0L;
        
        IgniteCache<String, Object> currCache = GridIgnite.getIgnite().getOrCreateCache(ConstantsGrid.CACHE_KEY_CACHE_NAME_REPORT);

        for(int i=0;i < NODES_THREADS.size();i++) {
            
            Object min = currCache.get(ConstantsGrid.CACHE_KEY_NAME_REPORT_SPLITS_COUNT + i);
            
            if(min == null) min = 0L;
            
            splitMin = Math.min(splitMin, (long)min);
            
            report += "\n" + ConstantsSimulator.BAR_LOGGER;
            report += "\n" + currCache.get(ConstantsGrid.CACHE_KEY_NAME_REPORT_SPLITS_DETAIL + i);
            report += "\n" + ConstantsSimulator.BAR_LOGGER;
            
            currCache.put(ConstantsGrid.CACHE_KEY_NAME_REPORT_SPLITS_DETAIL + i, "");
        }
        
        report += GridIgnite.getClusterInfo();
        
        report += "\n" + ConstantsSimulator.BAR_LOGGER;
        report += "\nCONFIGURATION - TIMING";
        report += "\n" + ConstantsSimulator.BAR_LOGGER;
        report += "\nconfigurated cycle time-------------: " + Strings.format(simulationInputData.getTimeSimulationCycle()) + "ms";
        report += "\nconfigurated simulation time--------: " + Strings.format(simulationInputData.getTimeSimulationTotal()) + "ms";
        report += "\nsplits count expected---------------: " + Strings.format(splitsCountExpected);
        report += "\nsplits count effective--------------: " + Strings.format(splitMin);
        report += "\n" + ConstantsSimulator.BAR_LOGGER;
        report += "\nCONFIGURATION - NETWORK";
        report += "\n" + ConstantsSimulator.BAR_LOGGER;
        report += "\nnodes-------------------------------: " + Strings.format(simulationInputData.getNodesNum());
        report += "\nneurons max-------------------------: " + Strings.format(simulationOutputData.getNeuronsNumberMaxOnSingleNode());
        report += "\nneurons sum on all nodes------------: " + Strings.format(simulationOutputData.getNeuronsNumberSumOnAllNodes());
        report += "\nexcitatory neurons------------------: " + Strings.format(simulationOutputData.getNeuronsNumberExcitatory());
        report += "\ninhibithory neurons-----------------: " + Strings.format(simulationOutputData.getNeuronsNumberInhibithory());
        report += "\ntotal internode connections---------: " + Strings.format(simulationInputData.getInternodeConnections().size());
        report += "\ntotal internode synapses------------: " + Strings.format(simulationOutputData.getInternodeSynapseNumber());
        report += "\navg neuronal signal speed-----------: " + Strings.format(simulationInputData.getXmlConfig().getAvg_neuronal_signal_speed());
        report += "\n" + ConstantsSimulator.BAR_LOGGER;
        report += "\nTIMING RESULTS - SETUP";
        report += "\n" + ConstantsSimulator.BAR_LOGGER;
        report += "\nconfig file read--------------------: " + Strings.format(simulationOutputData.getMsInitConfigFileRead())  + "ms";
        report += "\nwiring network----------------------: " + Strings.format(simulationOutputData.getMsInitNetworkBuilding()) + "ms";
        report += "\nsimulation init time----------------: " + Strings.format(simulationOutputData.getMsInitSimulation())      + "ms";
        report += "\ntotal time--------------------------: " + Strings.format(simulationOutputData.getMsInitTotalTime())       + "ms";
        report += "\n" + ConstantsSimulator.BAR_LOGGER;
        report += "\nTIMING RESULTS - SIMULATION";
        report += "\n" + ConstantsSimulator.BAR_LOGGER;
        report += "\ntotal running nodes in cluster------: " + GridIgnite.getIgnite().cluster().forServers().nodes().size();
        report += "\ntotal running time------------------: " + Strings.format((SIM_POST_TIME - SIM_START_ABSOLUTE)) + "ms";
        report += "\ntotal thread time-------------------: " + Strings.format((SIM_POST_TIME - SIM_START_THREADS))  + "ms";
        report += "\n" + ConstantsSimulator.BAR_LOGGER;

        log.info(report);
        
        putReport(report);
    }
    
    private void putReport(String report) {
        
        PrintWriter pw = null;
        
        int serverNodes = GridIgnite.getIgnite().cluster().forServers().nodes().size();
        
        try {
            pw = Files.getFile(
            simulationInputData.getSimulationConfig().getProjectFullPath(), 
            simulationInputData.getSimulationConfig().getStartTime(), 
            serverNodes, 
            "report_nodes");
        
            pw.write(report);

            pw.close();
        } catch(IOException reason) {
            log.error("error saving report", reason);
        } finally {
            if(pw != null) {
                pw.close();
            }
        }
    }
}
