/*
 * Copyright (c) 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.maple.core.increment.app;

import org.maple.core.increment.packet.Ethernet;
import org.maple.core.increment.packet.IPv4;
import org.maple.core.increment.tracetree.MaplePacket;
import org.maple.core.increment.tracetree.Path;
import org.maple.core.increment.tracetree.Port;
import org.maple.core.increment.tracetree.Route;
import org.maple.core.increment.tracetree.RouteAction;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;

public class ODLMapleApp2 extends MapleAppBase {


    private ShortestPath sp = new ShortestPath();
    
    private static int real_IP1 = IPv4.toIPv4Address("10.0.0.4");
    private static int real_IP2 = IPv4.toIPv4Address("10.0.0.6");
    private static int virtual_IP = IPv4.toIPv4Address("10.0.0.7");

    private static long real_MAC1 = Ethernet.toLong(Ethernet.toMACAddress("00:00:00:00:00:04"));
    private static long real_MAC2 = Ethernet.toLong(Ethernet.toMACAddress("00:00:00:00:00:06"));
    private static long virtual_MAC = Ethernet.toLong(Ethernet.toMACAddress("00:00:00:00:00:07"));
    
    private static final String topoPath = "/root/network-topology/topology";
	
    @Override
    public void onPacket(MaplePacket pkt) {
        if (pkt.ethTypeIs(Ethernet.TYPE_IPv4)) {
            if (pkt.IPv4DstIs(virtual_IP)) {
            	int srcIP = pkt.IPv4Src();
                int selectedServerId = srcIP % 2;
                if (selectedServerId == 1) {
                    forwardToRIP(pkt, srcIP, real_IP1, real_MAC1);
                } else {
                    forwardToRIP(pkt, srcIP, real_IP2, real_MAC2);
                }
            } else if (pkt.IPv4SrcIs(real_IP1) || pkt.IPv4SrcIs(real_IP2)) {
                backToClient(pkt);
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
        Port srcAttachPort = this.mapleCore.getHost2swTable().get(srcIP);
        Port dstAttachPort = this.mapleCore.getHost2swTable().get(rIP);
        Topology topo = (Topology) readData(topoPath);
        sp.setLinks(topo.getLink());
        pkt.setRoute(MapleUtil.shortestPath(topo.getLink(), srcAttachPort, dstAttachPort));
        //pkt.setRouteAction(new Path(sp.getFormattedPath(srcAttachPort, dstAttachPort), dstAttachPort.getId()));
    }

    private void backToClient(MaplePacket pkt) {
        Port srcAttachPort = this.mapleCore.getHost2swTable().get(pkt.IPv4Src());
        Port dstAttachPort = this.mapleCore.getHost2swTable().get(pkt.IPv4Dst());
        Topology topo = (Topology) readData(topoPath);
        sp.setLinks(topo.getLink());
        pkt.setEthSrc(virtual_MAC);
        pkt.setIPv4Src(virtual_IP);
        pkt.setRoute(MapleUtil.shortestPath(topo.getLink(), srcAttachPort, dstAttachPort));
        //pkt.setRouteAction(new Path(sp.getFormattedPath(srcAttachPort, dstAttachPort), dstAttachPort.getId()));
    }
}


