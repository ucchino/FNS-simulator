/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.node;

import java.io.Serializable;

public class NiceNode implements Comparable<NiceNode>,Serializable {

    private static final long serialVersionUID = 3248671994878955004L;
    
    public Long firingNeuronId;
    
    public Double timeToFire;

    public NiceNode(Long firingNeuronId, Double timeToFire) {
        this.firingNeuronId = firingNeuronId;
        this.timeToFire     = timeToFire;
    }

    @Override
    public String toString() {
        return "firing neuron: " + firingNeuronId + ",\ttime to fire: " + timeToFire;
    }

    @Override
    public int compareTo(NiceNode node) {
        return timeToFire.compareTo(node.timeToFire);
    }
}
