package jmxdemo.connectors;

public interface SimpleStandardMBean {

    /**
     * Getter: set the "State" attribute of the "SimpleStandard" standard
     * MBean.
     *
     * @return the current value of the "State" attribute.
     */
    public String getState();

    /**
     * Setter: set the "State" attribute of the "SimpleStandard" standard
     * MBean.
     *
     */
    public void setState(String s);

    /**
     * Getter: get the "NbChanges" attribute of the "SimpleStandard" standard
     * MBean.
     *
     * @return the current value of the "NbChanges" attribute.
     */
    public int getNbChanges();

    /**
     * Operation: reset to their initial values the "State" and "NbChanges"
     * attributes of the "SimpleStandard" standard MBean.
     */
    public void reset();
}
