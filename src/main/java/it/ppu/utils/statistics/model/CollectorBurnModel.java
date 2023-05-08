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

import java.io.Serializable;

import java.util.concurrent.atomic.AtomicLong;

import lombok.ToString;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

@ToString
public class CollectorBurnModel implements Serializable {

    private static long serialVersionUID = 3248671994878955043L;
    
    private static final AtomicLong IDGEN = new AtomicLong();

    private transient CollectorKey itemKey;

    @QuerySqlField(index = true)
    private Long id;

    @QuerySqlField(index = true)    
    private Integer burningNodeId;

    @QuerySqlField(index = true)    
    private Integer firingNodeId;
    
    @QuerySqlField    
    private Long firingNeuronId;

    @QuerySqlField    
    private Long burningNeuronId;

    @QuerySqlField    
    private Boolean fromExternalInput;

    @QuerySqlField    
    private Double burnTime;
    
    @QuerySqlField    
    private Double fromState;
    
    @QuerySqlField    
    private Double stepInState;
    
    @QuerySqlField    
    private Double postSynapticWeight;
    
    @QuerySqlField    
    private Double preSynapticWeight;
    
    @QuerySqlField    
    private Double timeToFire;
    
    @QuerySqlField    
    private Double fireTime;
    
    @QuerySqlField(index = true)    
    private Long messageId;
    
    @QuerySqlField    
    private Boolean splitComplete;
    
    @QuerySqlField(index = true)    
    private Long split;

    @QuerySqlField(index = true)    
    private Boolean refractPeriod;

    public CollectorBurnModel(
        Long firingNeuronId,
        int firingNodeId,
        Long burningNeuronId,
        int burningNodeId,
        Double burnTime,
        Boolean fromExternalInput,
        Double fromState,
        Double stepInState,
        Double postsynapticWeight,
        Double presynapticWeight,
        Double timeToFire,
        Double fireTime,
        Long messageId,
        Long split,
        Boolean splitComplete,
        Boolean refractPeriod) {

        this.id = IDGEN.incrementAndGet();
        
        this.firingNeuronId     = firingNeuronId;
        this.firingNodeId       = firingNodeId;
        this.burningNeuronId    = burningNeuronId;
        this.burningNodeId      = burningNodeId;
        this.burnTime           = burnTime;
        this.fromExternalInput  = fromExternalInput;
        this.fromState          = fromState;
        this.stepInState        = stepInState;
        this.postSynapticWeight = postsynapticWeight;
        this.preSynapticWeight  = presynapticWeight;
        this.timeToFire         = timeToFire;
        this.fireTime           = fireTime;
        this.messageId          = messageId;
        this.split              = split;
        this.splitComplete      = splitComplete;
        this.refractPeriod      = refractPeriod;
    }

    public CollectorKey getKey() {
        if (getItemKey() == null) {
            setItemKey(new CollectorKey(getId(), getBurningNodeId()));
        }
        return getItemKey();
    }

    public Integer getFromExternalInputInteger() {
        return getFromExternalInput() ? 1 : 0;
    }

    public CollectorKey getItemKey() {
        return itemKey;
    }

    public void setItemKey(CollectorKey spikeKey) {
        this.itemKey = spikeKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getBurningNodeId() {
        return burningNodeId;
    }

    public void setBurningNodeId(Integer burningNodeId) {
        this.burningNodeId = burningNodeId;
    }

    public Integer getFiringNodeId() {
        return firingNodeId;
    }

    public void setFiringNodeId(Integer firingNodeId) {
        this.firingNodeId = firingNodeId;
    }

    public Long getFiringNeuronId() {
        return firingNeuronId;
    }

    public void setFiringNeuronId(Long firingNeuronId) {
        this.firingNeuronId = firingNeuronId;
    }

    public Long getBurningNeuronId() {
        return burningNeuronId;
    }

    public void setBurningNeuronId(Long burningNeuronId) {
        this.burningNeuronId = burningNeuronId;
    }

    public Boolean getFromExternalInput() {
        return fromExternalInput;
    }

    public void setFromExternalInput(Boolean fromExternalInput) {
        this.fromExternalInput = fromExternalInput;
    }

    public Double getBurnTime() {
        return burnTime;
    }

    public void setBurnTime(Double burnTime) {
        this.burnTime = burnTime;
    }

    public Double getFromState() {
        return fromState;
    }

    public void setFromState(Double fromState) {
        this.fromState = fromState;
    }

    public Double getStepInState() {
        return stepInState;
    }

    public void setStepInState(Double stepInState) {
        this.stepInState = stepInState;
    }

    public Double getPostSynapticWeight() {
        return postSynapticWeight;
    }

    public void setPostSynapticWeight(Double postSynapticWeight) {
        this.postSynapticWeight = postSynapticWeight;
    }

    public Double getPreSynapticWeight() {
        return preSynapticWeight;
    }

    public void setPreSynapticWeight(Double preSynapticWeight) {
        this.preSynapticWeight = preSynapticWeight;
    }

    public Double getTimeToFire() {
        return timeToFire;
    }

    public void setTimeToFire(Double timeToFire) {
        this.timeToFire = timeToFire;
    }

    public Double getFireTime() {
        return fireTime;
    }

    public void setFireTime(Double fireTime) {
        this.fireTime = fireTime;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Boolean getSplitComplete() {
        return splitComplete;
    }

    public void setSplitComplete(Boolean splitComplete) {
        this.splitComplete = splitComplete;
    }

    public Long getSplit() {
        return split;
    }

    public void setSplit(Long split) {
        this.split = split;
    }

    public Boolean getRefractPeriod() {
        return refractPeriod;
    }

    public void setRefractPeriod(Boolean refractPeriod) {
        this.refractPeriod = refractPeriod;
    }

}
