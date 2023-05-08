/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.tools;

import it.ppu.consts.ConstantsSimulator;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

public class Strings implements Serializable {
    
    private static final long serialVersionUID = 3248671994878955054L;
    
    public static String format(Number n) {
        return ConstantsSimulator.FORMATTER.format(n);
    }
    
    public static Boolean isEmpty(String check) {
        if(check == null) return true;
        
        return (StringUtils.isEmpty(check.trim()));
    }
}
