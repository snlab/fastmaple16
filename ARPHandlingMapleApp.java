/*
 * Copyright (c) 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.maple.core.increment.app.systemApps;

import org.opendaylight.maple.core.increment.app.MapleAppBase;
import org.opendaylight.maple.core.increment.packet.ARP;
import org.opendaylight.maple.core.increment.packet.Ethernet;
import org.opendaylight.maple.core.increment.packet.IPv4;
import org.opendaylight.maple.core.increment.tracetree.MaplePacket;
import org.opendaylight.maple.core.increment.tracetree.Port;
import org.opendaylight.maple.core.increment.tracetree.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

package org.opendaylight.maple.core.increment.app.systemApps;

import org.opendaylight.maple.core.increment.app.MapleAppBase;
import org.opendaylight.maple.core.increment.packet.ARP;
import org.opendaylight.maple.core.increment.packet.Ethernet;
import org.opendaylight.maple.core.increment.packet.IPv4;
import org.opendaylight.maple.core.increment.tracetree.MaplePacket;
import org.opendaylight.maple.core.increment.tracetree.Port;
import org.opendaylight.maple.core.increment.tracetree.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ARPHandlingMapleApp extends MapleAppBase {
	
	private final static Logger LOG = LoggerFactory.getLogger(ARPHandlingMapleApp.class);
    
    private Map<String, Long> arp2time;
    
    private Map<Integer, String> inMemoryHost2PortTable;
    
    private static final String HOST_TABLE_URL = "/root/host-table";
    
    public ARPHandlingMapleApp() {
    	arp2time = new HashMap<String, Long>();
    	inMemoryHost2PortTable = new HashMap<Integer, String>();
    }

	@Override
	public void onPacket(MaplePacket pkt) {
		if(pkt.ethType() == Ethernet.TYPE_ARP){
        	
        	ARP arp = (ARP) pkt.frame.getPayload();
        	
        	String key = new String(arp.getSenderProtocolAddress()) + 
        			new String(arp.getTargetProtocolAddress());
        	
        	LOG.info("arp key: " + key);
        	
        	int srcIP = IPv4.toIPv4Address(arp.getSenderProtocolAddress());
        	
        	Port ingressPort = pkt.ingressPort();
        	
        	String arpKey = key + ingressPort.getId();
        	
        	// put to inmemory host table
        	if (!inMemoryHost2PortTable.containsKey(srcIP)) {
        		inMemoryHost2PortTable.put(srcIP, ingressPort.getId());
        		Host2PortEntry entry = new Host2PortEntry(srcIP, ingressPort.toString());
        		this.getMapleCore().writeData(HOST_TABLE_URL, entry);
        	}
        	
        	if (!arp2time.containsKey(arpKey)) {
        			setAction(arpKey, pkt);
        	} else {
        		long lastTime = arp2time.get(arpKey);
        		if (new Date().getTime() - lastTime > 5000) {
					setAction(arpKey, pkt);
        		} else {
        			LOG.info("drop this dublicated arp");
        			pkt.setRoute("null");
        			//pkt.setRoute(Route.DROP);
        		}
        	}
        }else{
        	pkt.setRoute(Route.DROP);
        }
	}

	private void setAction(String arpKey, MaplePacket pkt) {
		arp2time.put(arpKey, new Date().getTime());
		LOG.info("arp flooded");
		pkt.setRoute(Route.FLOOD);
	}
}
