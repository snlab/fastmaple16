/*
 * Copyright (c) 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mapleapp.impl;

import java.util.Map;

import org.opendaylight.maple.core.increment.app.MapleAppBase;
import org.opendaylight.maple.core.increment.app.MapleUtil;
import org.opendaylight.maple.core.increment.packet.Ethernet;
import org.opendaylight.maple.core.increment.packet.IPv4;
import org.opendaylight.maple.core.increment.tracetree.MaplePacket;
import org.opendaylight.maple.core.increment.tracetree.Port;
import org.opendaylight.maple.core.increment.tracetree.Route;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;

public class M2 extends MapleAppBase {

	private static final String TOPO_URL = "/root/network-topology/topology";
	private static final String HOST_TABLE_URL = "/root/host-table";
	private static final String H1 = "10.0.0.1";
	private static final String H2 = "10.0.0.2";
	private static final String[] H12_HIGH_PATH = { H1, "openflow:1:3", "openflow:2:2", "openflow:4:1" };
	private static final String[] H12_LOW_PATH = { H1, "openflow:1:4", "openflow:3:2", "openflow:4:1" };
	private static final String[] H21_HIGH_PATH = { H2, "openflow:4:4", "openflow:2:1", "openflow:1:1" };
	private static final String[] H21_LOW_PATH = { H2, "openflow:4:5", "openflow:3:1", "openflow:1:1" };

	private void checkStaticH12(MaplePacket pkt) {
		if (pkt.IPv4SrcIs(IPv4.toIPv4Address(H1)) && pkt.IPv4DstIs(IPv4.toIPv4Address(H2))) {
			String[] path = null;
			if (pkt.TCPDstPortIs(80)) {
				path = H12_HIGH_PATH;
			} else {
				path = H12_LOW_PATH;
			}
			pkt.setRoute(path);
		} else if (pkt.IPv4SrcIs(IPv4.toIPv4Address(H2)) && pkt.IPv4DstIs(IPv4.toIPv4Address(H1))) {
			String[] path = null;
			if (pkt.TCPSrcPortIs(80)) {
				path = H21_HIGH_PATH;
			} else {
				path = H21_LOW_PATH;
			}
			pkt.setRoute(path);
		}
	}

	@Override
	public void onPacket(MaplePacket pkt) {
		if (pkt.ethTypeIs(Ethernet.TYPE_IPv4)) {
			checkStaticH12(pkt);
			if (pkt.route() == null) {
				if (pkt.TCPDstPortIs(80) || pkt.TCPSrcPortIs(80)) {
					int srcIP = pkt.IPv4Src();
					int dstIP = pkt.IPv4Dst();
					Topology topo = (Topology) readData(TOPO_URL);
					Map<Integer, Port> hostTable = (Map<Integer, Port>) readData(HOST_TABLE_URL);
					Port srcPort = hostTable.get(srcIP);
					Port dstPort = hostTable.get(dstIP);
					pkt.setRoute(MapleUtil.shortestPath(topo.getLink(), srcPort, dstPort));
				} else {
					pkt.setRoute(Route.DROP);
				}
			}
		} else {
			this.passToNext(pkt);
		}
	}
}
