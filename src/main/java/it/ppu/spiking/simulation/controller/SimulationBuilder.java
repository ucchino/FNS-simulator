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
import it.ppu.spiking.node.controller.NodeMain;
import it.ppu.spiking.simulation.model.SimulationConfigModel;
import it.ppu.spiking.simulation.model.SimulationOutputDataModel;
import it.ppu.spiking.simulation.model.SimulationInputDataModel;
import it.ppu.spiking.node.model.internode.InternodeConnectionModel;
import it.ppu.spiking.node.model.NodeModel;
import it.ppu.utils.exceptions.BadCurveException;
import it.ppu.utils.exceptions.BadParametersException;
import it.ppu.utils.grid.GridIgnite;
import it.ppu.utils.node.Naming;
import it.ppu.utils.tools.Rand;

import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.math3.distribution.GammaDistribution;

@Slf4j
public class SimulationBuilder implements Serializable {

    private static final long serialVersionUID = 324867199487895580L;
    
    //----------------------------------------------------------------------
    
    private final SimulationConfigModel simulationConfig;
    
    private final SimulationOutputDataModel simulationOutputData = new SimulationOutputDataModel();
    
    private final SimulationMain simulationMain = new SimulationMain();

    //----------------------------------------------------------------------
    
    private Double timeBuildStart = 0.0;
    
    private Double averageNeuronalSignalSpeed = ConstantsNodeDefaults.XML_AVG_NEURONAL_SPEED;
    
    private Long internodeSynapseNumber = 0l;
    
    private final ArrayList<NodeMain> nodeThreadsList = new ArrayList<NodeMain>();
    
    //----------------------------------------------------------------------
    //SIMULATION BUILD LOCAL DATA - the total number of external inputs
    //----------------------------------------------------------------------
    
    private Double minTractLength = ConstantsNodeDefaults.SIM_MAX_TRACT_LENGTH;

    //----------------------------------------------------------------------
    //SIMULATION BUILD LOCAL DATA - setting default to 1 means every x
    //----------------------------------------------------------------------
    
    private Double gammaInverseCumulativeProbX = ConstantsNodeDefaults.SIM_GAMMA_INVERSE_CYUMULATIVE_PROB_X;

    //----------------------------------------------------------------------

    public SimulationBuilder(SimulationConfigModel simulationConfig) throws BadParametersException, IOException {
        
        this.simulationConfig = simulationConfig;

        if(simulationConfig == null) {
            return;
        }
                
        //----------------------------------------------------------------------

        SimulationInputDataModel simulationInputData = SimulationInputDataModel.builder(simulationConfig);

        //----------------------------------------------------------------------
        
        GridIgnite.boot(
            0,
            simulationInputData.getNodesNum(),
            ConstantsGrid.NODE_MAIN,
            simulationConfig.getAws(),
            simulationConfig.getSimOnDB(),
            false);

        //----------------------------------------------------------------------

        Double timeSimulationCycle = initNetwork(simulationInputData);
        
        //----------------------------------------------------------------------   
        
        simulationInputData.setTimeSimulationCycle(timeSimulationCycle);
        
        if(simulationInputData.getNodesNum() > ConstantsGrid.CUSTOMIZE_POOL_SIZE_MAX_NODES) {
            throw new BadParametersException(""
                + "Found " + simulationInputData.getNodesNum() + " nodes, pool size is " + ConstantsGrid.CUSTOMIZE_POOL_SIZE_MAX_NODES + ": "
                + "INCREASE POOL SIZE OR RUN LESS NODES"
            );
        }
        
        simulationOutputData.setInternodeSynapseNumber(internodeSynapseNumber);
        
        simulationMain.setSimulationInputData(simulationInputData);
        simulationMain.setSimulationOutputData(simulationOutputData);
        simulationMain.setSimulationProcesses(nodeThreadsList);
    }
    
    public SimulationMain getSimulationMain() {
        return simulationMain;
    }
    
