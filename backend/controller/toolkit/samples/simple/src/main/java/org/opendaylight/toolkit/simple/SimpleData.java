
package org.opendaylight.toolkit.simple;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class SimpleData {
    @XmlElement
    private String uuid;
    @XmlElement
    private String foo;
    @XmlElement
    private String bar;

    public String getUuid() {
        return uuid;
    }
    public String getFoo() {
        return foo;
    }
    public String getBar() {
        return bar;
    }
    public SimpleData() {
        super();
    }
    public SimpleData(String uuid, String foo, String bar) {
        super();
        this.uuid = uuid;
        this.foo = foo;
        this.bar = bar;
    }
}
