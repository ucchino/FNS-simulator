/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.spiking.node.model;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Slf4j
public final class SynapseModel implements Comparable<SynapseModel>,Serializable {

    private static final long serialVersionUID = 3248671994878955010L;
    
    private final Long firingNeuronId;
    private final Long burningNeuronId;
    
    private       Integer firingNodeId;
    private final Integer burningNodeId;

    private final Double averageLengthInternodeAxonLambda;
    
    private       Double postSynapticWeight;
    private final Double presynapticWeight;
    private       Double lastBurningTime;

    public SynapseModel(
        Integer firingNodeId,
        
        Long firingNeuronId,
        
        Integer burningNodeId,
        
        Long burningNeuronId,
        
        Double averageLengthInternodeAxonLambda,
        Double postSynapticWeight,
        Double presynapticWeight) {
        
        this.firingNodeId      = firingNodeId;
        this.firingNeuronId    = firingNeuronId;
        this.burningNodeId     = burningNodeId;
        this.burningNeuronId   = burningNeuronId;
        
        this.averageLengthInternodeAxonLambda = averageLengthInternodeAxonLambda;
        
        this.presynapticWeight = presynapticWeight;
        
        this.setPostsynapticWeight(postSynapticWeight);
    }

    public void _debugSetFiringNode_(int firingNodeId) {
        this.firingNodeId = firingNodeId;
    }
    
    public Long getAxonFiringNeuronId() {
        return firingNeuronId;
    }

    public Long getBurningNeuronId() {
        return burningNeuronId;
    }

    public Integer getAxonFiringNodeId() {
        return firingNodeId;
    }

    public Integer getBurningNodeId() {
        return burningNodeId;
    }

    public Double getAverageLengthInternodeAxonLambda() {
        return averageLengthInternodeAxonLambda;
    }

    public Double getPostSynapticWeight() {
    return (postSynapticWeight==null) ? 1.0 : postSynapticWeight;
        }

    public void setPostsynapticWeight(Double post_synaptic_weight) {
        if ((post_synaptic_weight != null) && (post_synaptic_weight == 1.0)) {
            this.postSynapticWeight = null;
        } else {
            this.postSynapticWeight = post_synaptic_weight;
        }
    }

    public Double getPreSynapticWeight() {
        return presynapticWeight;
    }

    public Double getLastBurningTime() {
        return lastBurningTime;
    }

    public void setLastBurningTime(Double lastBurningTime) {
        this.lastBurningTime = lastBurningTime;
    }

    @Override
    public String toString() {
        return 
            "[firing: " + firingNodeId  + "-" + firingNeuronId  + "," +
            "burning: " + burningNodeId + "-" + burningNeuronId +
            "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        SynapseModel rhs = (SynapseModel) obj;
        return new EqualsBuilder()
            .append(firingNodeId,    rhs.firingNodeId)
            .append(firingNeuronId,  rhs.firingNeuronId)
            .append(burningNodeId,   rhs.burningNodeId)
            .append(burningNeuronId, rhs.burningNeuronId).isEquals();
    }

    @Override
    public int hashCode() {
        //----------------------------------------------------------------------
        //you pick a hard-coded, casually chosen, non-zero, odd number
        //ideally different for each class
        //----------------------------------------------------------------------
        return new HashCodeBuilder(17, 37)
		.append(7l * firingNeuronId + 9l + firingNodeId)
		.append(burningNeuronId * burningNodeId + 3l * firingNeuronId + 17l)
		.toHashCode();
    }

    @Override
    public int compareTo(SynapseModel o) {
        if (this == o) {
            return 0;
        }

        int retval = firingNeuronId.compareTo(o.getAxonFiringNeuronId());
        if (retval != 0) {
            return retval;
        }
        
        retval = burningNeuronId.compareTo(o.getBurningNeuronId());
        if (retval != 0) {
            return retval;
        }
        
        retval = firingNodeId.compareTo(o.getAxonFiringNodeId());
        if (retval != 0) {
            return retval;
        }
        
        retval = burningNodeId.compareTo(o.getBurningNodeId());

        return retval;
    }
}
