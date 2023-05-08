/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */
package it.ppu.utils.grid;

import com.amazonaws.auth.BasicAWSCredentials;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.EventType;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.IgniteEvents;
import org.apache.ignite.events.CacheEvent;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.cloud.TcpDiscoveryCloudIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;

import it.ppu.consts.ConstantsGrid;
import it.ppu.consts.ConstantsSimulator;
import it.ppu.spiking.node.model.internode.InternodeSpikeModel;
import it.ppu.utils.grid.model.AwsIdentity;
import it.ppu.utils.node.Naming;
import it.ppu.utils.statistics.model.CollectorBurnModel;
import it.ppu.utils.statistics.model.CollectorFireModel;
import it.ppu.utils.statistics.model.CollectorKey;
import it.ppu.utils.statistics.model.CollectorMissedFireModel;
import it.ppu.utils.tools.Strings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.cache.QueryEntity;

import org.apache.ignite.cache.affinity.Affinity;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.IgniteNodeAttributes;
import org.apache.ignite.spi.discovery.tcp.ipfinder.s3.TcpDiscoveryS3IpFinder;
import org.w3c.dom.traversal.NodeFilter;

@Slf4j
public final class GridIgnite implements Serializable {

    private static final String LOG_DATA   = "./log";
    private static final String LOG_CONFIG = "java.util.logging.properties";

    private static final long serialVersionUID = 3248671994878955030L;

    private static Ignite igniteLocalInstance = null;

    private static IgniteAtomicLong atomicLong = null;

    private static final CacheAtomicityMode ATOMICITY_MODE = CacheAtomicityMode.ATOMIC;

    public static void updateCacheDatabaseSpikes(Object data) {

        if (data == null) return;

        if (data instanceof CollectorBurnModel) {
            CollectorKey       key   = ((CollectorBurnModel) data).getKey();
            CollectorBurnModel value = ((CollectorBurnModel) data);

            igniteLocalInstance.getOrCreateCache(ConstantsGrid.CACHE_KEY_CACHE_NAME_DATABASE_SPIKES).put(key, value);
        }

        if (data instanceof CollectorFireModel) {
            CollectorKey       key   = ((CollectorFireModel) data).getKey();
            CollectorFireModel value = ((CollectorFireModel) data);

            igniteLocalInstance.getOrCreateCache(ConstantsGrid.CACHE_KEY_CACHE_NAME_DATABASE_SPIKES).put(key, value);
        }

        if (data instanceof CollectorMissedFireModel) {
            CollectorKey             key   = ((CollectorMissedFireModel) data).getKey();
            CollectorMissedFireModel value = ((CollectorMissedFireModel) data);

            igniteLocalInstance.getOrCreateCache(ConstantsGrid.CACHE_KEY_CACHE_NAME_DATABASE_SPIKES).put(key, value);
        }
    }

