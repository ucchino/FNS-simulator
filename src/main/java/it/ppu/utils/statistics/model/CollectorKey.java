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

import org.apache.ignite.cache.affinity.*;

import java.io.*;

public class CollectorKey implements Serializable {

    private final Long id;

    @AffinityKeyMapped
    private final long nodeId;

    public CollectorKey(long id, long nodeId) {
        this.id     = id;
        this.nodeId = nodeId;
    }

    public long getSpikeId() {
        return id;
    }

    public long getNodeId() {
        return nodeId;
    }

    @Override public boolean equals(Object item) {
        if (this == item) {
            return true;
        }

        if (!(item instanceof CollectorKey)) {
            return false;
        }

        CollectorKey spikeKey = (CollectorKey)item;

        return (id == spikeKey.id) && (nodeId == spikeKey.nodeId);
    }

    @Override  public int hashCode() {
        int res = (int)(id ^ (id >>> 32));

        res = 31 * res + (int)(nodeId ^ (nodeId >>> 32));

        return res;
    }

    @Override public String toString() {
        return Long.toString(id);
    }
}
