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

import java.io.Serializable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Slf4j
public class IntegerCouple implements Comparable<IntegerCouple>,Serializable {
    
    private static final long serialVersionUID = 3248671994878955051L;
    
    private Integer src = 0;
    private Integer dst = 0;

    public IntegerCouple(Integer src, Integer dst) {
        this.src = src;
        this.dst = dst;
    }

    public Integer getSrc() {
        return src;
    }

    public Integer getDst() {
        return dst;
    }

    @Override
    public String toString() {
        return "IntegerCouple [a=" + src + ", b=" + dst + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        IntegerCouple rhs = (IntegerCouple) obj;
        
        return new EqualsBuilder().append(src, rhs.src).append(dst, rhs.dst).isEquals();
    }

    @Override
    public int hashCode() {
        //----------------------------------------------------------------------
        //you pick src hard-coded, casually chosen, non-zero, odd number
        //ideally different for each class
        //----------------------------------------------------------------------
        return new HashCodeBuilder(17, 37).append(7 * src).append(dst * dst + 3 * dst).toHashCode();
    }

    @Override
    public int compareTo(IntegerCouple o) {
        if (this == o) {
            return 0;
        }
        
        int retval = src.compareTo(o.getSrc());
        
        if (retval != 0) {
            return retval;
        }
        
        retval = dst.compareTo(o.getDst());
        
        return retval;
    }

    public static void _main(String[] args) {
        HashMap<Integer, Integer>       h = new HashMap<>();
        TreeMap<Integer, Integer>       t = new TreeMap<>();
        
        HashMap<IntegerCouple, Integer> kh = new HashMap<>();
        TreeMap<IntegerCouple, Integer> kt = new TreeMap<>();

        h.put(3, 3);
        h.put(3, 3);
        h.put(3, 3);

        kh.put(new IntegerCouple(3, 9), 3);
        kh.put(new IntegerCouple(3, 9), 3);
        kh.put(new IntegerCouple(3, 9), 3);

        t.put(3, 3);
        t.put(3, 3);
        t.put(3, 3);

        kt.put(new IntegerCouple(3, 9), 3);
        kt.put(new IntegerCouple(3, 9), 3);
        kt.put(new IntegerCouple(3, 9), 3);

        IntegerCouple ic = new IntegerCouple(0, 0);
        
        Iterator<Entry<Integer, Integer>> it = h.entrySet().iterator();
        ic.log.info("integer hash: ");
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            ic.log.info(pair.getKey() + " --> " + pair.getValue());
            it.remove();
        }
        
        ic.log.info("");

        Iterator<Entry<IntegerCouple, Integer>> itkh = kh.entrySet().iterator();
        ic.log.info("integer couple hash: ");
        while (itkh.hasNext()) {
            Map.Entry pair = (Map.Entry) itkh.next();
            ic.log.info(pair.getKey() + " --> " + pair.getValue());
            itkh.remove();
        }
        
        ic.log.info("");

        Iterator<Entry<Integer, Integer>> itt = t.entrySet().iterator();
        ic.log.info("integer tree: ");
        while (itt.hasNext()) {
            Map.Entry pair = (Map.Entry) itt.next();
            ic.log.info(pair.getKey() + " --> " + pair.getValue());
            itt.remove();
        }
        
        ic.log.info("");

        Iterator<Entry<IntegerCouple, Integer>> itkt = kt.entrySet().iterator();
        ic.log.info("integer couple tree: ");
        while (itkt.hasNext()) {
            Map.Entry pair = (Map.Entry) itkt.next();
            ic.log.info(pair.getKey() + " --> " + pair.getValue());
            itkt.remove();
        }
    }
}
