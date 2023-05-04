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
        double interarrivalTime = 0;
        //only schedule if patient will arrive before 12 hours
        while(currentTime + interarrivalTime <= 720){
            //get interarrival times
            currentTime = model.presentTime().getTimeAsDouble(TimeUnit.MINUTES);
            if (currentTime >= 0 && currentTime <= 120) {
                //sample morning interarrival time
                interarrivalTime = model.morningInterArrivalTime.sample();
            } else if (currentTime > 120 && currentTime <= 480) {
                //sample afternoon interarrival time
                interarrivalTime = model.afternoonInterArrivalTime.sample();
            } else if (currentTime > 480 && currentTime <= 720) {
                //sample evening interarrival time
                interarrivalTime = model.eveningInterArrivalTime.sample();
            }
            hold(new TimeSpan(interarrivalTime, TimeUnit.MINUTES));
            Patient patient = new Patient(model, "Patient", true);
            patient.activate();
        }
    }
}
