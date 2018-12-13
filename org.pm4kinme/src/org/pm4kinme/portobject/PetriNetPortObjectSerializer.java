package org.pm4kinme.portobject;

import java.io.IOException;
import java.util.zip.ZipEntry;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject.PortObjectSerializer;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
/**
 * To serialize an object means to convert its state to a byte stream 
 * so that the byte stream can be reverted back into a copy of the object. 
 * A Java object is serializable if its class or any of its superclasses implements either the java.io.Serializable interface.
 * When an object is serialized, information that identifies its class is recorded in the serialized stream.
 * However, the class's definition ("class file") itself is not recorded. It is the responsibility of the system 
 * that is deserializing the object to determine how to locate and load the necessary class files
 * 
 * @author dkf
 *
 */
public class PetriNetPortObjectSerializer extends PortObjectSerializer<PetriNetPortObject>{

	String FILE_NAME = PetriNetPortObjectSpec.TEMP_FILE_NAME;
	
	@Override
	public void savePortObject(PetriNetPortObject portObject, PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO save port object into FILE_NAME
		out.putNextEntry(new ZipEntry(FILE_NAME));
		out.write(portObject.toPnmlString().getBytes());
		
	}

	@Override
	public PetriNetPortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO load from temporary file 
		String entryName = in.getNextEntry().getName();
		PetriNetPortObject portObj = null;
		
		if (!entryName.equals(FILE_NAME)) {
            throw new IOException("Found unexpected zip entry "
                    + entryName + "! Expected " + FILE_NAME);
        }
        try {// here we need to take care about the environment, we need to create a context, because we don't have one
            portObj = new PetriNetPortObject();
            portObj.loadFrom((PetriNetPortObjectSpec)spec, in);
        } catch (Exception e) {
            throw new IOException(e);
        }
        return portObj;

	}

}
