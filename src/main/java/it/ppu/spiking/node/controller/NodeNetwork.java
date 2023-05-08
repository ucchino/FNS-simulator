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

import java.util.HashMap;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import it.ppu.spiking.node.model.NodeModel;
import it.ppu.utils.tools.Rand;
import it.ppu.consts.ConstantsNodeDefaults;
import it.ppu.utils.node.LongCouple;
import it.ppu.utils.node.Shuffler;
import it.ppu.spiking.simulation.model.SimulationInputDataModel;

import java.io.Serializable;

@Slf4j
class NodeNetwork implements Serializable {

    private static final long serialVersionUID = 3248671994878955012L;
    
    private final NodeModel nodeModel;

    private Long neuronsNumberExcitatory;
    private Long neuronsNumberInhibithory;
    
    //----------------------------------------------------------------------
    //The small world connection matrix
    //----------------------------------------------------------------------
    private HashMap<LongCouple, Double> connectionMatrix = new HashMap<>();
            
    private int externalOutJump = 1;
    
    private Double localExternalInputsTimeOffset;
    
    private Double localExternalTimeStep = ConstantsNodeDefaults.NODETHREAD_EXTERNAL_INPUTS_TIME_STEP;

    private final HashMap<Long, Boolean> localExternalInputsInit = new HashMap<Long, Boolean>();
    
    private Boolean simNoRandom = false;
    //----------------------------------------------------------------------
    
    public NodeNetwork(NodeModel nodeModel) {
        this.nodeModel = nodeModel;
        
        this.simNoRandom = nodeModel.isSimNoRand();
        
        localExternalInputsTimeOffset = nodeModel.getExternalInputsTimeOffset();
		
        if(nodeModel.hasExternalInputs()) {
            localExternalTimeStep = nodeModel.getExternalInputTimeStepBetweenInputEventsSameSource();
            
            do {
                this.externalOutJump = Rand.getNextInteger(1987,simNoRandom);
            } while (this.externalOutJump == 0);
        }
        
        println("init exc/inh neurons number");
        initNeuronsNumber();
        
        println("init of intranode synapses");
        initWiringIntranodeSynapses();

        println("init done");
    }

    private void initNeuronsNumber() {
        neuronsNumberExcitatory  = (long) Math.floor(nodeModel.getNeuronsNumber() * nodeModel.getExcitatoryNeuronsOverTotalsAsRatio());
        neuronsNumberInhibithory = nodeModel.getNeuronsNumber() - neuronsNumberExcitatory;
    }

    private void createNeuronConnectionInConnectionMatrix(Long firingNeuronId,Long burningNeuronId,Double presynaptic_weight) {
        connectionMatrix.put(new LongCouple(firingNeuronId, burningNeuronId),presynaptic_weight);
    }

    public Double getConnectionPresynapticWeight(Long firingNeuronId,Long burningNeuronId) {
        return (connectionMatrix.get(new LongCouple(firingNeuronId, burningNeuronId)) != null) ? connectionMatrix.get(new LongCouple(firingNeuronId, burningNeuronId)) : 0;
    }

    //----------------------------------------------------------------------
    //GET NUMBER OF SYNAPSES
    //----------------------------------------------------------------------
    public Iterator<LongCouple> getConnectionsKeyIterator() {
        return connectionMatrix.keySet().iterator();
    }