    private Double getMinTractLength() {
        return (gammaInverseCumulativeProbX.equals(ConstantsNodeDefaults.SIM_GAMMA_INVERSE_CYUMULATIVE_PROB_X)) ? minTractLength : gammaInverseCumulativeProbX;
    }

    private Double initNetwork(SimulationInputDataModel simulationInputData) throws BadParametersException, IOException {

        log.info("initializing simulation");

        timeBuildStart = (double)System.currentTimeMillis();
        
        averageNeuronalSignalSpeed = simulationInputData.getXmlConfig().getAvg_neuronal_signal_speed();

        Double diffTime = 0.0;
        Double lastTime = timeBuildStart;

        Double timeSimulationCycle = 0.0;
        
        //----------------------------------------------------------------------
        
        diffTime = System.currentTimeMillis() - lastTime;
        simulationOutputData.setMsInitConfigFileRead(diffTime);
        lastTime += diffTime;

        //----------------------------------------------------------------------
        
        log.info("creating and adding nodes");

        //TODOPPU DEV Il popolamento del modello viene fatto qui ma lo sposterei dopo la creazione 
        //del thread... per serializzare meno oggetti grandi nel caso di tanti neuroni
        
        Integer nodesTotal = simulationInputData.getNodesNum();

        for (int currNodeId = 0; currNodeId < nodesTotal; ++currNodeId) {
            
            NodeModel nodeModel = NodeModel.builder(currNodeId, simulationInputData);
            
            addNodeThread(new NodeMain(nodeModel));
            
            //TODOPPU DEV ^^^^^^^^^^^^^^^qui vengono allocati i neuroni...
        }

        //----------------------------------------------------------------------
        
        diffTime = System.currentTimeMillis() - lastTime;
        simulationOutputData.setMsInitNetworkBuilding(diffTime);
        lastTime += diffTime;

        //----------------------------------------------------------------------
        
        log.info("read and build internode connections / wiring");

        ArrayList<InternodeConnectionModel> internodeConnections = simulationInputData.getInternodeConnections();

        for (int i = 0; i < internodeConnections.size(); ++i) {

            InternodeConnectionModel interconnection = internodeConnections.get(i);

            log.info("adding nodes interconnection src/dst: " + interconnection.getSrc() + "-" + interconnection.getDst());

            //----------------------------------------------------------------------
            //Logic use. Just to test if always is:
            //NODE_THREADS.get(intercon.src/dst).nodeId == interconnection.src/dst
            //----------------------------------------------------------------------
            if ((nodeThreadsList.get(interconnection.getSrc()).getNodeId() != interconnection.getSrc())
                || 
                (nodeThreadsList.get(interconnection.getDst()).getNodeId() != interconnection.getDst())) {
                log.info("bad interconnection id");
                System.exit(ConstantsNodeDefaults.ERROR_BASE);
            }

            try {
                initWireInternodeConnection(
                    simulationConfig.getSimNoRandom(),
                    nodeThreadsList.get(interconnection.getSrc()).getNodeId(),
                    nodeThreadsList.get(interconnection.getDst()).getNodeId(),
                    interconnection);
            } catch (BadCurveException e) {
                log.error("bad curve", e);
            }
        }

        //----------------------------------------------------------------------
        
        if (nodeThreadsList.size() > 0) {

            String minTractLengthStr = (getMinTractLength() != ConstantsNodeDefaults.SIM_MAX_TRACT_LENGTH) ? ("" + getMinTractLength()) : " there are no connections between nodes";

            log.info("min tract length: " + minTractLengthStr);
            log.info("average neuronal signal speed: " + averageNeuronalSignalSpeed);

            timeSimulationCycle = (getMinTractLength() + ConstantsNodeDefaults.SIM_EPSILON_7) / (averageNeuronalSignalSpeed * ConstantsNodeDefaults.SIM_BOP_TO_CYCLE_FACTOR);
        }

        //----------------------------------------------------------------------
        
        for (int i = 0; i < nodeThreadsList.size(); i++) {

            Long neuronSingleNode   = simulationOutputData.getNeuronsNumberMaxOnSingleNode();
            Long neuronsSumAllNodes = simulationOutputData.getNeuronsNumberSumOnAllNodes();

            nodeThreadsList.get(i).setNetworkNeuronsNumbers(neuronSingleNode, neuronsSumAllNodes);
        }

        //----------------------------------------------------------------------
        
        diffTime = System.currentTimeMillis() - lastTime;
        
        simulationOutputData.setMsInitSimulation(diffTime);

        //----------------------------------------------------------------------
        
        log.info("init complete");

        //----------------------------------------------------------------------
        
        return timeSimulationCycle;
        
        //----------------------------------------------------------------------
    }
    
