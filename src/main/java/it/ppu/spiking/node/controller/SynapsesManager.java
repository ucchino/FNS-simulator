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

import it.ppu.spiking.node.model.SynapseModel;
import it.ppu.utils.node.LongCouple;
import it.ppu.utils.tools.Rand;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

/**
 * This manages all the region synapses data set. This implies both the
 * axons delays and the postsynaptic weights
 *
 * @author paracone
 *
 */
@Slf4j
class SynapsesManager implements Serializable {

    private static final long serialVersionUID = 3248671994878955015L;
    
    private final NodeNetwork NODE_NETWORK;

    //----------------------------------------------------------------------
    //the list of axonal delays, only for inter region connections
    //----------------------------------------------------------------------
    private HashMap<SynapseModel, Double> axonalDelaysMap;
    
    //----------------------------------------------------------------------
    //the list of all synapses with the axons belonging
    //to a neuron of the region
    //----------------------------------------------------------------------
    private final HashMap<SynapseModel, SynapseModel> synapses;
    
    //----------------------------------------------------------------------
    //the lists of connections, indexed by firing neuron
    //----------------------------------------------------------------------
    private final HashMap<Long, ArrayList<SynapseModel>> firingNeuronSynapses;
    
    //----------------------------------------------------------------------
    //the lists of connections, indexed by firing neuron
    //extendible
    //----------------------------------------------------------------------
    private HashMap<Long, ArrayList<SynapseModel>> burningNeuronSynapses;
    
    //----------------------------------------------------------------------
    //the lists of inter region connections, indexed by firing neuron
    //extendible
    //----------------------------------------------------------------------
    private final HashMap<Long, ArrayList<SynapseModel>> firingNeuronInternodeSynapses;
    
    //----------------------------------------------------------------------
    //the lists of inter region connections, indexed by firing neuron
    //extendible
    //----------------------------------------------------------------------
    private final HashMap<Long, ArrayList<SynapseModel>> burningNeuronInternodeConnections;
    
    private Double minAxonalDelay = Double.MAX_VALUE;
    
    private final Double avgNeuronalSignalSpeed;
    
    public SynapsesManager(NodeNetwork NODE_NETWORK, Double avgNeuronalSignalSpeed) {
        this.NODE_NETWORK = NODE_NETWORK;

        axonalDelaysMap = new HashMap<SynapseModel, Double>();
        synapses        = new HashMap<SynapseModel, SynapseModel>();

        firingNeuronSynapses = new HashMap<Long, ArrayList<SynapseModel>>();

        if(NODE_NETWORK.getNeuronPlasticity()) {
            burningNeuronSynapses = new HashMap<Long, ArrayList<SynapseModel>>();
        }
        
        firingNeuronInternodeSynapses     = new HashMap<Long, ArrayList<SynapseModel>>();
        burningNeuronInternodeConnections = new HashMap<Long, ArrayList<SynapseModel>>();

        this.avgNeuronalSignalSpeed = avgNeuronalSignalSpeed;
        
        init();
    }

    private void init() {
        Iterator<LongCouple> iterator = NODE_NETWORK.getConnectionsKeyIterator();
        
        Double tmp_presynaptic_w = null;
        
        //----------------------------------------------------------------------
        //SETTING INTRA-NODE SYNAPSES
        //----------------------------------------------------------------------
        while (iterator.hasNext()) {
            LongCouple tmpCouple = iterator.next();
            
            tmp_presynaptic_w = NODE_NETWORK.getConnectionPresynapticWeight(tmpCouple.getSrc(), tmpCouple.getDst());

            if (tmp_presynaptic_w == null) {
                continue;
            }

            double postSynW
                = Math.abs(
                    Rand.getNextGaussian(NODE_NETWORK.isSimNoRand())
                    * NODE_NETWORK.getSigma_w_agnostic(tmpCouple.getDst())
                    + NODE_NETWORK.getMu_w_agnostic(tmpCouple.getDst()));

            if (NODE_NETWORK.getMu_w_agnostic(tmpCouple.getDst()) < 0) {
                postSynW = -postSynW;
            }

            setIntraNodeSynapse(new SynapseModel(
                NODE_NETWORK.getNodeId(),
                tmpCouple.getSrc(),
                NODE_NETWORK.getNodeId(),
                tmpCouple.getDst(),
                0.0,
                postSynW,
                tmp_presynaptic_w));
        }
    }

