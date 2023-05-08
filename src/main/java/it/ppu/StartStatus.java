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
import it.ppu.consts.ConstantsSimulatorStatusCLI;
import it.ppu.spiking.simulation.model.SimulationConfigModel;
import it.ppu.utils.grid.GridIgnite;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.LogManager;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

@Slf4j
public class StartStatus {
    
    private final static String CMD_SYNTAX = "-";
    
    public static void main(String[] args) throws Exception {
        
        LogManager.getLogManager().getLogger("").setLevel(ConstantsSimulator.LOG_LEVEL);
        
        log.info(ConstantsSimulator.GPL);
        
        boolean LOCAL_SIMULATION_TESTING = false;
    
        if(LOCAL_SIMULATION_TESTING == true) {
            args = new String[] {
                "-" + ConstantsSimulatorStatusCLI.OPT_IDENTITY_SHORT,"IDENT",
                "-" + ConstantsSimulatorStatusCLI.OPT_CREDENTIAL_SHORT,"SECRET",
                "-" + ConstantsSimulatorStatusCLI.OPT_ZONES,"ZONE",
                "-" + ConstantsSimulatorStatusCLI.OPT_REGIONS,"REGION"
            };
        }
        
        CommandLineParser parser = new DefaultParser();
        
        HelpFormatter formatter = new HelpFormatter();

        CommandLine cmd = null;

        try {
            cmd = parser.parse(ConstantsSimulatorStatusCLI.options, args);

            if (cmd.hasOption(ConstantsSimulatorStatusCLI.OPT_HELP)) {
                formatter.printHelp(CMD_SYNTAX, ConstantsSimulatorStatusCLI.options);

                System.exit(ConstantsNodeDefaults.ERROR_NONE);
                
                return;
            }            
        } catch (ParseException e) {
            log.error("parse error", e);
            formatter.printHelp(CMD_SYNTAX, ConstantsSimulatorStatusCLI.options);

            log.info("bye!");
            System.exit(ConstantsNodeDefaults.ERROR_BASE);
        }       
        
        //----------------------------------------------------------------------

        SimulationConfigModel simulationConfig = SimulationConfigModel.builder(cmd, false);

        GridIgnite.boot(
            -1,
            0,
            ConstantsGrid.NODE_CLIENT,
            simulationConfig.getAws(),
            false,
            true);

        int nodesPre = 0;
        
        for(;;) {
            //----------------------------------------------------------------------

            log.info(GridIgnite.getClusterInfo());
                
            //----------------------------------------------------------------------
            int nodesAfter = GridIgnite.getClusterNodesNum();
            //----------------------------------------------------------------------
            if(nodesAfter < nodesPre) {
                //----------------------------------------------------------------------
                String currDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());;
                //----------------------------------------------------------------------
                log.error(ConstantsSimulator.BAR_LOGGER);
                log.error(ConstantsSimulator.BAR_LOGGER);
                //----------------------------------------------------------------------
                log.error(currDateTime);
                //----------------------------------------------------------------------
                log.error(ConstantsSimulator.BAR_LOGGER);
                log.error(ConstantsSimulator.BAR_LOGGER);
                //----------------------------------------------------------------------
                log.error("NODE LEAVE DETECTED ! ! !");
                //----------------------------------------------------------------------
                log.error(ConstantsSimulator.BAR_LOGGER);
                log.error(ConstantsSimulator.BAR_LOGGER);
                //----------------------------------------------------------------------
            }
            //----------------------------------------------------------------------
            nodesPre = nodesAfter;
            //----------------------------------------------------------------------
            Thread.sleep(1000L);
            //----------------------------------------------------------------------
        }
    }
}
