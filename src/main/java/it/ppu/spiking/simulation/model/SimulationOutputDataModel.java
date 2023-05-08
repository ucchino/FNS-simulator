/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.spiking.simulation.model;

import java.io.Serializable;

public final class SimulationOutputDataModel implements Serializable {

    private static final long serialVersionUID = 324867199487895586L;
    
    private Double msInitConfigFileRead_0;
    private Double msInitNetworkBuilding_1;
    private Double msInitSimulation_2;

    private Double msTotalTimeFromProgramStart;
    private Double msSimulationTimeConfiguration;
    private Double msSimulationTimeReal;

    private Integer totalInternodeAxonalConnections;

    private Long neuronsNumberSumOnAllNodes = 0l;
    private Long neuronsNumberMaxOnSingleNode = 0l;
    private Long neuronsNumberExcitatory = 0l;
    private Long neuronsNumberInhibithory = 0l;
    
    private Long internodeSynapseNumber;

    public Double getMsInitConfigFileRead() {
        return getMsInitConfigFileRead_0();
    }

    public void setMsInitConfigFileRead(Double msInitConfigFileRead_0) {
        this.setMsInitConfigFileRead_0(msInitConfigFileRead_0);
    }

    public Double getMsInitNetworkBuilding() {
        return getMsInitNetworkBuilding_1();
    }

    public void setMsInitNetworkBuilding(Double msInitNetworkBuilding_1) {
        this.setMsInitNetworkBuilding_1(msInitNetworkBuilding_1);
    }

    public Double getMsInitSimulation() {
        return getMsInitSimulation_2();
    }

    public void setMsInitSimulation(Double msInitSimulation_2) {
        this.setMsInitSimulation_2(msInitSimulation_2);
    }

    public Double getMsInitTotalTime() {
        return 0.0
            + getMsInitConfigFileRead_0()
            + getMsInitNetworkBuilding_1()
            + getMsInitSimulation_2();
    }

    public Double getMsTotalTimeFromProgramStart() {
        return msTotalTimeFromProgramStart;
    }

    public void setMsTotalTimeFromProgramStart(Double msTotalTimeFromProgramStart) {
        this.msTotalTimeFromProgramStart = msTotalTimeFromProgramStart;
    }

    public Double getMsSimulationTimeConfiguration() {
        return msSimulationTimeConfiguration;
    }

    public void setMsSimulationTimeConfiguration(Double msSimulationTimeConfiguration) {
        this.msSimulationTimeConfiguration = msSimulationTimeConfiguration;
    }

    public Double getMsSimulationTimeReal() {
        return msSimulationTimeReal;
    }

    public void setMsSimulationTimeReal(Double msSimulationTimeReal) {
        this.msSimulationTimeReal = msSimulationTimeReal;
    }

    public Integer getTotalInternodeAxonalConnections() {
        return totalInternodeAxonalConnections;
    }

    public void setTotalInternodeAxonalConnections(Integer totalInternodeAxonalConnections) {
        this.totalInternodeAxonalConnections = totalInternodeAxonalConnections;
    }

    public Double getMsInitConfigFileRead_0() {
        return msInitConfigFileRead_0;
    }

    public void setMsInitConfigFileRead_0(Double msInitConfigFileRead_0) {
        this.msInitConfigFileRead_0 = msInitConfigFileRead_0;
    }

    public Double getMsInitNetworkBuilding_1() {
        return msInitNetworkBuilding_1;
    }

    public void setMsInitNetworkBuilding_1(Double msInitNetworkBuilding_1) {
        this.msInitNetworkBuilding_1 = msInitNetworkBuilding_1;
    }

    public Double getMsInitSimulation_2() {
        return msInitSimulation_2;
    }

    public void setMsInitSimulation_2(Double msInitSimulation_2) {
        this.msInitSimulation_2 = msInitSimulation_2;
    }

    public Long getNeuronsNumberSumOnAllNodes() {
        return neuronsNumberSumOnAllNodes;
    }

    public void setNeuronsNumberSumOnAllNodes(Long neuronsNumberSumOnAllNodes) {
        this.neuronsNumberSumOnAllNodes = neuronsNumberSumOnAllNodes;
    }

    public Long getNeuronsNumberMaxOnSingleNode() {
        return neuronsNumberMaxOnSingleNode;
    }

    public void setNeuronsNumberMaxOnSingleNode(Long neuronsNumberMaxOnSingleNode) {
        this.neuronsNumberMaxOnSingleNode = neuronsNumberMaxOnSingleNode;
    }

    public Long getNeuronsNumberExcitatory() {
        return neuronsNumberExcitatory;
    }

    public void setNeuronsNumberExcitatory(Long neuronsNumberExcitatory) {
        this.neuronsNumberExcitatory = neuronsNumberExcitatory;
    }

    public Long getNeuronsNumberInhibithory() {
        return neuronsNumberInhibithory;
    }

    public void setNeuronsNumberInhibithory(Long neuronsNumberInhibithory) {
        this.neuronsNumberInhibithory = neuronsNumberInhibithory;
    }

    public Long getInternodeSynapseNumber() {
        return internodeSynapseNumber;
    }

    public void setInternodeSynapseNumber(Long internodeSynapseNumber) {
        this.internodeSynapseNumber = internodeSynapseNumber;
    }
}
