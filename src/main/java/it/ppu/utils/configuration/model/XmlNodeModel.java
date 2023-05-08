/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.configuration.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "node")
public class XmlNodeModel {

    private Integer id;
    
    private Double rewiring_P;
    
    private Integer k;
    
    private Long n;
    
    private Double R;
    private Double mu_w_exc;
    private Double mu_w_inh;
    private Double sigma_w_exc;
    private Double sigma_w_inh;
    private Double w_pre_exc;
    private Double w_pre_inh;

    private Integer external_inputs_number;
    private Integer external_inputs_type;
    private Integer external_inputs_fireduration;
    private Integer external_inputs_outdegree;
    
    private Double external_inputs_time_offset;
    private Double external_inputs_timestep;
    private Double external_inputs_amplitude;
    
    private Integer Bn;
    
    private Double IBI;
    
    private XmlNeuronManagerModel neuron_manager;
    
    private Boolean plasticity;
    
    private Double etap;
    private Double etam;
    private Double taup;
    private Double taum;
    private Double w_max;
    private Double plasticity_to;

    public Integer getId() {
        return id;
    }

    public Double getRewiring_P() {
        return rewiring_P;
    }

    public Integer getK() {
        return k;
    }

    public Long getN() {
        return n;
    }

    public Double getR() {
        return R;
    }

    public Double getMu_w_exc() {
        return mu_w_exc;
    }

    public Double getMu_w_inh() {
        return mu_w_inh;
    }

    public Double getSigma_w_exc() {
        return sigma_w_exc;
    }

    public Double getSigma_w_inh() {
        return sigma_w_inh;
    }

    public Double getW_pre_exc() {
        return w_pre_exc;
    }

    public Double getW_pre_inh() {
        return w_pre_inh;
    }

    public Integer getExternal_inputs_number() {
        return external_inputs_number;
    }

    public Integer getExternal_inputs_type() {
        return external_inputs_type;
    }

    public Integer getExternal_inputs_fireduration() {
        return external_inputs_fireduration;
    }

    public Integer getExternal_inputs_outdegree() {
        return external_inputs_outdegree;
    }

    public Double getExternal_inputs_time_offset() {
        return external_inputs_time_offset;
    }

    public Double getExternal_inputs_timestep() {
        return external_inputs_timestep;
    }

    public Double getExternal_inputs_amplitude() {
        return external_inputs_amplitude;
    }

    public Integer getBn() {
        return Bn;
    }

    public Double getIBI() {
        return IBI;
    }

    public XmlNeuronManagerModel getNeuron_manager() {
        return neuron_manager;
    }

    public Boolean getPlasticity() {
        return plasticity;
    }

    public Double getEtap() {
        return etap;
    }

    public Double getEtam() {
        return etam;
    }

    public Double getTaup() {
        return taup;
    }

    public Double getTaum() {
        return taum;
    }

    public Double getW_max() {
        return w_max;
    }

    public Double getPlasticity_To() {
        return plasticity_to;
    }
    
    //----------------------------------------------------------------------

    public void setId(Integer id) {
        this.id = id;
    }

    public void setRewiring_P(Double rewiring_P) {
        this.rewiring_P = rewiring_P;
    }

    public void setK(Integer k) {
        this.k = k;
    }

    public void setN(Long n) {
        this.n = n;
    }

    public void setR(Double R) {
        this.R = R;
    }

    public void setMu_w_exc(Double mu_w_exc) {
        this.mu_w_exc = mu_w_exc;
    }

    public void setMu_w_inh(Double mu_w_inh) {
        this.mu_w_inh = mu_w_inh;
    }

    public void setSigma_w_exc(Double sigma_w_exc) {
        this.sigma_w_exc = sigma_w_exc;
    }

    public void setSigma_w_inh(Double sigma_w_inh) {
        this.sigma_w_inh = sigma_w_inh;
    }

    public void setW_pre_exc(Double w_pre_exc) {
        this.w_pre_exc = w_pre_exc;
    }

    public void setW_pre_inh(Double w_pre_inh) {
        this.w_pre_inh = w_pre_inh;
    }

    public void setExternal_inputs_number(Integer external_inputs_number) {
        this.external_inputs_number = external_inputs_number;
    }

    public void setExternal_inputs_type(Integer external_inputs_type) {
        this.external_inputs_type = external_inputs_type;
    }

    public void setExternal_inputs_fireduration(Integer external_inputs_fireduration) {
        this.external_inputs_fireduration = external_inputs_fireduration;
    }

    public void setExternal_inputs_outdegree(Integer external_inputs_outdegree) {
        this.external_inputs_outdegree = external_inputs_outdegree;
    }

    public void setExternal_inputs_time_offset(Double external_inputs_time_offset) {
        this.external_inputs_time_offset = external_inputs_time_offset;
    }

    public void setExternal_inputs_timestep(Double external_inputs_timestep) {
        this.external_inputs_timestep = external_inputs_timestep;
    }

    public void setExternal_inputs_amplitude(Double external_inputs_amplitude) {
        this.external_inputs_amplitude = external_inputs_amplitude;
    }

    public void setBn(Integer Bn) {
        this.Bn = Bn;
    }

    public void setIBI(Double IBI) {
        this.IBI = IBI;
    }

    public void setNeuron_manager(XmlNeuronManagerModel neuron_manager) {
        this.neuron_manager = neuron_manager;
    }

    public void setPlasticity(Boolean plasticity) {
        this.plasticity = plasticity;
    }

    public void setEtap(Double etap) {
        this.etap = etap;
    }

    public void setEtam(Double etam) {
        this.etam = etam;
    }

    public void setTaup(Double taup) {
        this.taup = taup;
    }

    public void setTaum(Double taum) {
        this.taum = taum;
    }

    public void setW_max(Double w_max) {
        this.w_max = w_max;
    }

    public void setTo(Double plasticity_to) {
        this.plasticity_to = plasticity_to;
    }
}
