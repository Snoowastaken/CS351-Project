import desmoj.core.simulator.*;
import java.util.concurrent.TimeUnit;
import co.paralleluniverse.fibers.SuspendExecution;
public class Generator extends SimProcess {
    public Generator(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    public void lifeCycle() throws SuspendExecution{
        medicalModel model = (medicalModel)getModel();
        double currentTime = 0;
        while(currentTime <= 720) { //12 hours
            //get currentTime
            currentTime = model.presentTime().getTimeAsDouble(TimeUnit.MINUTES);
            double interarrivalTime = 0;
            if (currentTime >= 0 && currentTime <= 120) {
                //sample morning interarrival time
                interarrivalTime = model.morningInterArrivalTime.sample();
            } else if (currentTime > 120 && currentTime <= 480) {
                //sample afternoon interarrival time
                interarrivalTime = model.afternoonInterArrivalTime.sample();
            } else if (currentTime > 480) {
                //sample evening interarrival time
                interarrivalTime = model.eveningInterArrivalTime.sample();
            }
            hold(new TimeSpan(interarrivalTime, TimeUnit.MINUTES));
            Patient patient = new Patient(model, "Patient", true);
            patient.activate();
            }
        }
    }
