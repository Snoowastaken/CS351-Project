import desmoj.core.simulator.*;
import java.util.concurrent.TimeUnit;
import co.paralleluniverse.fibers.SuspendExecution;
public class Generator extends SimProcess {
    public Generator(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    public void lifeCycle() throws SuspendExecution{
        medicalModel model = (medicalModel)getModel();
        while(true){
            //get current time
            double currentTime = model.presentTime().getTimeAsDouble(TimeUnit.MINUTES);
            double interarrivalTime = 0;
            if(currentTime >= 480 && currentTime <= 600){
                //sample morning interarrival time
                interarrivalTime = model.morningInterArrivalTime.sample();
            } else if(currentTime > 600 && currentTime <= 840){
                //sample afternoon interarrival time
                interarrivalTime = model.afternoonInterArrivalTime.sample();
            } else if(currentTime > 840 && currentTime <= 1200){
                //sample evening interarrival time
                interarrivalTime = model.eveningInterArrivalTime.sample();
            } else if(currentTime < 480){
                //wait until 8am
                hold(new TimeSpan(480 - currentTime, TimeUnit.MINUTES));
                continue;
            } else if(currentTime > 1200){
                //stop generating patients
                break;
            }
            hold(new TimeSpan(interarrivalTime, TimeUnit.MINUTES));
            Patient patient = new Patient(model, "Patient", true);
            patient.activate();
        }
    }

}
