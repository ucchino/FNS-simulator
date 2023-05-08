/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.spiking.node.controller;

import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import it.ppu.utils.tools.Rand;
import it.ppu.consts.ConstantsNodeDefaults;
import it.ppu.utils.node.NiceNode;
import it.ppu.utils.node.NiceQueue;

import java.io.Serializable;

@Slf4j
class NodeNeuronsManager implements Serializable {

    //TODOPPU DEV SPOSTARE TUTTO IN NODEMAIN.. accentriamo...
    
    private static final long serialVersionUID = 3248671994878955014L;
    
    private final NodeNetwork NODE_NETWORK;
    
    //----------------------------------------------------------------------
    //spiking threshold
    //----------------------------------------------------------------------
    private final Double spikingThreshold;
    
    //----------------------------------------------------------------------
    //the list of active neurons - a map ordered on time 
    //values (as key) mapping neuronsId
    //----------------------------------------------------------------------
    private final NiceQueue ACTIVE_NEURONS;
    
    //----------------------------------------------------------------------
    //the neuron states for the node
    //----------------------------------------------------------------------
    private HashMap<Long, Double> neuronStates;
    
    private HashMap<Long, Double> timesToFire;
    private HashMap<Long, Double> timesLastFiring;
    private HashMap<Long, Double> timesLastBurning;
    
    private HashMap<Long, Double> presynapticWeight;
    
    private ExponentialDistribution exponentialDistro;

    public NodeNeuronsManager(NodeNetwork NODE_NETWORK) {
        this.NODE_NETWORK = NODE_NETWORK;
        
        this.spikingThreshold = NODE_NETWORK.getC_threshold() + 1;
        
        ACTIVE_NEURONS = new NiceQueue("activeNeuronsNodeId-" + NODE_NETWORK.getNodeId());
        
        init();
    }

    private void init() {
        
        neuronStates      = new HashMap<Long, Double>();
        timesToFire       = new HashMap<Long, Double>();
        timesLastFiring   = new HashMap<Long, Double>();
        timesLastBurning  = new HashMap<Long, Double>();
        presynapticWeight = new HashMap<Long, Double>();
        
        exponentialDistro = new ExponentialDistribution(NODE_NETWORK.getExternalInputTimeStepBetweenInputEventsSameSource());
    }

    public Double getSpikingThreshold() {
        return spikingThreshold;
    }

    public Double getLinearDecayD(Long neuronId) {
        return (NODE_NETWORK.isNeuronExcitatory(neuronId)) ? NODE_NETWORK.getDExc_linearDecayForEachNeuron() : NODE_NETWORK.getDInh_underthresholdDecayForEachNeuron();
    }

    public void setTimeToFire(Long neuronId, Double val) {
        timesToFire.put(neuronId, val);
    }

    public Double getTimeToFire(Long neuronId) {
        Double retval = timesToFire.get(neuronId);
        
        if (retval == null) {
            if (neuronId < NODE_NETWORK.getNeuronsNumber()) {
                return ConstantsNodeDefaults.NODETHREAD_FIRING_TIME;
            }
            return ConstantsNodeDefaults.NODETHREAD_EXTERNAL_INPUTS_FIRING_TIME;
        }
        
        return retval;
    }

    public void setPreSynapticWeight(Long neuronId, Double val) {
        presynapticWeight.put(neuronId, val);
    }

    public Double getPreSynapticWeight(Long neuronId) {
        Double retval = presynapticWeight.get(neuronId);
        
        if (retval == null) {
            retval = (isExcitatory(neuronId)) ? NODE_NETWORK.getWPre_presynapticWeightExcitatory() : NODE_NETWORK.getWPre_presynapticWeightInhibitory();
        }
        
        return retval;
    }

    public Double getState(Long neuronId) {
        Double retval = neuronStates.get(neuronId);
        
        if (retval == null) {
            Double lastBurningTime = timesLastBurning.get(neuronId);
            if (lastBurningTime == null) {
                neuronStates.put(neuronId, Rand.getDouble(NODE_NETWORK.isSimNoRand()));
                retval = neuronStates.get(neuronId);
            } else {
                retval = 0.0;
            }
        }
        
        return retval;
    }

    public void setState(Long neuronId, Double val) {
        neuronStates.put(neuronId, val);
    }

    public void resetState(Long neuronId) {
        neuronStates.put(neuronId, 0.0);
    }

    public void resetTimeToFire(Long neuronId) {
        timesToFire.remove(neuronId);
    }

    public void setLastFiringTime(Long neuronId, Double val) {
        timesLastFiring.put(neuronId, val);
    }

    public Double getLastFiringTime(Long neuronId) {
        Double retval = timesLastFiring.get(neuronId);
        
        if (retval == null) {
            retval = ConstantsNodeDefaults.NODETHREAD_FIRING_TIME;
        }
        
        return retval;
    }

    public NiceNode getNextFiringNeuron() {
        return ACTIVE_NEURONS.extractMin();
    }

    /**
     * @return the minimum time to fire for active neurons, without polling the
     * value from the queue If the queue is empty, null is returned
     */
    public Double getMinFiringTime() {
        return ACTIVE_NEURONS.getMinTime();
    }