    //----------------------------------------------------------------------
    //SYNAPSES
    //----------------------------------------------------------------------
    public void setIntraNodeSynapse(SynapseModel syn) {
        if (syn.getAxonFiringNodeId().equals(NODE_NETWORK.getNodeId())) {
            putFiringIntraNodeSynapse(syn);
        }
        if (syn.getBurningNodeId().equals(NODE_NETWORK.getNodeId())) {
            putBurningIntraNodeSynapse(syn);
        }
        if ((syn.getAxonFiringNodeId().equals(NODE_NETWORK.getNodeId())) && (syn.getBurningNodeId().equals(NODE_NETWORK.getNodeId()))) {
            synapses.put(syn, syn);
        } else {
            println("POSTSYNAPTICWEIGHT WARNING - adding an internode synapse as intranode");
        }
    }

    public void setIntraNodePostSynapticWeight(SynapseModel syn, Double postsynaptic_w) {
        SynapseModel s = synapses.get(syn);
        if (s != null) {
            s.setPostsynapticWeight(postsynaptic_w);
        }
    }

    private void putFiringIntraNodeSynapse(SynapseModel firingNeuronSynapse) {
        if (firingNeuronSynapses.size() >= Integer.MAX_VALUE) {
            throw new ArrayIndexOutOfBoundsException("You are triyng to add to much internode "
                + "connection to the same neuron: " + firingNeuronSynapse.getAxonFiringNeuronId() + " of "
                + "the node: " + NODE_NETWORK.getNodeId());
        }

        ArrayList<SynapseModel> list = firingNeuronSynapses.get(firingNeuronSynapse.getAxonFiringNeuronId());
        
        if (list == null) {
            firingNeuronSynapses.put(firingNeuronSynapse.getAxonFiringNeuronId(),new ArrayList<SynapseModel>());
            list = firingNeuronSynapses.get(firingNeuronSynapse.getAxonFiringNeuronId());
        }
        list.add(firingNeuronSynapse);
    }

    private void putBurningIntraNodeSynapse(SynapseModel burningNeuronSynapse) {
        if (!NODE_NETWORK.getNeuronPlasticity()) {
            return;
        }
        if (burningNeuronSynapses.size() >= Integer.MAX_VALUE) {
            throw new ArrayIndexOutOfBoundsException("You are triyng to add to much internode "
                + "connection to the same neuron: " + burningNeuronSynapse.getBurningNeuronId() + " of "
                + "the region: " + NODE_NETWORK.getNodeId());
        }
        
        ArrayList<SynapseModel> list = burningNeuronSynapses.get(burningNeuronSynapse.getBurningNeuronId());
        
        if (list == null) {
            burningNeuronSynapses.put(burningNeuronSynapse.getBurningNeuronId(), new ArrayList<SynapseModel>());
            
            list = burningNeuronSynapses.get(burningNeuronSynapse.getBurningNeuronId());
        }
        list.add(burningNeuronSynapse);
    }

    /**
     * @return the list for the specified neuron; if no such list exists, it
     * creates a new htree map and returns its pointer
     */
    public ArrayList<SynapseModel> getFiringNeuronSynapses(Long firingNeuronId) {
        ArrayList<SynapseModel> retval = firingNeuronSynapses.get(firingNeuronId);
        
        if (retval == null) {
            firingNeuronSynapses.put(firingNeuronId, new ArrayList<SynapseModel>());
            retval = firingNeuronSynapses.get(firingNeuronId);
        }
        
        return retval;
    }

    /**
     * @return the list for the specified neuron; if no such list exists, it
     * creates a new htree map and returns its pointer
     */
    public ArrayList<SynapseModel> getBurningNeuronSynapses(Long burningNeuronId) {
        ArrayList<SynapseModel> retval = burningNeuronSynapses.get(burningNeuronId);
        
        if (retval == null) {
            burningNeuronSynapses.put(burningNeuronId, new ArrayList<SynapseModel>());
            retval = burningNeuronSynapses.get(burningNeuronId);
        }
        
        return retval;
    }

    public void addInternodeSynapse(
        Integer firingNodeId,  Long firingNeuronId,
        Integer burningNodeId, Long burningNeuronId,
        
        Double postsynapticWeight, 
        Double presynapticWeight, 
        
        Double averageLengthInternodeAxonLambda) {
        
        SynapseModel newSynapse = new SynapseModel(
            firingNodeId,
            firingNeuronId,
            burningNodeId,
            burningNeuronId,
            averageLengthInternodeAxonLambda,
            postsynapticWeight,
            presynapticWeight);
        
        //----------------------------------------------------------------------
        //setInternodeSynapse
        //----------------------------------------------------------------------
        SynapseModel currSynapse = synapses.get(newSynapse);

        //----------------------------------------------------------------------
        //idempotency rule
        //----------------------------------------------------------------------
        if (currSynapse == null) {
            synapses.put(newSynapse, newSynapse);
        }        
        
        if (firingNodeId.equals(NODE_NETWORK.getNodeId())) {
            putFiringNeuronInternodeConnection(firingNeuronId, newSynapse);
        } else if (burningNodeId.equals(NODE_NETWORK.getNodeId())) {
            putBurningNeuronInternodeConnection(burningNeuronId, newSynapse);
        } else {
            println("WARNING - adding an internode synapse which does "
                + "not belong to the current node: "
                + "\n\tsynapse: " + newSynapse.toString()
                + "\n\tcurrent region: " + NODE_NETWORK.getNodeId());
        }
    }

