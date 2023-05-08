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

import it.ppu.spiking.simulation.model.SimulationInputDataModel;

import java.io.IOException;

import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

import it.ppu.consts.ConstantsNodeDefaults;
import it.ppu.consts.ConstantsSimulator;
import it.ppu.utils.configuration.model.XmlConfigModel;
import it.ppu.utils.configuration.model.XmlNeuronManagerModel;
import it.ppu.utils.configuration.model.XmlNodeModel;
import it.ppu.utils.exceptions.BadParametersException;
import it.ppu.utils.node.Naming;

import java.io.Serializable;

@Slf4j
public final class NodeModel implements Serializable {
    
    private static final long serialVersionUID = 324867199487895509L;
    //----------------------------------------------------------------------
    //the node nodeId
    //----------------------------------------------------------------------
    private Integer nodeId;

    private Integer nodesNum;
    
    //----------------------------------------------------------------------
    //the conn-degree of each neuron
    //----------------------------------------------------------------------
    private Integer neuronConnections;//TODOPPU DEV Meglio long! anzi UNSIGNED long
    
    //----------------------------------------------------------------------
    //neuronsNumber the number of neurons of the node
    //----------------------------------------------------------------------
    private Long neuronsNumber;

    //----------------------------------------------------------------------
    //excitRatio the ratio of excitatory neurons over the total number of neurons 'neuronsNumber'
    //----------------------------------------------------------------------
    private Double excitatoryNeuronsOverTotalsAsRatio;

    //----------------------------------------------------------------------
    //the mean of the postsynaptic weight distribution for excitatory neurons
    //----------------------------------------------------------------------
    private Double muW_postsynapticWeightExcitatory;
    
    //----------------------------------------------------------------------
    //the mean of the postsynaptic weight distribution for inhibitory neurons
    //----------------------------------------------------------------------
    private Double muW_postsynapticWeightInhibitory;
    
    //----------------------------------------------------------------------
    //the std deviation of the postsynaptic weight distribution for excitatory neurons
    //----------------------------------------------------------------------
    private Double sigmaW_postsynapticWeightStandardDeviationExcitatory;
    
    //----------------------------------------------------------------------
    //the std deviation of the postsynaptic weight distribution for inhibitory neurons
    //----------------------------------------------------------------------
    private Double sigmaW_postsynapticWeightStandardDeviationInhibitory;
    
    //----------------------------------------------------------------------
    //the presynaptic weight for excitatory neurons
    //----------------------------------------------------------------------
    private Double wPre_presynapticWeightExcitatory;
    
    //----------------------------------------------------------------------
    //the presynaptic weight for inhibitory neurons
    //----------------------------------------------------------------------
    private Double wPre_presynapticWeightInhibitory;

    //----------------------------------------------------------------------
    //the prob of small-world topology rewiring
    //----------------------------------------------------------------------
    private Double pRew_smallWorldTopologyRewiringProbability;

    //----------------------------------------------------------------------
    //the burst inter-spike time, InterBurst Interval
    //----------------------------------------------------------------------
    private Double ibi_interburstSpikeIntervalTime;
    //----------------------------------------------------------------------
    //the number of bursts spike for each firing neuron
    //----------------------------------------------------------------------
    private Integer bn_burstsSpikeForEachFiringNeuron;

    //----------------------------------------------------------------------
    //simulate neuron plasticity
    //----------------------------------------------------------------------
    private Boolean neuronPlasticity;

    //----------------------------------------------------------------------
    //the Eta plus learning constant for plasticity
    //----------------------------------------------------------------------
    private Double etap_etaPlusLearningPlasticity;
    
    //----------------------------------------------------------------------
    //the Eta minus learning constant for plasticity
    //----------------------------------------------------------------------
    private Double etam_etaMinusLearningPlasticity;
    
    //----------------------------------------------------------------------
    //the Tau plus positive time constants for long term potentiation for plasticity
    //----------------------------------------------------------------------
    private Double taup_tauPlusLongTermPotentiationPlasticity;
    
