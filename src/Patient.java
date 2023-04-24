import desmoj.core.dist.BoolDistBernoulli;
import desmoj.core.simulator.*;
import co.paralleluniverse.fibers.SuspendExecution;
import java.util.concurrent.TimeUnit;

public class Patient extends SimProcess{

    public Patient(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    public void lifeCycle() throws SuspendExecution{
        medicalModel model = (medicalModel)getModel();
        //get starting time
        double startTime = model.presentTime().getTimeAsDouble(TimeUnit.MINUTES);
        //get size of waiting room
        int waitingRoomSize = model.waitingRoom.size();
        BoolDistBernoulli balks = new BoolDistBernoulli(model, "Balks", (double) waitingRoomSize/8, true, true);
        //check if patient balks
        boolean balk = balks.sample();
        if(balk){
            //patient balks
            model.patientsBalk.update();
            model.sendTraceNote(this.getName() + " has balked");
        } else {
            //patient does not balk
            //enter waiting room
            model.waitingRoom.insert(this);
            model.sendTraceNote(this.getName() + " has entered the waiting room");
            if(model.nurseIsBusy){
                passivate();
            } else {
                model.nurseIsBusy = true;
                model.waitingRoom.removeFirst();
                double treatmentTime = model.treatmentTime.sample();
                hold(new TimeSpan(treatmentTime, TimeUnit.MINUTES));
                model.nurseIsBusy = false;
                //check if patient gets referred to specialist
                boolean referral = model.referralRate.sample();
                if(!referral){
                    //patient is fully treated
                    model.sendTraceNote(this.getName() + " has been treated");
                    model.patientsTreated.update();
                    model.responseTimeFullyTreated.update(model.presentTime().getTimeAsDouble(TimeUnit.MINUTES) - startTime);
                } else {
                    //patient is referred to specialist
                    model.sendTraceNote(this.getName() + " has been referred to a specialist");
                    model.specialistQueue.insert(this);
                    if(model.specialistIsBusy){
                        passivate();
                    } else {
                        model.specialistIsBusy = true;
                        model.specialistQueue.removeFirst();
                        double specialistTreatmentTime = model.specialistTreatmentTime.sample();
                        hold(new TimeSpan(specialistTreatmentTime, TimeUnit.MINUTES));
                        model.specialistIsBusy = false;
                        model.sendTraceNote(this.getName() + " has been treated");
                        model.patientsTreated.update();
                        model.responseTimeFullyTreated.update(model.presentTime().getTimeAsDouble(TimeUnit.MINUTES) - startTime);
                    }
                }

            }

        }









    }
}
