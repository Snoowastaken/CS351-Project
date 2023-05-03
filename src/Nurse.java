import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

public class Nurse extends SimProcess {
    public Nurse(Model model, String s, boolean b) {
        super(model, s, b);
    }

    @Override
    public void lifeCycle() throws SuspendExecution {
            medicalModel model = (medicalModel) getModel();
            while(true){ //FIXME: possibly use ModelCondition
                //no patients in waiting room
                if(model.waitingRoom.isEmpty()){
                    //insert nurse in queue of idle nurses
                    model.nurseIdleQueue.insert(this);
                    //nurse waits until patient activate()
                    model.nurseUtilization.update(0);
                    this.passivate();
                }
                else{
                    //nurse is being used
                    model.nurseUtilization.update(100);
                    //$100+ per patient treated by nurse
                    model.dailyOperatingCost.update(100);
                    //remove patient from waiting room
                    Patient p = model.waitingRoom.removeFirst();
                    this.hold(model.nurseTreatmentTime.sampleTimeSpan());
                    p.activate();
                }
            }
    }
}
