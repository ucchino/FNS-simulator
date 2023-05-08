/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.utils.grid.model;

import java.util.ArrayList;

import lombok.ToString;

@ToString
public class AwsIdentity {
    
    private String identityAccessKey;

    private String credentialSecretKey;

    private ArrayList<String> regions = new ArrayList<String>();
 
    private ArrayList<String> zones = new ArrayList<String>();

    public String getIdentityAccessKey() {
        return identityAccessKey;
    }

    public void setIdentityAccessKey(String identityAccessKey) {
        this.identityAccessKey = identityAccessKey;
    }

    public String getCredentialSecretKey() {
        return credentialSecretKey;
    }

    public void setCredentialSecretKey(String credentialSecret) {
        this.credentialSecretKey = credentialSecret;
    }

    public ArrayList<String> getRegions() {
        return regions;
    }

    public void setRegions(ArrayList<String> regions) {
        this.regions = regions;
    }

    public ArrayList<String> getZones() {
        return zones;
    }

    public void setZones(ArrayList<String> zones) {
        this.zones = zones;
    }
 
}
