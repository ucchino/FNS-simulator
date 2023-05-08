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

import java.io.Serializable;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import lombok.extern.slf4j.Slf4j;

import org.mapdb.HTreeMap;

@Slf4j
public class Shuffler implements Serializable {

    private static final long serialVersionUID = 3248671994878955006L;
    
    private static int[] shuffleIntegerArray(int n, boolean noRandom) {
        int[] retval = new int[n];
        
        for (int i = 0; i < n; ++i) {
            retval[i] = i;
        }
        
        if(noRandom) return retval;
        
        Random rnd = ThreadLocalRandom.current();
        
        for (int i = n - 1; i > 0; i--) {
            
            int index = rnd.nextInt(i + 1);
            //----------------------------------------------------------------------
            //Simple swap
            //----------------------------------------------------------------------
            int a = retval[index];
            
            retval[index] = retval[i];
            retval[i]     = a;
        }
        return retval;
    }

    public static void shuffleArray(HTreeMap<Long, Long> shuffled, long n, boolean noRandom) {

        for (long i = 0; i < n; ++i) {
            shuffled.put(i, i);
        }
        
        if(noRandom) return;
        
        Random rnd = ThreadLocalRandom.current();
        
        for (long i = n - 1; i > 0; i--) {
            
            Long tmp   = rnd.nextLong();
            Long index = (tmp < 0) ? ((-tmp) % (i)) : (tmp % (i));
            
            //----------------------------------------------------------------------
            //Simple swap
            //----------------------------------------------------------------------
            Long a = shuffled.get(index);
            
            shuffled.put(index, shuffled.get(i));
            shuffled.put(i, a);
        }
    }

    public static void _main(String[] args) {
        
        Shuffler s = new Shuffler();
        
        int[] a = Shuffler.shuffleIntegerArray(30,false);
        
        for (int i = 0; i < a.length; ++i) {
            s.log.info(i + ". " + a[i]);
        }
    }
}
