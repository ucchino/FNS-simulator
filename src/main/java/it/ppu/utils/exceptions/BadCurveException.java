/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.exceptions;

import java.io.Serializable;

public class BadCurveException extends Exception implements Serializable {

    private static final long serialVersionUID = 3248671994878955020L;
    
    public BadCurveException(String message) {
        super(message);
    }
}
