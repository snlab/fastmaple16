/*
 * Copyright (c) 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.maple.core.increment.app;

import org.opendaylight.maple.core.increment.tracetree.Path;
import org.opendaylight.maple.core.increment.tracetree.Port;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

import java.util.List;

public class MapleUtil {
	
	static ShortestPath sp = new ShortestPath();

	public static String[] shortestPath(List<Link> links, Port srcPort, Port dstPort) {
		sp.setLinks(links);
		List<String> path = sp.getFormattedPath(srcPort, dstPort);
		String[] returnPath = new String[path.size() + 1];
		int i = 0;
		for (String formattedlink: path) {
			String srcTpId = Path.getSrcTpId(formattedlink);
			returnPath[i++] = srcTpId;
		}
		returnPath[i] = dstPort.getId();
		return returnPath;
	}
}
