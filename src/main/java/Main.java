import model.NewBuildConfigurationModel;
import model.NewObjectFactory;
import model.OldBuildConfigurationModel;
import model.OldObjectFactory;
import org.w3c.dom.*;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;


public class Main {

    public static final String CONFIG_FILE_NAME = "\\config.xml";

    public static void main(String arg[]) {
        if (arg.length != 0) {
            String path = "";
            for(String str : arg) {
                path += str + " ";
            }
            path = path.substring(0, path.length()-1);
            File folder = new File(path);
            File[] folderEntries = folder.listFiles();
            try {
                for (File entry : folderEntries) {
                    if (entry.isDirectory()) {
                        updateXMLConfig(entry.getPath() + CONFIG_FILE_NAME);
                    }
                }
            }catch (Exception e) {
                System.out.println("Update XML config:" + e);
            }
            path = path.substring(0, path.indexOf("plugins")) + "jobs\\";
            File folderJobs = new File(path);
            File[] folderEntriesJobs = folderJobs.listFiles();
            try {
                for (File entry : folderEntriesJobs) {
                    if (entry.isDirectory()) {
                        updateXMLJob(entry.getPath() + CONFIG_FILE_NAME);
                    }
                }
            } catch (Exception e) {
                System.out.println("Update XML job config:" + e);
            }
        } else {
            System.out.println("Set folder to 'plugins\\BuildConfiguration' in Jenkins");
        }
    }