    //----------------------------------------------------------------------
    //the Tau minus positive time constants for long term depression for plasticity
    //----------------------------------------------------------------------
    private Double taum_tauMinusLongTermDepressionPlasticity;

    //----------------------------------------------------------------------
    //the max post-ynaptic weight (used for plasticity rules)
    //----------------------------------------------------------------------
    private Double pwMax_maxPostsynapticWeightForPlasticityRules;

    //----------------------------------------------------------------------
    //the timeout for plasticity rules
    //----------------------------------------------------------------------
    private Double to_plasticityRulesTimeout;

    //----------------------------------------------------------------------
    //threshold
    //----------------------------------------------------------------------
    private Double c_threshold;

    //----------------------------------------------------------------------
    //linear decay constant - underthreshold decay constant of each neuron
    //----------------------------------------------------------------------
    private Double dExc_linearDecayForEachNeuron;
    private Double dInh_underthresholdDecayForEachNeuron;

    //----------------------------------------------------------------------
    //refractory time constant - refractory period of each neuron (ms)
    //----------------------------------------------------------------------
    private Double tArp_refractoryTimeForEachNeuron;

    //----------------------------------------------------------------------
    //External inputs
    //----------------------------------------------------------------------
    private Integer externalInputs;
    private Integer externalInputsType;

    //----------------------------------------------------------------------
    //duration of the external inputs
    //----------------------------------------------------------------------
    private Integer externalInputsFireDuration;

    //----------------------------------------------------------------------
    //number of targets of each external inputs
    //----------------------------------------------------------------------
    private Integer externalInputsNumberOfTargetsFireOutDegree;

    //----------------------------------------------------------------------
    //external inputs initial time offset
    //----------------------------------------------------------------------
    private Double externalInputsTimeOffset;

    //----------------------------------------------------------------------
    //time step between two distinct input event from the same input source
    //for Poisson and constant inputs type
    //----------------------------------------------------------------------
    private Double externalInputsTimeStepBetweenInputEventsSameSource;

    private Double externalInputsPresynapticDefVal;

    //----------------------------------------------------------------------
    //amplitude of each external inputs
    //----------------------------------------------------------------------
    private Double externalInputsFireAmplitude;

    //----------------------------------------------------------------------
    //Velocita media segnale fra neuroni
    //----------------------------------------------------------------------
    private Double averageNeuronalSignalSpeed;

    private Boolean lif;
    private Boolean expDecay;

    private Boolean simNoRand;
    private Boolean simFast;

    //----------------------------------------------------------------------

    private SimulationInputDataModel simulationInputData;
    
    //----------------------------------------------------------------------

    private NodeModel() {
        //Col costruttore privato evito che la classe
        //sia istanziabile -> Obbligo all'uso del builder
    }
    
    public Integer getNodesNum() {
        return nodesNum;
    }

    public Boolean hasExternalInputs() {
        return (getExternalInputs() != null) && (getExternalInputs() > 0);
    }

