import desmoj.core.simulator.*;
public class Patient extends Entity{
    //Entity attributes
    protected boolean referred;
    protected double arrivalTime;



    public Patient(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }
}
