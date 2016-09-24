/*
 * Copyright (c) 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.maple.core.increment.app;

import org.opendaylight.maple.core.increment.tracetree.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

import java.util.ArrayList;
import java.util.List;

public class ShortestPath {

	NetworkGraphService ngs;
	
	public ShortestPath() {
		ngs = new NetworkGraphImpl();
	}
	
	public void setLinks(List<Link> links) {
		ngs.clear();
		ngs.addLinks(links);
	}
	
	private NodeId convertPort2NodeId(Port port) {
		TpId tpId = new TpId(port.getId());
		String nc_value = tpId.getValue();
		return new NodeId(nc_value.substring(0, nc_value.lastIndexOf(':')));
	}
	
	public List<String> getFormattedPath(Port srcPort, Port dstPort) {
		org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang
		.network.topology.rev131021.NodeId srcNodeIdForTBD = new org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang
	    .network.topology.rev131021.NodeId(
	    		convertPort2NodeId(srcPort));
		
		org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang
		.network.topology.rev131021.NodeId dstNodeIdForTBD = new org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang
	    .network.topology.rev131021.NodeId(
	    		convertPort2NodeId(dstPort));
		
		List<Link> path = null;
		try{
		    path = ngs.getPath(srcNodeIdForTBD, dstNodeIdForTBD);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.println("compute path size: " + path.size());
		
		List<String> returnPath = new ArrayList<String>();
		
		for (Link link: path) {
			String linkId = link.getLinkId().getValue();
			System.out.println("computed link id: " + linkId);
			String srcTpId = link.getSource().getSourceTp().getValue();
			System.out.println("computed src tpid: " + srcTpId);
			String dstTpId = link.getDestination().getDestTp().getValue();
			System.out.println("computed dst tpid: " + dstTpId);
			String value = "<" + linkId + "," + srcTpId + "," + dstTpId + ">";
			returnPath.add(value);
		}
		
		return returnPath;
	}
}
