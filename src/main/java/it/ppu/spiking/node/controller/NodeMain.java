/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.spiking.node.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

import it.ppu.consts.ConstantsGrid;
import it.ppu.spiking.node.model.SynapseModel;
import it.ppu.spiking.node.model.NodeModel;
import it.ppu.consts.ConstantsNodeDefaults;
import it.ppu.consts.ConstantsSimulator;
import it.ppu.spiking.node.model.internode.InternodeSpikeModel;
import it.ppu.utils.grid.GridIgnite;
import it.ppu.utils.node.FastMath;
import it.ppu.utils.node.Naming;
import it.ppu.utils.node.NiceNode;
import it.ppu.utils.node.NiceQueue;
import it.ppu.utils.statistics.CollectorNOIProducer;
import it.ppu.utils.tools.Log;
import it.ppu.utils.tools.Strings;

import java.io.IOException;
import java.io.Serializable;

import java.util.List;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCountDownLatch;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.lang.IgniteRunnable;

@Slf4j
public class NodeMain implements IgniteRunnable,Serializable {

    private static final long serialVersionUID = 3248671994878955011L;
    
    private NodeNetwork NODE_NETWORK;

    private NodeNeuronsManager eventManagerNodeNeurons;
    
    private SynapsesManager eventManagerSynapse;

    private Boolean plasticity;

    //----------------------------------------------------------------------
    //queues map enqueues the spikes sended by a specific firing neuron 
    //to a specific burning neuron extendible
    //----------------------------------------------------------------------
    private HashMap<SynapseModel, NiceQueue> queuesMap;

    private double timeCurrent   = 0.0;
    private double timeElapsed   = 0.0;
    private double timeSplitStop = 0.0;

    private Long splitsCountFound = 0L;

    private final PriorityQueue<InternodeBurningSpikeModel> interNodeBurningSpikesQueueTimeToBurn = new PriorityQueue<InternodeBurningSpikeModel>();
    private final PriorityQueue<FixedBurnSpikeModel>        burningSpikesQueueTimeToBurn          = new PriorityQueue<FixedBurnSpikeModel>();

    private final FastMath FAST_MATH = new FastMath();

    private Long missedFires = 0L;

    private Boolean keepRunningSimulation = true;

    private Boolean simFast  = false;
    private Boolean lif      = false;
    private Boolean expDecay = false;

    private long[] phasesTime = new long[10];

    private CollectorNOIProducer statsCollectorNOI;

    private Long neuronsNumberMaxOnSingleNode;
    
    private Double compressionFactor;

    private IgniteCountDownLatch nodeLatch1;
    private IgniteCountDownLatch nodeLatch2;
    private IgniteCountDownLatch nodeLatch3;

    private final Integer CURRENT_NODE_ID;

    private final int THREADS_NUM;

    private Long nodeMillisStart  = System.currentTimeMillis();
    private Long splitMillisStart = System.currentTimeMillis();

    private ArrayList<InternodeSpikeModel> internodeSpikesInput = new ArrayList<InternodeSpikeModel>();
    
    private String splitsDetail = "";
    
    public NodeMain(NodeModel nodeModel) {

        this.CURRENT_NODE_ID = nodeModel.getNodeId();

        this.THREADS_NUM = nodeModel.getNodesNum();
        
        init(nodeModel);
    }

    public Double setNetworkNeuronsNumbers(Long neuronsNumberMaxOnSingleNode, Long neuronsNumberSumOnAllNodes) {

        this.neuronsNumberMaxOnSingleNode = neuronsNumberMaxOnSingleNode;

        //----------------------------------------------------------------------
        //the number neuron Ids must be divided to allow the graphical compression
        //for plotting the neuron fires
        //----------------------------------------------------------------------
        if (neuronsNumberSumOnAllNodes > 1_000_000) {
            compressionFactor = Integer.valueOf(Integer.MAX_VALUE).doubleValue() / neuronsNumberSumOnAllNodes;
        } else {
            compressionFactor = ConstantsNodeDefaults.NODETHREAD_COMPRESSION_FACTOR;
        }

        return compressionFactor;
    }

    private void init(NodeModel nodeModel) {

        NODE_NETWORK = new NodeNetwork(nodeModel);//TODOPPU DEV !!!
        
        queuesMap = new HashMap<SynapseModel, NiceQueue>();

        eventManagerNodeNeurons = new NodeNeuronsManager(NODE_NETWORK);

        plasticity = NODE_NETWORK.getNeuronPlasticity();

        initExternalInput();

        eventManagerSynapse = new SynapsesManager(NODE_NETWORK, NODE_NETWORK.getAvgNeuronalSignalSpeed());

        println("Bn---: " + NODE_NETWORK.getBn_burstsSpikeForEachFiringNeuron()     + " -> Bursts Spike For Each Firing Neuron");
        println("IBI--: " + NODE_NETWORK.getIbi_interburstSpikeIntervalTime()       + " -> Ibi (interburst Spike Interval Time)");
        println("C----: " + NODE_NETWORK.getC_threshold()                           + " -> C (threshold)");
        println("DExc-: " + NODE_NETWORK.getDExc_linearDecayForEachNeuron()         + " -> DExc (neuron linear Decay))");
        println("DInh-: " + NODE_NETWORK.getDInh_underthresholdDecayForEachNeuron() + " -> DInh (neuro underthreshold Decay)");
        println("Tarp-: " + NODE_NETWORK.getTArp_refractoryTimeForEachNeuron()      + " -> TArp (neuron refractory Time))");

        lif      = NODE_NETWORK.getLif();
        expDecay = NODE_NETWORK.getExpDecay();
        simFast  = NODE_NETWORK.isSimFast();
    }

