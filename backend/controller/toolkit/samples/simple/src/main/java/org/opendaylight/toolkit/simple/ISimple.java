
package org.opendaylight.toolkit.simple;

import java.util.Map;
import java.util.UUID;

import org.opendaylight.controller.sal.utils.Status;

public interface ISimple {
    public UUID createData(SimpleData datum);
    public SimpleData readData(UUID uuid);
    public Map<UUID, SimpleData> readData();
    public Status updateData(UUID uuid, SimpleData data);
    public Status deleteData(UUID uuid);
}
