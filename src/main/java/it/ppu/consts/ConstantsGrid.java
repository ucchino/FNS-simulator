/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.consts;

public interface ConstantsGrid {

    //----------------------------------------------------------------------
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //----------------------------------------------------------------------
    //USE S3 ***ALWAYS*** - JCLOUDS SEEMS TO BE INSTABLE
    //NODES SEEMS TO HANGS TO REMAIN ALIGNED
    //----------------------------------------------------------------------
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //----------------------------------------------------------------------
    
    Boolean CUSTOMIZE_FINDER_JCLOUDS = false;
    Boolean CUSTOMIZE_FINDER_S3      = !CUSTOMIZE_FINDER_JCLOUDS;
    
    //----------------------------------------------------------------------
    
    Boolean CUSTOMIZE_EXPERIMENTAL_THREAD_MANUAL_MANAGE = true;

    //----------------------------------------------------------------------

    Integer CUSTOMIZE_CLIENTSIDE_SORTING_LIMIT = 50_000;
     
    Integer CUSTOMIZE_WAL_UNIT_MB = (1024*1204); 
    
    Integer CUSTOMIZE_POOL_SIZE_MAX_NODES = 64;
    
    //----------------------------------------------------------------------
    
    String NODE_WORKER_PREFIX = "WORKER";
    String NODE_MAIN          = "MAIN";
    String NODE_CLIENT        = "CLIENT";
    
    //----------------------------------------------------------------------
    
    Boolean ACTION_LISTENING_CONTINUE = true;
    Boolean ACTION_LISTENING_STOP     = false;

    //----------------------------------------------------------------------
    //LATCH ITEMS
    //----------------------------------------------------------------------
    
    String LATCH_NODE_STEP1 = "NodeLatchStep1";
    String LATCH_NODE_STEP2 = "NodeLatchStep2";
    String LATCH_NODE_STEP3 = "NodeLatchStep3";
    
    //----------------------------------------------------------------------
    //CACHED ITEMS
    //----------------------------------------------------------------------

    String CACHE_KEY_CACHE_NAME_INTERNODE_SPIKES = "SPIKE_CLUSTER_CACHE_INTERNODE_SPIKES";
    String CACHE_KEY_CACHE_NAME_DATABASE_SPIKES  = "SPIKE_CLUSTER_CACHE_DATABASE_SPIKES";
    String CACHE_KEY_CACHE_NAME_REPORT           = "SPIKE_CLUSTER_CACHE_NODEREPORT";

    //----------------------------------------------------------------------
    
    String CACHE_KEY_NAME_REPORT_SPLITS_COUNT  = "NODE_REPORT_SPLITS_COUNT";
    String CACHE_KEY_NAME_REPORT_SPLITS_DETAIL = "NODE_REPORT_SPLITS_DETAIL";
    
    //----------------------------------------------------------------------
    
    String TOPIC_NAME = "fns-stream";
    String TOPIC_KEY  = "fns-nodeid";

    //----------------------------------------------------------------------

}
