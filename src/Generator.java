import desmoj.core.simulator.*;

import java.util.concurrent.TimeUnit;

public class Generator extends ExternalEvent{
    //Constructor
    public Generator(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }
    //EventRoutine
    public void eventRoutine(){
        //get reference to model
        medicalModel model = (medicalModel)getModel();
        //create new patient
        Patient patient = new Patient(model, "Patient", true);
        //get current time
        double currentTime = model.presentTime().getTimeAsDouble();
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
        }
        patient.referred = false;
        if(currentTime < 480) {
            //close clinic
            model.sendTraceNote("Clinic is closed");
            Generator generator = new Generator(model, "Generator", true);
            generator.schedule(new TimeSpan(480, TimeUnit.MINUTES));
            model.dailyOperatingCost.update(1200);  //for the nurse daily
            model.dailyOperatingCost.update(1500);  //for the specialist daily
            model.dailyOperatingCost.update(300*4); //for each exam room's daily cost
        } else if(currentTime > 1200){
            model.sendTraceNote("Clinic is closed for the day!");
        } else {
            //send trace note saying patient has arrived
            model.sendTraceNote(patient + " has arrived");
            //schedule arrival event
            arrivalEvent arrival = new arrivalEvent(model, "Arrival Event", true);
            arrival.schedule(patient);
            //schedule next generator event
            Generator generator = new Generator(model, "Generator", true);
            generator.schedule(new TimeSpan(interarrivalTime, TimeUnit.MINUTES));
        }


    }
}
