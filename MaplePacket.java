/*
 * Copyright (c) 2016 SNLAB and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.maple.core.increment.tracetree;

import org.opendaylight.maple.core.increment.packet.Ethernet;
import org.opendaylight.maple.core.increment.packet.IPv4;
import org.opendaylight.maple.core.increment.packet.TCP;

import java.util.*;
/* A Maple onPacketIn method computes the Instruction on how to process a packet, 
where the Instruction consists of a set of actions, including 0 to multiple set-packet-header actions, 
one and only one route action (drop, a path on the topology, punt, flood):
- The set-packet-header actions will be inferred from the setHEADER() method calls on the pkt, and 
- The route action should be set explicitly by the user, otherwise, it is DROP as default.

====The user’s intent should be the following====:
(1) The packet should take the path specified by the route action;
(2) When the packet leaves the network, the set-packet-headers actions should 
lead to a result that for each packet header, the exit packet will have the last write value of the 
header according to the program.
 
Our design: We achieve the intent, and we are not obliged to conduct the actions at anywhere.
PROOF: your generated rules will achieve the intent.

There should be an Instruction to Flow Rules method, which will achieve the intent. The mapping from 
the Instruction to the Flow Rules may not be unique.


The implication:
 - getInstruction() method, which will construct the set of actions (from the route action, and 
		the set of modified packet headers)
 - No setInstruction() unless we want some kind of overriding
 - setRouteAction() method
 - getRouteAction() method
 - getSetPacketHeaderActions()
 - instruction2FlowRules() */

public class MaplePacket {


    /** Temp constructor for unit test **/
    public MaplePacket(){
    	
    }

    public Ethernet frame;
    private Port ingressPort;
    //private MapleCore mapleCore;
    //private Trace trace;
    
    public List<TraceItem> itemList = new LinkedList<TraceItem>();
    
    private Map<Match.Field, String> modifiedFieldValues = new HashMap<Match.Field, String>();

    private Action action;
    
    private Instruction instruction;
    
    public boolean isReadTopo;
    
    public MaplePacket(Ethernet frame, Port ingressPort) {
        this.frame = frame;
        //this.mapleCore = mapleCore;
        this.ingressPort = ingressPort;
        //this.trace = trace;
        instruction = new Instruction();
    }
    
    private TraceItem constructV(String field, String value) {
    	TraceItem item = new TraceItem();
        item.setType("V");
        item.setField(field);
        item.setValue(value);
        return item;
    }
    
    private TraceItem constructT(String field, String value, String branch) {
    	TraceItem item = new TraceItem();
    	item.setType("T");
    	item.setField(field);
    	item.setValue(value);
    	item.setBranch(branch);
    	return item;
    }
    
    public void setEthSrc(long value) {
    	instruction.addSPHAction(new SetPacketHeaderAction(Match.Field.ETH_SRC, String.valueOf(value)));
    }

    public final long ethSrc() {
    	if (instruction.isFieldModified(Match.Field.ETH_SRC)) {
    		return Long.parseLong(instruction.getSPHAction(Match.Field.ETH_SRC).getValue());
    	}
    	/*if (modifiedFieldValues.containsKey(Match.Field.ETH_SRC)) {
    		return Long.parseLong(modifiedFieldValues.get(Match.Field.ETH_SRC));
    	}*/
        long addr = Ethernet.toLong(frame.getSourceMACAddress());
        
        /*TraceItem item = new TraceItem();
        item.setType("V");
        item.setField("ETH_SRC");
        item.setValue(String.valueOf(addr));*/
        TraceItem item = constructV("ETH_SRC", String.valueOf(addr));
        this.itemList.add(item);
        return addr;
    }
    
    public void setEthDst(long value) {
    	instruction.addSPHAction(new SetPacketHeaderAction(Match.Field.ETH_DST, String.valueOf(value)));
    }

    public final long ethDst() {
    	if (instruction.isFieldModified(Match.Field.ETH_DST)) {
    		return Long.parseLong(instruction.getSPHAction(Match.Field.ETH_DST).getValue());
    	}
        long addr = Ethernet.toLong(frame.getDestinationMACAddress());
        
        TraceItem item = constructV("ETH_DST", String.valueOf(addr));
        /*item.setType("V");
        item.setField("ETH_DST");
        item.setValue(String.valueOf(addr));*/
        this.itemList.add(item);
        return addr;
    }
    
    public void setEthType(int value) {
    	instruction.addSPHAction(new SetPacketHeaderAction(Match.Field.ETH_TYPE, String.valueOf(value)));
    }

    public final int ethType() {
    	if (instruction.isFieldModified(Match.Field.ETH_TYPE)) {
    		return Integer.parseInt(instruction.getSPHAction(Match.Field.ETH_TYPE).getValue());
    	}
    	
    	TraceItem item = constructV("ETH_TYPE", String.valueOf(frame.getEtherType()));
        /*item.setType("V");
        item.setField("ETH_TYPE");
        item.setValue(String.valueOf(frame.getEtherType()));*/
        this.itemList.add(item);
        return frame.getEtherType();
    }

