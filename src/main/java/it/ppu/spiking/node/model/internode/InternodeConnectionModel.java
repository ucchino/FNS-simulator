/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.spiking.node.model.internode;

import lombok.extern.slf4j.Slf4j;

import it.ppu.utils.tools.IntegerCouple;

import it.ppu.consts.ConstantsNodeDefaults;

import java.io.Serializable;

@Slf4j
public final class InternodeConnectionModel extends IntegerCouple implements Serializable {

    private static final long serialVersionUID = 3248671994878955007L;
    
    /*
    * The schema for internode connections
    * 
    *   \        |       |       |       |
    *    \  FROM | mixed |  exc  |  inh  |
    *  TO \      |       |       |       |
    * ------------------------------------
    *    mixed   |   0   |   3   |   6   |
    * ------------------------------------
    *     exc    |   1   |   4   |   7   |
    * ------------------------------------
    *     inh    |   2   |   5   |   8   |
    * ------------------------------------
    *  
    */
    public static final Integer MIXED2MIXED = 0;
    public static final Integer MIXED2EXC   = 1;
    public static final Integer MIXED2INH   = 2;
    public static final Integer EXC2MIXED   = 3;
    public static final Integer EXC2EXC     = 4;
    public static final Integer EXC2INH     = 5;
    public static final Integer INH2MIXED   = 6;
    public static final Integer INH2EXC     = 7;
    public static final Integer INH2INH     = 8;
    public static final Integer MAX_TYPE    = 8;

    //----------------------------------------------------------------------
    //the sm interconnection probability
    //----------------------------------------------------------------------
    private Double neXnRatio_Weight;

    private Double muLambda_TractLength = null;
    private Double muOmega_Amplitude    = 1.0;
    
    private Double sigmaW_AmplitudeStdDeviation = 0.0;
    private Double alphaLambda_TractLengthsShapeParameter = null;

    private Integer internodeConnectionType = 0;

    public InternodeConnectionModel(Integer srcNodeId, Integer dstNodeId, Double neXnRatio_Weight) {
        super(srcNodeId, dstNodeId);
        
        this.neXnRatio_Weight = neXnRatio_Weight;
    }

    public void setMuLambda_TractLength(Double muLambda_TractLength) {
        this.muLambda_TractLength = muLambda_TractLength;
    }
    
    public Double getMuLambda_TractLength() {
        if (muLambda_TractLength == null) {
            
            log.error("[NODES INTERCONNECTION WARNING] length 0 error");
            
            try {
                throw new Exception();
            } catch (Exception e) {
                log.error("mu Lambda length error",e);
            }
            System.exit(ConstantsNodeDefaults.ERROR_BASE);
        }
        return muLambda_TractLength;
    }

    public void setInternodeConnectionType(Integer internodeConnectionType) {
        this.internodeConnectionType = (internodeConnectionType <= MAX_TYPE) ? internodeConnectionType : MIXED2MIXED;
    }

    public Double getNeXnRatio_Weight() {
        return neXnRatio_Weight;
    }

    public void setNeXnRatio_Weight(Double neXnRatio_Weight) {
        this.neXnRatio_Weight = neXnRatio_Weight;
    }

    public Double getMuOmega_Amplitude() {
        return muOmega_Amplitude;
    }

    public void setMuOmega_Amplitude(Double muOmega_Amplitude) {
        this.muOmega_Amplitude = muOmega_Amplitude;
    }

    public Double getSigmaW_AmplitudeStdDeviation() {
        return sigmaW_AmplitudeStdDeviation;
    }

    public void setSigmaW_AmplitudeStdDeviation(Double sigmaW_AmplitudeStdDeviation) {
        this.sigmaW_AmplitudeStdDeviation = sigmaW_AmplitudeStdDeviation;
    }

    public Double getAlphaLambda_TractLengthsShapeParameter() {
        return alphaLambda_TractLengthsShapeParameter;
    }

    public void setAlphaLambda_TractLengthsShapeParameter(Double alphaLambda_TractLengthsShapeParameter) {
        this.alphaLambda_TractLengthsShapeParameter = alphaLambda_TractLengthsShapeParameter;
    }

    public Integer getInternodeConnectionType() {
        return internodeConnectionType;
    }
    
    
}
