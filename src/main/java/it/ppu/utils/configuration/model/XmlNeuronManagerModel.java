/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.configuration.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "neuron_manager")
public class XmlNeuronManagerModel {

    private Double D_exc;
    private Double D_inh;
    private Double c;
    private Double t_arp;

    public Double getD_exc() {
        return D_exc;
    }

    public Double getD_inh() {
        return D_inh;
    }

    public Double getC() {
        return c;
    }

    public Double getT_arp() {
        return t_arp;
    }
    
    //----------------------------------------------------------------------

    public void setD_exc(Double D_exc) {
        this.D_exc = D_exc;
    }

    public void setD_inh(Double D_inh) {
        this.D_inh = D_inh;
    }

    public void setC(Double c) {
        this.c = c;
    }

    public void setT_arp(Double t_arp) {
        this.t_arp = t_arp;
    }
}
