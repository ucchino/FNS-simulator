/**
 * "FNS" (Firnet NeuroScience)
 *
 * FNS is an event-driven Spiking Neural Network framework, oriented
 * to data-driven neural simulations.
 *
 * For contacts, description, citation to acclude, licensing, please
 * read the file enclosed with code: README
 */
package it.ppu.utils.tools;

import it.ppu.consts.ConstantsNodeDefaults;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;

import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import lombok.extern.slf4j.Slf4j;

import it.ppu.utils.configuration.model.XmlConfigModel;
import it.ppu.utils.configuration.model.XmlNodeModel;
import it.ppu.utils.node.Naming;

@Slf4j
public class Files implements Serializable {

    private static final long serialVersionUID = 3248671994878955050L;

    public static HashMap<Integer, XmlNodeModel> loadAndGetXmlNodes(List<XmlNodeModel> nodes) {

        HashMap<Integer, XmlNodeModel> nodesCf = new HashMap<Integer, XmlNodeModel>();

        for (XmlNodeModel currNode : nodes) {
            nodesCf.put(currNode.getId(), currNode);
        }
        return nodesCf;
    }

    public static XmlConfigModel readConfigXmlFile(String path) {
        XmlConfigModel xmlConfig = null;

        try {
            File file = new File(path);

            JAXBContext jaxbContext = JAXBContext.newInstance(XmlConfigModel.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            xmlConfig = (XmlConfigModel) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            log.error("error reading file: " + path, e);
        }

        return xmlConfig;
    }

    public static XmlConfigModel readConfigFile(Class baseClass, String path) {
        XmlConfigModel xmlConfig = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(XmlConfigModel.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            InputStream stream = getFileFromResourceAsStream(baseClass, path);

            xmlConfig = (XmlConfigModel) jaxbUnmarshaller.unmarshal(stream);
        } catch (JAXBException e) {
            log.error("error reading file: " + path, e);
        }

        return xmlConfig;
    }

    public static InputStream getFileFromResourceAsStream(Class baseClass, String fileName) {

        ClassLoader classLoader = baseClass.getClassLoader();

        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        if (inputStream == null) {
            return null;
        } else {
            return inputStream;
        }
    }

    public static void printInputStream(InputStream is) {

        try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            log.error("error printInputStream", e);
        }
    }

    public static PrintWriter getFile(String fullPath, Long starTime, Integer nodeId, String prefix) throws IOException {

        String outF = fullPath + File.separator + "output_" + prefix;

        new File(outF).mkdirs();

        String filename = outF + File.separator + prefix + Naming.get(nodeId) + "_" + starTime + "." + ConstantsNodeDefaults.EXT;

        File burningTowritefile = new File(filename);

        burningTowritefile.delete();
        burningTowritefile.createNewFile();

        FileWriter     fw = new FileWriter(burningTowritefile);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter    pw = new PrintWriter(bw);

        return pw;
    }

    public void writeString(String content,String fileName)
        throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
            
            writer.flush();
        }
    }
}
