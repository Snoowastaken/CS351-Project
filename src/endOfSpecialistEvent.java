import desmoj.core.simulator.*;
public class endOfSpecialistEvent extends Event<Patient> {
    public endOfSpecialistEvent(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    public void eventRoutine(Patient patient){
        //get reference to model
        medicalModel model = (medicalModel)getModel();
        //get current time
        double currentTime = model.presentTime().getTimeAsDouble();
        double responseTime = currentTime - patient.arrivalTime;
        //send trace note about patient leaving
        model.sendTraceNote(patient + " is leaving");
    }
}