    public final Port ingressPort() {
    	TraceItem item = constructV("IN_PORT", ingressPort.id);
        this.itemList.add(item);
        return ingressPort;
    }

    public void setIPv4Src(int value) {
    	instruction.addSPHAction(new SetPacketHeaderAction(Match.Field.IPv4_SRC, String.valueOf(value)));
    }
    
    public final int IPv4Src() {
    	if (instruction.isFieldModified(Match.Field.IPv4_SRC)) {
    		return Integer.parseInt(instruction.getSPHAction(Match.Field.IPv4_SRC).getValue());
    	}
    	IPv4 pIP = (IPv4) frame.getPayload();
    	TraceItem item = constructV("IPv4_SRC", String.valueOf(pIP.getSourceAddress()));
        /*item.setType("V");
        item.setField("IPv4_SRC");
        item.setValue(String.valueOf(pIP.getSourceAddress()));*/
        this.itemList.add(item);
    	return pIP.getSourceAddress();
    }
    
    public void setIPv4Dst(int value) {
    	instruction.addSPHAction(new SetPacketHeaderAction(Match.Field.IPv4_DST, String.valueOf(value)));
    }

    public final int IPv4Dst() {
    	if (instruction.isFieldModified(Match.Field.IPv4_DST)) {
    		return Integer.parseInt(instruction.getSPHAction(Match.Field.IPv4_DST).getValue());
    	}
    	IPv4 pIP = (IPv4) frame.getPayload();
    	TraceItem item = constructV("IPv4_DST", String.valueOf(pIP.getDestinationAddress()));
    	
        this.itemList.add(item);
    	return pIP.getDestinationAddress();
    }
    
    public void setTCPSrcPort(int value) {
    	instruction.addSPHAction(new SetPacketHeaderAction(Match.Field.TCP_SRC_PORT, String.valueOf(value)));
    }

    public final int TCPSrcPort() {
    	if (instruction.isFieldModified(Match.Field.TCP_SRC_PORT)) {
    		return Integer.parseInt(instruction.getSPHAction(Match.Field.TCP_SRC_PORT).getValue());
    	}
    	IPv4 pIP = (IPv4) frame.getPayload();
    	TCP pTCP = (TCP) pIP.getPayload();
    	
    	TraceItem item = constructV("TCP_SRC_PORT", String.valueOf(pTCP.getSourcePort()));
    	
        this.itemList.add(item);
        return pTCP.getSourcePort();
    }
    
    public void setTCPDstPort(int value) {
    	instruction.addSPHAction(new SetPacketHeaderAction(Match.Field.TCP_DST_PORT, String.valueOf(value)));
    }

    public final int TCPDstPort() {
    	if (instruction.isFieldModified(Match.Field.TCP_DST_PORT)) {
    		return Integer.parseInt(instruction.getSPHAction(Match.Field.TCP_DST_PORT).getValue());
    	}
    	IPv4 pIP = (IPv4) frame.getPayload();
    	TCP pTCP = (TCP) pIP.getPayload();
    	TraceItem item = constructV("TCP_DST_PORT", String.valueOf(pTCP.getDestinationPort()));
        this.itemList.add(item);
        return pTCP.getDestinationPort();
    }

    public final boolean ethSrcIs(long exp) {
        long addr = Ethernet.toLong(frame.getSourceMACAddress());
        
        if (instruction.isFieldModified(Match.Field.ETH_SRC)) {
        	addr = Long.parseLong(instruction.getSPHAction(Match.Field.ETH_SRC).getValue());
        	return (addr == exp);
        }
        TraceItem item = constructT("ETH_SRC", String.valueOf(exp), addr == exp?"1":"0");
        this.itemList.add(item);
        return (addr == exp);
    }

    public final boolean ethDstIs(long exp) {
        long addr = Ethernet.toLong(frame.getDestinationMACAddress());
        
        if (instruction.isFieldModified(Match.Field.ETH_DST)) {
        	addr = Long.parseLong(instruction.getSPHAction(Match.Field.ETH_DST).getValue());
        	return (addr == exp);
        }
        TraceItem item = constructT("ETH_DST", String.valueOf(exp), addr == exp?"1":"0");
        this.itemList.add(item);
        return (addr == exp);
    }

    public final boolean ethTypeIs(int exp) {
    	int ethType = frame.getEtherType();
    	
    	if (instruction.isFieldModified(Match.Field.ETH_TYPE)) {
    		ethType = Integer.parseInt(instruction.getSPHAction(Match.Field.ETH_TYPE).getValue());
        	return (ethType == exp);
        }
        TraceItem item = constructT("ETH_TYPE", String.valueOf(exp), ethType == exp?"1":"0");
        this.itemList.add(item);
        return (ethType == exp);
    }

    public final boolean ingressPortIs(Port exp) {
    	TraceItem item = constructT("IN_PORT", exp.id, ingressPort.id.equals(exp.id)?"1":"0");
        this.itemList.add(item);
        return (ingressPort.id.equals(exp.id));
    }
    
