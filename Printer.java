
import java.util.LinkedList;

public class Printer {

    private String name; // Name of the printer
    private String status; // Status of the printer
    private LinkedList<Job> printerQueue; // Queue for printer jobs
    private int prevJobNumber;

    private class Job {
        int jobNumber;
        String fileName;

        public Job(String fileName) {
            this.fileName = fileName;
            this.jobNumber = prevJobNumber;
            prevJobNumber++;
        }
    }

    public Printer(String name) {
        this.name = name;
        prevJobNumber = 0;
        printerQueue = new LinkedList<Job>();
        // printerQueue = new HashMap<>;

    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String setStatus(String Status) {
        return this.status = status;
    }

    public void queue() { // lists the print queue for a given printer on the user's display in lines of
                          // the form <job number> <file name>
        for (Job j : printerQueue) {
            System.out.println(j.jobNumber + "   " + j.fileName); 
        }
    }

    public void addToQueue(String fileName) { // adds job to printer's queue
        printerQueue.add(new Job(fileName));
    }

    public void topQueue(int job) { // moves job to the top of the queue
        Job foundJob = null;
        for (Job j : printerQueue) {
            if (j.jobNumber == job) {
                foundJob = j;
                break;
            }
        }
        if (foundJob == null) {
            return;
        }
        printerQueue.remove(foundJob);
        printerQueue.add(0, foundJob);
    }

    public void clearPrinterQueue() { // Clears the printer's queue so it can restart
        printerQueue.clear();
    }

    public String printJobs(){
        String print="";
        for (Job j : printerQueue) {
            print= print + "Job number: " + j.jobNumber+ " and filename "+j.fileName +"\n"; 
            }

        return print;
    }
}