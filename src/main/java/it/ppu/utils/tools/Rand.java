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

import java.util.Random;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;

import it.ppu.consts.ConstantsNodeDefaults;

import java.io.Serializable;

public class Rand implements Serializable {

    private static final long serialVersionUID = 3248671994878955053L;
    
    private final static Random RANDOM = new Random();
    
    public static Double getExponential(Boolean noRandom,ExponentialDistribution distro) {
        if(noRandom) {
            return ConstantsNodeDefaults.NO_RANDOM;
        } else {
            return distro.sample();
        }
    }
 
    public static Double getGamma(Boolean noRandom, GammaDistribution distro) {
        if(noRandom) {
            return ConstantsNodeDefaults.NO_RANDOM;
        } else {
            return distro.sample();
        }
    }

    public static Double getDouble(Boolean noRandom) {
        if (noRandom) {
            return ConstantsNodeDefaults.NO_RANDOM;
        }
        return Math.random();
    }

    public static Double getNextGaussian(Boolean noRandom) {
        if (noRandom) {
            return ConstantsNodeDefaults.NO_RANDOM;
        }
        return RANDOM.nextGaussian();
    }

    public static Integer getNextInteger(Integer bound, Boolean noRandom) {
        if (noRandom) {
            return (bound-1);
        }
        return RANDOM.nextInt(bound);
    }

}
