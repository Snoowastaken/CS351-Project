import desmoj.core.simulator.*;
import co.paralleluniverse.fibers.SuspendExecution;
import java.util.concurrent.TimeUnit;

public class Patient extends SimProcess {
    protected int room = 0;
    protected double arrivalTime;

    public Patient(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    public void lifeCycle() throws SuspendExecution {
        medicalModel model = (medicalModel) getModel();
        /*NURSE STAGE*/
        //get patient arrival time
        this.arrivalTime = model.presentTime().getTimeAsDouble(TimeUnit.MINUTES);
        model.numPatientsInSystem.update(1);
        //update patients arrived
        model.patientsArrived.update(1);
        //get size of waiting room
        //if random int balk <= k number of people in waiting room
       if (model.balk.sample() <= model.waitingRoom.size()) {
            //patient balks
            model.patientsBalk.update(1);
            model.sendTraceNote(this.getName() + " balked due to waiting room size: " + model.waitingRoom.size());
            model.dailyOperatingCost.update(500);
        } else {
            //patient does not balk and enters waiting room
            model.waitingRoom.insert(this);
            model.sendTraceNote("Waiting room size: " + model.waitingRoom.size());
            //if no nurses are available
            if (!model.nurseIdleQueue.isEmpty()) {
                //activate first nurse available
                Nurse n = model.nurseIdleQueue.removeFirst();
                n.activate();
            }
                //patient passivates until nurse is done with them
                this.passivate();
                //get patient waiting time by subtracting current time from start time
                double waitingTime = model.presentTime().getTimeAsDouble(TimeUnit.MINUTES) - this.arrivalTime;
                if (!model.referral.sample()) {
                    //patient is fully treated by nurse
                    model.sendTraceNote(this.getName() + " has been treated by nurse");
                    model.patientsTreated.update(1);
                    model.responseTimeFullyTreated.update(waitingTime);
                }
                //needs Specialist
                else {
                    if (waitingTime >= 30) {
                        //patient diverted to ER
                        model.patientsDiverted.update(1);
                        model.sendTraceNote(this.getName() + " sent to ER due to waiting time: " + waitingTime);
                        model.dailyOperatingCost.update(500);
                    }
                    //SPECIALIST STAGE
                    else {
                        if (model.specialistWaitingQueue.size() >= model.numExamRooms - model.numSpecialists) {
                            //no clinic room available
                            model.sendTraceNote(this.getName() + " sent to ER due to no clinic rooms available");
                            model.patientsDiverted.update(1);
                            model.dailyOperatingCost.update(500);
                        } else {
                            //assign patient to clinic room
                            for (int i = 0; i < model.numExamRooms; i++) {
                                //first available empty room found is assigned to patient
                                if (model.clinicRoom[i] == 0) {
                                    //occupy room
                                    model.clinicRoom[i] = 1;
                                    model.sendTraceNote(this + " Occupies Room " + i);
                                    this.room = i;
                                    //wait for specialist (fifo)
                                    model.specialistWaitingQueue.insert(this);
                                    model.sendTraceNote(" Specialist queue size: " + model.specialistWaitingQueue.size());
                                    break;
                                }
                            }

                            if (!model.specialistIdleQueue.isEmpty()) {
                                Specialist s = model.specialistIdleQueue.removeFirst();
                                s.activate();
                            }

                            //wait to be called by specialist
                            this.passivate();

                            //patient leaves the room, available for next patient
                            model.clinicRoom[this.room] = 0;
                            model.patientsTreated.update(1);
                        }
                    }
                }
           }
            model.numPatientsInSystem.update(-1);
        }
    }