    /**
     * Creates a new inter-node connection Each connection is to be intended as
     * a couple of boundle of synapsis between two nodes, one per each direction
     * adds an inter node connection using the weight as the number of
     * connections between neurons of the two nodes
     */
    private void initWireInternodeConnection(Boolean simNoRandom, Integer nodeId1Src, Integer nodeId2Dst, InternodeConnectionModel internodeConnection) throws BadCurveException {

        if (internodeConnection.getMuLambda_TractLength() == null) {
            log.info("length is null");
            System.exit(ConstantsNodeDefaults.ERROR_BASE);
        }

        if (minTractLength == null) {
            log.info("min tract length is null");
            System.exit(ConstantsNodeDefaults.ERROR_BASE);
        }

        if (internodeConnection.getMuLambda_TractLength() < minTractLength) {
            minTractLength = internodeConnection.getMuLambda_TractLength();
        }

        /*
        * The schema for internode connections
        * 
        *      \     |       |       |       |
        *       \ to | mixed |  exc  |  inh  |
        *  from  \   |       |       |       |
        * ------------------------------------
        *    mixed   |   0   |   1   |   2   |
        * ------------------------------------
        *     exc    |   3   |   4   |   5   |
        * ------------------------------------
        *     inh    |   6   |   7   |   8   |
        * ------------------------------------
        *  
         */
        
        long Nsrc = 0;
        //----------------------------------------------------------------------
        //case EXC2*
        //----------------------------------------------------------------------
        if (
            (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.EXC2MIXED)
            || 
            (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.EXC2EXC)
            || 
            (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.EXC2INH)) {
            Nsrc = nodeThreadsList.get(nodeId1Src).getNeuronsNumberExcitatory();
        }
        //----------------------------------------------------------------------
        //case INH2*
        //----------------------------------------------------------------------
        else if (
            (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.INH2MIXED)
            || 
            (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.INH2EXC)
            || 
            (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.INH2INH)) {
            Nsrc = nodeThreadsList.get(nodeId1Src).getNeuronsNumberInhibithory();
        }
        //----------------------------------------------------------------------
        //case MIXED2*
        //----------------------------------------------------------------------
        else {
            Nsrc = nodeThreadsList.get(nodeId1Src).getNeuronsNumber();
        }

        long interNodeSynapsesConnectionsToLoop = (long) (Nsrc * internodeConnection.getNeXnRatio_Weight());

        Long neuronsNumberSrc, neuronsNumberDst;

        GammaDistribution gammaDistro = (internodeConnection.getAlphaLambda_TractLengthsShapeParameter() != null) ? new GammaDistribution(internodeConnection.getAlphaLambda_TractLengthsShapeParameter(), internodeConnection.getMuLambda_TractLength() / internodeConnection.getAlphaLambda_TractLengthsShapeParameter()) : null;

        if (simNoRandom) {
            gammaInverseCumulativeProbX = ((internodeConnection.getAlphaLambda_TractLengthsShapeParameter() != null) && (ConstantsNodeDefaults.SIM_BOP_CONSERVATIVE_P != null)) ? ConstantsNodeDefaults.NO_RANDOM : ConstantsNodeDefaults.SIM_GAMMA_INVERSE_CYUMULATIVE_PROB_X;
        } else {
            gammaInverseCumulativeProbX = ((internodeConnection.getAlphaLambda_TractLengthsShapeParameter() != null) && (ConstantsNodeDefaults.SIM_BOP_CONSERVATIVE_P != null)) ? gammaDistro.inverseCumulativeProbability(ConstantsNodeDefaults.SIM_GAMMA_INVERSE_CYUMULATIVE_PROB_X - ConstantsNodeDefaults.SIM_BOP_CONSERVATIVE_P) : ConstantsNodeDefaults.SIM_GAMMA_INVERSE_CYUMULATIVE_PROB_X;
        }

        for (long i = 0; i < interNodeSynapsesConnectionsToLoop; ++i) {

            //----------------------------------------------------------------------
            //case EXC2*
            //----------------------------------------------------------------------
            if (
                (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.EXC2MIXED)
                || 
                (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.EXC2EXC)
                || 
                (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.EXC2INH)) {
                neuronsNumberSrc = (long) (Rand.getDouble(simNoRandom) * nodeThreadsList.get(nodeId1Src).getNeuronsNumberExcitatory());
            }
            //----------------------------------------------------------------------
            //case INH2*
            //----------------------------------------------------------------------
            else if (
                (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.INH2MIXED)
                || 
                (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.INH2EXC)
                || 
                (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.INH2INH)) {
                neuronsNumberSrc = nodeThreadsList.get(nodeId1Src).getNeuronsNumberExcitatory() + ((long) (Rand.getDouble(simNoRandom) * nodeThreadsList.get(nodeId1Src).getNeuronsNumberInhibithory()));
            }
            //----------------------------------------------------------------------
            //case MIXED2*
            //----------------------------------------------------------------------
            else {
                neuronsNumberSrc = (long) (Rand.getDouble(simNoRandom) * nodeThreadsList.get(nodeId1Src).getNeuronsNumber());
            }
            //----------------------------------------------------------------------
            //case *2EXC
            //----------------------------------------------------------------------
            if (
                (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.MIXED2EXC)
                || 
                (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.EXC2EXC)
                || 
                (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.INH2EXC)) {
                neuronsNumberDst = (long) (Rand.getDouble(simNoRandom) * nodeThreadsList.get(nodeId2Dst).getNeuronsNumberExcitatory());
            }
            //----------------------------------------------------------------------
            //case *2INH
            //----------------------------------------------------------------------
            else if (
                (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.MIXED2INH)
                || 
                (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.EXC2INH)
                || 
                (internodeConnection.getInternodeConnectionType() == InternodeConnectionModel.INH2INH)) {
                neuronsNumberDst = nodeThreadsList.get(nodeId2Dst).getNeuronsNumberExcitatory() + ((long) (Rand.getDouble(simNoRandom) * nodeThreadsList.get(nodeId2Dst).getNeuronsNumberInhibithory()));
            }
            //----------------------------------------------------------------------
            //case *2MIXED
            //----------------------------------------------------------------------
            else {
                neuronsNumberDst = (long) (Rand.getDouble(simNoRandom) * nodeThreadsList.get(nodeId2Dst).getNeuronsNumber());
            }
            //----------------------------------------------------------------------
            
            Double postsynapticWeightSinapseMu, averageLengthInternodeAxonLambda = -1.0;

            postsynapticWeightSinapseMu = (internodeConnection.getSigmaW_AmplitudeStdDeviation() != null) ? Math.abs(Rand.getNextGaussian(simNoRandom) * internodeConnection.getSigmaW_AmplitudeStdDeviation() + internodeConnection.getMuOmega_Amplitude()) : internodeConnection.getMuOmega_Amplitude();

            //----------------------------------------------------------------------
            
            if (internodeConnection.getMuOmega_Amplitude() < 0 && postsynapticWeightSinapseMu > 0) {
                postsynapticWeightSinapseMu = -postsynapticWeightSinapseMu;
            }

            //----------------------------------------------------------------------
            
            int goodCurveGuess = 0;

            while ((averageLengthInternodeAxonLambda < 0) && (goodCurveGuess < ConstantsNodeDefaults.SIM_CURVE_GUESS_THRESHOLD_BAD)) {

                averageLengthInternodeAxonLambda = (gammaDistro != null) ? Rand.getGamma(simNoRandom, gammaDistro) : internodeConnection.getMuLambda_TractLength();

                ++goodCurveGuess;
            }

            //----------------------------------------------------------------------
            
            if (goodCurveGuess >= ConstantsNodeDefaults.SIM_CURVE_GUESS_THRESHOLD_GOOD) {
                log.warn("bad curve detected!");

                if (goodCurveGuess >= ConstantsNodeDefaults.SIM_CURVE_GUESS_THRESHOLD_BAD) {
                    throw new BadCurveException("the gamma curve G(" + internodeConnection.getAlphaLambda_TractLengthsShapeParameter() + ", " + (internodeConnection.getAlphaLambda_TractLengthsShapeParameter() / internodeConnection.getMuLambda_TractLength()) + " has a shape which is not compliant with firnet scope");
                }
            }

            //----------------------------------------------------------------------
            
            nodeThreadsList.get(nodeId1Src).addInternodeSynapse(
                nodeId1Src, neuronsNumberSrc,
                nodeId2Dst, neuronsNumberDst,
                
                //nodeThreads.get(nodeId1Src).getExcitatoryPresynapticWeight(),
                
                postsynapticWeightSinapseMu, 
                
                averageLengthInternodeAxonLambda);

            nodeThreadsList.get(nodeId2Dst).addInternodeSynapse(
                //TODOPPU APPROFONDIRE 6b - I nodi sorgente e destinazione non dovrebbero essere invertiti ?
                nodeId1Src, neuronsNumberSrc,
                nodeId2Dst, neuronsNumberDst,
                
                //TODOPPU APPROFONDIRE 6 - non dovrebbe essere nodeid2 -> nodeid1 ? in realta' NON VIENE USATO !!!
                
                //nodeThreads.get(nodeId1Src).getExcitatoryPresynapticWeight(),
                
                postsynapticWeightSinapseMu,
                
                averageLengthInternodeAxonLambda);

            //----------------------------------------------------------------------
            ++internodeSynapseNumber;
            //----------------------------------------------------------------------
        }
    }
    
