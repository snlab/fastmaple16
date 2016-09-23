public class M1 extends MapleAppBase {

	private static final String H1 = "10.0.0.1";
	private static final String H2 = "10.0.0.2";
	private static final String[] H12_HIGH_PATH = { H1, "openflow:1:3", "openflow:2:2", "openflow:4:1" };
	private static final String[] H12_LOW_PATH = { H1, "openflow:1:4", "openflow:3:2", "openflow:4:1" };
	private static final String[] H21_HIGH_PATH = { H2, "openflow:4:4", "openflow:2:1", "openflow:1:1" };
	private static final String[] H21_LOW_PATH = { H2, "openflow:4:5", "openflow:3:1", "openflow:1:1" };

	@Override
	public void onPacket(MaplePacket pkt) {
		if (pkt.ethTypeIs(Ethernet.TYPE_IPv4)) {
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
			} else {
				pkt.setRoute(Route.DROP);
			}
		} else {
			this.passToNext(pkt);
		}
	}
}