    public final boolean IPv4SrcIs(int exp) {
    	IPv4 ipv4 = (IPv4)frame.getPayload();
    	int addr = ipv4.getSourceAddress();
    	
    	if (instruction.isFieldModified(Match.Field.IPv4_SRC)) {
    		addr = Integer.parseInt(instruction.getSPHAction(Match.Field.IPv4_SRC).getValue());
        	return (addr == exp);
        }
        TraceItem item = constructT("IPv4_SRC", String.valueOf(exp), addr == exp?"1":"0");
        this.itemList.add(item);
        return (addr == exp);
    }
    
    public final boolean IPv4DstIs(int exp) {
    	IPv4 ipv4 = (IPv4)frame.getPayload();
    	int addr = ipv4.getDestinationAddress();
    	
    	if (instruction.isFieldModified(Match.Field.IPv4_DST)) {
    		addr = Integer.parseInt(instruction.getSPHAction(Match.Field.IPv4_DST).getValue());
        	return (addr == exp);
        }
        TraceItem item = constructT("IPv4_DST", String.valueOf(exp), addr == exp?"1":"0");
        this.itemList.add(item);
        return (addr == exp);
    }
    
    public final boolean TCPDstPortIs(int exp) {
        IPv4 pIP = (IPv4) frame.getPayload();
        if (pIP.getProtocol() != IPv4.PROTOCOL_TCP){
        	TraceItem item = constructT("TCP_DST_PORT", String.valueOf(exp), "0");
            this.itemList.add(item);
        	return false;
        }
        TCP pTCP = (TCP) pIP.getPayload();
        int addr = pTCP.getDestinationPort();

        if (instruction.isFieldModified(Match.Field.TCP_DST_PORT)) {
            addr = Integer.parseInt(instruction.getSPHAction(Match.Field.TCP_DST_PORT).getValue());
            return (addr == exp);
        }

        TraceItem item = constructT("TCP_DST_PORT", String.valueOf(exp), addr == exp?"1":"0");
        this.itemList.add(item);
        return (addr == exp);
    }

    public final boolean TCPSrcPortIs(int exp) {
        IPv4 pIP = (IPv4) frame.getPayload();
        if (pIP.getProtocol() != IPv4.PROTOCOL_TCP){
        	TraceItem item = constructT("TCP_SRC_PORT", String.valueOf(exp), "0");
            this.itemList.add(item);
        	return false;
        }
        TCP pTCP = (TCP) pIP.getPayload();
        int addr = pTCP.getSourcePort();

        if (instruction.isFieldModified(Match.Field.TCP_SRC_PORT)) {
            addr = Integer.parseInt(instruction.getSPHAction(Match.Field.TCP_SRC_PORT).getValue());
            return (addr == exp);
        }

        TraceItem item = constructT("TCP_SRC_PORT", String.valueOf(exp), addr == exp?"1":"0");
        this.itemList.add(item);
        return (addr == exp);
    }
    
    public Instruction getInstruction() {
    	return this.instruction;
    }
    
    private void setRouteAction(RouteAction routeAction) {
    	this.instruction.setRouteAction(routeAction);
    }
    
    public void setRoute(String otherAction) {
    	if (otherAction.equals(Route.DROP)) {
    		setRouteAction(RouteAction.Drop());
    	} else if (otherAction.equals(Route.FLOOD)) {
    		setRouteAction(RouteAction.Flood());
    	} else if (otherAction.equals(Route.PUNT)) {
    		setRouteAction(RouteAction.Punt());
    	} else if (otherAction.equals("null")) {
    		List<String> pathList = new ArrayList<String>();
    		setRouteAction(new Path(pathList, "openflow:0:0"));
    	}
    }
    
    private String constructLinkString(String tpIdString) {
    	return "<" + tpIdString + "," + tpIdString + "," + "openflow:0:0" + ">";
    }
    
    public void setRoute(String[] path) {
    	List<String> pathList = new ArrayList<String>();
    	String lastTpIdString = null;
    	for (int i = 0; i < path.length; i++) {
    		String item = path[i];
    		if (i == path.length - 1) {
    			// this is lasttpid
    			lastTpIdString = path[i];
    		} else if (item.contains("openflow")) {
    			// this is tpid
    			String linkString = constructLinkString(item);
    			pathList.add(linkString);
    		}
    	}
    	setRouteAction(new Path(pathList, lastTpIdString));
    }
    
    public RouteAction route() {
    	return this.instruction.getRouteAction();
    }
    
    public Map<Match.Field, SetPacketHeaderAction> getSPHActions() {
    	return this.instruction.getSPHActions();
    }
    
    public void setIsReadTopo() {
    	this.isReadTopo = true;
    }
    
    public void clearIsReadTopo() {
    	this.isReadTopo = false;
    }

	@Override
    public String toString() {
		
        return "MaplePacket [ingressPort=" + ingressPort + ", frame=" + frame + "]";
    }
	
	public int hashCode(){
		IPv4 ipv4 = (IPv4)frame.getPayload();
    	int addr = ipv4.getSourceAddress();
    	int group = addr % 4;
    	if (group == 0 || group == 1) return 0;
    	else return 1;
	}

}
