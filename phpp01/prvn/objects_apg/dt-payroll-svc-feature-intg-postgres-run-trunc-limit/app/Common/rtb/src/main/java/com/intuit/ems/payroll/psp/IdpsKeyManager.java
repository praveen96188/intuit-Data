package com.intuit.ems.payroll.psp;

import com.intuit.idps.IdpsClient;
import com.intuit.idps.domain.item.Key;
import com.intuit.idps.domain.item.NoSuchItemException;
import com.intuit.idps.service.IdpsException;
import com.intuit.idps.service.IdpsRuntimeException;
import com.intuit.idps.service.rest.IdpsCommunicationException;
import com.intuit.sbd.payroll.psp.configuration.IDPSManager;
import com.paycycle.util.StringUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IdpsKeyManager {

    public static void main(String[] args) throws Exception {
        System.out.println("Number of args passed: " + args.length);

        List<String> keys;
        String version;
        String rotationPeriod;

        CommandLineParser commandLineParser = new PosixParser();
        CommandLine commandLine = commandLineParser.parse(getOptions(), args);

        IdpsKeyManagerModel idpsKeyManagerModel = new IdpsKeyManagerModelBuilder().buildKeyManagerModel(commandLine);

        try {

            if(idpsKeyManagerModel.getOperation().equals(IdpsKeyManagerModel.Operation.HELP)) {
                System.out.println("For details to use this tool, go to this link: https://wiki.intuit.com/x/i3HJL");
                return;
            }

            IDPSManager.initialize();

            keys = idpsKeyManagerModel.getKeys();

            if(idpsKeyManagerModel.getOperation().equals(IdpsKeyManagerModel.Operation.ROTATE)) {
                // Get rotation period
                rotationPeriod = idpsKeyManagerModel.getRotationPeriod();
                for(String keyName: keys) {
                    rotateKeyAndSetRotationPeriod(keyName.trim(), rotationPeriod);
                }

            } else if(idpsKeyManagerModel.getOperation().equals(IdpsKeyManagerModel.Operation.DELETE)) {
                // Get version
                version = idpsKeyManagerModel.getVersion();
                Integer intVersion = new Integer(version);
                for(int i = 0; i <= intVersion; i++) {
                    deleteKey(keys.get(0).trim(), i);
                }
            }

        } catch (IdpsCommunicationException idpsCommunicationException) {
            System.out.println("ERROR: IDPSCommunicationException occurred in IdpsKeyManager");
            throw idpsCommunicationException;
        } catch (IdpsException idpsException) {
            System.out.println("ERROR: IDPSException occurred in IdpsKeyManager");
            throw idpsException;
        } catch (Exception exception) {
            throw exception;
        }
    }

    private static void rotateKeyAndSetRotationPeriod(String keyName, String rotationPeriodString) throws IdpsCommunicationException, IdpsException {

        IdpsClient idpsClient = IDPSManager.getIdpsClient();

        try {
            Key key = idpsClient.newKeyHandleLatest(keyName);

            System.out.println("INFO: Rotating key with keyName=" + keyName + " with version=" + key.getVersion() + " with Rotation period=" + key.getRotationPeriod());
            if(!StringUtil.isNullOrEmpty(rotationPeriodString)) {
                Integer rotationPeriod = new Integer(rotationPeriodString);
                System.out.println("INFO: Updating rotation period of key=" + keyName + " to=" + rotationPeriod);
                idpsClient.updateKeyRotationPeriod(keyName, rotationPeriod);
                System.out.println("INFO: Updated rotation period of key=" + keyName +" to=" + rotationPeriod);
            }
            Key newKey = idpsClient.updateKeyGenerate(key);
            System.out.println("INFO: Rotated key with keyName=" + keyName + " to new version=" + newKey.getVersion() + " with Rotation period=" + newKey.getRotationPeriod());
        } catch (NoSuchItemException noSuchItemException) {
            System.out.println("ERROR: KeyName=" + keyName + " is invalid");
            throw noSuchItemException;
        }
        catch (IdpsRuntimeException | IdpsException | IdpsCommunicationException idpsRuntimeException) {
            System.out.println("ERROR: Not able to rotate keyName=" + keyName + " due to Idps sException ");
            throw idpsRuntimeException;
        } catch (Exception e) {
            System.out.println("ERROR: Unexpected exception occurred while rotation of keyName=" + keyName);
            throw e;
        }
    }


    private static void deleteKey(String keyName, int version) {
        // TODO
        return;
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption("rp","rotationPeriod", true, "Specify Rotation Period of Key");
        options.addOption("k", "keys", true, "List of Keys");
        options.addOption("v", "version", true, "Key version");
        options.addOption("h", "help", false, "Help doc");
        return options;
    }
}

class IdpsKeyManagerModelBuilder {

    public IdpsKeyManagerModel buildKeyManagerModel(CommandLine commandLine) throws IOException {
        if(commandLine.getArgs().length == 0 || commandLine.getArgs().length > 1) {
            if(commandLine.hasOption("h")) {
                return new IdpsKeyManagerModel(IdpsKeyManagerModel.Operation.HELP, new ArrayList<>(), null, null);
            }
            throw new IOException("Please specify only one argument");
        }

        IdpsKeyManagerModel.Operation operation = IdpsKeyManagerModel.Operation.ROTATE;
        List<String> keys = new ArrayList<>();
        String rotationPeriod = null;
        String version = null;

        if(commandLine.hasOption("k")) {
            String[] keyListString = commandLine.getOptionValue("k").split(",");
            keys = Arrays.asList(keyListString);
        }

        if(keys.size() == 0) {
            throw new IOException("Please specify at least one key");
        }

        if(commandLine.getArgs()[0].equals("rotateKey")) {
            operation = IdpsKeyManagerModel.Operation.ROTATE;
            // Get rotation period
            if(commandLine.hasOption("rp")) {
                if(keys.size() > 1) {
                    throw new IOException("-rp option can only be specified with single key argument");
                }
                rotationPeriod = commandLine.getOptionValue("rp");
            }
        } else if(commandLine.getArgs()[0].equals("deactivateKey")) {
            operation = IdpsKeyManagerModel.Operation.DELETE;
            if(commandLine.hasOption("v")) {
                if(keys.size() > 1) {
                    throw new IOException("-v option can only be specified with single key argument");
                }
                version = commandLine.getOptionValue("v");
                if(StringUtil.isNullOrEmpty(version)) {
                    throw new IOException("Version is null");
                }
            }
        }

        return new IdpsKeyManagerModel(operation, keys, rotationPeriod, version);
    }
}

class IdpsKeyManagerModel {
    public enum Operation {
        ROTATE,
        DELETE,
        HELP
    }

    Operation operation;
    List<String> keys;
    String rotationPeriod;
    String version;

    public IdpsKeyManagerModel(Operation operation, List<String> keys, String rotationPeriod, String version) {
        this.operation = operation;
        this.keys = keys;
        this.rotationPeriod = rotationPeriod;
        this.version = version;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public String getRotationPeriod() {
        return rotationPeriod;
    }

    public void setRotationPeriod(String rotationPeriod) {
        this.rotationPeriod = rotationPeriod;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
