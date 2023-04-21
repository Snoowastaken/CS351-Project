import desmoj.core.dist.BoolDistBernoulli;
import desmoj.core.simulator.*;
import java.util.concurrent.TimeUnit;

public class arrivalEvent extends Event<Patient> {
    public arrivalEvent(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    public void eventRoutine(Patient patient){
        //get reference to model
        medicalModel model = (medicalModel)getModel();
        //update patientsArrived
        model.patientsArrived.update();
        //check if patient balks
        //get size of waiting room
        int waitingRoomSize = model.waitingRoom.size();
        BoolDistBernoulli balk = new BoolDistBernoulli(model, "Balk", (double) waitingRoomSize /8, true, true);
        boolean balks = balk.sample();
        if(balks) {
            //patient balks
            model.patientsBalk.update();
            model.patientsDiverted.update();
            model.dailyOperatingCost.update(500);
        } else {
            patient.arrivalTime = model.presentTime().getTimeAsDouble();
            //add patient to waiting room
            model.waitingRoom.insert(patient);
            //update average waiting room size
            model.avgNumPatientsWaiting.update(model.waitingRoom.size());
            //check if nurse is busy
            if(!model.nurseIsBusy) {
                //nurse is not busy
                //set nurse to busy
                model.nurseIsBusy = true;
                model.nurseUtilization.update(1);
                //remove patient from waiting room
                model.waitingRoom.remove(patient);
                //get current time
                double currentTime = model.presentTime().getTimeAsDouble();
                double responseTime = currentTime - patient.arrivalTime;
                if(responseTime > 30){
                    model.nurseIsBusy = false;
                    model.nurseUtilization.update(0);
                    model.patientsDiverted.update();
                    model.dailyOperatingCost.update(500);
                } else {
                    //sample treatment time
                    double treatmentTime = model.treatmentTime.sample();
                    //schedule end of nurse event
                    endOfNurseEvent endOfNurse = new endOfNurseEvent(model, "End of Nurse Event", true);
                    endOfNurse.schedule(patient, new TimeSpan(treatmentTime, TimeUnit.MINUTES));
                    //add to daily operating cost
                    model.dailyOperatingCost.update(100);
                    model.nurseIsBusy = false;
                    model.nurseUtilization.update(0);
                    model.patientsTreated.update();
                    model.responseTimeFullyTreated.update(model.presentTime().getTimeAsDouble() - patient.arrivalTime);
                }
            } else {
                model.sendTraceNote("Nurse is busy");
            }
        }
    }
}