    private void initWiringIntranodeSynapses() {
        
        Long neuronsNumber = nodeModel.getNeuronsNumber();
        
        if (neuronsNumber <= 1) return;

        println("wiring intra-node synapses...");
        
        //----------------------------------------------------------------------
        //make adjacency casual
        //----------------------------------------------------------------------
        DB tmpDb1 = DBMaker.memoryDirectDB().make();
        DB tmpDb2 = DBMaker.memoryDirectDB().make();
        
        HTreeMap<Long, Long> shuffled      = tmpDb1.hashMap("shuffle",Serializer.LONG, Serializer.LONG).create();
        HTreeMap<Long, Long> shuffled_rand = tmpDb2.hashMap("shuffle",Serializer.LONG, Serializer.LONG).create();
        
        Shuffler.shuffleArray(shuffled,      neuronsNumber, simNoRandom);
        Shuffler.shuffleArray(shuffled_rand, neuronsNumber, simNoRandom);
        
        int neuronConnections_HALF = nodeModel.getNeuronConnections() / 2;
        
        Double tmpAmpl;
        
        long neuron = 0;
        
        //----------------------------------------------------------------------
        //Ciclo sui neuroni presenti DENTRO il nodo
        //----------------------------------------------------------------------
        for (long currNeuron = 0; currNeuron < neuronsNumber; ++currNeuron) {
            final Long TMP_SRC = shuffled.get(currNeuron);
            
            if (isNeuronExcitatory(shuffled.get(currNeuron))) {
                tmpAmpl = nodeModel.getWPre_presynapticWeightExcitatory();
            } else {
                tmpAmpl = nodeModel.getWPre_presynapticWeightInhibitory();
            }

            //TODOPPU APPROFONDIRE 13 Creo le connessioni nel nodo corrente 
            //----------------------------------------------------------------------
            //Per un numero di connessioni (PARAMETRO K in conf) diviso 2 creo le connessioni neuronali
            //----------------------------------------------------------------------
            for (long currConnection = 1; currConnection <= neuronConnections_HALF; ++currConnection) {
                
                //----------------------------------------------------------------------
                //rewiring condition
                //----------------------------------------------------------------------
                if (Rand.getDouble(simNoRandom) < nodeModel.getPRew_smallWorldTopologyRewiringProbability()) {
                    Long tmp;
                    for (;
                          ((tmp = shuffled_rand.get(neuron)).equals(TMP_SRC))
                        || (tmp.equals(shuffled.get((currNeuron + currConnection) % neuronsNumber)))
                        || (connectionMatrix.get(new LongCouple(TMP_SRC, tmp)) != null);
                        
                        //CIRCULAR LIST
                        
                        neuron = (neuron + 1) % neuronsNumber) {

                        //EMPTY FOR
                    }
                    createNeuronConnectionInConnectionMatrix(shuffled.get(currNeuron), tmp, tmpAmpl);
                } else {
                    createNeuronConnectionInConnectionMatrix(shuffled.get(currNeuron),shuffled.get((currNeuron + currConnection) % neuronsNumber),tmpAmpl);
                }
                
                if (Rand.getDouble(simNoRandom) < nodeModel.getPRew_smallWorldTopologyRewiringProbability()) {
                    Long tmp;
                    for (;
                          ((tmp = shuffled_rand.get(neuron)).equals(TMP_SRC))
                        || (tmp.equals(shuffled.get((neuronsNumber + currNeuron - currConnection) % neuronsNumber)))
                        || (connectionMatrix.get(new LongCouple(TMP_SRC,tmp)) != null);
                        
                        neuron = (neuron + 1) % neuronsNumber) {

                        //EMPTY FOR
                    }

                    createNeuronConnectionInConnectionMatrix(shuffled.get(currNeuron), tmp, tmpAmpl);
                    
                } else {
                    createNeuronConnectionInConnectionMatrix(shuffled.get(currNeuron),shuffled.get((neuronsNumber + currNeuron - currConnection) % neuronsNumber),tmpAmpl);
                }
            }
        }
        
        shuffled.close();
        
        println("intra-node wiring done");
    }

    public Long getNeuronsNumberExcitatory() {
        return neuronsNumberExcitatory;
    }

    public Long getNeuronsNumberInhibithory() {
        return neuronsNumberInhibithory;
    }

    public Double getMu_w_agnostic(Long neuronId) {
        return (isNeuronExcitatory(neuronId) ? nodeModel.getMuW_postsynapticWeightExcitatory() : nodeModel.getMuW_postsynapticWeightInhibitory());
    }

    public Double getSigma_w_agnostic(Long neuronId) {
        Double retval = isNeuronExcitatory(neuronId) ? nodeModel.getSigmaW_postsynapticWeightStandardDeviationExcitatory() : nodeModel.getSigmaW_postsynapticWeightStandardDeviationInhibitory();
        return (retval == null) ? 1 : retval;
    }

    public boolean isNeuronExcitatory(Long neuronId) {
        return (neuronId < neuronsNumberExcitatory);
    }

    public Double getPresynapticForNeuron(Long neuronId) {
        return (isNeuronExcitatory(neuronId) ? nodeModel.getWPre_presynapticWeightExcitatory() : nodeModel.getWPre_presynapticWeightInhibitory());
    }

    public int getExternalOutJump() {
        return externalOutJump;
    }

    public void printNodesConnections() {
        Long neuronsNumber = nodeModel.getNeuronsNumber();
        
        println("printing connections: ");
        
        for (long i = 0; i < neuronsNumber; ++i) {
            println("neuron number: " + i);
            for (long j = 0; j < neuronsNumber; ++j) {
                println(getConnectionPresynapticWeight(i, j) + ", ");
            }
            println("");
        }
    }

    private void println(String s) {
        log.info("[id" + nodeModel.getNodeId() + "] " + s);
    }
    
    public Double getAmplitudeValue(Long extNeuronGlobalId) {
        if ((extNeuronGlobalId - nodeModel.getNeuronsNumber()) > Integer.MAX_VALUE) {
            throw new IndexOutOfBoundsException("[NODE ERROR] The external input id is too big");
        }
        
        if (nodeModel.hasExternalInputs()) {
            return nodeModel.getExternalInputsFireAmplitude();
        }

        return null;
        //TODOPPU APPROFONDIRE 7 - ma nel model ho:
        //public Double getExternalAmplitude() {
        //   return hasExternalInputs() ? externalAmplitude : Constants.EXTERNAL_AMPLITUDE;
        //}
		//Il return null e' corretto o andrebbe un return Constants.EXTERNAL_AMPLITUDE
    }