    @Override
    public void run() {

        final Ignite ignite = GridIgnite.getIgnite();

        nodeLatch1 = ignite.countDownLatch(ConstantsGrid.LATCH_NODE_STEP1, THREADS_NUM, true, true);
        nodeLatch2 = ignite.countDownLatch(ConstantsGrid.LATCH_NODE_STEP2, THREADS_NUM, true, true);
        nodeLatch3 = ignite.countDownLatch(ConstantsGrid.LATCH_NODE_STEP3, THREADS_NUM, true, true);

        Long fires = 0L;
        
        NiceNode minFiringNeuron;

        Long firingEventNeuronId;

        Double spikeTime;

        Double tmpMinFiringTime;
        Double tmpMinInternodeBurningTime;

        Double minFixedBurnTime;

        InternodeBurningSpikeModel tmpInternodeBurningSpike;

        FixedBurnSpikeModel tmpFixedBurnSpike;

        Integer lastCollectedBurstFiringNodeId = -1;
        
        Long lastCollectedBurstFiringNeuronId = -1l;
        
        Double lastCollectedBurstFiringBurnTime = -1.0;

        //----------------------------------------------------------------------
        
        Boolean isCurrNodeNOI = NODE_NETWORK.getSimulationInputData().getNOI().contains(CURRENT_NODE_ID);

        if (isCurrNodeNOI == true) {
            statsCollectorNOI = new CollectorNOIProducer(CURRENT_NODE_ID, NODE_NETWORK.getSimulationInputData());
        }
        
        nodeMillisStart = System.currentTimeMillis();

        //----------------------------------------------------------------------
        //LOOP ON **ALL** SPLITS
        //----------------------------------------------------------------------        
        
        while(keepRunningSimulation) {
            
            //----------------------------------------------------------------------
            
            splitMillisStart = System.currentTimeMillis();
            
            //----------------------------------------------------------------------
            
            Boolean stoppedBySplitTimeout = false;
            
            //----------------------------------------------------------------------
            //LOOP TIMING ON **SPLIT Nth**
            //----------------------------------------------------------------------
            
            for (;timeCurrent < timeSplitStop; ++fires) {

                //----------------------------------------------------------------------

                if (stoppedBySplitTimeout == false) {
                    //----------------------------------------------------------------------
                    //check which is the minimum between the next firing time, the next burn
                    //due to inter-node spikes and the next burn due to bursting queue
                    //of a fire already happened
                    //----------------------------------------------------------------------
                    tmpMinFiringTime         = (eventManagerNodeNeurons.getMinFiringTime());
                    tmpInternodeBurningSpike = (interNodeBurningSpikesQueueTimeToBurn.peek());
                    tmpFixedBurnSpike        = (burningSpikesQueueTimeToBurn.peek());
                    minFixedBurnTime         = (tmpFixedBurnSpike == null) ? Double.MAX_VALUE : tmpFixedBurnSpike.getBurnTime();
                    //----------------------------------------------------------------------
                    //case of first arrival of inter-node burn
                    //----------------------------------------------------------------------
                    if(tmpInternodeBurningSpike != null) {

                        tmpMinInternodeBurningTime = tmpInternodeBurningSpike.getTimeToBurn();

                        if ((tmpMinInternodeBurningTime != null) && (tmpMinInternodeBurningTime < timeSplitStop)) {
                            if (tmpMinFiringTime != null) {

                                //----------------------------------------------------------------------
                                //internode burn processing first
                                //----------------------------------------------------------------------
                                if ((tmpMinInternodeBurningTime < tmpMinFiringTime) && (tmpMinInternodeBurningTime < minFixedBurnTime)) {
                                    InternodeSpikeModel irs = interNodeBurningSpikesQueueTimeToBurn.poll().getInternodeSpike();
                                    if (tmpMinInternodeBurningTime < timeCurrent) {
                                        println(""
                                            + "internode burning: " + tmpMinInternodeBurningTime
                                            + " min FixedBurn: "    + minFixedBurnTime
                                            + " tmpMinFiringTime: " + tmpMinFiringTime);

                                        if (interNodeBurningSpikesQueueTimeToBurn.peek() != null) {
                                            println(""
                                                + "polled: "  + irs.getBurningTime()
                                                + " peeked: " + interNodeBurningSpikesQueueTimeToBurn.peek().getTimeToBurn()
                                                + "\n");
                                        }
                                    }

                                    if ((interNodeBurningSpikesQueueTimeToBurn.peek() != null) && (irs.getBurningTime() > interNodeBurningSpikesQueueTimeToBurn.peek().getTimeToBurn())) {
                                        println(""
                                            + "polled: "  + irs.getBurningTime()
                                            + " peeked: " + interNodeBurningSpikesQueueTimeToBurn.peek().getTimeToBurn()
                                            + " syn: "    + tmpInternodeBurningSpike.getInternodeSpike().getSynapse());
                                    }
                                    //----------------------------------------------------------------------
                                    timeCurrent = tmpMinInternodeBurningTime;
                                    //----------------------------------------------------------------------
                                    burnNeuron(
                                        irs.getSynapse(),
                                        irs.getBurningTime(),
                                        irs.getFiringTime(),
                                        false);
                                    //----------------------------------------------------------------------
                                    continue;
                                    //----------------------------------------------------------------------
                                }
                            } //----------------------------------------------------------------------
                            //there is no next node-internal spike, check inter-node 
                            //against fixed burn
                            //----------------------------------------------------------------------
                            else if (tmpMinInternodeBurningTime < minFixedBurnTime) {
                                //----------------------------------------------------------------------
                                InternodeSpikeModel irs = interNodeBurningSpikesQueueTimeToBurn.poll().getInternodeSpike();
                                //----------------------------------------------------------------------
                                timeCurrent = tmpMinInternodeBurningTime;
                                //----------------------------------------------------------------------
                                burnNeuron(
                                    irs.getSynapse(),
                                    irs.getBurningTime(),
                                    irs.getFiringTime(),
                                    false);
                                //----------------------------------------------------------------------
                                continue;
                                //----------------------------------------------------------------------
                            }
                        }
                    }
                    //----------------------------------------------------------------------
                    //case of first arrival of bursting queue spike to be burn
                    //----------------------------------------------------------------------
                    if (minFixedBurnTime < timeSplitStop) {
                        if (tmpMinFiringTime != null) {
                            if (minFixedBurnTime < tmpMinFiringTime) {
                                FixedBurnSpikeModel fixedBurnSpike = burningSpikesQueueTimeToBurn.poll();
                                if (tmpFixedBurnSpike.getBurnTime() != fixedBurnSpike.getBurnTime()) {
                                    println("tada!: "
                                        + tmpFixedBurnSpike.getBurnTime()
                                        + "!="
                                        + fixedBurnSpike.getBurnTime());
                                    System.exit(ConstantsNodeDefaults.ERROR_BASE);
                                }
                                //----------------------------------------------------------------------
                                timeCurrent = minFixedBurnTime;
                                //----------------------------------------------------------------------
                                burnNeuron(
                                    fixedBurnSpike.getSynapse(),
                                    fixedBurnSpike.getBurnTime(),
                                    minFixedBurnTime,
                                    false);
                                //----------------------------------------------------------------------
                                if ((!lastCollectedBurstFiringNodeId.equals(CURRENT_NODE_ID))
                                    || (!lastCollectedBurstFiringNeuronId.equals(fixedBurnSpike.getSynapse().getAxonFiringNeuronId()))
                                    || (!lastCollectedBurstFiringBurnTime.equals(fixedBurnSpike.getBurnTime()))) {
                                    //----------------------------------------------------------------------
                                    lastCollectedBurstFiringNodeId   = CURRENT_NODE_ID;
                                    lastCollectedBurstFiringNeuronId = fixedBurnSpike.getSynapse().getAxonFiringNeuronId();
                                    lastCollectedBurstFiringBurnTime = fixedBurnSpike.getBurnTime();
                                    //----------------------------------------------------------------------
                                    if (statsCollectorNOI != null) {
                                        statsCollectorNOI.collectFireSpike(CURRENT_NODE_ID,
                                            fixedBurnSpike.getSynapse().getAxonFiringNeuronId(),
                                            fixedBurnSpike.getBurnTime(),
                                            neuronsNumberMaxOnSingleNode,
                                            compressionFactor,
                                            (fixedBurnSpike.getSynapse().getAxonFiringNeuronId() < NODE_NETWORK.getNeuronsNumberExcitatory()),
                                            (fixedBurnSpike.getSynapse().getAxonFiringNeuronId() >= NODE_NETWORK.getNeuronsNumber()),
                                            splitsCountFound);
                                    }
                                }
                                continue;
                            }
                        } else if (minFixedBurnTime < Double.MAX_VALUE) {
                            //----------------------------------------------------------------------
                            FixedBurnSpikeModel fixedBurnSpike = burningSpikesQueueTimeToBurn.poll();
                            //----------------------------------------------------------------------
                            timeCurrent = minFixedBurnTime;
                            //----------------------------------------------------------------------
                            burnNeuron(
                                fixedBurnSpike.getSynapse(),
                                fixedBurnSpike.getBurnTime(),
                                fixedBurnSpike.getFireTime(),
                                false);
                            //----------------------------------------------------------------------
                            if ((!lastCollectedBurstFiringNodeId.equals(CURRENT_NODE_ID))
                                || (!lastCollectedBurstFiringNeuronId.equals(fixedBurnSpike.getSynapse().getAxonFiringNeuronId()))
                                || (!lastCollectedBurstFiringBurnTime.equals(fixedBurnSpike.getBurnTime()))) {
                                //----------------------------------------------------------------------
                                lastCollectedBurstFiringNodeId   = CURRENT_NODE_ID;
                                lastCollectedBurstFiringNeuronId = fixedBurnSpike.getSynapse().getAxonFiringNeuronId();
                                lastCollectedBurstFiringBurnTime = fixedBurnSpike.getBurnTime();
                                //----------------------------------------------------------------------
                                if (statsCollectorNOI != null) {
                                    statsCollectorNOI.collectFireSpike(CURRENT_NODE_ID,
                                        fixedBurnSpike.getSynapse().getAxonFiringNeuronId(),
                                        fixedBurnSpike.getBurnTime(),
                                        neuronsNumberMaxOnSingleNode,
                                        compressionFactor,
                                        (fixedBurnSpike.getSynapse().getAxonFiringNeuronId() < NODE_NETWORK.getNeuronsNumberExcitatory()),
                                        (fixedBurnSpike.getSynapse().getAxonFiringNeuronId() >= NODE_NETWORK.getNeuronsNumber()),
                                        splitsCountFound);
                                }
                            }
                            //----------------------------------------------------------------------
                            continue;
                            //----------------------------------------------------------------------
                        } else {
                            stoppedBySplitTimeout = true;
                            break;
                        }
                    }
                    if ((tmpMinFiringTime == null) || (tmpMinFiringTime > timeSplitStop)) {
                        stoppedBySplitTimeout = true;
                        break;
                    }

                    //----------------------------------------------------------------------
                    //get the next neuron ready to fire in the list of the active neurons
                    //----------------------------------------------------------------------
                    minFiringNeuron = eventManagerNodeNeurons.getNextFiringNeuron();
                    //----------------------------------------------------------------------

                    if (minFiringNeuron == null) {
                        stoppedBySplitTimeout = true;
                        break;
                    }

                    firingEventNeuronId = minFiringNeuron.firingNeuronId;

                    spikeTime = minFiringNeuron.timeToFire;

                    if (spikeTime > timeSplitStop) {
                        stoppedBySplitTimeout = true;
                        break;
                    }
                } else {
                    break;
                }

                //----------------------------------------------------------------------
                //case of first firing of a burst time update
                //----------------------------------------------------------------------
                timeCurrent = spikeTime;

                //----------------------------------------------------------------------
                //firing spikes detecting and storing
                //----------------------------------------------------------------------
                if(firingEventNeuronId < NODE_NETWORK.getNeuronsNumber()) {
                    //----------------------------------------------------------------------
                    //State resetting to passive mode
                    //----------------------------------------------------------------------
                    eventManagerNodeNeurons.resetState(firingEventNeuronId);
                    eventManagerNodeNeurons.resetTimeToFire(firingEventNeuronId);
                    //----------------------------------------------------------------------
                }

                //----------------------------------------------------------------------
                //last firing time for neuron
                //----------------------------------------------------------------------
                eventManagerNodeNeurons.setLastFiringTime(firingEventNeuronId, timeCurrent);

                //----------------------------------------------------------------------
                //An external neuron fired
                //----------------------------------------------------------------------
                if (firingEventNeuronId >= NODE_NETWORK.getNeuronsNumber()) {
                    //----------------------------------------------------------------------
                    //ext time-to-fire resetting
                    //----------------------------------------------------------------------
                    eventManagerNodeNeurons.resetTimeToFire(firingEventNeuronId);
                    //----------------------------------------------------------------------
                    //external routine
                    //----------------------------------------------------------------------
                    eventManagerNodeNeurons.externalInputReset(firingEventNeuronId, timeCurrent);
                    //----------------------------------------------------------------------
                    if (timeCurrent > NODE_NETWORK.getExternalInputsFireDuration()) {
                        continue;
                    }
                }

                if (statsCollectorNOI != null) {
                    statsCollectorNOI.collectFireSpike(CURRENT_NODE_ID,
                        firingEventNeuronId,
                        spikeTime,
                        neuronsNumberMaxOnSingleNode,
                        compressionFactor,
                        (firingEventNeuronId < NODE_NETWORK.getNeuronsNumberExcitatory()),
                        (firingEventNeuronId >= NODE_NETWORK.getNeuronsNumber()),
                        splitsCountFound);
                }
                //----------------------------------------------------------------------
                //search for burning neurons connected to neuron_id
                //----------------------------------------------------------------------
                makeNeuronFireToEachconnectedBurning(firingEventNeuronId, timeCurrent);
                //----------------------------------------------------------------------
            }
            
            //----------------------------------------------------------------------
            //SYNC CYCLIC BARRIER
            //----------------------------------------------------------------------

            nodeLatch1.countDown();
            nodeLatch1.await();
            nodeLatch2 = ignite.countDownLatch(ConstantsGrid.LATCH_NODE_STEP2, THREADS_NUM, true, true);            
            
            //----------------------------------------------------------------------

            managementCollectInternodeSpike();
            
            //----------------------------------------------------------------------
            //SYNC CYCLIC BARRIER
            //----------------------------------------------------------------------

            nodeLatch2.countDown();
            nodeLatch2.await();
            nodeLatch3 = ignite.countDownLatch(ConstantsGrid.LATCH_NODE_STEP3, THREADS_NUM, true, true);            
            
            //----------------------------------------------------------------------

            managementDeliverInternodeSpike();
            
            //----------------------------------------------------------------------
            //SYNC CYCLIC BARRIER
            //----------------------------------------------------------------------

            
            nodeLatch3.countDown();
            nodeLatch3.await();
            nodeLatch1 = ignite.countDownLatch(ConstantsGrid.LATCH_NODE_STEP1, THREADS_NUM, true, true);
            
            //----------------------------------------------------------------------
            
            managementSplitCompleted();
            
            //----------------------------------------------------------------------
            
            stoppedBySplitTimeout = false;

            //----------------------------------------------------------------------
        }

        if(statsCollectorNOI != null) {
            try {
                statsCollectorNOI.close();
            } catch(IOException reason) {
                log.error("error on close collector", reason);
            }
        }
        
        Long stopMillis = System.currentTimeMillis();
        
        println("nodeId" + Naming.get(CURRENT_NODE_ID) + " total simulation time " + Strings.format(stopMillis-nodeMillisStart) + "ms");
        
        IgniteCache<String, Object> currCache = GridIgnite.getIgnite().getOrCreateCache(ConstantsGrid.CACHE_KEY_CACHE_NAME_REPORT);

        currCache.put(ConstantsGrid.CACHE_KEY_NAME_REPORT_SPLITS_DETAIL + CURRENT_NODE_ID, splitsDetail);
        currCache.put(ConstantsGrid.CACHE_KEY_NAME_REPORT_SPLITS_COUNT  + CURRENT_NODE_ID, splitsCountFound);
    }

