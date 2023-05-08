/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.node;

import it.ppu.consts.ConstantsGrid;

public class Naming {
    
    public static String get(Integer value) {
        return String.format("%02d", value);
    }

    public static String get(Long value) {
        return String.format("%02d", value);
    }
    
    public static String getCacheName(int nodeNumber) {
        return ConstantsGrid.CACHE_KEY_CACHE_NAME_INTERNODE_SPIKES + nodeNumber;
    }
}
