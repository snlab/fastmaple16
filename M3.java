/*
 * Copyright (c) 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.maple.core.increment.app;

import java.util.Map;

import org.maple.core.increment.packet.Ethernet;
import org.maple.core.increment.packet.IPv4;
import org.maple.core.increment.tracetree.MaplePacket;
import org.maple.core.increment.tracetree.Path;
import org.maple.core.increment.tracetree.Port;
import org.maple.core.increment.tracetree.Route;
import org.maple.core.increment.tracetree.RouteAction;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;

public class ODLMapleApp extends MapleAppBase {
    
    private static final int REAL_IP1 = IPv4.toIPv4Address("10.0.0.4");
    private static final int REAL_IP2 = IPv4.toIPv4Address("10.0.0.6");
    private static final int VIRTUAL_IP = IPv4.toIPv4Address("10.0.0.7");

    private static final long REAL_MAC1 = Ethernet.toLong(Ethernet.toMACAddress("00:00:00:00:00:04"));
    private static final long REAL_MAC2 = Ethernet.toLong(Ethernet.toMACAddress("00:00:00:00:00:06"));
    private static final long VIRTUAL_MAC = Ethernet.toLong(Ethernet.toMACAddress("00:00:00:00:00:07"));
    
    private static final String TOPO_URL = "/root/network-topology/topology";
    private static final String HOST_TABLE_URL = "/root/host-table";

    @Override
	public void onPacket(MaplePacket pkt) {
		if (pkt.ethTypeIs(Ethernet.TYPE_IPv4)) {
			if (pkt.IPv4DstIs(VIRTUAL_IP)) {
				int srcIP = pkt.IPv4Src();
				int selectedServerId = srcIP % 2;
				if (selectedServerId == 1) {
					forwardToRIP(pkt, srcIP, REAL_IP1, REAL_MAC1);
				} else {
					forwardToRIP(pkt, srcIP, REAL_IP2, REAL_MAC2);
				}
			} else if (pkt.IPv4SrcIs(REAL_IP1) || pkt.IPv4SrcIs(REAL_IP2)) {
				int srcIP = pkt.IPv4Src();
				int dstIP = pkt.IPv4Dst();
				backToClient(pkt, srcIP, dstIP);
			} else {
				pkt.setRoute(Route.DROP);
			}
		} else {
			passToNext(pkt);
		}
	}

    private void forwardToRIP(MaplePacket pkt, int srcIP, int rIP, long rMAC) {
        pkt.setIPv4Dst(rIP);
        pkt.setEthDst(rMAC);
        Map<Integer, Port> hostTable = (Map<Integer, Port>) readData(HOST_TABLE_URL);
		Port srcPort = hostTable.get(srcIP);
		Port dstPort = hostTable.get(rIP);
        Topology topo = (Topology) readData(TOPO_URL);
        pkt.setRoute(MapleUtil.shortestPath(topo.getLink(), srcPort, dstPort));
    }

    private void backToClient(MaplePacket pkt, int srcIP, int dstIP) {
    	pkt.setEthSrc(VIRTUAL_MAC);
        pkt.setIPv4Src(VIRTUAL_IP);
        Map<Integer, Port> hostTable = (Map<Integer, Port>) readData(HOST_TABLE_URL);
		Port srcPort = hostTable.get(srcIP);
		Port dstPort = hostTable.get(dstIP);
        Topology topo = (Topology) readData(TOPO_URL);
        pkt.setRoute(MapleUtil.shortestPath(topo.getLink(), srcPort, dstPort));
    }
}

