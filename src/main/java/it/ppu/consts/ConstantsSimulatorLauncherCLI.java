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

import org.apache.commons.cli.Options;

public class ConstantsSimulatorLauncherCLI {

    public final static String OPT_PROJECT_SHORT = "p";
    
    public final static String OPT_IDENTITY_SHORT   = "a";
    public final static String OPT_CREDENTIAL_SHORT = "s";
    public final static String OPT_REGIONS_SHORT    = "r";
    public final static String OPT_ZONES_SHORT      = "z";

    public final static String OPT_OUTPUT_GEPHI_SHORT  = "g";
    public final static String OPT_OUTPUT_MATLAB_SHORT = "m";
    
    public final static String OPT_HELP_SHORT                 = "h";
    public final static String OPT_KAFKA_SHORT                = "k";
    public final static String OPT_NODES_LIST_SHORT           = "l";    
    public final static String OPT_REDUCED_OUTPUT_SHORT       = "o";
    public final static String OPT_ULTRA_REDUCED_OUTPUT_SHORT = "u";

    public final static String OPT_SIM_ON_KINESIS_SHORT = "i";
    public final static String OPT_SIM_NO_RANDOM_SHORT  = "n";
    public final static String OPT_SIM_ON_DB_SHORT      = "d";
    public final static String OPT_SIM_FAST_SHORT       = "f";
    
    public final static String OPT_PROJECT = "projectname";
    
    public final static String OPT_IDENTITY   = "accesskey";
    public final static String OPT_CREDENTIAL = "secretkey";
    public final static String OPT_REGIONS    = "regions";
    public final static String OPT_ZONES      = "zones";
    
    public final static String OPT_OUTPUT_GEPHI  = "outfile-gephi";
    public final static String OPT_OUTPUT_MATLAB = "outfile-matlab";

    public final static String OPT_HELP                 = "help";
    public final static String OPT_KAFKA                = "kafka-address";
    public final static String OPT_NODES_LIST           = "nodes-list";
    public final static String OPT_REDUCED_OUTPUT       = "reduced-output";
    public final static String OPT_ULTRA_REDUCED_OUTPUT = "ultra-reduced-output";
    
    public final static String OPT_SIM_ON_KINESIS = "sim-kinesis";
    public final static String OPT_SIM_NO_RANDOM  = "sim-no-random";
    public final static String OPT_SIM_ON_DB      = "sim-memory-db";
    public final static String OPT_SIM_FAST       = "sim-fast";
    
    public final static Options options = new Options();

    static {
        options.addOption(OPT_PROJECT_SHORT, OPT_PROJECT, true, ""
            + "Project name to identify folder contained in " + ConstantsSimulator.PROJECTS_BASE
            + "");            

        options.addOption(OPT_IDENTITY_SHORT, OPT_IDENTITY, true, ""
            + "Access key (e.g.: https://us-east-1.console.aws.amazon.com/iamv2/home#/users/details/FNS?section=security_credentials)"
            + "");

        options.addOption(OPT_CREDENTIAL_SHORT, OPT_CREDENTIAL, true, ""
            + "Secret key (e.g.: https://us-east-1.console.aws.amazon.com/iamv2/home#/users/details/FNS?section=security_credentials)"
            + "");

        options.addOption(OPT_REGIONS_SHORT, OPT_REGIONS, true, ""
            + "List of regions separated by commas **ONLY** => (e.g.: us-east-1,us-west-1)"
            + "");

        options.addOption(OPT_ZONES_SHORT, OPT_ZONES, true, ""
            + "List of zones separated by commas **ONLY** => (e.g.:  us-east-1a,us-east-1b)"
            + "");

        options.addOption(OPT_NODES_LIST_SHORT, OPT_NODES_LIST, true, "followed by the "
            + "list of the node of interest (NOI) for which store the output data. "
            + "The format for such list is (0 BASED) like this example: [3,25,12] "
            + "If this switch is not present, the entire set of nodes"
            + "will be considered for the generation of output data");

        options.addOption(OPT_KAFKA_SHORT, OPT_KAFKA, true, "followed by the address of the queue to write on - ALTERNATIVE TO: MEMDB / KINESIS / FILE");

        options.addOption(OPT_SIM_ON_KINESIS_SHORT, OPT_SIM_ON_KINESIS, false, "produce output on a kinesis instance (see terraform for details) - ALTERNATIVE TO: MEMDB / KAFKA / FILE");

        options.addOption(OPT_OUTPUT_MATLAB_SHORT, OPT_OUTPUT_MATLAB, false, "provides with a set "
            + "of matlab-compliant CSV files, in addition to the output CSVs. "
            + "Used only if output is file and NOT specified " + OPT_KAFKA);

        options.addOption(OPT_OUTPUT_GEPHI_SHORT, OPT_OUTPUT_GEPHI, false, "provides with a set "
            + "of gephi-compliant CSV files, in addition to the output CSVs "
            + "Used only if output is file and NOT specified " + OPT_KAFKA);

        options.addOption(OPT_SIM_NO_RANDOM_SHORT, OPT_SIM_NO_RANDOM, false, "disable random "
            + "generators and use 0.5 as fixed value for random fields "
            + "or distribution value");

        options.addOption(OPT_SIM_ON_DB_SHORT, OPT_SIM_ON_DB, false, "enable ignite memory db - ALTERNATIVE TO: KAFKA / KINESIS / FILE");

        options.addOption(OPT_SIM_FAST_SHORT, OPT_SIM_FAST, false, "enables faster "
            + "algorithms at different levels, in return for some approximations");

        options.addOption(OPT_REDUCED_OUTPUT_SHORT, OPT_REDUCED_OUTPUT, false, ""
            + "enables reduced output events, i.e., outputs that indicates "
            + "only spiking events and inner states of the neurons");

        options.addOption(OPT_ULTRA_REDUCED_OUTPUT_SHORT, OPT_ULTRA_REDUCED_OUTPUT, false,
            "enables ultra-reduced output events, i.e., outputs that indicates "
            + "only spiking events and inner states of the neurons "
            + "with reduced precision");

        options.addOption(OPT_HELP_SHORT, OPT_HELP, false, "shows this help");
    }
}
