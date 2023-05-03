import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

public class Specialist extends SimProcess {
    public Specialist(Model model, String s, boolean b) {
        super(model, s, b);
    }

    @Override
    public void lifeCycle() throws SuspendExecution {
        medicalModel model = (medicalModel) getModel();
        while(true) { //FIXME add ModelCondition
            //if no one is waiting for specialist
            if(model.specialistWaitingQueue.isEmpty()){
                //insert specialist into queue of idle specialists
                model.specialistIdleQueue.insert(this);
                //wait until patient is activates specialist
                model.specialistUtilization.update(0);
                this.passivate();
            }
            else{
                model.specialistUtilization.update(100);
                //get patient
                Patient p = model.specialistWaitingQueue.removeFirst();
                //increase operating cost $200 for seeing specialist
                model.dailyOperatingCost.update(200);
                //hold for specialist treatment time
                this.hold(model.specialistTreatmentTime.sampleTimeSpan());
                //activate patient p to leave specialist
                p.activate();
            }
        }
    }
}