    private void addNodeThread(NodeMain nodeThread) {
        nodeThreadsList.add(nodeThread);

        simulationOutputData.setNeuronsNumberSumOnAllNodes(simulationOutputData.getNeuronsNumberSumOnAllNodes() + nodeThread.getNeuronsNumber());

        //----------------------------------------------------------------------
        //updating the maximum number of neuron within a same node
        //----------------------------------------------------------------------
        
        if (nodeThread.getNeuronsNumber() > simulationOutputData.getNeuronsNumberMaxOnSingleNode()) {
            simulationOutputData.setNeuronsNumberMaxOnSingleNode(nodeThread.getNeuronsNumber());
        }

        //----------------------------------------------------------------------
        //updating the maximum number of neuron within a same node
        //----------------------------------------------------------------------
        log.info("adding nodeId" + Naming.get(nodeThread.getNodeId()) + ", neurons: " + nodeThread.getNeuronsNumber());

        simulationOutputData.setNeuronsNumberExcitatory(simulationOutputData.getNeuronsNumberExcitatory()   + nodeThread.getNeuronsNumberExcitatory());
        simulationOutputData.setNeuronsNumberInhibithory(simulationOutputData.getNeuronsNumberInhibithory() + nodeThread.getNeuronsNumberInhibithory());
    }
}
