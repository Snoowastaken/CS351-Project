import java.util.concurrent.TimeUnit;
import desmoj.core.simulator.*;
import desmoj.core.statistic.*;
public class ReplicationModel extends Model{
    //program constants and config variables
    public static final int NUM_REPLICATIONS = 100;
    public static final boolean INCLUDE_OUTPUT_PER_REPLICATION = true;
    public static final boolean INCLUDE_REPORT_PER_REPLICATION = true;
    protected ConfidenceCalculator repDailyOperatingCost;
    protected ConfidenceCalculator repPatientsArrived;
    protected ConfidenceCalculator repPatientsBalked;
    protected ConfidenceCalculator repPatientsDiverted;
    protected ConfidenceCalculator repResponseTime;
    protected ConfidenceCalculator repNurseUtilization;
    protected ConfidenceCalculator repSpecialistUtilization;
    protected ConfidenceCalculator repWaitingRoomSize;

    // Constructor
    public ReplicationModel(Model owner, String name, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
    }

    //Description of the Model
    public String description() {
        return "A model for running multiple medical model expermiments.";
    }

    public void doInitialSchedules() {
        if(INCLUDE_OUTPUT_PER_REPLICATION) {
            System.out.format(
                    "                  Patients  Patients  Patients  Response  Avg.Nurse(s)  Avg.Specialist(s) Waiting Room Daily Operating \n"
                            + "Repl.#  Arrived   Balked    Diverted  Time      Util.         Util.             Size         Costs\n"
                            + "---------------------------------------------------------------------------------------\n"
            );
        }
        for(int i = 1; i <= NUM_REPLICATIONS; i++) {
            boolean runSuccessful;
            do{
                runSuccessful = runSimulation(i);
                if(!runSuccessful) {
                    System.out.println("Simulation failed, retrying in two seconds");
                    try{
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        //do nothing :)
                    }
                }
            }while (!runSuccessful);
        }
    }

    public boolean runSimulation(int replicationNumber) {
        medicalModel model = new medicalModel(null, "Medical Clinic", true, true);
        String outputFilename = "output/medicalModel" + "_Repl_" + replicationNumber + ".txt";
        Experiment exp = new Experiment(outputFilename, INCLUDE_REPORT_PER_REPLICATION);
        exp.setSeedGenerator(979 + 2 * replicationNumber);
        model.connectToExperiment(exp);
        exp.setShowProgressBar(false);
        exp.stop(medicalModel.numInSystemCondition);
        //TODO: ADD VARIABLES TO CHANGE NUM ROOMS, SPECIALISTS, AND NURSE
        exp.traceOff(new TimeInstant(0));
        exp.debugOff(new TimeInstant(0));
        exp.setSilent(true);
        try {
            exp.start();
        } catch (Exception e) {
            System.out.format("WARNING: Rep %d: Exception during run; "
                    + "retrying...\n", replicationNumber);
            exp.finish();
            return false;
        }
        //check for problems
        if(exp.hasError() || exp.isAborted()){
            System.out.format("WARNING: Rep %d: Error during run; "
                    + "retrying...\n", replicationNumber);
            exp.finish();
            return false;
        }

        // If experiment stopped without errors, sleep for a bit before
        // generating the report and finishing things off. This seems to
        // significantly reduce the occurrence of threading issues over
        // repeated replications.
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Do nothing
        }
        // Generate report as needed, then stop and close all output files
        exp.report();
        exp.finish();

        // Get the output values from the model
        double patientsArrived = model.patientsArrived.getObservations();
        double patientsBalk = model.patientsBalk.getObservations();
        double patientsDiverted = model.patientsDiverted.getValue();
        double responseTime = model.responseTimeFullyTreated.getMean();
        double nurseUtilization = model.nurseUtilization.getMean();
        double specialistUtilization = model.specialistUtilization.getMean();
        double waitingRoomSize = model.waitingRoom.averageLength();
        double dailyOperatingCost = model.dailyOperatingCost.getValue();

        // Lastly, check for bad values in output to prevent them from
        // corrupting the aggregate replication results.
        if (patientsArrived <= 0 || waitingRoomSize < 0 || patientsBalk < 0 || patientsDiverted < 0
                || responseTime < 0 || nurseUtilization < 0 || specialistUtilization < 0 || dailyOperatingCost < 0) {
            System.out.format("WARNING: Rep %d: Bad output values from run; "
                    + "retrying...\n", replicationNumber);
            return false;
        }

        repPatientsArrived.update(patientsArrived);
        repPatientsBalked.update(patientsBalk);
        repResponseTime.update(responseTime);
        repPatientsDiverted.update(patientsDiverted);
        repNurseUtilization.update(nurseUtilization);
        repSpecialistUtilization.update(specialistUtilization);
        repWaitingRoomSize.update(waitingRoomSize);
        repDailyOperatingCost.update(dailyOperatingCost);

        // Print replication results.
        if (INCLUDE_OUTPUT_PER_REPLICATION) {
            System.out.format("%6d: %7.3f %7.3f %7.3f %7.3f %7.3f %7.3f %7.3f %7.3f\n",
                    replicationNumber, patientsArrived, patientsBalk, patientsDiverted,
                    responseTime, nurseUtilization, specialistUtilization,
                    waitingRoomSize, dailyOperatingCost);
        }
        return true;
    }

    public void init(){
            repPatientsArrived = new ConfidenceCalculator(this, "Across Replic: Patients Arrived",  true, false);
            repPatientsBalked = new ConfidenceCalculator(this, "Across Replic: Patients Balked",  true, false);
            repPatientsDiverted = new ConfidenceCalculator(this, "Across Replic: Patients Diverted",  true, false);
            repResponseTime = new ConfidenceCalculator(this, "Across Replic: Avg. Patients Response Time",  true, false);;
            repNurseUtilization = new ConfidenceCalculator(this, "Across Replic: Nurse Utilization",  true, false);;
            repSpecialistUtilization = new ConfidenceCalculator(this, "Across Replic: Specialist Utilization",  true, false);;
            repWaitingRoomSize = new ConfidenceCalculator(this, "Across Replic: Avg. Waiting Room Size", true, false);
            repDailyOperatingCost = new ConfidenceCalculator(this, "Across Replic: Daily Operating Costs", true, false);
    }
    public static void main(String[] args){
        //add command line options
        Experiment.setReferenceUnit(TimeUnit.MINUTES);
        ReplicationModel repModel = new ReplicationModel(null, "Replication model for Medical Model", true, true);
        Experiment exp = new Experiment("MedicalReplication");
        repModel.connectToExperiment(exp);
        exp.setShowProgressBar(false);
        exp.stop(new TimeInstant(0));
        exp.traceOff(new TimeInstant(0));
        exp.debugOff(new TimeInstant(0));
        exp.setSilent(true);
        exp.start();
        exp.report();
        exp.finish();
    }
}