    //TODOPPU APPROFONDIRE 8 - A CHE SERVE? Non dovrebbe essere return nodeModel.getExternalInputsTimeOffset(); ?
    public Double getExternalInputsTimeOffset(Long extNeuronId) {
        
        Boolean present = localExternalInputsInit.get(extNeuronId);
        
        double  originalTimeOffset = localExternalInputsTimeOffset;
        
        if (present == null) {
            localExternalInputsInit.put(extNeuronId, true);
        } else {
            //----------------------------------------------------------------------
            //Update input time offset
            //----------------------------------------------------------------------
            localExternalInputsTimeOffset = localExternalTimeStep;
            //TODOPPU APPROFONDIRE 9 - timeStep o timeOffset?
        }
        
        return originalTimeOffset;
    }

    //----------------------------------------------------------------------
    
    public Integer getNodeId() {
        return nodeModel.getNodeId();
    }
    
    public Boolean isSimNoRand() {
        return nodeModel.isSimNoRand();
    }

    public Boolean getNeuronPlasticity() {
         return nodeModel.getNeuronPlasticity();
    }

    public Double getC_threshold() {
         return nodeModel.getC_threshold();
    }

    public Double getExternalInputTimeStepBetweenInputEventsSameSource() {
        return nodeModel.getExternalInputTimeStepBetweenInputEventsSameSource();
    }

    public Double getDExc_linearDecayForEachNeuron() {
        return nodeModel.getDExc_linearDecayForEachNeuron();
    }

    public Double getDInh_underthresholdDecayForEachNeuron() {
        return nodeModel.getDInh_underthresholdDecayForEachNeuron();
    }

    public Long getNeuronsNumber() {
        return nodeModel.getNeuronsNumber();
    }

    public Double getWPre_presynapticWeightExcitatory() {
        return nodeModel.getWPre_presynapticWeightExcitatory();
    }

    public Double getWPre_presynapticWeightInhibitory() {
        return nodeModel.getWPre_presynapticWeightInhibitory();
    }

    public Integer getExternalInputsFireDuration() {
        return nodeModel.getExternalInputsFireDuration();
    }

    public Integer getExternalInputsType() {
        return nodeModel.getExternalInputsType();
    }

    public Double getAvgNeuronalSignalSpeed() {
        return nodeModel.getAvgNeuronalSignalSpeed();
    }

    public Integer getBn_burstsSpikeForEachFiringNeuron() {
        return nodeModel.getBn_burstsSpikeForEachFiringNeuron();
    }

    public Double getIbi_interburstSpikeIntervalTime() {
        return nodeModel.getIbi_interburstSpikeIntervalTime();
    }

    public Double getTArp_refractoryTimeForEachNeuron() {
        return nodeModel.getTArp_refractoryTimeForEachNeuron();
    }

    public Boolean getLif() {
        return nodeModel.getLif();
    }

    public Boolean getExpDecay() {
        return nodeModel.getExpDecay();
    }

    public Boolean isSimFast() {
        return nodeModel.isSimFast();
    }

    public Integer getNodesNum() {
        return nodeModel.getNodesNum();
    }

    public Boolean hasExternalInputs() {
        return nodeModel.hasExternalInputs();
    }

    public Integer getExternalInputs() {
        return nodeModel.getExternalInputs();
    }

    public Double getTo_plasticityRulesTimeout() {
        return nodeModel.getTo_plasticityRulesTimeout();
    }

    public Double getPwMax_maxPostsynapticWeightForPlasticityRules() {
        return nodeModel.getPwMax_maxPostsynapticWeightForPlasticityRules();
    }

    public Double getEtap_etaPlusLearningPlasticity() {
        return nodeModel.getEtap_etaPlusLearningPlasticity();
    }

    public Double getTaup_tauPlusLongTermPotentiationPlasticity() {
        return nodeModel.getTaup_tauPlusLongTermPotentiationPlasticity();
    }

    public Double getEtam_etaMinusLearningPlasticity() {
        return nodeModel.getEtam_etaMinusLearningPlasticity();
    }

    public Double getTaum_tauMinusLongTermDepressionPlasticity() {
        return nodeModel.getTaum_tauMinusLongTermDepressionPlasticity();
    }

    public Boolean isExternalInput(Long firingNeuronId) {
        return nodeModel.isExternalInput(firingNeuronId);
    }

    public Integer getExternalInputsNumberOfTargetsFireOutDegree() {
        return nodeModel.getExternalInputsNumberOfTargetsFireOutDegree();
    }

    public Double getExternalInputsFireAmplitude() {
        return nodeModel.getExternalInputsFireAmplitude();
    }
    
    public SimulationInputDataModel getSimulationInputData() {
        return nodeModel.getSimulationInputData();
    }
}