    private void managementCollectInternodeSpike() {
        
        internodeSpikesInput = new ArrayList<InternodeSpikeModel>();
        
        //----------------------------------------------------------------------
        //SINGLE CACHE
        //----------------------------------------------------------------------
        //        String prefix = GridIgnite.getKeyPrefix(CURRENT_NODE_ID);
        //
        //        IgniteCache<String, Object> cache = GridIgnite.getIgnite().cache(ConstantsGrid.CACHE_KEY_CACHE_NAME_INTERNODE_SPIKES);
        //
        //        ScanQuery<String, Object> scanQuery = new ScanQuery<>((key, value) -> key.startsWith(prefix));
        //
        //        try (QueryCursor<Cache.Entry<String, Object>> cursor = cache.query(scanQuery)) {
        //            Iterator<Cache.Entry<String, Object>> iterator = cursor.iterator();
        //
        //            while (iterator.hasNext()) {
        //
        //                Cache.Entry<String, Object> entry = iterator.next();
        //
        //                String key = entry.getKey();
        //
        //                InternodeSpikeModel item = GridIgnite.getAndRemoveGlobalCacheInternodeSpikes(key);
        //                internodeSpikesTemp.add(item);
        //            }
        //        }
        //----------------------------------------------------------------------
        //MULTI CACHE
        //----------------------------------------------------------------------
        String cacheName = Naming.getCacheName(CURRENT_NODE_ID);
        
        IgniteCache cache = GridIgnite.getIgnite().cache(cacheName);

        CachePeekMode[] peekModes = new CachePeekMode[]{CachePeekMode.PRIMARY};

        long totalKeys = cache.sizeLong(peekModes);
        
        log.info("Recovering " + totalKeys + " keys from cache");
        
        if(totalKeys > ConstantsGrid.CUSTOMIZE_CLIENTSIDE_SORTING_LIMIT) {

            //----------------------------------------------------------------------
            
            SqlFieldsQuery query = new SqlFieldsQuery("SELECT _key, _val FROM " + InternodeSpikeModel.class.getSimpleName() + " ORDER BY _key ASC");

            QueryCursor<List<?>> cursor = cache.query(query);

             Iterator<List<?>> it = cursor.iterator();
             
            //----------------------------------------------------------------------
            while (it.hasNext()) {
                //----------------------------------------------------------------------
                List<?> entry = it.next();
                //----------------------------------------------------------------------
                Long                key   = (Long)                entry.get(0);
                InternodeSpikeModel value = (InternodeSpikeModel) entry.get(1);
                //----------------------------------------------------------------------
                internodeSpikesInput.add((InternodeSpikeModel)value);
                //----------------------------------------------------------------------
                cache.remove(key);
                //----------------------------------------------------------------------
            }             
        } else {
            Iterator<IgniteCache.Entry<Long, Object>> iterator = cache.iterator();

            ArrayList<Long> keysList = new ArrayList<Long>();
        
            while(iterator.hasNext()) {
                //----------------------------------------------------------------------
                IgniteCache.Entry<Long, Object> entry = iterator.next();
                //----------------------------------------------------------------------
                Long key = entry.getKey();
                //----------------------------------------------------------------------
                keysList.add(key);
                //----------------------------------------------------------------------
            }
            //----------------------------------------------------------------------
            Collections.sort(keysList);
            //----------------------------------------------------------------------
            for(Long key:keysList) {
                //----------------------------------------------------------------------
                Object value = cache.get(key);
                //----------------------------------------------------------------------
                //L'instanceof serve per via di questo caso:
                //(java.lang.ClassCastException) java.lang.ClassCastException: 
                //class org.apache.ignite.internal.processors.cache.query.CacheQueryEntry 
                //cannot be cast to class it.ppu.spiking.node.model.internode.InternodeSpikeModel 
                //----------------------------------------------------------------------
                if(value instanceof InternodeSpikeModel) {
                    internodeSpikesInput.add((InternodeSpikeModel)value);
                }

                cache.remove(key);
            }
        }
        //----------------------------------------------------------------------
        Collections.sort(internodeSpikesInput);
        //----------------------------------------------------------------------
    }
    
