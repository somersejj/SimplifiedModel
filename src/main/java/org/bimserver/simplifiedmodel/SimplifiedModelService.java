package org.bimserver.simplifiedmodel;

import java.util.Collection;

import org.bimserver.LocalDevSetup;
import org.bimserver.emf.IdEObject;

/**Copyright (C) 2009-2015  BIMserver.org
* 
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
* The Simplified Model Service generates a simplified model' 
* and puts it in a read only project 
* 
* @author  Erwin Somers
* @version 0.1
* @since   2015-07-31 
*/
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.interfaces.objects.SInternalServicePluginConfiguration;
import org.bimserver.interfaces.objects.SObjectType;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.models.log.AccessMethod;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.models.store.ServiceDescriptor;
import org.bimserver.models.store.StoreFactory;
import org.bimserver.models.store.Trigger;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginManagerInterface;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.bimserver.plugins.services.NewRevisionHandler;
import org.bimserver.plugins.services.ServicePlugin;
import org.bimserver.shared.exceptions.BimServerClientException;
import org.bimserver.shared.exceptions.PluginException;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplifiedModelService extends ServicePlugin  {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedModelService.class);
	private boolean initialized;
	private static final String NAMESPACE = "http://bimserver.org/simpleProjectService";
	
	@Override
    public void init(PluginManagerInterface pluginManager) throws PluginException {
        super.init(pluginManager);
        initialized = true;
    }

	@Override
	public void register(long uoid,
			SInternalServicePluginConfiguration internalService,
			PluginConfiguration pluginConfiguration) 
	{
		ServiceDescriptor simplifiedModel= StoreFactory.eINSTANCE.createServiceDescriptor();
		simplifiedModel.setProviderName("BIMserver");
		simplifiedModel.setIdentifier("" + internalService.getOid());
		simplifiedModel.setName("Simplified Model service plugin");
		simplifiedModel.setDescription("Simplified Model service plugin");
		simplifiedModel.setNotificationProtocol(AccessMethod.INTERNAL);
		simplifiedModel.setReadRevision(true);
		simplifiedModel.setWriteExtendedData(NAMESPACE);
		simplifiedModel.setTrigger(Trigger.NEW_REVISION);
		registerNewRevisionHandler(uoid, 
								   simplifiedModel, 
								   new NewRevisionHandler() 
		{
			public void newRevision(BimServerClientInterface bimServerClientInterface, long poid, long roid, String userToken, long soid, SObjectType settings) 
					    throws ServerException, UserException 
			{
				
				LOGGER.info("Simplified model Service is called");
			
				
				try {
			        SProject project;
				
					project = bimServerClientInterface.getBimsie1ServiceInterface().getProjectByPoid(poid);
					IfcModelInterface model = bimServerClientInterface.getModel(project, roid, true, false);
					//get all ifcElements. since the elements are physically existent objects, they can have geometrics. 
					
					
					Collection<IdEObject> objects = model.getValues();
					LOGGER.info("# of objects: "  + objects.size());
			/*		for (IdEObject idEObject : objects) 
					{ 
						
						if (idEObject instanceof IfcProduct)
						{   
							LOGGER.info("idEObject: "  + idEObject.getClass().getName());
							LOGGER.info("Is instance of product");
							IfcProduct product = (IfcProduct) model.get(idEObject.getOid());
							IfcProductRepresentation representations = product.getRepresentation();
							if (product.getGeometry() != null)
								LOGGER.info("Geometric Oid : " + product.getGeometry().getOid());
							if (product.getRepresentation() != null)
							{
								for (IfcRepresentation representation  : representations.getRepresentations())
								{
									IfcRepresentationContext context= representation.getContextOfItems();
									LOGGER.info(context.getContextType());
								}
							}
							LOGGER.info("end of loop");
						}
						
						
					}	*/
					// create new project 
					LOGGER.info("creating new project: test branch");
					BimServerClientInterface client = LocalDevSetup.setupJson("http://localhost:8080");
					client.getSettingsInterface().setGenerateGeometryOnCheckin(false);
					SProject cpid = client.getBimsie1ServiceInterface().addProject(project.getName() + "_simplified", project.getSchema());
					LOGGER.info("Simplified project added: " + cpid.getName());
					
				   
					
				} catch (PublicInterfaceNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BimServerClientException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		);
		LOGGER.info("Simplified model Service plugin loaded");
	}

	@Override
	public void unregister(SInternalServicePluginConfiguration internalService) {
		 	
	}
	@Override
	public boolean isInitialized() {
		return initialized;
	}
	
	public String getDescription() {
		return "Simplified Model service plugin" ;
	}

	public String getDefaultName() {
		return  "Simplified Model service plugin" ;
	}

	public String getVersion() {
		return "0.1" ;
	}

	public ObjectDefinition getSettingsDefinition() {
		return null;
	}

	@Override
	public String getTitle() {
		return "Simplified Model service plugin" ;
	}
}
