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

import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;

public interface ConstantsSimulator {

    public NumberFormat FORMATTER = NumberFormat.getNumberInstance(Locale.ITALY);
    
    public Level LOG_LEVEL = Level.OFF;
        
    public String BAR_LOGGER = "----------------------------------------------------------------------";
    
    public String PROJECTS_BASE = "./doc_experiments/";
    
    public int PHASE_CONFIGFILE_0   = 0;
    public int PHASE_NET_BUILDING_1 = 1;
    public int PHASE_INTERNODE_2    = 2;
    public int PHASE_SIM_INIT_3     = 3;
    public int PHASE_TOTAL_4        = 4;
    
    public boolean KAFKA_MESSAGES_ON_FILE = true;
    
    public enum NodeOutputModes {
        NORMAL,
        REDUCED,
        MINIMAL
    } 
    
    public String WHO = "Gianluca Susi, Emanuele Paracone, Mario Salerno, Alessandro Cristini, Fernando Maestú";
    
    public String NL = "\n* ";
    
    public String GPL = ""
        + NL + BAR_LOGGER
        + NL + "FNS (Firnet NeuroScience)"
        + NL + BAR_LOGGER
        + NL + "FNS is an event-driven Spiking Neural Network framework, oriented"
        + NL + "to data-driven neural simulations."
        + NL + BAR_LOGGER
        + NL + "(c) 2020/2023 " + WHO
        + NL + BAR_LOGGER
        + NL + "CITATION:"
        + NL + "When using FNS for scientific publications, cite us as follows:"
        + NL
        + NL + "" + WHO + ", Pilar Garcés, Ernesto Pereda."
        + NL + BAR_LOGGER
        + NL + "FNS: an event-driven spiking neural network simulator based on the"
        + NL + "LIFL neuron model"
        + NL + "Laboratory of Cognitive and Computational Neuroscience, UPM-UCM"
        + NL + "Centre for Biomedical Technology, Technical University of Madrid;"
        + NL + "University of Rome Tor Vergata"
        + NL + BAR_LOGGER
        + NL + "Paper under review."
        + NL + BAR_LOGGER
        + NL + "FNS is free software: you can redistribute it and/or modify it"
        + NL + "under the terms of the GNU General Public License version 3 as"
        + NL + "published by the Free Software Foundation."
        + NL + BAR_LOGGER
        + NL + "FNS is distributed in the hope that it will be useful, but WITHOUT"
        + NL + "ANY WARRANTY; without even the implied warranty of MERCHANTABILITY"
        + NL + "or FITNESS FOR A PARTICULAR PURPOSE."
        + NL + "See the GNU General Public License for more details."
        + NL + BAR_LOGGER
        + NL + "You should have received a copy of the GNU General Public License"
        + NL + "enclose with FNS. If not, see <http://www.gnu.org/licenses/>."
        + NL
        + NL + "Website: http://www.fnsneuralsimulator.org"
        + NL
        + NL + "Contacts: "
        + NL + "    fnsneuralsimulator (at) gmail.com"
        + NL + "    gianluca.susi82    (at) gmail.com"
        + NL + "    emanuele.paracone  (at) gmail.com"
        + NL
        + NL + "Other contributors: "
        + NL + "    ucchino (at) gmail.com"
        + NL + "        (from 3 to 4 version, ignite datagrid/cloud support)"
        + NL
        + NL + BAR_LOGGER;

}
