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

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import it.ppu.consts.ConstantsNodeDefaults;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "fns_config")
public class XmlConfigModel {

    private Long stop = ConstantsNodeDefaults.XML_SIMULATION_TIME_MS;
    
    private Boolean lif       = ConstantsNodeDefaults.XML_LIF;
    private Boolean exp_decay = ConstantsNodeDefaults.XML_EXP_DECAY;
    
    private Double avg_neuronal_signal_speed = ConstantsNodeDefaults.XML_AVG_NEURONAL_SPEED;
    
    private XmlNeuronManagerModel global_neuron_manager;
    
    private ArrayList<XmlNodeModel> node = new ArrayList<XmlNodeModel>();
    
    private Double glob_rewiring_P = ConstantsNodeDefaults.XML_P_REW;
    
    private Integer glob_k = ConstantsNodeDefaults.XML_NEURON_CONNECTIONS;//TODOPPU DEV LOOOONGGGGG

    private Long glob_n = ConstantsNodeDefaults.XML_NEURONS_NUMBER;

    private Double glob_R           = ConstantsNodeDefaults.XML_R;
    private Double glob_mu_w_exc    = ConstantsNodeDefaults.XML_MU_W_EXC;
    private Double glob_mu_w_inh    = ConstantsNodeDefaults.XML_MU_W_INH;
    private Double glob_sigma_w_exc = ConstantsNodeDefaults.XML_SIGMA_W_EXC;
    private Double glob_sigma_w_inh = ConstantsNodeDefaults.XML_SIGMA_W_INH;
    private Double glob_w_pre_exc   = ConstantsNodeDefaults.XML_W_PRE_EXC;
    private Double glob_w_pre_inh   = ConstantsNodeDefaults.XML_W_PRE_INH;

    private Double glob_IBI = ConstantsNodeDefaults.XML_IBI;
    
    private Integer glob_Bn = ConstantsNodeDefaults.XML_BN;
    
    private Double glob_external_inputs_time_offset = ConstantsNodeDefaults.XML_EXTERNAL_INPUTS_TIME_OFFSET;
    private Double glob_external_inputs_timestep    = ConstantsNodeDefaults.XML_EXTERNAL_INPUTS_TIME_STEP;
    private Double glob_external_inputs_amplitude   = ConstantsNodeDefaults.XML_EXTERNAL_INPUTS_AMPLITUDE;

    //TODOPPU DEV LOOOONG!!
    private Integer glob_external_inputs_number       = ConstantsNodeDefaults.XML_EXTERNAL_INPUTS_NUMBER;
    private Integer glob_external_inputs_type         = ConstantsNodeDefaults.XML_EXTERNAL_INPUTS_TYPE;
    private Integer glob_external_inputs_fireduration = ConstantsNodeDefaults.XML_EXTERNAL_INPUTS_FIRE_DURATION;
    private Integer glob_external_inputs_outdegree    = ConstantsNodeDefaults.XML_EXTERNAL_INPUTS_FIREOUT_DEGREE;

    private Boolean glob_plasticity = ConstantsNodeDefaults.XML_PLASTICITY;
    
    private Double glob_etap  = ConstantsNodeDefaults.XML_ETAP;
    private Double glob_etam  = ConstantsNodeDefaults.XML_ETAM;
    private Double glob_taup  = ConstantsNodeDefaults.XML_TAUP;
    private Double glob_taum  = ConstantsNodeDefaults.XML_TAUM;
    private Double glob_w_max = ConstantsNodeDefaults.XML_PWMAX;
    
    private Double glob_plasticity_to = ConstantsNodeDefaults.XML_PLASTICITY_TO;

    public Integer getGlob_Bn() {
        return (glob_Bn != null) ? glob_Bn : 1;
    }

    public Double getGlob_IBI() {
        return (glob_IBI != null) ? glob_IBI : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
            "stop ms: "   + getStop()
            + ", d_exc: " + getGlobal_neuron_manager().getD_exc()
            + ", d_inh: " + getGlobal_neuron_manager().getD_inh()
            + ", c: "     + getGlobal_neuron_manager().getC()
            + ", t_arp: " + getGlobal_neuron_manager().getT_arp() + "\nNodes/regions: ");
        
        for (int i = 0; i < getNode().size(); ++i) {
            sb.append("\n" + i + ". " + getNode().get(i).getId());
        }
        
        sb.append("\n\n");
        