    private void managementDeliverInternodeSpike() {
        //----------------------------------------------------------------------
        //the comparison is done against the stoptime, since when this method is called 
        //it still holds the value for the last split run (not the current one) and
        //current time may hold a very old value
        //----------------------------------------------------------------------
        Iterator<InternodeSpikeModel> iterator = internodeSpikesInput.iterator();
        //----------------------------------------------------------------------
        while(iterator.hasNext()) {
            //----------------------------------------------------------------------
            InternodeSpikeModel internodeSpikeLocal = iterator.next();
            //----------------------------------------------------------------------
            if (internodeSpikeLocal.getBurningTime() >= timeSplitStop) {
                interNodeBurningSpikesQueueTimeToBurn.add(new InternodeBurningSpikeModel(internodeSpikeLocal));
            } else {
                statsCollectorNOI.collectMissedFire(internodeSpikeLocal, timeCurrent, missedFires++);
            }
            //----------------------------------------------------------------------
            iterator.remove();
            //----------------------------------------------------------------------
        }
        //----------------------------------------------------------------------
        internodeSpikesInput = new ArrayList<InternodeSpikeModel>();
        //----------------------------------------------------------------------
    }
    
    private void managementSplitCompleted() {

        //----------------------------------------------------------------------

        Long currMillis = System.currentTimeMillis();
            
        //----------------------------------------------------------------------
            
        final double timeSimulationLimit = NODE_NETWORK.getSimulationInputData().getTimeSimulationTotal();
        final double timeSimulationCycle = NODE_NETWORK.getSimulationInputData().getTimeSimulationCycle();

        final Long splitsCountExpected = (long) Math.ceil(timeSimulationLimit / timeSimulationCycle);

        final boolean isRand = NODE_NETWORK.getSimulationInputData().getSimulationConfig().getSimNoRandom();
        
        //----------------------------------------------------------------------
        
        splitsDetail += ""
            + "uuid="     + GridIgnite.getNodeIdUUIDRestartProof() + ";"
            + "norand="   + isRand                                 + ";" 
            + "nodeId="   + Naming.get(CURRENT_NODE_ID)            + ";" 
            + "split="    + Naming.get(splitsCountFound)           + ";" 
            + (currMillis-nodeMillisStart) + "\n";

        //----------------------------------------------------------------------
            
        splitsCountFound++;
            
        //----------------------------------------------------------------------

        boolean stop = false;
        
        String reason = "";
        
        if (timeElapsed      >= timeSimulationLimit) {stop = true; reason += "+overtime";}
        if (splitsCountFound > splitsCountExpected)  {stop = true; reason += "+oversplit";}

        if(stop == true) {
            println("stop sim raised - time: " + timeElapsed + "/" + timeSimulationLimit + " - split: "+ (splitsCountFound-1) + "/" + splitsCountExpected + " - reason: " + reason);

            keepRunningSimulation = false;

            return;
        }

        //----------------------------------------------------------------------
        
        timeElapsed += timeSimulationCycle;

        //----------------------------------------------------------------------
        
        double newStopTime = (timeElapsed > timeSimulationLimit) ? timeSimulationLimit : timeElapsed;

        //---------------------------------------------------------------------- 
        //current time is updated with the last value for stoptime, otherwise it can hold
        //value belonging to a past split and then could generate inconsistency against
        //spikes coming from external nodes
        //----------------------------------------------------------------------
        if (timeCurrent < timeSplitStop) {
            timeCurrent = timeSplitStop;
        }

        timeSplitStop = newStopTime;

        //----------------------------------------------------------------------
        
        Long splitMillisElapsed = (System.currentTimeMillis()-splitMillisStart);

        //----------------------------------------------------------------------

        println("running split " + Naming.get(splitsCountFound) + "/" + Naming.get(splitsCountExpected) + " on node" + Naming.get(CURRENT_NODE_ID) + " with new stop simulated time: " + newStopTime + " - ELAPSED " + Strings.format(splitMillisElapsed) + "ms");

        //----------------------------------------------------------------------
    }

