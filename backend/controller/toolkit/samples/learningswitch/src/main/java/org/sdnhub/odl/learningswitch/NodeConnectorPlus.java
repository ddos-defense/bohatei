package org.sdnhub.odl.learningswitch;
import org.opendaylight.controller.sal.core.ConstructionException;
import org.opendaylight.controller.sal.core.NodeConnector;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javax.xml.bind.annotation.XmlRootElement;

class Pair<F, S> {
    @XmlElement(name="first")
	private F first;
    @XmlElement(name="second")
    private S second;
    public Pair(F f, S s) {
    	first = f;
    	second = s;
    }
}

@XmlRootElement(name="NodeConnector")
public class NodeConnectorPlus {
	
	@XmlElement(name="nodeconnector")
	NodeConnector nc;
	// History of all MACs / IPs / whatever seen on this node conncetor
	List<Pair<String, Date> > history; 
	// Add an
	public NodeConnectorPlus(NodeConnector nc) {
		this.nc = nc;
		history = new ArrayList<Pair<String, Date>>();
	}
	
	public String toString() {
		return nc.toString();
	}

}
