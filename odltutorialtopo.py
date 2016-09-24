#!/usr/bin/env python

import sys

from mininet.net import Mininet
from mininet.topo import Topo
from mininet.node import RemoteController
from mininet.cli import CLI
from functools import partial
from mininet.node import OVSSwitch

def main():
    controller_ip = sys.argv[1]
    switch = partial( OVSSwitch, protocols='OpenFlow13' )

    topo = Topo()

    h1 = topo.addHost("h1", ip='10.0.0.1', mac='12:34:56:78:90:01')
    h2 = topo.addHost("h2", ip='10.0.0.2', mac='12:34:56:78:90:02')
    h3 = topo.addHost("h3", ip='10.0.0.3', mac='12:34:56:78:90:03')
    h4 = topo.addHost("h4", ip='10.0.0.4', mac='12:34:56:78:90:04')
    h6 = topo.addHost("h6", ip='10.0.0.6', mac='12:34:56:78:90:06')

    s1 = topo.addSwitch("s1")
    s2 = topo.addSwitch("s2")
    s3 = topo.addSwitch("s3")
    s4 = topo.addSwitch("s4")

    topo.addLink(h1, s1)
    topo.addLink(h3, s1)
    topo.addLink(s1, s2)
    topo.addLink(s1, s3)
    topo.addLink(s2, s4)
    topo.addLink(s3, s4)
    topo.addLink(h2, s4)
    topo.addLink(h4, s4)
    topo.addLink(h6, s4)

    net = Mininet(topo=topo, switch=switch, controller=RemoteController,
                  build=False, autoStaticArp = True)
    net.addController(ip=controller_ip)
    net.start()
    CLI(net)
    net.stop()

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print "Usage: python ./onesw.py [CONTROLLER_ADDR]"
        sys.exit()
    main()
