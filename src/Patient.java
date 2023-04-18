import desmoj.core.simulator.*;
public class Patient extends Entity{
    //Entity attributes
    protected boolean referred;


    public Patient(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }
}