    /**
     * Adds a new internode fire spike
     *
     * @param syn the inter-node synapse object through wich the spike signal is
     * sent
     * @param fireTime the fire-spike generation time
     */
    private void addInternodeFire(SynapseModel syn, Double fireTime) {
        
        Double axonalDelay = eventManagerSynapse.getAxonalDelay(syn);

        InternodeSpikeModel internodeSpike = new InternodeSpikeModel(syn, fireTime + axonalDelay, fireTime, axonalDelay);

        Integer nodeIdDestination = internodeSpike.getSynapse().getBurningNodeId();

        //----------------------------------------------------------------------
        //MULTI CACHE
        //----------------------------------------------------------------------
        GridIgnite.insertMultiCacheInternodeSpikes(nodeIdDestination, internodeSpike);
        //----------------------------------------------------------------------
    }

    /**
     * @return the (unique) node id for the current node
     */
    public Integer getNodeId() {
        return CURRENT_NODE_ID;
    }

    /**
     * create and adds a new inter-node synapse
     *
     * @param firingNodeId the id of the firing node
     * @param firingNeuronId the id of the firing neuron (within a node)
     * @param burningNodeId the id of the burning node
     * @param burningNeuronId the id of the burning neuron (within a node)
     * @param postsynapticWeightSinapseMu the postsynaptic weight for the
     * synapse
     * @param averageLengthInternodeAxonLambda the avg length of the inter-node
     * axon
     */
    public void addInternodeSynapse(
        Integer firingNodeId, Long firingNeuronId,
        Integer burningNodeId, Long burningNeuronId,
        Double postsynapticWeightSinapseMu,
        Double averageLengthInternodeAxonLambda) {

        eventManagerSynapse.addInternodeSynapse(
            firingNodeId, firingNeuronId,
            burningNodeId, burningNeuronId,
            postsynapticWeightSinapseMu,
            NODE_NETWORK.getPresynapticForNeuron(firingNeuronId),
            averageLengthInternodeAxonLambda);
    }

    public Long getNeuronsNumber() {
        return NODE_NETWORK.getNeuronsNumber();
    }

    public Long getNeuronsNumberExcitatory() {
        return NODE_NETWORK.getNeuronsNumberExcitatory();
    }

    public Long getNeuronsNumberInhibithory() {
        return NODE_NETWORK.getNeuronsNumberInhibithory();
    }

    public Double getExcitatoryPresynapticWeight() {
        return NODE_NETWORK.getWPre_presynapticWeightExcitatory();
    }

    public boolean hasExternalInput() {
        return NODE_NETWORK.hasExternalInputs();
    }

    public Integer getExternalInputs() {
        return NODE_NETWORK.getExternalInputs();
    }

