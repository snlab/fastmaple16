/*
 * Copyright (c) 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mapleapp.impl;

import org.opendaylight.maple.core.increment.app.MapleAppBase;
import org.opendaylight.maple.core.increment.packet.Ethernet;
import org.opendaylight.maple.core.increment.packet.IPv4;
import org.opendaylight.maple.core.increment.tracetree.MaplePacket;
import org.opendaylight.maple.core.increment.tracetree.Route;

public class M4 extends MapleAppBase {

	String[] allowSrcIPs = { "10.0.0.1", "10.0.0.4", "10.0.0.6" };

	private boolean allow(int srcIP) {
		String srcIPString = IPv4.fromIPv4Address(srcIP);
		for (String ip : allowSrcIPs) {
			if (ip.equals(srcIPString))
				return true;
		}
		return false;
	}

	@Override
	public void onPacket(MaplePacket pkt) {
		if (pkt.ethTypeIs(Ethernet.TYPE_IPv4)) {
			int srcIP = pkt.IPv4Src();
			if (allow(srcIP)) {
				this.passToNext(pkt);
			} else {
				pkt.setRoute(Route.DROP);
				return;
			}
		} else {
			this.passToNext(pkt);
		}
	}
}
