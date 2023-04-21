import desmoj.core.simulator.*;
import java.util.concurrent.TimeUnit;

public class endOfNurseEvent extends Event<Patient> {
    public endOfNurseEvent(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    public void eventRoutine(Patient patient){
        //get reference to model
        medicalModel model = (medicalModel)getModel();
        //sample referral rate
        boolean referred = model.referralRate.sample();
        patient.referred = referred;
        if(!referred){
            //patient is not referred
            //patient leaves clinic
            model.patientsTreated.update();
            model.responseTimeFullyTreated.update(model.presentTime().getTimeAsDouble() - patient.arrivalTime);
            model.dailyOperatingCost.update(100);
            //send trace note saying patient is leaving
            model.sendTraceNote("Patient is leaving");
        } else {
            //patient is referred
            //send trace note saying patient is being referred
            model.sendTraceNote("Patient is being referred");
            //get specialist queue size
            int specialistQueueSize = model.specialistQueue.size();
            if(specialistQueueSize == 4){
                //specialist queue is full
                //send trace note saying patient is being diverted
                model.sendTraceNote("Patient is being diverted");
                model.patientsDiverted.update();
                model.dailyOperatingCost.update(500);
            } else {
                //specialist queue is not full
                //add patient to specialist queue
                model.specialistQueue.insert(patient);
                //check if specialist is busy
                if(!model.specialistIsBusy){
                    //specialist is not busy
                    //set specialist to busy
                    model.specialistIsBusy = true;
                    //remove patient from specialist queue
                    model.specialistQueue.remove(patient);
                    //get current time
                    double currentTime = model.presentTime().getTimeAsDouble();
                    double responseTime = currentTime - patient.arrivalTime;
                    if(responseTime > 30){
                        model.specialistIsBusy = false;
                        model.patientsDiverted.update();
                        model.dailyOperatingCost.update(500);
                    } else {
                        //sample specialist treatment time
                        double specialistTreatmentTime = model.specialistTreatmentTime.sample();
                        //schedule end of specialist event
                        endOfSpecialistEvent endOfSpecialist = new endOfSpecialistEvent(model, "End of Specialist Event", true);
                        endOfSpecialist.schedule(patient, new TimeSpan(specialistTreatmentTime, TimeUnit.MINUTES));
                    }
                } else {
                    model.sendTraceNote("Specialist is busy");
                }
            }

        }





    }
}