    /**
     * Plasticity Rule. Multiplicative Learning Rule using STDP (soft-bound)
     * Spike time depending plasticity
     *
     * LTP: Pw = Pwold + (pwmax - Pwold)*Etap*(-delta/taup) LTD: Pw = Pwold -
     * Pwold*Etam*(delta/taum) with delta = tpost - tpre
     *
     * NB: in the case of LTD, tpost represents the burning neuron last burning
     * time, whereas tpre is the current "tempo". This rule is applied for only
     * exc-exc intermolule connections
     *
     * Plasticity rule for firing events. Update postsynaptic weight, increasing
     * it according to the delta i between the firing time and the last burning
     * time of the firing neuron.
     *
     * @param syn
     * @param fireTime
     */
    private void fire_ltp(SynapseModel syn, Double fireTime) {
        if (!plasticity) {
            return;
        }
        if (syn.getAxonFiringNeuronId() == null) {
            return;
        }

        Long firingNeuronId = syn.getAxonFiringNeuronId();

        ArrayList<SynapseModel> synapses = eventManagerSynapse.getFiringNeuronSynapses(firingNeuronId);
        ArrayList<SynapseModel> interNodeSynapses = eventManagerSynapse.getFiringNeuronInternodesSynapses(firingNeuronId);

        for (int i = 0; i < synapses.size(); ++i) {
            if (synapses.get(i).getLastBurningTime() == null) {
                continue;
            }

            Double delta = fireTime - synapses.get(i).getLastBurningTime();

            if (delta < NODE_NETWORK.getTo_plasticityRulesTimeout()) {
                Double wp = synapses.get(i).getPostSynapticWeight();
                double wpold = wp;
                wp += simFast
                    ? (NODE_NETWORK.getPwMax_maxPostsynapticWeightForPlasticityRules() - wp) * NODE_NETWORK.getEtap_etaPlusLearningPlasticity() * FAST_MATH.fastExp(-delta / NODE_NETWORK.getTaup_tauPlusLongTermPotentiationPlasticity())
                    : (NODE_NETWORK.getPwMax_maxPostsynapticWeightForPlasticityRules() - wp) * NODE_NETWORK.getEtap_etaPlusLearningPlasticity() * Math.exp(-delta / NODE_NETWORK.getTaup_tauPlusLongTermPotentiationPlasticity());
                eventManagerSynapse.setIntraNodePostSynapticWeight(synapses.get(i), wp);
            }
        }

        for (int i = 0; i < interNodeSynapses.size(); ++i) {
            if (interNodeSynapses.get(i).getLastBurningTime() == null) {
                continue;
            }

            Double delta = fireTime - interNodeSynapses.get(i).getLastBurningTime();

            if (delta < NODE_NETWORK.getTo_plasticityRulesTimeout()) {
                Double wp = interNodeSynapses.get(i).getPostSynapticWeight();
                double wpold = wp;
                wp += simFast
                    ? (NODE_NETWORK.getPwMax_maxPostsynapticWeightForPlasticityRules() - wp) * NODE_NETWORK.getEtap_etaPlusLearningPlasticity() * FAST_MATH.fastExp(-delta / NODE_NETWORK.getTaup_tauPlusLongTermPotentiationPlasticity())
                    : (NODE_NETWORK.getPwMax_maxPostsynapticWeightForPlasticityRules() - wp) * NODE_NETWORK.getEtap_etaPlusLearningPlasticity() * Math.exp(-delta / NODE_NETWORK.getTaup_tauPlusLongTermPotentiationPlasticity());
                eventManagerSynapse.setIntraNodePostSynapticWeight(
                    interNodeSynapses.get(i),
                    wp);
            }
        }
    }

    /**
     * Plasticity rule for burning events. Update postsynaptic weight,
     * decreasing it according to the delta between the burning time and the
     * last firing time of the burning neuron.
     *
     * @param synapse
     * @param lastBurningTime
     * @param fireTime
     */
    private void burning_ltd(SynapseModel synapse, Double burningTime, Double lastFiringTime) {
        if (!plasticity) {
            return;
        }

        if (synapse.getBurningNeuronId() == null) {
            return;
        }

        synapse.setLastBurningTime(burningTime);

        Double delta = burningTime - lastFiringTime;

        if (delta < NODE_NETWORK.getTo_plasticityRulesTimeout()) {
            Double wp = synapse.getPostSynapticWeight();
            double wpold = wp;
            wp -= simFast
                ? wp * NODE_NETWORK.getEtam_etaMinusLearningPlasticity() * FAST_MATH.fastExp(-delta / NODE_NETWORK.getTaum_tauMinusLongTermDepressionPlasticity())
                : wp * NODE_NETWORK.getEtam_etaMinusLearningPlasticity() * Math.exp(-delta / NODE_NETWORK.getTaum_tauMinusLongTermDepressionPlasticity());
            if (wp < 0) {
                wp = 0.0;
            }
            eventManagerSynapse.setIntraNodePostSynapticWeight(synapse, wp);
        }
    }

    private void makeNeuronFireToEachconnectedBurning(Long firingNeuronId, Double currentTime) {

        ArrayList<SynapseModel> synapses          = eventManagerSynapse.getFiringNeuronSynapses(firingNeuronId);
        ArrayList<SynapseModel> interNodeSynapses = eventManagerSynapse.getFiringNeuronInternodesSynapses(firingNeuronId);

        if (NODE_NETWORK.isExternalInput(firingNeuronId)) {

            int eod = NODE_NETWORK.getExternalInputsNumberOfTargetsFireOutDegree();
            int eoj = NODE_NETWORK.getExternalOutJump();

            final boolean FROM_EXTERNAL_INPUT = true;

            if (eod == 1) {
                burnNeuron(
                    null,
                    firingNeuronId,
                    CURRENT_NODE_ID,
                    firingNeuronId % NODE_NETWORK.getNeuronsNumber(),
                    CURRENT_NODE_ID,
                    0.1, //AXON LENGTH
                    1.0, //Post-synaptic weight
                    NODE_NETWORK.getExternalInputsFireAmplitude(), //Pre-synaptic weight
                    currentTime,
                    currentTime,
                    FROM_EXTERNAL_INPUT);
            } else {
                for (int i = 0; i < eod; ++i) {
                    burnNeuron(
                        null,
                        firingNeuronId,
                        CURRENT_NODE_ID,
                        (firingNeuronId + (eoj * i)) % NODE_NETWORK.getNeuronsNumber(),
                        CURRENT_NODE_ID,
                        0.1, //AXON LENGTH
                        1.0, //Post-synaptic weight
                        NODE_NETWORK.getExternalInputsFireAmplitude(), //Pre-synaptic weight
                        currentTime,
                        currentTime,
                        FROM_EXTERNAL_INPUT);
                }
            }
            return;
        }
        
        for (int i = 0; i < synapses.size(); ++i) {

            fire_ltp(synapses.get(i), currentTime);
            //----------------------------------------------------------------------
            //this is an inter-node synapse, the burning node must deal with this spike
            //----------------------------------------------------------------------
            if (!(synapses.get(i).getBurningNodeId().equals(CURRENT_NODE_ID))) {
                continue;
            }

            burnNeuron(synapses.get(i), currentTime, currentTime, false);

            for (int j = 1; j < NODE_NETWORK.getBn_burstsSpikeForEachFiringNeuron(); ++j) {
                burningSpikesQueueTimeToBurn.add(new FixedBurnSpikeModel(synapses.get(i),
                    (currentTime + NODE_NETWORK.getIbi_interburstSpikeIntervalTime() * j),
                    currentTime));
            }
        }

        for (int i = 0; i < interNodeSynapses.size(); ++i) {
            for (int j = 0; j < NODE_NETWORK.getBn_burstsSpikeForEachFiringNeuron(); ++j) {
                addInternodeFire(interNodeSynapses.get(i), (currentTime + NODE_NETWORK.getIbi_interburstSpikeIntervalTime() * j));
            }
        }
    }

