import desmoj.core.simulator.*;
import desmoj.core.dist.*;
import desmoj.core.statistic.*;
import java.util.concurrent.TimeUnit;

public class medicalModel extends Model {
    /* State Variables */
    protected int numExamRooms = 4;
    protected int numNurses = 1;
    protected int numSpecialists = 1;

    /* Structures */
    //patients waiting for nurse
    protected ProcessQueue<Patient> waitingRoom;
    //patients waiting for specialist
    protected ProcessQueue<Patient> specialistWaitingQueue;
    //Nurse(s) waiting for patients
    protected ProcessQueue<Nurse> nurseIdleQueue;
    //Specialist(s) waiting for patients
    protected ProcessQueue<Specialist> specialistIdleQueue;

    //Keeps track of available clinic rooms if patient needs Specialist
    // clinicRoom[i] == false  (clinic room is available for patient)
    // clinicRoom[i] != true: (clinic room is occupied)
    protected boolean[] occupiedClinicRooms;

    /* Distribution Variables */
    protected ContDistExponential morningInterArrivalTime;
    protected ContDistExponential afternoonInterArrivalTime;
    protected ContDistExponential eveningInterArrivalTime;
    protected ContDistExponential nurseTreatmentTime;
    protected BoolDistBernoulli referral;

    protected DiscreteDistUniform balk;
    protected ContDistExponential specialistTreatmentTime;

    /* Statistics */
    protected Count patientsArrived;
    protected Count patientsBalk;
    protected Count patientsDiverted;
    protected Count patientsTreated;
    protected Count numPatientsInSystem;
    protected Tally responseTimeFullyTreated;
    protected Accumulate nurseUtilization;
    protected Accumulate specialistUtilization;
    //protected Count avgNumPatientsWaiting;
    protected Count dailyOperatingCost;

    //Model Condition
    protected static ModelCondition numInSystemCondition;

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

        //adding idle Nurses
        for(int i = 0; i < numNurses; i++){
            Nurse n = new Nurse(this, "Nurse", true);
            nurseIdleQueue.insert(n);
            dailyOperatingCost.update(1200);
        }
        //adding idle Specialists
        for(int i = 0; i < numSpecialists; i++){
            Specialist s = new Specialist(this, "Specialist", true);
            specialistIdleQueue.insert(s);
            dailyOperatingCost.update(1500);
        }
        //initializing clinic rooms to available
        for(int i = 0; i < numExamRooms; i++){
            occupiedClinicRooms[i] = false;
            dailyOperatingCost.update(300);
        }

        Generator generator = new Generator(this, "Generator", true);
        generator.activate();
    }

    //Initializes the model
    public void init() {
        // Initialize state variables

        // Initialize structures
        waitingRoom = new ProcessQueue<>(this, "Waiting Room", true, true);
        specialistWaitingQueue = new ProcessQueue<>(this, "Specialist Queue", true, true);
        nurseIdleQueue = new ProcessQueue<>(this, "Nurse(s) Idle", true, true);
        specialistIdleQueue = new ProcessQueue<>(this, "Specialist(s) Idle", true, true);
        occupiedClinicRooms = new boolean[numExamRooms];

        // Initialize distributions
        morningInterArrivalTime = new ContDistExponential(this, "Morning Interarrival Time", 15.0, true, true);
        afternoonInterArrivalTime = new ContDistExponential(this, "Afternoon Interarrival Time", 6.0, true, true);
        eveningInterArrivalTime = new ContDistExponential(this, "Evening Interarrival Time", 9.0, true, true);
        nurseTreatmentTime = new ContDistExponential(this, "Treatment Time", 8.0, true, true);
        referral = new BoolDistBernoulli(this, "Referral Rate", 0.4, true, true);
        specialistTreatmentTime = new ContDistExponential(this, "Specialist Treatment Time", 25.0, true, true);
        balk = new DiscreteDistUniform(this, "Balk Rate", 1, 8, true, true);

        // Initialize statistics
        patientsArrived = new Count(this, "Patients Arrived", true, true);
        patientsBalk = new Count(this, "Patients Balked", true, true);
        patientsDiverted = new Count(this, "Patients Diverted", true, true);
        patientsTreated = new Count(this, "Patients Fully Treated", true, true);
        numPatientsInSystem = new Count(this, "Number of Patients in System", true, true);
        responseTimeFullyTreated = new Tally(this, "Response Time Fully Treated", true, true);
        nurseUtilization = new Accumulate(this, "Nurse Utilization", true, true);
        specialistUtilization = new Accumulate(this, "Specialist Utilization", true, true);
        dailyOperatingCost = new Count(this, "Daily Operating Cost", true, true);

        //Model Condition
        numInSystemCondition = new ModelCondition(this, "Number in System Condition", true, this.numPatientsInSystem, this.getModel().presentTime()) {;
            @Override
            public boolean check() {
               // return numPatientsInSystem.getValue() == 0 && ;
                return this.presentTime().getTimeAsDouble() >= 720 && numPatientsInSystem.getValue() == 0;
            }
        };
    }

        /*public static void main(String[] args){
        //add command line options
       Experiment.setReferenceUnit(TimeUnit.MINUTES);
        medicalModel model = new medicalModel(null, "Medical Model", true, true);
        Experiment exp = new Experiment("Single run of Medical Model");
        model.connectToExperiment(exp);
        exp.setShowProgressBar(false);
        exp.stop(numInSystemCondition);
        exp.traceOff(new TimeInstant(960));
        exp.debugOff(new TimeInstant(960));
        exp.start();
        exp.report();
        exp.finish();
    } */
}
