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

import java.text.DecimalFormat;

public interface ConstantsNodeDefaults {

    //----------------------------------------------------------------------
    
    public Long MS_SECOND = 1_000l;
    public Long MS_100    = 100l;
    public Long MS_10     = 10l;
    
    //----------------------------------------------------------------------

    public DecimalFormat FORMAT = new DecimalFormat("#.################");
    
    public String CSV_SEPARATOR = ", ";
    
    public String EXT = "txt";
    
    //----------------------------------------------------------------------
    
    public Double NO_RANDOM = 0.5;
    
    //----------------------------------------------------------------------
    
    public int ERROR_NONE  = 0;
    public int ERROR_BASE  = 1;
    
    //----------------------------------------------------------------------
    //VOLUTAMENTE PRIMITIVE INT
    //----------------------------------------------------------------------
    
    public int EXTERNAL_DISTRO_UNKNOWN  = -1;
    public int EXTERNAL_DISTRO_POISSON  =  0;
    public int EXTERNAL_DISTRO_CONSTANT =  1;
    public int EXTERNAL_DISTRO_NOISE    =  2;

    //----------------------------------------------------------------------
    //the coefficient for which multiply the BOP to obtain a cycle: 
    //1. if it is less than 1, than we fall into the optimistic 
    //   simulation
    //2. if it is greater or equal to 1, than we have a conservative 
    //   behavior
    //Note: using a gamma distribution with shape parameter 
    //      (alpha_lambda) for the connectivity topology lengths, we 
    //      need to re-calculate such factor in order to controll 
    //      lossess between nodes under the bop_conservative_p
    //      probability
    //----------------------------------------------------------------------
    
    public Double SIM_BOP_TO_CYCLE_FACTOR = 1.0;
    public Double SIM_BOP_CONSERVATIVE_P  = 0.9999;
    public Double SIM_MAX_TRACT_LENGTH    = 100_000.0;
    public Double SIM_EPSILON_7           = 0.00000001;
    
    public Integer SIM_CURVE_GUESS_THRESHOLD_GOOD = 5;
    public Integer SIM_CURVE_GUESS_THRESHOLD_BAD  = 15;
    
    public Double SIM_MIN_NE_XN_RATIO_DEF = Double.MAX_VALUE;
    public Double SIM_MAX_NE_XN_RATIO_DEF = 0.0;
    
    public Double SIM_MIN_AMPLITUDE_DEF = Double.MAX_VALUE;
    public Double SIM_MAX_AMPLITUDE_DEF = 0.0;
    
    //----------------------------------------------------------------------
    //setting default to 1 means every x
    //----------------------------------------------------------------------
    
    public Double SIM_GAMMA_INVERSE_CYUMULATIVE_PROB_X = 1.0;
    
    //----------------------------------------------------------------------
    
    public Double XML_AVG_NEURONAL_SPEED = 5.1;
    public Double XML_P_REW              = 0.0;
    public Double XML_R                  = 0.8;
    public Double XML_MU_W_EXC           = 0.04;
    public Double XML_MU_W_INH           = 0.03;
    public Double XML_SIGMA_W_EXC        = 0.08;
    public Double XML_SIGMA_W_INH        = 0.08;
    public Double XML_W_PRE_INH          = 0.02;
    public Double XML_W_PRE_EXC          = 0.02;
    public Double XML_ETAP               = 0.01;
    public Double XML_ETAM               = 0.05;
    public Double XML_TAUP               = 15.0;
    public Double XML_TAUM               = 30.0;
    public Double XML_PWMAX              = 1.0;
    public Double XML_IBI                = 1.0;
    public Double XML_PLASTICITY_TO      = 3.0;
    
    public Double XML_EXTERNAL_INPUTS_TIME_OFFSET = 0.0;
    public Double XML_EXTERNAL_INPUTS_AMPLITUDE   = 0.07;
    
    public Double XML_EXTERNAL_INPUTS_TIME_STEP = 1.0;
    
    public Boolean XML_LIF        = false;
    public Boolean XML_EXP_DECAY  = false;
    public Boolean XML_PLASTICITY = false;
    
    public Integer XML_BN                             = 1;
    public Integer XML_NEURON_CONNECTIONS             = 20;
    public Integer XML_EXTERNAL_INPUTS_TYPE           = EXTERNAL_DISTRO_CONSTANT;
    public Integer XML_EXTERNAL_INPUTS_NUMBER         = 0;
    public Integer XML_EXTERNAL_INPUTS_FIRE_DURATION  = 1_000;
    public Integer XML_EXTERNAL_INPUTS_FIREOUT_DEGREE = 1;
    
    public Long XML_NEURONS_NUMBER     = 100l;
    public Long XML_SIMULATION_TIME_MS = MS_SECOND;
    
    //----------------------------------------------------------------------
    
    public Double NODETHREAD_COMPRESSION_FACTOR = 1.0;
    public Double NODETHREAD_EPSILON_5          = 0.000001;
    public Double NODETHREAD_BURNING_TIME       = -1.0;
    public Double NODETHREAD_FIRING_TIME        = -1.0;
    
    public Integer NODETHREAD_EXTERNAL_INPUTS_FIREOUT_DEGREE_NONE = 0;

    public Double NODETHREAD_EXTERNAL_INPUTS_PRESYNAPTIC_EXC = 1.0;
    public Double NODETHREAD_EXTERNAL_INPUTS_PRESYNAPTIC_INH = -1.0;
    public Double NODETHREAD_EXTERNAL_INPUTS_TIME_STEP       = -1.0;
    public Double NODETHREAD_EXTERNAL_INPUTS_FIRING_TIME     = Double.MAX_VALUE;//TODOPPU 1A DA APPROFONDIRE LA DISCREPANZA CON LA CONTROPARTE
    
    //----------------------------------------------------------------------
    
    //TODOPPU DA APPROFONDIRE LA DISCREPANZA CON LA CONTROPARTE COSTANTE FILE XML
    public Double NODETHREAD_EXTERNAL_INPUTS_TIME_OFFSET  = 0.1;
    public Double NODETHREAD_EXTERNAL_INPUTS_AMPLITUDE    = 0.1;
    
    //----------------------------------------------------------------------    

}
