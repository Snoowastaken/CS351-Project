import desmoj.core.simulator.*;
import co.paralleluniverse.fibers.SuspendExecution;
import java.util.concurrent.TimeUnit;

public class Patient extends SimProcess{

    public Patient(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    public void lifeCycle() throws SuspendExecution{
        medicalModel model = (medicalModel)getModel();






    }
}