    private void burnNeuron(SynapseModel synapseNullable, Double burnTime, Double fireTime, Boolean fromExternalInput) {
        burnNeuron(
            synapseNullable,
            synapseNullable.getAxonFiringNeuronId(),
            synapseNullable.getAxonFiringNodeId(),
            synapseNullable.getBurningNeuronId(),
            synapseNullable.getBurningNodeId(),
            synapseNullable.getAverageLengthInternodeAxonLambda(),
            synapseNullable.getPostSynapticWeight(),
            synapseNullable.getPreSynapticWeight(),
            burnTime,
            fireTime,
            fromExternalInput);
    }

    private void burnNeuron(
        SynapseModel synapseNullable,
        long firingNeuronId,
        int firingNodeId,
        long burningNeuronId,
        int burningNodeId,
        double axon_length,
        double postsynapticWeight,
        double presynapticWeight,
        double burnTime,
        double fireTime,
        boolean fromExternalInput) {
        double tmp, dsxNumerator, dsxDenominator, riseTermXFactor, oldSx;
        int arp;

        //----------------------------------------------------------------------
        //distinguish cases of no initial network activity : already activated
        //----------------------------------------------------------------------
        
        arp = (eventManagerNodeNeurons.getLastFiringTime(burningNeuronId).equals(ConstantsNodeDefaults.NODETHREAD_FIRING_TIME)) ? 0 : 1;

        //----------------------------------------------------------------------
        //absolutely refractory period check
        //----------------------------------------------------------------------
        if (burnTime >= ((eventManagerNodeNeurons.getLastFiringTime(burningNeuronId) + NODE_NETWORK.getTArp_refractoryTimeForEachNeuron() + ((NODE_NETWORK.getBn_burstsSpikeForEachFiringNeuron() - 1) * NODE_NETWORK.getIbi_interburstSpikeIntervalTime())) * arp)) {
            long burnStartTime = System.currentTimeMillis();

            if (!fromExternalInput) {
                burning_ltd(synapseNullable, burnTime, eventManagerNodeNeurons.getLastFiringTime(burningNeuronId));
            }
            tmp = eventManagerNodeNeurons.getState(burningNeuronId);

            //----------------------------------------------------------------------
            //passive state linear decay
            //----------------------------------------------------------------------
            if (tmp < eventManagerNodeNeurons.getSpikingThreshold()) {

                Double decay;

                //----------------------------------------------------------------------
                //LINEAR decay
                //----------------------------------------------------------------------
                if (!expDecay) {
                    decay = (eventManagerNodeNeurons.getLinearDecayD(burningNeuronId) * (burnTime - (eventManagerNodeNeurons.getLastBurningTime(burningNeuronId))));
                    eventManagerNodeNeurons.setState(
                        burningNeuronId,
                        tmp - decay);
                } 
                //----------------------------------------------------------------------
                //EXPONENTIAL decay
                //----------------------------------------------------------------------
                //Sj = Spj + A * W -Tl =  A W + Spj e^(-delta t / D)
                //Tl  =  Spj (1 - e^(-delta t / D))
                //----------------------------------------------------------------------
                else {
                    decay = simFast ? (tmp * (1 - FAST_MATH.fastExp(
                        -(burnTime
                        - eventManagerNodeNeurons.getLastBurningTime(burningNeuronId))
                        / eventManagerNodeNeurons.getLinearDecayD(burningNeuronId)))) : (tmp * (1 - Math.exp(
                        -(burnTime
                        - eventManagerNodeNeurons.getLastBurningTime(burningNeuronId))
                        / eventManagerNodeNeurons.getLinearDecayD(burningNeuronId))));
                    eventManagerNodeNeurons.setState(
                        burningNeuronId,
                        tmp - decay);
                }

                if (eventManagerNodeNeurons.getState(burningNeuronId) < 0.0) {
                    eventManagerNodeNeurons.setState(burningNeuronId, 0.0);
                }
            }
            phasesTime[0] += System.currentTimeMillis() - burnStartTime;
            burnStartTime = System.currentTimeMillis();

            //----------------------------------------------------------------------
            //BURNING NEURON
            //----------------------------------------------------------------------
            double sx = eventManagerNodeNeurons.getState(burningNeuronId);
            oldSx = sx;

            //----------------------------------------------------------------------
            //step in state
            //----------------------------------------------------------------------
            double sy = postsynapticWeight * presynapticWeight;

            //----------------------------------------------------------------------
            //UPDATING List of Active Neurons case of passive neuron
            //----------------------------------------------------------------------
            if (eventManagerNodeNeurons.getTimeToFire(burningNeuronId).equals(ConstantsNodeDefaults.NODETHREAD_FIRING_TIME)) {
                oldSx = sx;
                sx = ((sx + sy) < 0) ? 0 : sx + sy;
                eventManagerNodeNeurons.setState(burningNeuronId, sx);

                //----------------------------------------------------------------------
                //passive to active
                //----------------------------------------------------------------------
                if (sx >= eventManagerNodeNeurons.getSpikingThreshold()) {
                    //nnMan.setTimeToFire(s.getBurning(), burnTime+ 1.0/(sx-1));
                    if (lif) {
                        eventManagerNodeNeurons.setTimeToFire(burningNeuronId,
                            burnTime + ConstantsNodeDefaults.NODETHREAD_EPSILON_5);
                    } else {
                        double activeTransitionDelay = (1.0 / (sx - 1));
                        eventManagerNodeNeurons.setTimeToFire(
                            burningNeuronId,
                            burnTime + activeTransitionDelay);
                    }
                    eventManagerNodeNeurons.addActiveNeuron(
                        burningNeuronId,
                        eventManagerNodeNeurons.getTimeToFire(burningNeuronId),
                        timeCurrent, "burnNeuron-passive to active");
                }
                phasesTime[1] += System.currentTimeMillis() - burnStartTime;
            } 
            //----------------------------------------------------------------------
            //case of active neuron avoid update on lif
            //----------------------------------------------------------------------
            else if (!lif) {
                if (eventManagerNodeNeurons.getTimeToFire(burningNeuronId) == 0.0) {
                    eventManagerNodeNeurons.setTimeToFire(burningNeuronId, ConstantsNodeDefaults.NODETHREAD_EPSILON_5);
                }

                if (sx >= eventManagerNodeNeurons.getSpikingThreshold()) {
                    eventManagerNodeNeurons.removeActiveNeuron(burningNeuronId);
                    if ((burnTime < eventManagerNodeNeurons.getTimeToFire(burningNeuronId)) && (!eventManagerNodeNeurons.getLastBurningTime(burningNeuronId).equals(ConstantsNodeDefaults.NODETHREAD_BURNING_TIME))) {

                        //----------------------------------------------------------------------
                        //Rise Term
                        //----------------------------------------------------------------------
                        riseTermXFactor = (burnTime == eventManagerNodeNeurons.getLastBurningTime(burningNeuronId)) ? ConstantsNodeDefaults.NODETHREAD_EPSILON_5 : (burnTime - eventManagerNodeNeurons.getLastBurningTime(burningNeuronId));
                        tmp = (sx - 1) * riseTermXFactor;
                        dsxNumerator = (sx - 1) * tmp;
                        dsxDenominator = 1.0 - tmp;
                        sx += (dsxNumerator / dsxDenominator);
                        //----------------------------------------------------------------------
                    }
                    //----------------------------------------------------------------------
                    oldSx = sx;
                    sx += sy;
                    //----------------------------------------------------------------------
                    eventManagerNodeNeurons.setState(burningNeuronId, sx);
                    //----------------------------------------------------------------------
                    //active to passive
                    //----------------------------------------------------------------------
                    if (sx < eventManagerNodeNeurons.getSpikingThreshold()) {
                        eventManagerNodeNeurons.removeActiveNeuron(burningNeuronId);
                        eventManagerNodeNeurons.resetTimeToFire(burningNeuronId);
                    } else {
                        //----------------------------------------------------------------------
                        //updating firing delay
                        //----------------------------------------------------------------------
                        eventManagerNodeNeurons.setTimeToFire(burningNeuronId, burnTime + 1.0 / (sx - 1));
                        eventManagerNodeNeurons.setState(burningNeuronId, sx);
                        eventManagerNodeNeurons.addActiveNeuron(burningNeuronId,
                            eventManagerNodeNeurons.getTimeToFire(burningNeuronId),
                            timeCurrent,
                            "burnNeuron-updating firing delay");
                    }
                    //----------------------------------------------------------------------
                    //active to passive
                    //----------------------------------------------------------------------
                    if (sx < 0) {
                        //----------------------------------------------------------------------
                        sx = 0.0;
                        oldSx = sx;
                        //----------------------------------------------------------------------
                        eventManagerNodeNeurons.setState(burningNeuronId, sx);
                        eventManagerNodeNeurons.removeActiveNeuron(burningNeuronId);
                        eventManagerNodeNeurons.resetTimeToFire(burningNeuronId);
                        //----------------------------------------------------------------------
                    }
                } else {
                    //----------------------------------------------------------------------
                    oldSx = sx;
                    //----------------------------------------------------------------------
                    eventManagerNodeNeurons.removeActiveNeuron(burningNeuronId);
                    eventManagerNodeNeurons.resetTimeToFire(burningNeuronId);
                    //----------------------------------------------------------------------
                }
                phasesTime[2] += System.currentTimeMillis() - burnStartTime;
            }

            //----------------------------------------------------------------------
            //end of case of active neuron
            //----------------------------------------------------------------------
            burnStartTime = System.currentTimeMillis();

            eventManagerNodeNeurons.setLastBurningTime(burningNeuronId, burnTime);
            phasesTime[4] += System.currentTimeMillis() - burnStartTime;

            //----------------------------------------------------------------------
            //collecting the spike
            //----------------------------------------------------------------------
            if (statsCollectorNOI != null) {
                statsCollectorNOI.collectBurnSpike(
                    firingNeuronId,
                    firingNodeId,
                    burningNeuronId,
                    burningNodeId,
                    burnTime,
                    fromExternalInput,
                    oldSx,
                    sy,
                    postsynapticWeight,
                    presynapticWeight,
                    eventManagerNodeNeurons.getTimeToFire(burningNeuronId),
                    fireTime,
                    splitsCountFound,
                    false);
            }
            phasesTime[3] += System.currentTimeMillis() - burnStartTime;
        } else {
            //----------------------------------------------------------------------
            //collecting the spike
            //----------------------------------------------------------------------
            if (statsCollectorNOI != null) {
                statsCollectorNOI.collectBurnSpike(
                    firingNeuronId,
                    firingNodeId,
                    burningNeuronId,
                    burningNodeId,
                    burnTime,
                    fromExternalInput,
                    null,
                    null,
                    postsynapticWeight,
                    presynapticWeight,
                    eventManagerNodeNeurons.getTimeToFire(burningNeuronId),
                    fireTime,
                    splitsCountFound,
                    true);
            }
        }
    }