    public boolean isExcitatory(Long neuronId) {
        return NODE_NETWORK.isNeuronExcitatory(neuronId);
    }

    public Double getLastBurningTime(Long neuronId) {
        Double retval = timesLastBurning.get(neuronId);
        
        if (retval == null) {
            retval = 0.0;
        }
        
        return retval;
    }

    public void setLastBurningTime(Long neuronId, Double lastBurningTime) {
        this.timesLastBurning.put(neuronId, lastBurningTime);
    }

    /**
     * @param externalNeuronId the id of the external input as referred into the node
     * reg
     * @param currentTime the current simulation time
     */
    public void externalInputReset(Long externalNeuronId, double currentTime) {
        //TODOPPU SDEV SPOSTARE MAGARI IN NODEMAIN
        
        if (currentTime > NODE_NETWORK.getExternalInputsFireDuration()) {
            removeActiveNeuron(externalNeuronId);
            return;
        }
        
        Double externalInputTimeFire   = 0.0;
        String externalInputDistroName = "UNKNOWN";
        
        //----------------------------------------------------------------------
        //case of constant external inputs
        //----------------------------------------------------------------------
        if (NODE_NETWORK.getExternalInputsType() == ConstantsNodeDefaults.EXTERNAL_DISTRO_CONSTANT) {
            //----------------------------------------------------------------------
            externalInputDistroName = "CONSTANT";
            //----------------------------------------------------------------------
            if (currentTime == 0.0) {
                externalInputTimeFire = NODE_NETWORK.getExternalInputsTimeOffset(externalNeuronId);
            } else {
                externalInputTimeFire = currentTime + NODE_NETWORK.getExternalInputTimeStepBetweenInputEventsSameSource();
            }
        } else if (NODE_NETWORK.getExternalInputsType() == ConstantsNodeDefaults.EXTERNAL_DISTRO_NOISE) {
            //----------------------------------------------------------------------
            externalInputDistroName = "NOISE";
            //----------------------------------------------------------------------
            //NOISE <-> UNIFORMLY DISTRIBUTED
            //----------------------------------------------------------------------
            if (currentTime == 0.0) {
                externalInputTimeFire = NODE_NETWORK.getExternalInputsTimeOffset(externalNeuronId) + Rand.getDouble(NODE_NETWORK.isSimNoRand()) * 2 * NODE_NETWORK.getExternalInputTimeStepBetweenInputEventsSameSource();
            } else {
                externalInputTimeFire = currentTime                                        + Rand.getDouble(NODE_NETWORK.isSimNoRand()) * 2 * NODE_NETWORK.getExternalInputTimeStepBetweenInputEventsSameSource();
            }
        } else if (NODE_NETWORK.getExternalInputsType() == ConstantsNodeDefaults.EXTERNAL_DISTRO_POISSON) {
            //----------------------------------------------------------------------
            externalInputDistroName = "POISSON";
            //----------------------------------------------------------------------
            //case of POISSON <->> EXPONENTIAL external inputs
            //----------------------------------------------------------------------
            externalInputTimeFire = currentTime + Rand.getExponential(NODE_NETWORK.isSimNoRand(), exponentialDistro);
            //----------------------------------------------------------------------
        } else {
            log.info("distribution " + externalInputDistroName + " - available only are: costant,noise,poisson");
            System.exit(ConstantsNodeDefaults.ERROR_BASE);
        }
            
        //----------------------------------------------------------------------
        //Upon event an external sinaptic weigth is always the same?
        //NODE.getAmplitudeValue(externalNeuronId) never changes during simulation 
        //TODOPPU APPROFONDIRE 4 - ask to Susi
        //----------------------------------------------------------------------
        setPreSynapticWeight(externalNeuronId, NODE_NETWORK.getAmplitudeValue(externalNeuronId));
        
        setTimeToFire(externalNeuronId, externalInputTimeFire);
        
        addActiveNeuron(externalNeuronId, externalInputTimeFire, currentTime, "external input set to->" + externalInputDistroName);
    }

    public void addActiveNeuron(Long neuronId, Double fireTime, Double currentTime, String location) {
        //TODOPPU DEV SPOSTARE IN NODEMAIN
        if (fireTime < currentTime) {
            println(""
                + "\n...................."
                + "\ndebug location " + location
                + "\n...................."
                + "\nfire time: " + fireTime + " current time: " + currentTime);
            System.exit(ConstantsNodeDefaults.ERROR_BASE);
        }
        
        ACTIVE_NEURONS.insert(fireTime, neuronId);
    }

    public void removeActiveNeuron(Long neuronId) {
        ACTIVE_NEURONS.delete(neuronId);
    }

    public int getActiveNeuronsNum() {
        return ACTIVE_NEURONS.size();
    }

    private void println(String s) {
        log.info("[id" + NODE_NETWORK.getNodeId() + "] " + s);
    }

    public void printActiveNeurons() {
        println("printing active neurons: ");
        NiceNode activeNeurons[] = ACTIVE_NEURONS.toArray();
        for (int i = 0; i < activeNeurons.length; ++i) {
            println("active neuron id" + i + ": " + activeNeurons[i].toString() + ", state: " + getState(activeNeurons[i].firingNeuronId));
        }
    }
}
