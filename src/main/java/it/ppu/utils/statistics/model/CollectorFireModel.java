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
public class CollectorFireModel implements Serializable {

    private static final long serialVersionUID = 3248671994878955044L;

    private static final AtomicLong IDGEN = new AtomicLong();

    private transient CollectorKey itemKey;

    @QuerySqlField(index = true)
    private Long id;
    
    @QuerySqlField(index = true)   
    private Integer firingNodeId;

    @QuerySqlField    
    private Long firingNeuronId;
    
    @QuerySqlField
    private Long maxNeurons;

    @QuerySqlField    
    private Double firingTime;

    @QuerySqlField    
    private Double compressionFactor;
    
    @QuerySqlField    
    private Boolean excitatory;

    @QuerySqlField    
    private Boolean external;

    @QuerySqlField(index = true)    
    private Long messageId;
    
    @QuerySqlField(index = true)    
    private Long split;
    
    public CollectorFireModel(
        Integer firingNodeId, 
        Long    firingNeuronId, 
        Double  firingTime,
        Long    maxNeurons, 
        Double  compressionFactor,
        Boolean excitatory, 
        Boolean external,
        Long messageId,
        Long split) {
        
        this.id = IDGEN.incrementAndGet();
        
        this.firingNodeId      = firingNodeId;
        this.firingNeuronId    = firingNeuronId;
        this.firingTime        = firingTime;
        this.maxNeurons        = maxNeurons;
        this.compressionFactor = compressionFactor;
        this.excitatory        = excitatory;
        this.external          = external;
        this.messageId         = messageId;
        this.split             = split;
    }

    public CollectorKey getKey() {
        if (getItemKey() == null) {
            setItemKey(new CollectorKey(getId(), getFiringNodeId()));
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

    public Long getMaxNeurons() {
        return maxNeurons;
    }

    public void setMaxNeurons(Long maxNeurons) {
        this.maxNeurons = maxNeurons;
    }

    public Double getFiringTime() {
        return firingTime;
    }

    public void setFiringTime(Double firingTime) {
        this.firingTime = firingTime;
    }

    public Double getCompressionFactor() {
        return compressionFactor;
    }

    public void setCompressionFactor(Double compressionFactor) {
        this.compressionFactor = compressionFactor;
    }

    public Boolean getExcitatory() {
        return excitatory;
    }

    public void setExcitatory(Boolean isExcitatory) {
        this.excitatory = isExcitatory;
    }

    public Boolean getExternal() {
        return external;
    }

    public void setExternal(Boolean external) {
        this.external = external;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getSplit() {
        return split;
    }

    public void setSplit(Long split) {
        this.split = split;
    }


}
