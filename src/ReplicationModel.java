import java.util.concurrent.TimeUnit;
import desmoj.core.simulator.*;
import desmoj.core.statistic.*;
public class ReplicationModel extends Model{
    //program constants and config variables
    public static final int NUM_REPLICATIONS = 100;
    public static final boolean INCLUDE_OUTPUT_PER_REPLICATION = true;
    public static final boolean INCLUDE_REPORT_PER_REPLICATION = true;

    //trackers for replication model
    //TODO: ALL OF THESE

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
            //TODO THIS STATS THING
        }
        for(int i = 0; i < NUM_REPLICATIONS; i++) {
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
        exp.setSeedGenerator(979 + 2L * replicationNumber);
        model.connectToExperiment(exp);
        exp.setShowProgressBar(false);
        exp.stop(new TimeInstant(24, TimeUnit.HOURS));
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

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Do nothing
        }

        exp.report();
        exp.finish();

        //TODO GET STATS

        return true;
    }

    public void init(){
        //TODO THIS SHIT

    }
    public static void main(String[] args){
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
