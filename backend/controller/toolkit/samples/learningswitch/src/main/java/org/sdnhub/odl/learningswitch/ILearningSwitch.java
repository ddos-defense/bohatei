
package org.sdnhub.odl.learningswitch;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.utils.Status;
import org.sdnhub.odl.learningswitch.MacTable.MacPortTableElem;

public interface ILearningSwitch {
	public Table getData();
	public void deleteData();
	public String getFunction();
	public Boolean setFunction(String function);
}
