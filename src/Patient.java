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
            if (model.nurseIsBusy) {
                passivate();
            } else {
                //get current time
                double currentTime = model.presentTime().getTimeAsDouble(TimeUnit.MINUTES);
                //check how long patient has been waiting
                double waitingTime = currentTime - startTime;
                if (waitingTime > 30) {
                    //patient leaves
                    model.sendTraceNote(this.getName() + " has been sent to the ER");
                } else {
                    model.nurseIsBusy = true;
                    model.waitingRoom.removeFirst();
                    double treatmentTime = model.treatmentTime.sample();
                    hold(new TimeSpan(treatmentTime, TimeUnit.MINUTES));
                    model.nurseIsBusy = false;
                    //check if patient gets referred to specialist
                    boolean referral = model.referralRate.sample();
                    if (!referral) {
                        //patient is fully treated
                        model.sendTraceNote(this.getName() + " has been treated");
                        model.patientsTreated.update();
                        model.responseTimeFullyTreated.update(model.presentTime().getTimeAsDouble(TimeUnit.MINUTES) - startTime);
                    } else {

                    }

                }

            }


        }




    }
}
