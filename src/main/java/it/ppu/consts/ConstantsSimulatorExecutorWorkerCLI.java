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

public class ConstantsSimulatorExecutorWorkerCLI {

    public final static String OPT_INSTANCE_ID_SHORT = "x";
    public final static String OPT_HELP_SHORT        = ConstantsSimulatorLauncherCLI.OPT_HELP_SHORT;
    public final static String OPT_IDENTITY_SHORT    = ConstantsSimulatorLauncherCLI.OPT_IDENTITY_SHORT;
    public final static String OPT_CREDENTIAL_SHORT  = ConstantsSimulatorLauncherCLI.OPT_CREDENTIAL_SHORT;
    public final static String OPT_REGIONS_SHORT     = ConstantsSimulatorLauncherCLI.OPT_REGIONS_SHORT;
    public final static String OPT_ZONES_SHORT       = ConstantsSimulatorLauncherCLI.OPT_ZONES_SHORT;

    
    public final static String OPT_INSTANCE_ID = "instanceid";
    public final static String OPT_HELP        = ConstantsSimulatorLauncherCLI.OPT_HELP;
    public final static String OPT_IDENTITY    = ConstantsSimulatorLauncherCLI.OPT_IDENTITY;
    public final static String OPT_CREDENTIAL  = ConstantsSimulatorLauncherCLI.OPT_CREDENTIAL;
    public final static String OPT_REGIONS     = ConstantsSimulatorLauncherCLI.OPT_REGIONS;
    public final static String OPT_ZONES       = ConstantsSimulatorLauncherCLI.OPT_ZONES;
    
    public final static Options options = new Options();

    static {
        options.addOption(OPT_INSTANCE_ID_SHORT, OPT_INSTANCE_ID, true, ""
            + "ID of the worker istance - NUMBER BETWEEN 0..9999 - REQUIRED VALUE"
            + "");            

        options.addOption(OPT_IDENTITY_SHORT, OPT_IDENTITY, true, ""
            + ConstantsSimulatorLauncherCLI.options.getOption(OPT_CREDENTIAL_SHORT).getDescription());

        options.addOption(OPT_CREDENTIAL_SHORT, OPT_CREDENTIAL, true, ""
            + ConstantsSimulatorLauncherCLI.options.getOption(OPT_CREDENTIAL_SHORT).getDescription());

        options.addOption(OPT_REGIONS_SHORT, OPT_REGIONS, true, ""
            + ConstantsSimulatorLauncherCLI.options.getOption(OPT_REGIONS).getDescription());

        options.addOption(OPT_ZONES_SHORT, OPT_ZONES, true, ""
            + ConstantsSimulatorLauncherCLI.options.getOption(OPT_ZONES).getDescription());

        options.addOption(OPT_HELP_SHORT, OPT_HELP, false, ""
            + ConstantsSimulatorLauncherCLI.options.getOption(OPT_HELP).getDescription());
    }
}
