/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu;

import it.ppu.consts.ConstantsNodeDefaults;
import it.ppu.consts.ConstantsSimulator;
import it.ppu.consts.ConstantsSimulatorLauncherCLI;
import it.ppu.spiking.simulation.model.SimulationConfigModel;
import it.ppu.spiking.simulation.controller.SimulationBuilder;
import it.ppu.spiking.simulation.controller.SimulationMain;
import it.ppu.utils.exceptions.BadParametersException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

@Slf4j
public class StartMain {
    
    private static final String CMD_SYNTAX = "-";
    
    private static String BANNER = "";

    static {
        BANNER += "\n";
        BANNER += "===========================================\n";
        BANNER += "=    ---------------------------------    =\n";
        BANNER += "=                  F N S                  =\n";
        BANNER += "=    Spiking Neural Networks Simulator    =\n";
        BANNER += "=    ---------------------------------    =\n";
        BANNER += "===========================================\n";
        BANNER += "\n";
    }

    public static void main(String[] args) throws Exception {
        
        LogManager.getLogManager().getLogger("").setLevel(ConstantsSimulator.LOG_LEVEL);
        
        log.info(ConstantsSimulator.GPL);
        
        boolean LOCAL_SIMULATION_TESTING = false;
    
        if(LOCAL_SIMULATION_TESTING == true) {
            args = new String[] {
                "--" + ConstantsSimulatorLauncherCLI.OPT_PROJECT,
                "_current_",
                "--" + ConstantsSimulatorLauncherCLI.OPT_NODES_LIST, "[0]",
                "-" + ConstantsSimulatorLauncherCLI.OPT_SIM_NO_RANDOM_SHORT
            };
        }
        
        log.info(BANNER);

        CommandLineParser parser = new DefaultParser();
        
        HelpFormatter formatter = new HelpFormatter();

        CommandLine cmd;

        try {
            cmd = parser.parse(ConstantsSimulatorLauncherCLI.options, args);

            if (cmd.hasOption(ConstantsSimulatorLauncherCLI.OPT_HELP) || (cmd.getOptions().length == 0)) {
                formatter.printHelp(CMD_SYNTAX, ConstantsSimulatorLauncherCLI.options);

                System.exit(ConstantsNodeDefaults.ERROR_NONE);
                
                return;
            }

            //----------------------------------------------------------------------

            SimulationConfigModel simulationConfig = SimulationConfigModel.builder(cmd, true);

            SimulationBuilder simulationBuilder = new SimulationBuilder(simulationConfig);

            //----------------------------------------------------------------------

            SimulationMain simulationManager = simulationBuilder.getSimulationMain();
            
            simulationManager.managementStart();
            
            //----------------------------------------------------------------------
            
        } catch (BadParametersException | IOException | ParseException e) {
            log.error("bad configuration error",e);
            System.exit(ConstantsNodeDefaults.ERROR_BASE);
        }
    }
}
