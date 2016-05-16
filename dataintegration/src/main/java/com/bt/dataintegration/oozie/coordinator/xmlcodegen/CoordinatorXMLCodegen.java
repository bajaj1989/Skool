package com.bt.dataintegration.oozie.coordinator.xmlcodegen;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.bt.dataintegration.oozie.coordinator.main.ActionMain;
import com.bt.dataintegration.oozie.coordinator.main.ControlsMain;
import com.bt.dataintegration.oozie.coordinator.main.CoordinatorMain;
import com.bt.dataintegration.oozie.coordinator.main.WorkflowMain;
import com.bt.dataintegration.oozie.coordinator.tags.Action;
import com.bt.dataintegration.oozie.coordinator.tags.Controls;
import com.bt.dataintegration.oozie.coordinator.tags.Coordinator;
import com.bt.dataintegration.oozie.coordinator.tags.Workflow;
import com.bt.dataintegration.property.config.HadoopConfig;
import com.bt.dataintegration.utilities.DirectoryHandler;
import static com.bt.dataintegration.constants.Constants.*;

public class CoordinatorXMLCodegen {

	final static Logger logger = Logger.getLogger(CoordinatorXMLCodegen.class);
	
	public void generateXML(HadoopConfig hconf) {
		
		//HadoopConfig hconf = new HadoopConfig().getHadoopConfigProperties();
		Workflow workflow = new WorkflowMain().setWorkflowMain();
		Controls ctl = new ControlsMain().setControlsMain();
		Action act = new ActionMain().setActionMain(workflow);
		Coordinator coord = new CoordinatorMain().setCoordinatorMain(ctl, act, hconf);
		
		try {

			logger.info("Generating Oozie "+COORDINATOR_XML_FILE+"...");
			String tableName = null;
			if(SQOOP_IMPORT.equalsIgnoreCase(hconf.getImport_export_flag())) {
				tableName = hconf.getTableName();
			} else if(FILE_IMPORT.equalsIgnoreCase(hconf.getImport_export_flag())) {
				tableName = hconf.getHiveTableName();
			}
			File file = new File(tableName +"/"+COORDINATOR_XML_FILE);
			JAXBContext jaxbContext = JAXBContext
					.newInstance(Coordinator.class);
			javax.xml.bind.Marshaller jaxbMarshaller = jaxbContext
					.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(
					javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(coord, file);
			//jaxbMarshaller.marshal(workflow, System.out);			
			logger.info("coordinator.xml generated !");
			
			DirectoryHandler.sendFileToHDFS(hconf, COORDINATOR_XML_FILE);
			DirectoryHandler.givePermissionToHDFSFile(hconf, COORDINATOR_XML_FILE);
			
		} catch (JAXBException e) {
			logger.error("Error generating XML for oozie Workflow", e);
		}
	}
}
