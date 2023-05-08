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

import it.ppu.consts.ConstantsGrid;
import it.ppu.consts.ConstantsNodeDefaults;
import it.ppu.consts.ConstantsSimulator;
import it.ppu.consts.ConstantsSimulatorExecutorWorkerCLI;
import it.ppu.spiking.simulation.model.SimulationConfigModel;
import it.ppu.utils.grid.GridIgnite;
import java.util.logging.Level;
import java.util.logging.LogManager;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

@Slf4j
public class StartWorker {
    
    private final static String CMD_SYNTAX = "-";
    
    public static void main(String[] args) throws Exception {
        
        LogManager.getLogManager().getLogger("").setLevel(ConstantsSimulator.LOG_LEVEL);
        
        log.info(ConstantsSimulator.GPL);
        
        boolean LOCAL_SIMULATION_TESTING = false;
    
        if(LOCAL_SIMULATION_TESTING == true) {
            args = new String[] {
                "-" + ConstantsSimulatorExecutorWorkerCLI.OPT_IDENTITY_SHORT,"IDENT",
                "-" + ConstantsSimulatorExecutorWorkerCLI.OPT_CREDENTIAL_SHORT,"SECRET",
                "-" + ConstantsSimulatorExecutorWorkerCLI.OPT_ZONES,"ZONE",
                "-" + ConstantsSimulatorExecutorWorkerCLI.OPT_REGIONS,"REGION",
                "-" + ConstantsSimulatorExecutorWorkerCLI.OPT_INSTANCE_ID_SHORT,"INSTANCE",
            };
        }
        
        CommandLineParser parser = new DefaultParser();
        
        HelpFormatter formatter = new HelpFormatter();

        CommandLine cmd = null;

        try {
            cmd = parser.parse(ConstantsSimulatorExecutorWorkerCLI.options, args);

            if (cmd.hasOption(ConstantsSimulatorExecutorWorkerCLI.OPT_HELP)) {
                formatter.printHelp(CMD_SYNTAX, ConstantsSimulatorExecutorWorkerCLI.options);

                System.exit(ConstantsNodeDefaults.ERROR_NONE);
                
                return;
            }            
        } catch (ParseException e) {
            log.error("parse error", e);
            formatter.printHelp(CMD_SYNTAX, ConstantsSimulatorExecutorWorkerCLI.options);

            log.info("bye!");
            System.exit(ConstantsNodeDefaults.ERROR_BASE);
        }       
        
        //----------------------------------------------------------------------
        
        Integer instanceId = null;
        
        if(cmd.hasOption(ConstantsSimulatorExecutorWorkerCLI.OPT_INSTANCE_ID) == true) {
            instanceId = Integer.parseInt(cmd.getOptionValue(ConstantsSimulatorExecutorWorkerCLI.OPT_INSTANCE_ID));
        }
        
        //----------------------------------------------------------------------

        SimulationConfigModel simulationConfig = SimulationConfigModel.builder(cmd, false);

        GridIgnite.boot(
            instanceId,
            0,
            ConstantsGrid.NODE_WORKER_PREFIX + instanceId,
            simulationConfig.getAws(),false,false);

        //----------------------------------------------------------------------

    }
}
