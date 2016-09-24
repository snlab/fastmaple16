/*
 * Copyright (c) 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

//TODO: package name
package org.opendaylight.mapleapp.impl;

// TODO: Need all these packages??
import org.opendaylight.maple.core.increment.app.MapleAppBase;
import org.opendaylight.maple.core.increment.packet.Ethernet;
import org.opendaylight.maple.core.increment.packet.IPv4;
import org.opendaylight.maple.core.increment.tracetree.MaplePacket;
import org.opendaylight.maple.core.increment.tracetree.Route;

public class M1 extends MapleAppBase {

	private static final String      H1    = "10.0.0.1";
	private static final IPv4Address H1_IP = IPv4.toIPv4Address(H1);

	private static final String      H2    = "10.0.0.2";
	private static final IPv4Address H2_IP = IPv4.toIPv4Address(H2);

	private static final HTTP_PORT = 80;

	// TODO: Better explain the path construct:
	// TODO: Use s1, s2, ... in the construction if possible
	private static final String[] H12_HIGH_PATH = { H1, "openflow:1:3", "openflow:2:2", "openflow:4:1" };
	private static final String[] H12_LOW_PATH  = { H1, "openflow:1:4", "openflow:3:2", "openflow:4:1" };
	private static final String[] H21_HIGH_PATH = { H2, "openflow:4:4", "openflow:2:1", "openflow:1:1" };
	private static final String[] H21_LOW_PATH  = { H2, "openflow:4:5", "openflow:3:1", "openflow:1:1" };

	@Override
	public void onPacket(MaplePacket pkt) {

		// For non-IPv4 traffic; Use the next Maple App
		if ( !pkt.ethTypeIs(Ethernet.TYPE_IPv4) ) {

			this.passToNext(pkt);

		} else {

			// H1 (client) -> H2 (server)
			if ( pkt.IPv4SrcIs(H1_IP) && pkt.IPv4DstIs(H2_IP) ) {

				String[] path = null;

				if ( ! pkt.TCPDstPortIs(HTTP_PORT) ) {
					path = H12_LOW_PATH;
				} else {
					path = H12_HIGH_PATH;
				}

				// ***TODO***: Need to agree on either Route or Path, not both
				pkt.setRoute(path);

			// Reverse: H2 -> H1
			} else if ( pkt.IPv4SrcIs(H2_IP) && pkt.IPv4DstIs(H1_IP) ) {

				String[] path = null;

				if ( ! pkt.TCPSrcPortIs(HTTP_PORT) ) {
					path = H21_LOW_PATH;
				} else {
					path = H21_HIGH_PATH;
				}
				pkt.setRoute(path);

			// Other host pairs
			} else {

				pkt.setRoute(Route.DROP);

			}
		}

	} // end of onPacket
}