    public int interNodeConnectionsNum() {
        return axonalDelaysMap.size();
    }

    private void putFiringNeuronInternodeConnection(Long firingNeuronId, SynapseModel neuronNodeConnection) {
        if (firingNeuronInternodeSynapses.size() >= Integer.MAX_VALUE) {
            throw new ArrayIndexOutOfBoundsException("You are triyng to add too much interregion "
                + "connections to the same neuron: " + firingNeuronId + " of "
                + "the region: " + NODE_NETWORK.getNodeId());
        }
        
        ArrayList<SynapseModel> list = firingNeuronInternodeSynapses.get(firingNeuronId);
        
        if (list == null) {
            firingNeuronInternodeSynapses.put(firingNeuronId, new ArrayList<SynapseModel>());
            list = firingNeuronInternodeSynapses.get(firingNeuronId);
        }
        list.add(neuronNodeConnection);
        firingNeuronInternodeSynapses.put(firingNeuronId, list);
    }

    /**
     * @return the list for the specified neuron; if no such list exists, it
     * creates a new htree map and returns its pointer
     */
    public ArrayList<SynapseModel> getFiringNeuronInternodesSynapses(Long firingNeuronId) {
        ArrayList<SynapseModel> retval = firingNeuronInternodeSynapses.get(firingNeuronId);
        if (retval == null) {
            firingNeuronInternodeSynapses.put(firingNeuronId, new ArrayList<SynapseModel>());
            retval = firingNeuronInternodeSynapses.get(firingNeuronId);
        }
        return retval;
    }

    private void putBurningNeuronInternodeConnection(Long burningNeuronId, SynapseModel neuronNodeConnection) {
        if (burningNeuronInternodeConnections.size() >= Integer.MAX_VALUE) {
            throw new ArrayIndexOutOfBoundsException("You are triyng to add to much interregion "
                + "connection to the same neuron: " + burningNeuronId + " of "
                + "the region: " + NODE_NETWORK.getNodeId());
        }
        
        ArrayList<SynapseModel> list = burningNeuronInternodeConnections.get(burningNeuronId);
        
        if (list == null) {
            burningNeuronInternodeConnections.put(burningNeuronId, new ArrayList<SynapseModel>());
            list = burningNeuronInternodeConnections.get(burningNeuronId);
        }
        list.add(neuronNodeConnection);
    }

    /**
     * @return the htreemap list for the specified neuron; if no such list
     * exists, it creates a new htree map and returns its pointer
     */
    public ArrayList<SynapseModel> getBurningNeuronInternodeConnections(Long burningNeuronId) {
        ArrayList<SynapseModel> retval = burningNeuronInternodeConnections.get(burningNeuronId);
        if (retval == null) {
            burningNeuronInternodeConnections.put(burningNeuronId, new ArrayList<SynapseModel>());
            retval = burningNeuronInternodeConnections.get(burningNeuronId);
        }
        return retval;
    }

    public Double getAxonalDelay(SynapseModel syn) {
        Double delta = (syn.getAverageLengthInternodeAxonLambda()) / (avgNeuronalSignalSpeed);
        
        if (delta < minAxonalDelay) {
            minAxonalDelay = delta;
        }
        
        return delta;
    }

    public Iterator<Long> getNeuronIntermoduleConnectionIterator() {
        return firingNeuronInternodeSynapses.keySet().iterator();
    }

    //----------------------------------------------------------------------
    
    private void printAxionMap() {
        println(" printing axion map");
        Iterator<SynapseModel> it = axonalDelaysMap.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            SynapseModel k = it.next();
            println((i++) + ". " + k.toString() + " - " + axonalDelaysMap.get(k));
        }
    }

    private void println(String s) {
        if (NODE_NETWORK != null) {
            log.info("id" + NODE_NETWORK.getNodeId() + "] " + s);
        } else {
            log.info("id" + NODE_NETWORK.getNodeId() + "/-]" + s);
        }
    }
}
