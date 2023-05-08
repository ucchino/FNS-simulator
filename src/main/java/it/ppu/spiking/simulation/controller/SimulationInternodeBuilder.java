/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.spiking.simulation.controller;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

import it.ppu.utils.tools.IntegerCouple;
import it.ppu.spiking.node.model.internode.InternodeConnectionModel;

import java.io.Serializable;

@Slf4j
public class SimulationInternodeBuilder implements Serializable {

    private static final long serialVersionUID = 324867199487895581L;

    private final ArrayList<PackageNodeInfo> nodes = new ArrayList<>();

    private final SimulationInternodeReader internodeReader = new SimulationInternodeReader(this);

    private final ArrayList<InternodeConnectionModel> internodeConnections = new ArrayList<>();

    private final HashMap<IntegerCouple, Integer> indexes = new HashMap<>();
    
    public SimulationInternodeBuilder(String dataFolder) throws IOException {
        internodeReader.readConnectivityPackage(dataFolder);
    }

    private void addNode(PackageNodeInfo r) {
        if (nodes == null) {
            return;
        }
        nodes.add(r);
    }

    public void addNode(Integer nodeId) {
        addNode(new PackageNodeInfo(nodeId));
    }

    public void addEdge(Integer src, Integer dst, Double ne_xn_ratio_weight) {
        if ((nodes.get(src) != null) && (nodes.get(dst) != null)) {

            IntegerCouple tmp = new IntegerCouple(src, dst);
            Integer index = indexes.get(tmp);

            if (index == null) {
                indexes.put(tmp, internodeConnections.size());
                internodeConnections.add(new InternodeConnectionModel(src, dst, ne_xn_ratio_weight));
            }
        }
    }

    public void addMuOmega_Amplitude(Integer src, Integer dst, Double amplitude) {
        if ((nodes.get(src) != null) && (nodes.get(dst) != null)) {

            IntegerCouple tmp = new IntegerCouple(src, dst);
            Integer index = indexes.get(tmp);

            if (index != null) {
                internodeConnections.get(index).setMuOmega_Amplitude(amplitude);
            }
        }
    }

    public void addSigmaOmega_AmplitudeStdVariation(Integer src, Integer dst, Double amplitude) {
        if ((nodes.get(src) != null) && (nodes.get(dst) != null)) {

            IntegerCouple tmp = new IntegerCouple(src, dst);
            Integer index = indexes.get(tmp);

            if (index != null) {
                internodeConnections.get(index).setSigmaW_AmplitudeStdDeviation(amplitude);
            }
        }
    }

    public void addMuLambda_Length(Integer src, Integer dst, double lenght) {
        if ((nodes.get(src) != null) && (nodes.get(dst) != null)) {

            IntegerCouple tmp = new IntegerCouple(src, dst);
            Integer index = indexes.get(tmp);

            if (index != null) {
                internodeConnections.get(index).setMuLambda_TractLength(lenght);
            }
        }
    }

    public void addAlphaLambda_LengthShapeParameter(Integer src, Integer dst, double lenghtShapeParameter) {
        if ((nodes.get(src) != null) && (nodes.get(dst) != null)) {

            IntegerCouple tmp = new IntegerCouple(src, dst);
            Integer index = indexes.get(tmp);

            if (index != null) {
                internodeConnections.get(index).setAlphaLambda_TractLengthsShapeParameter(lenghtShapeParameter);
            }
        }
    }

    public void addInternodeConnectionType(Integer src, Integer dst, Integer type) {
        if ((nodes.get(src) != null) && (nodes.get(dst) != null)) {

            IntegerCouple tmp = new IntegerCouple(src, dst);
            Integer index = indexes.get(tmp);

            if (index != null) {
                internodeConnections.get(index).setInternodeConnectionType(type);
            }
        }
    }

    public Integer getNodesNum() {
        return nodes.size();
    }

    public Integer getInternodeConnectionsNumber() {
        return internodeConnections.size();
    }

    public ArrayList<InternodeConnectionModel> getInternodeConnections() {
        return internodeConnections;
    }

    public Double getMinNeXnRatio() {
        return internodeReader.getMinNeXnRatio();
    }

    public Double getMaxNeXnRatio() {
        return internodeReader.getMaxNeXnRatio();
    }
}

class PackageNodeInfo {

    private final Integer nodeId;

    protected PackageNodeInfo(Integer id) {
        this.nodeId = id;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public String toString() {
        return "nodeId: " + nodeId;
    }
}
