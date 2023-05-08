/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.spiking.node.model.internode;

import it.ppu.spiking.node.model.SynapseModel;

import java.io.Serializable;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public final class InternodeSpikeModel implements Comparable<InternodeSpikeModel>,Serializable {

    private static final long serialVersionUID = 3248671994878955008L;
    
    private SynapseModel synapse;
    
    private Double burningTime;
    private Double firingTime;
    
    private Double axonalDelay;

    public InternodeSpikeModel() {
        this.synapse = null;
        
        this.burningTime = null;
        this.firingTime  = null;
        
        this.axonalDelay = null;
    }
    
    public InternodeSpikeModel(SynapseModel synapse, Double burningTime, Double firingTime, Double axonalDelay) {
        this.synapse = synapse;
        
        this.burningTime = burningTime;
        this.firingTime  = firingTime;
        
        this.axonalDelay = axonalDelay;
    }

    public SynapseModel getSynapse() {
        return synapse;
    }

    public Double getBurningTime() {
        return burningTime;
    }

    public Double getFiringTime() {
        return firingTime;
    }

    public Double getAxonalDelay() {
        return axonalDelay;
    }
    
    @Override
    public int compareTo(InternodeSpikeModel bean) {
        return this.synapse.getAxonFiringNodeId().compareTo(bean.getSynapse().getAxonFiringNodeId());
    }

    public void setSynapse(SynapseModel synapse) {
        this.synapse = synapse;
    }

    public void setBurningTime(Double burningTime) {
        this.burningTime = burningTime;
    }

    public void setFiringTime(Double firingTime) {
        this.firingTime = firingTime;
    }

    public void setAxonalDelay(Double axonalDelay) {
        this.axonalDelay = axonalDelay;
    }    
}
