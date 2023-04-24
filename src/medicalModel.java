import desmoj.core.simulator.*;
import desmoj.core.dist.*;
import desmoj.core.statistic.*;
import java.util.concurrent.TimeUnit;

public class medicalModel extends Model {
    /* State Variables */
    protected boolean nurseIsBusy;
    protected boolean specialistIsBusy;
    protected int examRooms;
    protected int numNurses;
    protected int numSpecialists;





    /* Structures */
    protected ProcessQueue<Patient> waitingRoom;
    protected ProcessQueue<Patient> specialistQueue;


    /* Distribution Variables */
    protected ContDistExponential morningInterArrivalTime;
    protected ContDistExponential afternoonInterArrivalTime;
    protected ContDistExponential eveningInterArrivalTime;
    protected ContDistExponential treatmentTime;
    protected BoolDistBernoulli referralRate;
    protected ContDistExponential specialistTreatmentTime;



    /* Statistics */
    protected Count patientsArrived;
    protected Count patientsBalk;
    protected Count patientsDiverted;
    protected Count patientsTreated;
    protected Tally responseTimeFullyTreated;
    protected Accumulate nurseUtilization;
    protected Accumulate specialistUtilization;
    protected Count avgNumPatientsWaiting;
    protected Count dailyOperatingCost;



    // Constructor
    public medicalModel(Model owner, String name, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
    }

    //Description of the Model
    public String description() {
        return "This is a simple model of a medical clinic.";
    }

    //Creates initial processes
    public void doInitialSchedules() {
        Generator generator = new Generator(this, "Generator", true);
        generator.activate();
    }

    //Initializes the model
    public void init() {
        // Initialize state variables
        nurseIsBusy = false;
        specialistIsBusy = false;
        examRooms = 4;
        numNurses = 1;
        numSpecialists = 1;

        // Initialize structures
        waitingRoom = new ProcessQueue<>(this, "Waiting Room", true, true);
        specialistQueue = new ProcessQueue<>(this, "Specialist Queue", true, true);

        // Initialize distributions
        morningInterArrivalTime = new ContDistExponential(this, "Morning Inter-Arrival Time", 15.0, true, true);
        afternoonInterArrivalTime = new ContDistExponential(this, "Afternoon Inter-Arrival Time", 6.0, true, true);
        eveningInterArrivalTime = new ContDistExponential(this, "Evening Inter-Arrival Time", 9.0, true, true);
        treatmentTime = new ContDistExponential(this, "Treatment Time", 8.0, true, true);
        referralRate = new BoolDistBernoulli(this, "Referral Rate", 0.4, true, true);
        specialistTreatmentTime = new ContDistExponential(this, "Specialist Treatment Time", 25.0, true, true);

        // Initialize statistics
        patientsArrived = new Count(this, "Patients Arrived", true, true);
        patientsBalk = new Count(this, "Patients Balked", true, true);
        patientsDiverted = new Count(this, "Patients Diverted", true, true);
        patientsTreated = new Count(this, "Patients Treated", true, true);
        responseTimeFullyTreated = new Tally(this, "Response Time Fully Treated", true, true);
        nurseUtilization = new Accumulate(this, "Nurse Utilization", true, true);
        specialistUtilization = new Accumulate(this, "Specialist Utilization", true, true);
        avgNumPatientsWaiting = new Count(this, "Average Number of Patients Waiting", true, true);
        dailyOperatingCost = new Count(this, "Daily Operating Cost", true, true);
    }

    //runs the model
    public static void main(String[] args){
        Experiment.setReferenceUnit(TimeUnit.MINUTES);
        medicalModel model = new medicalModel(null, "Medical Model", true, true);
        Experiment exp = new Experiment("Medical Model Experiment");
        model.connectToExperiment(exp);
        exp.setShowProgressBar(false);
        exp.stop(new TimeInstant(24, TimeUnit.HOURS));
        exp.tracePeriod(new TimeInstant(0, TimeUnit.MINUTES),
                new TimeInstant(1440, TimeUnit.MINUTES));
        exp.debugPeriod(new TimeInstant(0, TimeUnit.MINUTES),
                new TimeInstant(1440, TimeUnit.MINUTES));
        exp.start();
        exp.report();
        exp.finish();
    }



}
