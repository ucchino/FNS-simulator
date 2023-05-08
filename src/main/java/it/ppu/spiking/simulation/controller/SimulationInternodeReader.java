/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */

package it.ppu.spiking.simulation.controller;

import it.ppu.consts.ConstantsNodeDefaults;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class SimulationInternodeReader implements Serializable {

    private static final long serialVersionUID = 324867199487895582L;
    
    private Integer vertexNum = 0;

    private final SimulationInternodeBuilder packageInternodeBuilder;

    private final static Double ZERO_DOUBLE = Double.valueOf(0);

    private static final String AMPLITUDES_FILE_NAME    = "mu_omega.txt";
    private static final String TRACT_LENGTHS_FILE_NAME = "mu_lambda.txt";

    private static final String AMPLITUDES_STD_DEVIATION_FILE_NAME       = "sigma_omega.txt";
    private static final String TRACT_LENGTHS_SHAPE_PARAMETERS_FILE_NAME = "alpha_lambda.txt";

    private static final String WEIGHTS_FILE_NAME          = "Ne_xn_ratio.txt";
    private static final String CONNECTIONS_TYPE_FILE_NAME = "conn_type.txt";

    private Double maxNeXnRatio = ConstantsNodeDefaults.SIM_MAX_NE_XN_RATIO_DEF;
    private Double minNeXnRatio = ConstantsNodeDefaults.SIM_MIN_NE_XN_RATIO_DEF;
    private Double maxAmplitude = ConstantsNodeDefaults.SIM_MAX_AMPLITUDE_DEF;
    private Double minAmplitude = ConstantsNodeDefaults.SIM_MIN_AMPLITUDE_DEF;

    protected SimulationInternodeReader(SimulationInternodeBuilder packageInternodeBuilder) {
        this.packageInternodeBuilder = packageInternodeBuilder;
    }

    public void readConnectivityPackage(String path) throws FileNotFoundException, IOException {
        BufferedReader reader = null;

        String packagePath = path;

        if (path.charAt(path.length() - 1) != '/') {
            packagePath += '/';
        }

        log.info("reading connectivity file");
        if ((new File(packagePath + WEIGHTS_FILE_NAME)).exists()) {
            reader = new BufferedReader(new FileReader(packagePath + WEIGHTS_FILE_NAME));
            readCentersFile_VerterxNumCalculate(reader);
            reader.close();
        }

        log.info("reading the Ne-en-ratio file");
        if ((new File(packagePath + WEIGHTS_FILE_NAME)).exists()) {
            reader = new BufferedReader(new FileReader(packagePath + WEIGHTS_FILE_NAME));
            readWeightsFile_NeEnRatio(reader);
            reader.close();
        }

        log.info("reading the mu omega file");
        if ((new File(packagePath + AMPLITUDES_FILE_NAME)).exists()) {
            reader = new BufferedReader(new FileReader(packagePath + AMPLITUDES_FILE_NAME));
            readAmplitudesFile_MuOmega(reader);
            reader.close();
        }

        log.info("reading the sigma omega file");
        if ((new File(packagePath + AMPLITUDES_STD_DEVIATION_FILE_NAME)).exists()) {
            reader = new BufferedReader(new FileReader(packagePath + AMPLITUDES_STD_DEVIATION_FILE_NAME));
            readAmplitudesStdDeviationFile_SigmaOmega(reader);
            reader.close();
        }

        log.info("reading the mu lambda file");
        if ((new File(packagePath + TRACT_LENGTHS_FILE_NAME)).exists()) {
            reader = new BufferedReader(new FileReader(packagePath + TRACT_LENGTHS_FILE_NAME));
            readLengthsFile_MuLambda(reader);
            reader.close();
        }

        log.info("reading the alpha lambda file");
        if ((new File(packagePath + TRACT_LENGTHS_SHAPE_PARAMETERS_FILE_NAME)).exists()) {
            reader = new BufferedReader(new FileReader(packagePath + TRACT_LENGTHS_SHAPE_PARAMETERS_FILE_NAME));
            readLengthsShapeParametersFile_AlphaLambda(reader);
            reader.close();
        }

        log.info("reading the connections type file");
        if ((new File(packagePath + CONNECTIONS_TYPE_FILE_NAME)).exists()) {
            reader = new BufferedReader(new FileReader(packagePath + CONNECTIONS_TYPE_FILE_NAME));
            readConnectionsTypeFile_Conn(reader);
            reader.close();
        }

        log.info("loading done");
    }

    private void readCentersFile_VerterxNumCalculate(BufferedReader reader) {
        try {
            int lines = 0;

            while (reader.readLine() != null) {
                packageInternodeBuilder.addNode(lines);
                ++lines;
            }

            reader.close();

            vertexNum = lines;
        } catch (FileNotFoundException e) {
            log.error("file not found", e);
        } catch (IOException e) {
            log.error("error in readCentersFile", e);
        }
    }

    private void readWeightsFile_NeEnRatio(BufferedReader reader) {
        int src = 0;

        Double weight;

        try {
            for (String line; (line = reader.readLine()) != null;) {

                String[] tokens = line.split(" ");
                String[] goodTkns = new String[vertexNum];

                int k = 0;

                for (int j = 0; k < vertexNum; ++j) {
                    if (!tokens[j].isEmpty()) {
                        goodTkns[k++] = tokens[j];
                    }
                }

                for (int dst = 0; dst < vertexNum; ++dst) {
                    if (src == dst) {
                        continue;
                    }
                    weight = Double.valueOf(goodTkns[dst]);

                    if (weight > maxNeXnRatio) {
                        maxNeXnRatio = weight;
                    }

                    if (!weight.equals(ZERO_DOUBLE)) {
                        if (weight < minNeXnRatio) {
                            minNeXnRatio = weight;
                        }
                        packageInternodeBuilder.addEdge(src, dst, weight);
                    }
                }
                ++src;
            }
        } catch (FileNotFoundException e) {
            log.error("file not found", e);
        } catch (IOException e) {
            log.error("error in readWeightsFile", e);
        }
    }

    private void readAmplitudesFile_MuOmega(BufferedReader reader) {
        int src = 0;

        Double amplitude;

        try {
            for (String line; (line = reader.readLine()) != null;) {

                String[] tokens = line.split(" ");
                String[] goodTkns = new String[vertexNum];

                int k = 0;

                for (int j = 0; k < vertexNum; ++j) {
                    if (!tokens[j].isEmpty()) {
                        goodTkns[k++] = tokens[j];
                    }
                }

                for (int dst = 0; dst < vertexNum; ++dst) {
                    if (src == dst) {
                        continue;
                    }
                    amplitude = Double.valueOf(goodTkns[dst]);
                    if (amplitude > maxAmplitude) {
                        maxAmplitude = amplitude;
                    }
                    if (amplitude < minAmplitude) {
                        minAmplitude = amplitude;
                    }
                    packageInternodeBuilder.addMuOmega_Amplitude(src, dst, amplitude);
                }
                ++src;
            }
        } catch (FileNotFoundException e) {
            log.error("file not found", e);
        } catch (IOException e) {
            log.error("error in readAmplitudesFile", e);
        }
    }

    private void readAmplitudesStdDeviationFile_SigmaOmega(BufferedReader reader) {
        int src = 0;

        Double amplitude;

        try {
            for (String line; (line = reader.readLine()) != null;) {

                String[] tokens = line.split(" ");
                String[] goodTkns = new String[vertexNum];

                int k = 0;

                for (int j = 0; k < vertexNum; ++j) {
                    if (!tokens[j].isEmpty()) {
                        goodTkns[k++] = tokens[j];
                    }
                }

                for (int dst = 0; dst < vertexNum; ++dst) {
                    if (src == dst) {
                        continue;
                    }
                    amplitude = Double.valueOf(goodTkns[dst]);

                    if (!amplitude.equals(ZERO_DOUBLE)) {
                        if (amplitude < minNeXnRatio) {
                            minNeXnRatio = amplitude;
                        }
                        packageInternodeBuilder.addSigmaOmega_AmplitudeStdVariation(src, dst, amplitude);
                    }
                }
                ++src;
            }
        } catch (FileNotFoundException e) {
            log.error("file not found", e);
        } catch (IOException e) {
            log.error("error in readAmplitudesStdDeviationFile", e);
        }
    }

    private void readLengthsFile_MuLambda(BufferedReader reader) {
        int src = 0;

        Double lenght;

        try {
            for (String line; (line = reader.readLine()) != null;) {

                String[] tokens = line.split(" ");
                String[] goodTkns = new String[vertexNum];

                int k = 0;

                for (int j = 0; k < vertexNum; ++j) {
                    if (!tokens[j].isEmpty()) {
                        goodTkns[k++] = tokens[j];
                    }
                }

                for (int dst = 0; dst < vertexNum; ++dst) {
                    if (src == dst) {
                        continue;
                    }
                    lenght = Double.valueOf(goodTkns[dst]);
                    if (!lenght.equals(ZERO_DOUBLE)) {
                        packageInternodeBuilder.addMuLambda_Length(src, dst, lenght);
                    }
                }
                ++src;
            }
        } catch (FileNotFoundException e) {
            log.error("file not found", e);
        } catch (IOException e) {
            log.error("error in readLengthsFile", e);
        }
    }

    private void readLengthsShapeParametersFile_AlphaLambda(BufferedReader reader) {
        int src = 0;

        Double lenghtShape;

        try {
            for (String line; (line = reader.readLine()) != null;) {

                String[] tokens = line.split(" ");
                String[] goodTkns = new String[vertexNum];

                int k = 0;

                for (int j = 0; k < vertexNum; ++j) {
                    if (!tokens[j].isEmpty()) {
                        goodTkns[k++] = tokens[j];
                    }
                }

                for (int dst = 0; dst < vertexNum; ++dst) {
                    if (src == dst) {
                        continue;
                    }
                    lenghtShape = Double.valueOf(goodTkns[dst]);
                    if (!lenghtShape.equals(ZERO_DOUBLE)) {
                        packageInternodeBuilder.addAlphaLambda_LengthShapeParameter(src, dst, lenghtShape);
                    }
                }
                ++src;
            }
        } catch (FileNotFoundException e) {
            log.error("file not found", e);
        } catch (IOException e) {
            log.error("error in readLengthsShapeParametersFile", e);
        }
    }

    private void readConnectionsTypeFile_Conn(BufferedReader reader) {
        int src = 0;

        Integer connType;

        try {
            for (String line; (line = reader.readLine()) != null;) {

                String[] tokens = line.split(" ");
                String[] goodTkns = new String[vertexNum];

                int k = 0;

                for (int j = 0; k < vertexNum; ++j) {
                    if (!tokens[j].isEmpty()) {
                        goodTkns[k++] = tokens[j];
                    }
                }

                for (int dst = 0; dst < vertexNum; ++dst) {
                    if (src == dst) {
                        continue;
                    }
                    connType = Integer.valueOf(goodTkns[dst]);
                    if (!connType.equals(0)) {
                        packageInternodeBuilder.addInternodeConnectionType(src, dst, connType);
                    }
                }

                ++src;
            }
        } catch (FileNotFoundException e) {
            log.error("file not found", e);
        } catch (IOException e) {
            log.error("error in readConnectionsTypeFile", e);
        }
    }

    public Double getMinNeXnRatio() {
        return minNeXnRatio;
    }

    public Double getMaxNeXnRatio() {
        return maxNeXnRatio;
    }
}
