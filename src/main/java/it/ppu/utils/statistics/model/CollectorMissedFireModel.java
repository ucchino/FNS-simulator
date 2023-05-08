/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.statistics.model;

import it.ppu.spiking.node.model.internode.InternodeSpikeModel;
import it.ppu.spiking.node.model.SynapseModel;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import lombok.ToString;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

@ToString
public class CollectorMissedFireModel implements Serializable {

    private static final long serialVersionUID = 3248671994878955045L;
    
    private static final AtomicLong IDGEN = new AtomicLong();

    private transient CollectorKey itemKey;

    @QuerySqlField(index = true)
    private Long id;
    
    @QuerySqlField(index = true)   
    private Integer nodeId;
    
    @QuerySqlField    
    private Double missedAtTime;

    @QuerySqlField    
    private Double axonalDelay;

    @QuerySqlField    
    private Double fireTime;

    @QuerySqlField    
    private Double currentTime;
    
    private final SynapseModel synapse;

    @QuerySqlField    
    private Long missedFires;
    
    @QuerySqlField    
    private Double minMissedAxonalDelay;
    
    public CollectorMissedFireModel(InternodeSpikeModel internodeSpike, Double currentTime,Long missedFires,Double minMissedAxonalDelay) {

        this.id = IDGEN.incrementAndGet();
        
        this.nodeId = internodeSpike.getSynapse().getAxonFiringNodeId();
        
        this.axonalDelay  = internodeSpike.getAxonalDelay();
        this.missedAtTime = internodeSpike.getBurningTime();
        this.fireTime     = internodeSpike.getFiringTime();
        this.synapse      = internodeSpike.getSynapse();
        
        this.currentTime = currentTime;
        
        this.missedFires = missedFires;
        
        this.minMissedAxonalDelay = minMissedAxonalDelay;
    }

    public CollectorKey getKey() {
        if (getItemKey() == null) {
            setItemKey(new CollectorKey(getId(), getNodeId()));
        }
        return getItemKey();
    }

    public CollectorKey getItemKey() {
        return itemKey;
    }

    public void setItemKey(CollectorKey itemKey) {
        this.itemKey = itemKey;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getMissedAtTime() {
        return missedAtTime;
    }

    public Double getAxonalDelay() {
        return axonalDelay;
    }

    public Double getFireTime() {
        return fireTime;
    }

    public Double getCurrentTime() {
        return currentTime;
    }

    public String getSynapse() {
        return synapse.toString();
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public Long getMissedFires() {
        return missedFires;
    }

    public Double getMinMissedAxonalDelay() {
        return minMissedAxonalDelay;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public void setMissedAtTime(Double missedAtTime) {
        this.missedAtTime = missedAtTime;
    }

    public void setAxonalDelay(Double axonalDelay) {
        this.axonalDelay = axonalDelay;
    }

    public void setFireTime(Double fireTime) {
        this.fireTime = fireTime;
    }

    public void setCurrentTime(Double currentTime) {
        this.currentTime = currentTime;
    }

    public void setMissedFires(Long missedFires) {
        this.missedFires = missedFires;
    }

    public void setMinMissedAxonalDelay(Double minMissedAxonalDelay) {
        this.minMissedAxonalDelay = minMissedAxonalDelay;
    }
}
