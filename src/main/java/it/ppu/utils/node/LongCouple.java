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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Slf4j
public class LongCouple implements Comparable<LongCouple>,Serializable {

    private static final long serialVersionUID = 3248671994878955003L;

    private Long src = 0L;
    private Long dst = 0L;

    public LongCouple(Long src, Long dst) {
        this.src = src;
        this.dst = dst;
    }

    public Long getSrc() {
        return src;
    }

    public Long getDst() {
        return dst;
    }

    @Override
    public String toString() {
        return "[a=" + src + ", b=" + dst + "]";
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

        LongCouple rhs = (LongCouple) obj;
        return new EqualsBuilder()
            .append(src, rhs.src)
            .append(dst, rhs.dst).isEquals();
    }

    @Override
    public int hashCode() {
        //----------------------------------------------------------------------
        //you pick a hard-coded, casually chosen, non-zero, odd number
        //ideally different for each class
        //----------------------------------------------------------------------
        if (src == null) {
            log.info("a null");
            log.info("this: " + toString());
        }
        
        if (dst == null) {
            log.info("b null");
            log.info("this: " + toString());
        }
        return new HashCodeBuilder(17, 37).append(7l * src + 9l).append(dst * dst + 3l * dst + 17l).toHashCode();
    }

    @Override
    public int compareTo(LongCouple o) {
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

    private void torem(Long a) {
        log.info("torem");
        a = 3L;
    }

    public static void _main(String[] args) {
        
        HashMap<Long, Long> h = new HashMap<>();
        TreeMap<Long, Long> t = new TreeMap<>();
        
        HashMap<LongCouple, Long> kh = new HashMap<>();
        TreeMap<LongCouple, Long> kt = new TreeMap<>();

        h.put(3L, 3L);
        h.put(3L, 3L);
        h.put(3L, 3L);

        kh.put(new LongCouple(3l, 9l), 3L);
        kh.put(new LongCouple(3l, 9l), 3L);
        kh.put(new LongCouple(3l, 9l), 3L);

        t.put(3L, 3L);
        t.put(3L, 3L);
        t.put(3L, 3L);

        kt.put(new LongCouple(3l, 9l), 3L);
        kt.put(new LongCouple(3l, 9l), 3L);
        kt.put(new LongCouple(3l, 9l), 3L);

        LongCouple lc = new LongCouple(0L, 0L);
        
        Iterator<Entry<Long, Long>> it = h.entrySet().iterator();
        lc.log.info("Long hash: ");
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            lc.log.info(pair.getKey() + " --> " + pair.getValue());
            it.remove();
        }
        
        lc.log.info("");

        Iterator<Entry<LongCouple, Long>> itkh = kh.entrySet().iterator();
        lc.log.info("Long couple hash: ");
        while (itkh.hasNext()) {
            Map.Entry pair = (Map.Entry) itkh.next();
            lc.log.info(pair.getKey() + " --> " + pair.getValue());
            itkh.remove();
        }
        
        lc.log.info("");

        Iterator<Entry<Long, Long>> itt = t.entrySet().iterator();
        lc.log.info("Long tree: ");
        while (itt.hasNext()) {
            Map.Entry pair = (Map.Entry) itt.next();
            lc.log.info(pair.getKey() + " --> " + pair.getValue());
            itt.remove();
        }
        
        lc.log.info("");

        Iterator<Entry<LongCouple, Long>> itkt = kt.entrySet().iterator();
        lc.log.info("Long couple tree: ");
        while (itkt.hasNext()) {
            Map.Entry pair = (Map.Entry) itkt.next();
            lc.log.info(pair.getKey() + " --> " + pair.getValue());
            itkt.remove();
        }
        
        lc.log.info("");

        Long b = 9l;
        
        new LongCouple(3l, 4l).torem(b);
        
        lc.log.info("" + b);
    }
}