    private void initExternalInput() {

        println("initializing external input");

        for (int j = 0; j < NODE_NETWORK.getExternalInputs(); ++j) {
            eventManagerNodeNeurons.externalInputReset(NODE_NETWORK.getNeuronsNumber() + j, 0.0);
        }

        println("external input initialization done");
    }

    public void printQueues() {
        Iterator<SynapseModel> it = queuesMap.keySet().iterator();

        println("printing queues: ");

        while (it.hasNext()) {
            SynapseModel s = it.next();
            println(s + ": ");
            ((NiceQueue) queuesMap.get(s)).printQueue();

        }
        println("");
    }

    private void println(String s) {
        Log.doo(log,"nodeId" + Naming.get(CURRENT_NODE_ID) + "] " + s);
    }
}

final class FixedBurnSpikeModel implements Comparable<FixedBurnSpikeModel> {

    private final SynapseModel synapse;

    private final Double burnTime;
    private final Double fireTime;

    public FixedBurnSpikeModel(SynapseModel synapse, Double burnTime, Double fireTime) {
        this.synapse = synapse;

        this.burnTime = burnTime;
        this.fireTime = fireTime;
    }

    public SynapseModel getSynapse() {
        return synapse;
    }

    public Double getBurnTime() {
        return burnTime;
    }

    public Double getFireTime() {
        return fireTime;
    }

    @Override
    public int compareTo(FixedBurnSpikeModel node) {
        return Double.compare(burnTime, node.getBurnTime());
    }

    @Override
    public String toString() {
        return "fixed burn spike: " + synapse + ", time to burn: " + burnTime;
    }
}

final class InternodeBurningSpikeModel implements Comparable<InternodeBurningSpikeModel> {

    private final Double timeToBurn;

    private final InternodeSpikeModel internodeSpike;

    public InternodeBurningSpikeModel(InternodeSpikeModel internodeSpike) {
        this.internodeSpike = internodeSpike;
        this.timeToBurn     = internodeSpike.getBurningTime();
    }

    public Double getTimeToBurn() {
        return timeToBurn;
    }

    public InternodeSpikeModel getInternodeSpike() {
        return internodeSpike;
    }

    @Override
    public String toString() {
        String out = "EMPTY";

        if (internodeSpike != null) {
            out = internodeSpike.toString();
        }

        return "interNodeSpike: " + out + ",\ntimeToBurn: " + timeToBurn;
    }

    @Override
    public int compareTo(InternodeBurningSpikeModel node) {
        return timeToBurn.compareTo(node.getTimeToBurn());
    }
}