        return sb.toString();
    }

    public Long getStop() {
        return stop;
    }

    public void setStop(Long stop) {
        this.stop = stop;
    }

    public Boolean getLif() {
        return lif;
    }

    public void setLif(Boolean lif) {
        this.lif = lif;
    }

    public Boolean getExp_decay() {
        return exp_decay;
    }

    public void setExp_decay(Boolean exp_decay) {
        this.exp_decay = exp_decay;
    }

    public Double getAvg_neuronal_signal_speed() {
        return avg_neuronal_signal_speed;
    }

    public void setAvg_neuronal_signal_speed(Double avg_neuronal_signal_speed) {
        this.avg_neuronal_signal_speed = avg_neuronal_signal_speed;
    }

    public XmlNeuronManagerModel getGlobal_neuron_manager() {
        return global_neuron_manager;
    }

    public void setGlobal_neuron_manager(XmlNeuronManagerModel global_neuron_manager) {
        this.global_neuron_manager = global_neuron_manager;
    }

    public ArrayList<XmlNodeModel> getNode() {
        return node;
    }

    public void setNode(ArrayList<XmlNodeModel> node) {
        this.node = node;
    }

    public Double getGlob_rewiring_P() {
        return glob_rewiring_P;
    }

    public void setGlob_rewiring_P(Double glob_rewiring_P) {
        this.glob_rewiring_P = glob_rewiring_P;
    }

    public Integer getGlob_k() {
        return glob_k;
    }

    public void setGlob_k(Integer glob_k) {
        this.glob_k = glob_k;
    }

    public Long getGlob_n() {
        return glob_n;
    }

    public void setGlob_n(Long glob_n) {
        this.glob_n = glob_n;
    }

    public Double getGlob_R() {
        return glob_R;
    }

    public void setGlob_R(Double glob_R) {
        this.glob_R = glob_R;
    }

    public Double getGlob_mu_w_exc() {
        return glob_mu_w_exc;
    }

    public void setGlob_mu_w_exc(Double glob_mu_w_exc) {
        this.glob_mu_w_exc = glob_mu_w_exc;
    }

    public Double getGlob_mu_w_inh() {
        return glob_mu_w_inh;
    }

    public void setGlob_mu_w_inh(Double glob_mu_w_inh) {
        this.glob_mu_w_inh = glob_mu_w_inh;
    }

    public Double getGlob_sigma_w_exc() {
        return glob_sigma_w_exc;
    }

    public void setGlob_sigma_w_exc(Double glob_sigma_w_exc) {
        this.glob_sigma_w_exc = glob_sigma_w_exc;
    }

    public Double getGlob_sigma_w_inh() {
        return glob_sigma_w_inh;
    }

    public void setGlob_sigma_w_inh(Double glob_sigma_w_inh) {
        this.glob_sigma_w_inh = glob_sigma_w_inh;
    }

    public Double getGlob_w_pre_exc() {
        return glob_w_pre_exc;
    }

    public void setGlob_w_pre_exc(Double glob_w_pre_exc) {
        this.glob_w_pre_exc = glob_w_pre_exc;
    }

    public Double getGlob_w_pre_inh() {
        return glob_w_pre_inh;
    }

    public void setGlob_w_pre_inh(Double glob_w_pre_inh) {
        this.glob_w_pre_inh = glob_w_pre_inh;
    }

    public void setGlob_IBI(Double glob_IBI) {
        this.glob_IBI = glob_IBI;
    }

    public void setGlob_Bn(Integer glob_Bn) {
        this.glob_Bn = glob_Bn;
    }

    public Double getGlob_external_inputs_time_offset() {
        return glob_external_inputs_time_offset;
    }

    public void setGlob_external_inputs_time_offset(Double glob_external_inputs_time_offset) {
        this.glob_external_inputs_time_offset = glob_external_inputs_time_offset;
    }

    public Double getGlob_external_inputs_timestep() {
        return glob_external_inputs_timestep;
    }

    public void setGlob_external_inputs_timestep(Double glob_external_inputs_timestep) {
        this.glob_external_inputs_timestep = glob_external_inputs_timestep;
    }

    public Double getGlob_external_inputs_amplitude() {
        return glob_external_inputs_amplitude;
    }

    public void setGlob_external_inputs_amplitude(Double glob_external_inputs_amplitude) {
        this.glob_external_inputs_amplitude = glob_external_inputs_amplitude;
    }

    public Integer getGlob_external_inputs_number() {
        return glob_external_inputs_number;
    }

    public void setGlob_external_inputs_number(Integer glob_external_inputs_number) {
        this.glob_external_inputs_number = glob_external_inputs_number;
    }

    public Integer getGlob_external_inputs_type() {
        return glob_external_inputs_type;
    }

    public void setGlob_external_inputs_type(Integer glob_external_inputs_type) {
        this.glob_external_inputs_type = glob_external_inputs_type;
    }

    public Integer getGlob_external_inputs_fireduration() {
        return glob_external_inputs_fireduration;
    }

    public void setGlob_external_inputs_fireduration(Integer glob_external_inputs_fireduration) {
        this.glob_external_inputs_fireduration = glob_external_inputs_fireduration;
    }

    public Integer getGlob_external_inputs_outdegree() {
        return glob_external_inputs_outdegree;
    }

    public void setGlob_external_inputs_outdegree(Integer glob_external_inputs_outdegree) {
        this.glob_external_inputs_outdegree = glob_external_inputs_outdegree;
    }

    public Boolean getGlob_plasticity() {
        return glob_plasticity;
    }

    public void setGlob_plasticity(Boolean glob_plasticity) {
        this.glob_plasticity = glob_plasticity;
    }

    public Double getGlob_etap() {
        return glob_etap;
    }

    public void setGlob_etap(Double glob_etap) {
        this.glob_etap = glob_etap;
    }

    public Double getGlob_etam() {
        return glob_etam;
    }

    public void setGlob_etam(Double glob_etam) {
        this.glob_etam = glob_etam;
    }

    public Double getGlob_taup() {
        return glob_taup;
    }

    public void setGlob_taup(Double glob_taup) {
        this.glob_taup = glob_taup;
    }

    public Double getGlob_taum() {
        return glob_taum;
    }

    public void setGlob_taum(Double glob_taum) {
        this.glob_taum = glob_taum;
    }

    public Double getGlob_w_max() {
        return glob_w_max;
    }

    public void setGlob_w_max(Double glob_w_max) {
        this.glob_w_max = glob_w_max;
    }

    public Double getGlob_plasticity_to() {
        return glob_plasticity_to;
    }

    public void setGlob_plasticity_to(Double glob_plasticity_to) {
        this.glob_plasticity_to = glob_plasticity_to;
    }
    
    
}
