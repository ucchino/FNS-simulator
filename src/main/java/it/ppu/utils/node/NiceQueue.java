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

import java.util.PriorityQueue;

import java.util.HashMap;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NiceQueue implements Serializable {

    private static final long serialVersionUID = 3248671994878955001L;
    
    private final PriorityQueue<NiceNode> queue;
    
    private final HashMap<Long, NiceNode> nodesHash;

    public NiceQueue(String tmpHashName) {
        queue     = new PriorityQueue<NiceNode>();
        nodesHash = new HashMap<Long, NiceNode>();
    }

    public void insert(Double tf, Long fn) {
        NiceNode node = new NiceNode(fn, tf);
        queue.add(node);
        nodesHash.put(fn, node);
    }

    public NiceNode min() {
        //----------------------------------------------------------------------
        //The peek() method only retrieved the element at the head of the queue
        //----------------------------------------------------------------------
        return queue.peek();
    }

    public void update(double x) {
        Iterator<NiceNode> it = queue.iterator();
        
        while (it.hasNext()) {
            it.next().timeToFire -= x;
        }
    }

    /**
     * @return the minimum time to fire for active neurons, without polling the
     * value from the queue If the queue is empty, null is returned
     */
    public Double getMinTime() {
        NiceNode min = min();
        if (min == null) {
            return null;
        }
        return min.timeToFire;
    }

    public NiceNode extractMin() {
        
        //----------------------------------------------------------------------
        //The java.util.PriorityQueue.poll() method in Java is used to retrieve
        //or fetch and remove the first element of the Queue or the element
        //present at the head of the Queue. The peek() method only retrieved
        //the element at the head but the poll() also removes the element along
        //with the retrieval. It returns NULL if the queue is empty.
        //----------------------------------------------------------------------
        NiceNode min = queue.poll();
        if (min != null) {
            nodesHash.remove(min.firingNeuronId);
        }
        return min;
    }

    public void delete(Long fn) {
        NiceNode q = nodesHash.get(fn);
        if (q != null) {
            queue.remove(q);
        }
    }

    public int size() {
        return queue.size();
    }

    public void printQueue() {
        Iterator<NiceNode> it = queue.iterator();
        while (it.hasNext()) {
            log.info(it.next().toString());
        }
    }

    public static void _main(String[] args) {

        NiceQueue nq = new NiceQueue("tmp");
        
        for (Long i = 0L; i < 10; ++i) {
            nq.insert(Double.valueOf((23 + i + (i * i % 7)) % 27), i);
        }

        nq.printQueue();
        
        NiceNode nn = nq.min();
        
        nq.log.info("min node: " + nn.toString());
        
        nq.printQueue();
    }

    public NiceNode[] toArray() {
        NiceNode[] aux = new NiceNode[queue.size()];
        return queue.toArray(aux);
    }
}