    private static void setupLogging() throws IOException {

        Path path = Paths.get(LOG_DATA);

        if ((new File(LOG_DATA)).exists() == false) {
            Files.createDirectory(path);
        }

        if ((new File(LOG_CONFIG)).exists() == true) {
            return;
        }

        String str = ""
            + "\n############################################################"
            + "\n#  	Default Logging Configuration File"
            + "\n#"
            + "\n# You can use a different file by specifying a filename"
            + "\n# with the java.util.logging.config.file system property."
            + "\n# For example java -Djava.util.logging.config.file=myfile"
            + "\n############################################################"
            + "\n"
            + "\n############################################################"
            + "\n#  	Global properties"
            + "\n############################################################"
            + "\n"
            + "\n# 'handlers' specifies a comma separated list of log Handler"
            + "\n# classes.  These handlers will be installed during VM startup."
            + "\n# Note that these classes must be on the system classpath."
            + "\n# By default we only configure a ConsoleHandler, which will only"
            + "\n# show messages at the INFO and above levels."
            + "\n"
            + "\n# To also add the FileHandler, use the following line instead."
            + "\n#handlers=java.util.logging.ConsoleHandler"
            + "\nhandlers=java.util.logging.FileHandler, java.util.logging.ConsoleHandler"
            + "\n"
            + "\n# Default global logging level."
            + "\n# This specifies which kinds of events are logged across"
            + "\n# all loggers.  For any given facility this global level"
            + "\n# can be overriden by a facility specific level"
            + "\n# Note that the ConsoleHandler also has a separate level"
            + "\n# setting to limit messages printed to the console."
            + "\n.level=INFO"
            + "\n"
            + "\n# SEVERE (highest)"
            + "\n# WARNING"
            + "\n# INFO"
            + "\n# CONFIG"
            + "\n# FINE"
            + "\n# FINER"
            + "\n# FINEST"
            + "\n"
            + "\n############################################################"
            + "\n# Handler specific properties. Limit to 50MB"
            + "\n# Describes specific configuration info for Handlers."
            + "\n############################################################"
            + "\n"
            + "\n# default file output is in user's home directory."
            + "\njava.util.logging.FileHandler.pattern = " + LOG_DATA + "/node_log_%u.txt"
            + "\njava.util.logging.FileHandler.limit = 50000000"
            + "\njava.util.logging.FileHandler.count = 10"
            + "\n"
            + "\n############################################################"
            + "\n# Default number of locks FileHandler can obtain synchronously."
            + "\n# This specifies maximum number of attempts to obtain lock file by FileHandler"
            + "\n# implemented by incrementing the unique field %u as per FileHandler API documentation."
            + "\n############################################################"
            + "\njava.util.logging.FileHandler.maxLocks = 100"
            + "\n"
            + "\n#java.util.logging.FileHandler.formatter = java.util.logging.XMLFormatter"
            + "\njava.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter"
            + "\n"
            + "\n# Limit the message that are printed on the console to INFO and above."
            + "\njava.util.logging.ConsoleHandler.level = FINE"
            + "\njava.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter"
            + "\n"
            + "\n############################################################"
            + "\n# Example to customize the SimpleFormatter output format"
            + "\n# to print one-line log message like this:"
            + "\n#     <level>: <log message> [<date/time>]"
            + "\n############################################################"
            + "\njava.util.logging.SimpleFormatter.format=[%1$tF %1$tT] [%4$-7s] %5$s%n"
            + "\n#java.util.logging.SimpleFormatter.format=[%1$tF %1$tT] [%4$-7s] %5$s - [%6$s]%n"
            + "\n"
            + "\n# %1$ - timestamp"
            + "\n# %2$ - source"
            + "\n# %4$ - log level"
            + "\n# %5$ - log message"
            + "\n# %6$ - throwable and stacktrace"
            + "\n"
            + "\n############################################################"
            + "\n# Facility specific properties."
            + "\n# Provides extra control for each logger."
            + "\n############################################################"
            + "\n"
            + "\n# For example, set the com.xyz.foo logger to only log SEVERE"
            + "\n# messages:"
            + "\n# com.xyz.foo.level = SEVERE"
            + "\n";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_CONFIG))) {
            writer.write(str);
        }
    }

    public static void boot(Integer physicalNodeId, int nodes, String nodeName, AwsIdentity aws, boolean simOnDB, boolean clientMode) {

        if (igniteLocalInstance != null) {
            return;
        }

        try {
            setupLogging();
        } catch (IOException reason) {
            log.error("error writing log properties file: " + LOG_CONFIG, reason);
        }

        System.setProperty("java.util.logging.config.file", LOG_CONFIG);

        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();

        TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();

        IgniteConfiguration igniteCfgGlobal = new IgniteConfiguration();

        if (aws != null) {
            //----------------------------------------------------------------------

            TcpDiscoveryCloudIpFinder finderJClouds = new TcpDiscoveryCloudIpFinder();

            finderJClouds.setProvider("aws-ec2");
            finderJClouds.setIdentity(aws.getIdentityAccessKey());
            finderJClouds.setCredential(aws.getCredentialSecretKey());
            finderJClouds.setRegions(aws.getRegions());
            finderJClouds.setZones(aws.getZones());

            //----------------------------------------------------------------------
            TcpDiscoveryS3IpFinder finderS3 = new TcpDiscoveryS3IpFinder();

            BasicAWSCredentials awsCred = new BasicAWSCredentials(aws.getIdentityAccessKey(), aws.getCredentialSecretKey());

            //----------------------------------------------------------------------
            //Customizable finder:
            //    1) Automatic via JClouds
            //    2) S3 Bucket called : fns-bucket-code
            //----------------------------------------------------------------------
            finderS3.setAwsCredentials(awsCred);
            finderS3.setBucketName("fns-bucket-discovery");

            if (ConstantsGrid.CUSTOMIZE_FINDER_JCLOUDS == true) discoverySpi.setIpFinder(finderJClouds);
            if (ConstantsGrid.CUSTOMIZE_FINDER_S3      == true) discoverySpi.setIpFinder(finderS3);

            //----------------------------------------------------------------------
            discoverySpi.setLocalPort(TcpDiscoverySpi.DFLT_PORT);
            discoverySpi.setLocalPortRange(TcpDiscoverySpi.DFLT_PORT_RANGE);

            //----------------------------------------------------------------------
            log.info(ConstantsSimulator.BAR_LOGGER);
            log.info("connecting with AWS using");
            log.info(ConstantsSimulator.BAR_LOGGER);
            log.info("port----: " + TcpDiscoverySpi.DFLT_PORT);
            log.info("access--: " + aws.getIdentityAccessKey());
            log.info("secret--: " + aws.getCredentialSecretKey());
            log.info("regions-: " + Arrays.toString(aws.getRegions().toArray()));
            log.info("zones---: " + Arrays.toString(aws.getZones().toArray()));
            log.info(ConstantsSimulator.BAR_LOGGER);

            //----------------------------------------------------------------------
        } else {
            TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();

            ipFinder.setAddresses(Collections.singletonList("127.0.0.1"));

            discoverySpi.setLocalPortRange(255);
            discoverySpi.setIpFinder(ipFinder);

            log.info(ConstantsSimulator.BAR_LOGGER);
            log.info("local discovery on localhost - USING LOCAL GRID RESOURCES");
            log.info(ConstantsSimulator.BAR_LOGGER);
        }

        igniteCfgGlobal.setDiscoverySpi(discoverySpi);

        igniteCfgGlobal.setClientMode(clientMode);

        communicationSpi.setMessageQueueLimit(1024);

        //Data storage metrics for local node (to disable set 'metricsLogFrequency' to 0)
        //^-- Node [id=683a6374, uptime=00:03:00.817]
        //^-- Cluster [hosts=1, CPUs=4, servers=2, clients=0, topVer=36, minorTopVer=1]
        //^-- Network [addrs=[0:0:0:0:0:0:0:1, 127.0.0.1, 172.30.240.1, 192.168.178.21, 192.168.56.1], discoPort=47000, commPort=47101]
        //^-- CPU [CPUs=4, curLoad=0%, avgLoad=28.36%, GC=0%]
        //^-- Heap [used=251MB, free=87.56%, comm=414MB]
        //^-- Outbound messages queue [size=0]
        //^-- Public thread pool [active=0, idle=0, qSize=0]
        //^-- System thread pool [active=0, idle=7, qSize=0]
        //^-- Striped thread pool [active=0, idle=8, qSize=0]        
        //PERSISTENCE STORAGE DISABLED FOR PERFORMANCE REASONS
        //DataStorageConfiguration storageCfg = new DataStorageConfiguration();
        //storageCfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);
        //storageCfg.setWalSegmentSize(ConstantsGrid.WAL_UNIT_MB * 64);
        //storageCfg.setWalMode(WALMode.LOG_ONLY);
        //igniteCfgGlobal.setDataStorageConfiguration(storageCfg);
        igniteCfgGlobal.setMetricsLogFrequency(0);

        igniteCfgGlobal.setPeerClassLoadingEnabled(false);

        String abs = Paths.get(".").toAbsolutePath().normalize().toString();

        igniteCfgGlobal.setWorkDirectory(abs + "/" + "ignite_" + nodeName);

        igniteCfgGlobal.setPublicThreadPoolSize(ConstantsGrid.CUSTOMIZE_POOL_SIZE_MAX_NODES);

        igniteCfgGlobal.setDiscoverySpi(discoverySpi);

        //DISABLED FOR PERFORMANCE REASON
        //igniteCfgGlobal.setIncludeEventTypes(
        //    EventType.EVT_NODE_JOINED,
        //    EventType.EVT_NODE_LEFT,
        //    EventType.EVT_NODE_FAILED);
        igniteCfgGlobal.setCommunicationSpi(communicationSpi);

        igniteLocalInstance = Ignition.getOrStart(igniteCfgGlobal);

        igniteLocalInstance.cluster().state(ClusterState.ACTIVE);

        log.info(getClusterInfo());

        //----------------------------------------------------------------------
        //DONT' TOUCH CACHES IF STARTING AS CLIENT ! ! ! ! ! ! ! !
        //----------------------------------------------------------------------
        if (clientMode == false) {
            resetCaches(nodes, simOnDB);
        }

        atomicLong = igniteLocalInstance.atomicLong("globalFireCounter", 0, true);

        long BYTES_TO_KB = 1024;
        long BYTES_TO_MB = 1024 * BYTES_TO_KB;
        long BYTES_TO_GB = 1024 * BYTES_TO_MB;

        //----------------------------------------------------------------------
        //getDefaultDataRegionConfiguration : If this property is not set then the
        //default region can consume up to 20% of RAM available on a local machine.
        //----------------------------------------------------------------------
        log.info(ConstantsSimulator.BAR_LOGGER);
        
        if (igniteCfgGlobal.getDataStorageConfiguration() != null) {
            log.info("Concurrency cpu level----------------: " + (igniteCfgGlobal.getDataStorageConfiguration().getConcurrencyLevel()));
            log.info("Size of the default data region in GB: " + (igniteCfgGlobal.getDataStorageConfiguration().getDefaultDataRegionConfiguration().getMaxSize() / BYTES_TO_GB));
        } else {
            log.info("NO DATA STORAGE CONFIGURATION FOUND");
        }
        log.info(ConstantsSimulator.BAR_LOGGER);
    }

    private static void printCachesOnNode() {

        for (String cacheName : igniteLocalInstance.cacheNames()) {

            Affinity<Object> affinity = igniteLocalInstance.affinity(cacheName);

            int numPartitions = affinity.partitions();

            for (int partition = 0; partition < numPartitions; partition++) {

                Collection<ClusterNode> allNodes = affinity.mapPartitionToPrimaryAndBackups(partition);

                log.info(ConstantsSimulator.BAR_LOGGER);
                
                for (ClusterNode currNode : allNodes) {
                    log.info("listing -> cache: " + cacheName + " - partition: " + (partition + 1) + "/" + numPartitions + " node: " + currNode.consistentId());
                }
                
                log.info(ConstantsSimulator.BAR_LOGGER);                
            }
        }
    }

    public static void resetCaches(int caches, boolean simOnDB) {

        int numNodesPhysical = igniteLocalInstance.cluster().forServers().nodes().size();
        
        int cacheCounter = 0;
        
        //----------------------------------------------------------------------
        //MULTI CACHE
        //----------------------------------------------------------------------
        for (int currCache = 0; currCache < caches; currCache++) {

            int targetNodePhysycal = currCache % numNodesPhysical;
            
            ClusterNode targetNode = igniteLocalInstance.cluster().forServers().nodes().stream()
                .skip(targetNodePhysycal)
                .findFirst()
                .orElse(null);

            if (targetNode == null) {
                throw new IllegalStateException("No physical node found for cache " + currCache);           
            }
            
            cacheCounter++;
            
            String cacheInternodeSpikeByNodeID = Naming.getCacheName(currCache);

            CacheConfiguration<Long, InternodeSpikeModel> cfgCacheInternodeSpikeByNodeID = new CacheConfiguration<Long, InternodeSpikeModel>(cacheInternodeSpikeByNodeID);

            cfgCacheInternodeSpikeByNodeID.setAtomicityMode(ATOMICITY_MODE);
            cfgCacheInternodeSpikeByNodeID.setCacheMode(CacheMode.PARTITIONED);
            cfgCacheInternodeSpikeByNodeID.setIndexedTypes(Long.class, InternodeSpikeModel.class);

            if(ConstantsGrid.CUSTOMIZE_EXPERIMENTAL_THREAD_MANUAL_MANAGE == true) {

                int numberOfPartitions = 1;//TODOPPU TEST nodes;

                cfgCacheInternodeSpikeByNodeID.setBackups(0);
                cfgCacheInternodeSpikeByNodeID.setAffinity(new RendezvousAffinityFunction().setPartitions(numberOfPartitions));

                cfgCacheInternodeSpikeByNodeID.setNodeFilter(node -> node.equals(targetNode));
            }
            
            igniteLocalInstance.destroyCache(cfgCacheInternodeSpikeByNodeID.getName());
            igniteLocalInstance.getOrCreateCache(cfgCacheInternodeSpikeByNodeID);

            log.info("creating cache with name " + cacheInternodeSpikeByNodeID);
        }

        //----------------------------------------------------------------------
        //SINGLE CACHE
        //----------------------------------------------------------------------
        //
        //String cacheInternodeSpikeSingle = ConstantsGrid.CACHE_KEY_CACHE_NAME_INTERNODE_SPIKES;
        //
        //CacheConfiguration<String, Object> cfgCacheInternodeSpikeSingle = new CacheConfiguration<String, Object>(cacheInternodeSpikeSingle);
        //
        //cfgCacheInternodeSpikeSingle.setCacheMode(CacheMode.PARTITIONED);
        //
        //cfgCacheInternodeSpikeSingle.setAtomicityMode(ATOMICITY_MODE);
        //
        //igniteInstance.destroyCache(cfgCacheInternodeSpikeSingle.getName());
        //igniteInstance.getOrCreateCache(cfgCacheInternodeSpikeSingle);
        //
        //----------------------------------------------------------------------
        //Print all caches info located on all nodes
        //----------------------------------------------------------------------

        printCachesOnNode();

        //----------------------------------------------------------------------
        if (simOnDB == true) {
            CacheConfiguration<CollectorKey, CollectorBurnModel> cacheCfgDatabaseSpikes = new CacheConfiguration<>(ConstantsGrid.CACHE_KEY_CACHE_NAME_DATABASE_SPIKES);

            cacheCfgDatabaseSpikes.setCacheMode(CacheMode.PARTITIONED);

            cacheCfgDatabaseSpikes.setIndexedTypes(CollectorKey.class, CollectorBurnModel.class);
            cacheCfgDatabaseSpikes.setIndexedTypes(CollectorKey.class, CollectorFireModel.class);

            igniteLocalInstance.destroyCache(cacheCfgDatabaseSpikes.getName());
            igniteLocalInstance.getOrCreateCache(cacheCfgDatabaseSpikes);
        }
    }

    public static String getClusterInfo() {
        ClusterGroup servers = igniteLocalInstance.cluster().forServers();

        String info = "";

        String currDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());;

        info += "\n" + ConstantsSimulator.BAR_LOGGER;
        info += "\n" + ConstantsSimulator.BAR_LOGGER;
        info += "\n" + currDateTime;
        info += "\n" + ConstantsSimulator.BAR_LOGGER;
        info += "\n" + ConstantsSimulator.BAR_LOGGER;

        ArrayList<String> nodes = new ArrayList<String>();

        for (ClusterNode node : servers.nodes()) {
            String clusterName = "-";

            if (Strings.isEmpty(node.attribute(IgniteNodeAttributes.ATTR_IGNITE_INSTANCE_NAME)) == false) {
                clusterName = node.attribute(IgniteNodeAttributes.ATTR_IGNITE_INSTANCE_NAME);
            }

            nodes.add("\n" + servers.nodes().size() + ") CLUSTER " + clusterName + " - NODE UUID:" + node.consistentId().toString());
        }

        Collections.sort(nodes);

        info += String.join("", nodes);

        info += "\n" + ConstantsSimulator.BAR_LOGGER;
        info += "\n" + ConstantsSimulator.BAR_LOGGER;

        return info;
    }

    public static int getClusterNodesNum() {
        ClusterGroup servers = igniteLocalInstance.cluster().forServers();

        return servers.nodes().size();
    }

    public static Ignite getIgnite() {
        if (igniteLocalInstance == null) {
            throw new RuntimeException("BOOT IGNITE PLEASE");
        }

        return igniteLocalInstance;
    }

    public static String getNodeIdUUIDRestartProof() {
        return igniteLocalInstance.cluster().localNode().consistentId().toString();
    }

    private void _UNUSED_addGridEventsListenerNodeJoined() {
        IgniteEvents events = igniteLocalInstance.events();

        IgnitePredicate<CacheEvent> filter = evt -> {
            System.out.println("remote event: " + evt.name());
            return true;
        };

        events.remoteListen(new IgniteBiPredicate<UUID, CacheEvent>() {

            @Override
            public boolean apply(UUID uuid, CacheEvent e) {
                return ConstantsGrid.ACTION_LISTENING_CONTINUE;
            }
        }, filter, EventType.EVT_NODE_JOINED);
    }

    private void _UNUSED_addGridEventsListenerNodeError() {
        IgniteEvents events = igniteLocalInstance.events();

        IgnitePredicate<CacheEvent> filter = evt -> {
            log.info("remote event: " + evt.name());
            return ConstantsGrid.ACTION_LISTENING_CONTINUE;
        };

        events.remoteListen(new IgniteBiPredicate<UUID, CacheEvent>() {

            @Override
            public boolean apply(UUID uuid, CacheEvent evt) {
                log.error("[NODE LEFT GRID] " + evt.name());
                return ConstantsGrid.ACTION_LISTENING_STOP;
            }
        }, filter,
            EventType.EVT_NODE_LEFT,
            EventType.EVT_NODE_FAILED,
            EventType.EVT_NODE_JOINED);
    }

    public static String getKeyPrefix(Integer node) {
        return "NODE" + node + "-";
    }

    public static String getKey(Integer node, Long atomicIncrement) {
        return getKeyPrefix(node) + atomicIncrement;
    }

    public static void insertMultiCacheInternodeSpikes(Integer nodeDest, InternodeSpikeModel internodeSpike) {

        String cacheName = Naming.getCacheName(nodeDest);
        
        IgniteCache<Long, InternodeSpikeModel> currCache = igniteLocalInstance.getOrCreateCache(cacheName);

        Long key = atomicLong.getAndIncrement();

        currCache.put(key, internodeSpike);
    }
}