    private static void updateXMLJob(String path) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(new InputSource(path));
            if(doc.getElementsByTagName("hudson.plugins.git.GitSCM").getLength() == 1) {
                XPath xpath = XPathFactory.newInstance().newXPath();
                NodeList nodes = (NodeList) xpath.evaluate("//*[contains(@class, 'org.jenkinsci.plugins.multiplescms.MultiSCM')]",
                        doc, XPathConstants.NODESET);
                for (int idx = 0; idx < nodes.getLength(); idx++) {
                    Node value = nodes.item(idx).getAttributes().getNamedItem("class");
                    String val = value.getNodeValue();
                    value.setNodeValue(val.replaceAll("org.jenkinsci.plugins.multiplescms.MultiSCM", "hudson.plugins.git.GitSCM"));
                    value = nodes.item(idx).getAttributes().getNamedItem("plugin");
                    val = value.getNodeValue();
                    value.setNodeValue(val.replaceAll("multiple-scms@0.6", "git@2.4.2"));
                }
                String docString = toString(doc).replace("<scms>","").replace("</scms>", "").replace("<hudson.plugins.git.GitSCM plugin=\"git@2.4.2\">","")
                        .replace("</hudson.plugins.git.GitSCM>","");
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(docString));
                Document docNew = db.parse(is);
                removeWhitespaceNodes(docNew.getDocumentElement());
                Transformer xformer = TransformerFactory.newInstance().newTransformer();
                xformer.setOutputProperty(OutputKeys.INDENT, "yes");
                xformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
                xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                xformer.transform(new DOMSource(docNew), new StreamResult(new File(path)));
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public static void removeWhitespaceNodes(Element e) {
        NodeList children = e.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child instanceof Text && ((Text) child).getData().trim().length() == 0) {
                e.removeChild(child);
            }
            else if (child instanceof Element) {
                removeWhitespaceNodes((Element) child);
            }
        }
    }

    private static String toString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    private static void updateXMLConfig(String path) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(OldObjectFactory.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            OldBuildConfigurationModel oldConfig =
                    (OldBuildConfigurationModel)unmarshaller.unmarshal(
                            new File(path));

            NewObjectFactory newObjectFactory = new NewObjectFactory();
            NewBuildConfigurationModel newConfig = createNewConfig(newObjectFactory, oldConfig);

            JAXBContext context = JAXBContext.newInstance(NewObjectFactory.class);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output",Boolean.TRUE);
            marshaller.marshal(newConfig, new File(path));
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

    private static NewBuildConfigurationModel createNewConfig(NewObjectFactory newObjectFactory, OldBuildConfigurationModel oldConfig) {

        NewBuildConfigurationModel newConfig = newObjectFactory.createComAmcbridgeJenkinsPluginsModelsBuildConfigurationModel();
        newConfig.setProjectName(oldConfig.getProjectName());
        newConfig.setEmail(oldConfig.getEmail());
        newConfig.setCreator(oldConfig.getCreator());
        newConfig.setDate(oldConfig.getDate());
        newConfig.setRejectionReason(oldConfig.getRejectionReason());
        newConfig.setScm(oldConfig.getScm());
        newConfig.setConfigEmail(oldConfig.getConfigEmail());
        newConfig.setScriptType(oldConfig.getScriptType());
        newConfig.setPreScript(oldConfig.getPreScript());
        newConfig.setPostScript(oldConfig.getPostScript());
        newConfig.setIsJobUpdate(oldConfig.getIsJobUpdate());
        newConfig.setState(oldConfig.getState());
        NewBuildConfigurationModel.ProjectToBuild projectToBuilds = newObjectFactory.createComAmcbridgeJenkinsPluginsModelsBuildConfigurationModelProjectToBuild();
        for(OldBuildConfigurationModel.ProjectToBuild.ComAmcbridgeJenkinsPluginsModelsProjectToBuildModel oldProjectToBuild : oldConfig.getProjectToBuild().getComAmcbridgeJenkinsPluginsModelsProjectToBuildModel()) {
            NewBuildConfigurationModel.ProjectToBuild.ComAmcbridgeJenkinsPluginsModelsProjectToBuildModel newProjectToBuildModel = newObjectFactory.createComAmcbridgeJenkinsPluginsModelsBuildConfigurationModelProjectToBuildComAmcbridgeJenkinsPluginsModelsProjectToBuildModel();
            newProjectToBuildModel.setProjectUrl(oldProjectToBuild.getProjectUrl());
            newProjectToBuildModel.setFileToBuild(oldProjectToBuild.getFileToBuild());
            newProjectToBuildModel.setLocalDirectoryPath(oldProjectToBuild.getLocalDirectoryPath());
            newProjectToBuildModel.setBranchName(oldProjectToBuild.getBranchName());
            newProjectToBuildModel.setCredentials(oldProjectToBuild.getCredentials());
            newProjectToBuildModel.setIsVersionFiles(oldProjectToBuild.getIsVersionFiles());
            NewBuildConfigurationModel.ProjectToBuild.ComAmcbridgeJenkinsPluginsModelsProjectToBuildModel.Builders builders = newObjectFactory.createComAmcbridgeJenkinsPluginsModelsBuildConfigurationModelProjectToBuildComAmcbridgeJenkinsPluginsModelsProjectToBuildModelBuilders();
            for(OldBuildConfigurationModel.ProjectToBuild.ComAmcbridgeJenkinsPluginsModelsProjectToBuildModel.Builders.ComAmcbridgeJenkinsPluginsModelsBuilderConfigModel oldBuilderConfigModel : oldProjectToBuild.getBuilders().getComAmcbridgeJenkinsPluginsModelsBuilderConfigModel()) {
                NewBuildConfigurationModel.ProjectToBuild.ComAmcbridgeJenkinsPluginsModelsProjectToBuildModel.Builders.ComAmcbridgeJenkinsPluginsModelsBuilderConfigModel newBuilderConfigModel = newObjectFactory.createComAmcbridgeJenkinsPluginsModelsBuildConfigurationModelProjectToBuildComAmcbridgeJenkinsPluginsModelsProjectToBuildModelBuildersComAmcbridgeJenkinsPluginsModelsBuilderConfigModel();
                newBuilderConfigModel.setBuilder(oldBuilderConfigModel.getBuilder());
                newBuilderConfigModel.setPlatform(oldBuilderConfigModel.getPlatform());
                newBuilderConfigModel.setUserConfig(oldBuilderConfigModel.getUserConfig());
                NewBuildConfigurationModel.ProjectToBuild.ComAmcbridgeJenkinsPluginsModelsProjectToBuildModel.Builders.ComAmcbridgeJenkinsPluginsModelsBuilderConfigModel.Configs configs = newObjectFactory.createComAmcbridgeJenkinsPluginsModelsBuildConfigurationModelProjectToBuildComAmcbridgeJenkinsPluginsModelsProjectToBuildModelBuildersComAmcbridgeJenkinsPluginsModelsBuilderConfigModelConfigs();
                for (String configuration: oldBuilderConfigModel.getConfigs().getComAmcbridgeJenkinsPluginsEnumsConfiguration()) {
                    configs.getComAmcbridgeJenkinsPluginsEnumsConfiguration().add(configuration);
                }
                newBuilderConfigModel.setConfigs(configs);
                newBuilderConfigModel.setBuilderArgs(oldBuilderConfigModel.getBuilderArgs());
                newBuilderConfigModel.setBuilder(oldBuilderConfigModel.getBuilder());
                newBuilderConfigModel.setGuid(oldBuilderConfigModel.getGuid());
                builders.getComAmcbridgeJenkinsPluginsModelsBuilderConfigModel().add(newBuilderConfigModel);
            }
            newProjectToBuildModel.setBuilders(builders);
            NewBuildConfigurationModel.ProjectToBuild.ComAmcbridgeJenkinsPluginsModelsProjectToBuildModel.Artifacts artifacts = newObjectFactory.createComAmcbridgeJenkinsPluginsModelsBuildConfigurationModelProjectToBuildComAmcbridgeJenkinsPluginsModelsProjectToBuildModelArtifacts();
            for (String artifact : oldProjectToBuild.getArtefacts().getString()) {
                artifacts.getString().add(artifact);
            }
            newProjectToBuildModel.setArtifacts(artifacts);
            NewBuildConfigurationModel.ProjectToBuild.ComAmcbridgeJenkinsPluginsModelsProjectToBuildModel.VersionFiles versionFiles = newObjectFactory.createComAmcbridgeJenkinsPluginsModelsBuildConfigurationModelProjectToBuildComAmcbridgeJenkinsPluginsModelsProjectToBuildModelVersionFiles();
            for (String versionFile : oldProjectToBuild.getVersionFiles().getString()) {
                versionFiles.getString().add(versionFile);
            }
            newProjectToBuildModel.setVersionFiles(versionFiles);
            newProjectToBuildModel.setGuid(oldProjectToBuild.getGuid());
            projectToBuilds.getComAmcbridgeJenkinsPluginsModelsProjectToBuildModel().add(newProjectToBuildModel);
        }
        newConfig.setProjectToBuild(projectToBuilds);
        NewBuildConfigurationModel.BuildMachineConfiguration buildMachineConfiguration = newObjectFactory.createComAmcbridgeJenkinsPluginsModelsBuildConfigurationModelBuildMachineConfiguration();
        for (String name : oldConfig.getBuildMachineConfiguration().getString()) {
            NewBuildConfigurationModel.BuildMachineConfiguration.Entry entry = newObjectFactory.createComAmcbridgeJenkinsPluginsModelsBuildConfigurationModelBuildMachineConfigurationEntry();
            entry.setBoolean("true");
            entry.setString(name);
            buildMachineConfiguration.getEntry().add(entry);
        }
        newConfig.setBuildMachineConfiguration(buildMachineConfiguration);
        newConfig.setComments(oldConfig.getComments());
        NewBuildConfigurationModel.UserWithAccess newUserWithAccess = newObjectFactory.createComAmcbridgeJenkinsPluginsModelsBuildConfigurationModelUserWithAccess();
        if (oldConfig.getUserWithAccess() != null) {
            for (OldBuildConfigurationModel.UserWithAccess.ComAmcbridgeJenkinsPluginsModelsUserAccessModel oldUserAccessModel : oldConfig.getUserWithAccess().getComAmcbridgeJenkinsPluginsModelsUserAccessModel()) {
                NewBuildConfigurationModel.UserWithAccess.ComAmcbridgeJenkinsPluginsModelsUserAccessModel newUserAccessModel = newObjectFactory.createComAmcbridgeJenkinsPluginsModelsBuildConfigurationModelUserWithAccessComAmcbridgeJenkinsPluginsModelsUserAccessModel();
                newUserAccessModel.setUserName(oldUserAccessModel.getUserName());
                newUserWithAccess.getComAmcbridgeJenkinsPluginsModelsUserAccessModel().add(newUserAccessModel);
            }
        }
        newConfig.setUserWithAccess(newUserWithAccess);
        newConfig.setCleanWorkspace(oldConfig.getCleanWorkspace());
        newConfig.setRegExp(oldConfig.getRegExp());
        newConfig.setPollSCMTrigger("H * * * *");
        newConfig.setBuildOnCommitTrigger("true");
        newConfig.setBuildPeriodicallyTrigger("");
        newConfig.setPlugin("build-configurator@1.0.6.0");
        return newConfig;
    }

}