    public Boolean isExternalInput(Long neuronId) {
        return neuronId >= neuronsNumber;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public Integer getNeuronConnections() {
        return neuronConnections;
    }

    public Long getNeuronsNumber() {
        return neuronsNumber;
    }

    public Double getExcitatoryNeuronsOverTotalsAsRatio() {
        return excitatoryNeuronsOverTotalsAsRatio;
    }

    public Double getMuW_postsynapticWeightExcitatory() {
        return muW_postsynapticWeightExcitatory;
    }

    public Double getMuW_postsynapticWeightInhibitory() {
        return muW_postsynapticWeightInhibitory;
    }

    public Double getSigmaW_postsynapticWeightStandardDeviationExcitatory() {
        return sigmaW_postsynapticWeightStandardDeviationExcitatory;
    }

    public Double getSigmaW_postsynapticWeightStandardDeviationInhibitory() {
        return sigmaW_postsynapticWeightStandardDeviationInhibitory;
    }

    public Double getWPre_presynapticWeightExcitatory() {
        return wPre_presynapticWeightExcitatory;
    }

    public Double getWPre_presynapticWeightInhibitory() {
        return ((wPre_presynapticWeightInhibitory < 0) ? wPre_presynapticWeightInhibitory : -wPre_presynapticWeightInhibitory);
    }

    public Double getPRew_smallWorldTopologyRewiringProbability() {
        return pRew_smallWorldTopologyRewiringProbability;
    }

    public Double getIbi_interburstSpikeIntervalTime() {
        return ibi_interburstSpikeIntervalTime;
    }

    public Integer getBn_burstsSpikeForEachFiringNeuron() {
        return (bn_burstsSpikeForEachFiringNeuron != null) ? bn_burstsSpikeForEachFiringNeuron : 1;
    }

    public Boolean getNeuronPlasticity() {
        return neuronPlasticity;
    }

    public Double getEtap_etaPlusLearningPlasticity() {
        return etap_etaPlusLearningPlasticity;
    }

    public Double getEtam_etaMinusLearningPlasticity() {
        return etam_etaMinusLearningPlasticity;
    }

    public Double getTaup_tauPlusLongTermPotentiationPlasticity() {
        return taup_tauPlusLongTermPotentiationPlasticity;
    }

    public Double getTaum_tauMinusLongTermDepressionPlasticity() {
        return taum_tauMinusLongTermDepressionPlasticity;
    }

    public Double getPwMax_maxPostsynapticWeightForPlasticityRules() {
        return pwMax_maxPostsynapticWeightForPlasticityRules;
    }

    public Double getTo_plasticityRulesTimeout() {
        return to_plasticityRulesTimeout;
    }

    public Double getC_threshold() {
        return c_threshold;
    }

    public Double getDExc_linearDecayForEachNeuron() {
        return dExc_linearDecayForEachNeuron;
    }

    public Double getDInh_underthresholdDecayForEachNeuron() {
        return dInh_underthresholdDecayForEachNeuron;
    }

    public Double getTArp_refractoryTimeForEachNeuron() {
        return tArp_refractoryTimeForEachNeuron;
    }

    public Integer getExternalInputs() {
        return externalInputs;
    }

    public Integer getExternalInputsType() {
        return externalInputsType;
    }

    public Integer getExternalInputsFireDuration() {
        return externalInputsFireDuration;
    }

    public Double getExternalInputsTimeOffset() {
        return externalInputsTimeOffset;
    }

    public Double getExternalInputTimeStepBetweenInputEventsSameSource() {
        return externalInputsTimeStepBetweenInputEventsSameSource;
    }

    public Double getExternalInputsPresynapticDefVal() {
        return externalInputsPresynapticDefVal;
    }

    public Double getExternalInputsFireAmplitude() {
        return hasExternalInputs() ? externalInputsFireAmplitude : ConstantsNodeDefaults.NODETHREAD_EXTERNAL_INPUTS_AMPLITUDE;
    }

    public int getExternalInputsNumberOfTargetsFireOutDegree() {
        return hasExternalInputs() ? externalInputsNumberOfTargetsFireOutDegree : ConstantsNodeDefaults.NODETHREAD_EXTERNAL_INPUTS_FIREOUT_DEGREE_NONE;
    }

    public Double getAvgNeuronalSignalSpeed() {
        return averageNeuronalSignalSpeed;
    }

    public Boolean getLif() {
        return lif;
    }

    public Boolean getExpDecay() {
        return expDecay;
    }

    public Boolean isSimFast() {
        return simFast;
    }

    public Boolean isSimNoRand() {
        return simNoRand;
    }

    public SimulationInputDataModel getSimulationInputData() {
        return simulationInputData;
    }
    
    public static NodeModel builder(Integer nodeId, SimulationInputDataModel simulationInputData) throws BadParametersException, IOException {

        Integer nodesNum = simulationInputData.getNodesNum();

        HashMap<Integer, XmlNodeModel> xmlNodes = simulationInputData.getXmlNodes();

        XmlConfigModel xmlConfig = simulationInputData.getXmlConfig();

        XmlNodeModel xmlNode = (xmlNodes.get(nodeId) != null) ? xmlNodes.get(nodeId) : null;

        XmlNeuronManagerModel xmlNeuronManager = ((xmlNode != null) && (xmlNode.getNeuron_manager() != null)) ? xmlNode.getNeuron_manager() : xmlConfig.getGlobal_neuron_manager();

        Long tmpNeuronsNumber = ((xmlNode != null) && (xmlNode.getN() != null)) ? xmlNode.getN() : xmlConfig.getGlob_n();

        Double tmpExcitatoryNeuronsOverTotalsAsRatio = ((xmlNode != null) && (xmlNode.getR() != null)) ? xmlNode.getR() : xmlConfig.getGlob_R();

        Double tmpMuW_postsynapticWeightExcitatory = ((xmlNode != null) && (xmlNode.getMu_w_exc() != null)) ? xmlNode.getMu_w_exc() : xmlConfig.getGlob_mu_w_exc();
        Double tmpMuW_postsynapticWeightInhibitory = ((xmlNode != null) && (xmlNode.getMu_w_inh() != null)) ? xmlNode.getMu_w_inh() : xmlConfig.getGlob_mu_w_inh();
        
        Double tmpSigmaW_postsynapticWeightStandardDeviationExcitatory = ((xmlNode != null) && (xmlNode.getSigma_w_exc() != null)) ? xmlNode.getSigma_w_exc() : xmlConfig.getGlob_sigma_w_exc();
        Double tmpSigmaW_postsynapticWeightStandardDeviationInhibitory = ((xmlNode != null) && (xmlNode.getSigma_w_inh() != null)) ? xmlNode.getSigma_w_inh() : xmlConfig.getGlob_sigma_w_inh();
        
        Double tmpWPre_presynapticWeightExcitatory = ((xmlNode != null) && (xmlNode.getW_pre_exc() != null)) ? xmlNode.getW_pre_exc() : xmlConfig.getGlob_w_pre_exc();
        Double tmpWPre_presynapticWeightInhibitory = ((xmlNode != null) && (xmlNode.getW_pre_inh() != null)) ? xmlNode.getW_pre_inh() : xmlConfig.getGlob_w_pre_inh();
        
        Double tmpPRew_smallWorldTopologyRewiringProbability = ((xmlNode != null) && (xmlNode.getRewiring_P() != null)) ? xmlNode.getRewiring_P() : xmlConfig.getGlob_rewiring_P();
        
        Double tmpIbi_interburstSpikeIntervalTime = ((xmlNode != null) && (xmlNode.getIBI() != null)) ? xmlNode.getIBI() : xmlConfig.getGlob_IBI();
        
        Double tmpEtap_etaPlusLearningPlasticity  = ((xmlNode != null) && (xmlNode.getEtap() != null)) ? xmlNode.getEtap() : xmlConfig.getGlob_etap();
        Double tmpEtam_etaMinusLearningPlasticity = ((xmlNode != null) && (xmlNode.getEtam() != null)) ? xmlNode.getEtam() : xmlConfig.getGlob_etam();
        
        Double tmpTaup_tauPlusLongTermPotentiationPlasticity = ((xmlNode != null) && (xmlNode.getTaup() != null)) ? xmlNode.getTaup() : xmlConfig.getGlob_taup();
        Double tmpTaum_tauMinusLongTermDepressionPlasticity  = ((xmlNode != null) && (xmlNode.getTaum() != null)) ? xmlNode.getTaum() : xmlConfig.getGlob_taum();
        
        Double tmpPwMax_maxPostsynapticWeightForPlasticityRules = ((xmlNode != null) && (xmlNode.getW_max() != null)) ? xmlNode.getW_max() : xmlConfig.getGlob_w_max();
        
        Double tmpTo_plasticityRulesTimeout = ((xmlNode != null) && (xmlNode.getPlasticity_To() != null)) ? xmlNode.getPlasticity_To() : xmlConfig.getGlob_plasticity_to();

        Integer tmpNeuronConnections = ((xmlNode != null) && (xmlNode.getK() != null)) ? xmlNode.getK() : xmlConfig.getGlob_k();
        
        Integer tmpExternalInputs = ((xmlNode != null) && (xmlNode.getExternal_inputs_number() != null)) ? xmlNode.getExternal_inputs_number() : xmlConfig.getGlob_external_inputs_number();
        
        Integer tmpBn_burstsSpikeForEachFiringNeuron = ((xmlNode != null) && (xmlNode.getBn() != null)) ? xmlNode.getBn() : xmlConfig.getGlob_Bn();

        Boolean tmpNeuronPlasticity = ((xmlNode != null) && (xmlNode.getPlasticity() != null)) ? xmlNode.getPlasticity() : xmlConfig.getGlob_plasticity();

        if (tmpBn_burstsSpikeForEachFiringNeuron == 0) {
            tmpBn_burstsSpikeForEachFiringNeuron = 1;
        }

        if (tmpNeuronConnections >= tmpNeuronsNumber) {
            throw new BadParametersException("bad parameters exception, " + "n has to be greater than k (now n is " + tmpNeuronsNumber + " and k is +" + tmpNeuronConnections + ")");
        }

        //----------------------------------------------------------------------
        //add a new node with or without external inputs
        //----------------------------------------------------------------------
        log.info(ConstantsSimulator.BAR_LOGGER);
        
        log.info("adding node: id" + Naming.get(nodeId));

        Integer tmpExternalInputsType         = null;
        Integer tmpExternalInputsFireDuration = null;
        
        Integer externalInputsNumberOfTargetsFireOutDegree = null;

        Double tmpExternalTimeStep         = null;
        Double tmpExternalFireAmplitude    = null;
        Double tmpExternalInputsTimeOffset = null;

        if (!tmpExternalInputs.equals(0)) {

            tmpExternalInputsType                      = ((xmlNode != null) && (xmlNode.getExternal_inputs_type()         != null)) ? xmlNode.getExternal_inputs_type()         : xmlConfig.getGlob_external_inputs_type();
            tmpExternalInputsFireDuration              = ((xmlNode != null) && (xmlNode.getExternal_inputs_fireduration() != null)) ? xmlNode.getExternal_inputs_fireduration() : xmlConfig.getGlob_external_inputs_fireduration();
            externalInputsNumberOfTargetsFireOutDegree = ((xmlNode != null) && (xmlNode.getExternal_inputs_outdegree()    != null)) ? xmlNode.getExternal_inputs_outdegree()    : xmlConfig.getGlob_external_inputs_outdegree();

            tmpExternalTimeStep         = ((xmlNode != null) && (xmlNode.getExternal_inputs_timestep()    != null)) ? xmlNode.getExternal_inputs_timestep()    : xmlConfig.getGlob_external_inputs_timestep();
            tmpExternalFireAmplitude    = ((xmlNode != null) && (xmlNode.getExternal_inputs_amplitude()   != null)) ? xmlNode.getExternal_inputs_amplitude()   : xmlConfig.getGlob_external_inputs_amplitude();
            tmpExternalInputsTimeOffset = ((xmlNode != null) && (xmlNode.getExternal_inputs_time_offset() != null)) ? xmlNode.getExternal_inputs_time_offset() : xmlConfig.getGlob_external_inputs_time_offset();
        }

        NodeModel nodeModel = new NodeModel();

        nodeModel.simulationInputData = simulationInputData;
        
        nodeModel.nodeId   = nodeId;
        nodeModel.nodesNum = nodesNum;

        nodeModel.neuronConnections = tmpNeuronConnections;
        nodeModel.neuronsNumber     = tmpNeuronsNumber;

        nodeModel.excitatoryNeuronsOverTotalsAsRatio = tmpExcitatoryNeuronsOverTotalsAsRatio;

        nodeModel.muW_postsynapticWeightExcitatory = tmpMuW_postsynapticWeightExcitatory;
        nodeModel.muW_postsynapticWeightInhibitory = tmpMuW_postsynapticWeightInhibitory;
        
        nodeModel.sigmaW_postsynapticWeightStandardDeviationExcitatory = tmpSigmaW_postsynapticWeightStandardDeviationExcitatory;
        nodeModel.sigmaW_postsynapticWeightStandardDeviationInhibitory = tmpSigmaW_postsynapticWeightStandardDeviationInhibitory;
        
        nodeModel.wPre_presynapticWeightExcitatory = tmpWPre_presynapticWeightExcitatory;
        nodeModel.wPre_presynapticWeightInhibitory = tmpWPre_presynapticWeightInhibitory;

        nodeModel.pRew_smallWorldTopologyRewiringProbability = tmpPRew_smallWorldTopologyRewiringProbability;
        
        nodeModel.ibi_interburstSpikeIntervalTime = tmpIbi_interburstSpikeIntervalTime;

        nodeModel.bn_burstsSpikeForEachFiringNeuron = tmpBn_burstsSpikeForEachFiringNeuron;

        nodeModel.neuronPlasticity = tmpNeuronPlasticity;

        nodeModel.etap_etaPlusLearningPlasticity  = tmpEtap_etaPlusLearningPlasticity;
        nodeModel.etam_etaMinusLearningPlasticity = tmpEtam_etaMinusLearningPlasticity;

        nodeModel.taup_tauPlusLongTermPotentiationPlasticity = tmpTaup_tauPlusLongTermPotentiationPlasticity;
        nodeModel.taum_tauMinusLongTermDepressionPlasticity  = tmpTaum_tauMinusLongTermDepressionPlasticity;

        nodeModel.pwMax_maxPostsynapticWeightForPlasticityRules = tmpPwMax_maxPostsynapticWeightForPlasticityRules;
        
        nodeModel.to_plasticityRulesTimeout = tmpTo_plasticityRulesTimeout;

        nodeModel.c_threshold = xmlNeuronManager.getC();
        
        nodeModel.dExc_linearDecayForEachNeuron         = xmlNeuronManager.getD_exc();
        nodeModel.dInh_underthresholdDecayForEachNeuron = xmlNeuronManager.getD_inh();
        nodeModel.tArp_refractoryTimeForEachNeuron      = xmlNeuronManager.getT_arp();

        nodeModel.externalInputs     = tmpExternalInputs;
        nodeModel.externalInputsType = tmpExternalInputsType;
        
        nodeModel.externalInputsFireDuration = tmpExternalInputsFireDuration;
        
        nodeModel.externalInputsNumberOfTargetsFireOutDegree = externalInputsNumberOfTargetsFireOutDegree;

        nodeModel.externalInputsTimeOffset = ((tmpExternalInputsTimeOffset != null) && (tmpExternalInputsTimeOffset >= 0)) ? tmpExternalInputsTimeOffset : ConstantsNodeDefaults.NODETHREAD_EXTERNAL_INPUTS_TIME_OFFSET;

        nodeModel.externalInputsTimeStepBetweenInputEventsSameSource = tmpExternalTimeStep;
        
        nodeModel.externalInputsPresynapticDefVal = ConstantsNodeDefaults.NODETHREAD_EXTERNAL_INPUTS_PRESYNAPTIC_EXC;
        nodeModel.externalInputsFireAmplitude = tmpExternalFireAmplitude;

        nodeModel.averageNeuronalSignalSpeed = simulationInputData.getXmlConfig().getAvg_neuronal_signal_speed();

        nodeModel.lif      = xmlConfig.getLif();
        nodeModel.expDecay = xmlConfig.getExp_decay();

        nodeModel.simNoRand = simulationInputData.getSimulationConfig().getSimNoRandom();
        nodeModel.simFast   = simulationInputData.getSimulationConfig().getSimFast();

        return nodeModel;
    }
}
