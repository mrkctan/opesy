//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.util.Scanner;
import java.util.concurrent.Semaphore;


public class Main{
    static class Team {
        int teamId;
        int totalMembers = 0;
        int superCitizenCount = 0;
        int regularCitizenCount = 0;
        public Team(int teamId) {
            this.teamId = teamId;
        }
        public void addMember(Citizen citizen){
            System.out.println(citizen.type + "Citizen " + citizen.id + " has joined team " + teamId);
            if(citizen.getType().equals("Super")){
                superCitizenCount ++;
                System.out.println("Team " + this.teamId + " Super count: " + this.superCitizenCount);
            }
            else{
                regularCitizenCount++;
                System.out.println("Team " + this.teamId + " Regular count: " + this.regularCitizenCount);
            }
            totalMembers++;
        }

        public void launchTeam() {
            int superCount = superCitizenCount;
            int regularCount = regularCitizenCount;
            System.out.println("Team " + teamId + " is ready and now launching to battle (sc: " + superCount + " | rc: " + regularCount + ")");
            System.out.println("Total members: " + this.totalMembers);
        }

        public Boolean isFull(){
            if(totalMembers >= 4){
                return true;
            }
            return false;
        }

        public String requires(){
            if(this.superCitizenCount == 2){
                return "Regular";
            }
            if(this.regularCitizenCount == 3){
                return "Super";
            }
            
            return "Any";
        }

        public Boolean canRecruitSuper(){
            if(this.superCitizenCount < 2){
                return true;
            }
            System.out.println("Cannot insert super citizen because super citizen count is:" + this.superCitizenCount);
            return false;
        }

        public Boolean canRecruitRegular(){
            switch(this.superCitizenCount){
                case 0:
                case 1:
                    if(this.regularCitizenCount < 3){
                        return true;
                    }
                    break;
                case 2:
                    if(this.regularCitizenCount < 2){
                        return true;
                    }
            }
            return false;
        }

        public int getSuperCount(){
            return superCitizenCount;
        }

        public int getRegularCount(){
            return regularCitizenCount;
        }
    }

    public static Boolean canStillFormTeams(int totalCitizens, int superCitizenCount, int regularCitizenCount){
        if(totalCitizens < 4){
            return false;
        }
        if(superCitizenCount < 2 && regularCitizenCount < 2){
            return false;
        }
        if(superCitizenCount == 0 || regularCitizenCount == 0){
            return false;
        }
        if(regularCitizenCount < 2){
            return false;
        }
        if(superCitizenCount < 2 && regularCitizenCount < 4){
            return true;
        }
        return true;
    }
    
    static Semaphore mutex = new Semaphore(1);
    static Semaphore superWaitQueue = new Semaphore(0);
    static Semaphore regularWaitQueue = new Semaphore(0);
    static int teamCount = 0;
    static int launchedTeams = 0;
    static int regularCitizenCount = 0;
    static int superCitizenCount = 0;
    static int totalCitizens = 0;
    static int mutexCount = 1;
    static Team currTeam = null;

    static class Citizen extends Thread{
        String type;
        int id;
        Boolean hasSigned = false;
        public Citizen(String type, int id) {
            this.type = type;
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public long  getId() {
            return id;
        }

        public void signUp(){
            if(hasSigned == false){
                System.out.println(this.type + "Citizen " + this.id + " is signing up.");
            }
            hasSigned = true;
        }

        public String getThreadName(){
            return this.type+"Citizen "+ this.id + ": ";
        }

        
        @Override
        public void run() {
            try{
                Thread.sleep(1000);

                /*Initially, there was an instruction here that decrements the mutex count by one to know how many threads are in the 
                 * mutex lock. But we realized there is a race condition that can occur here because its before the mutex lock itself.
                 */
                mutex.acquire();
                System.out.println(getThreadName() + "mutex: " + mutex.getQueueLength());
                System.out.println(getThreadName() + "Super: " + superWaitQueue.getQueueLength());
                System.out.println(getThreadName() + "Regular: " + regularWaitQueue.getQueueLength());
                if (canStillFormTeams(totalCitizens, superCitizenCount, regularCitizenCount)) {
                    signUp();
                    if(type.equals("Super")){
                        if(currTeam.canRecruitSuper()){
                            //if the value of the superQueue is 0 (its initial value), then the current thread will just pass through the release and acquire here.
                            //we do a release (signal) here to apply the fcfs principle but more specifically in a citizen type.
                            //This is saying that if there is a sleeping thread in the super queue, I should wake that up and I should wait at the queue.
                            System.out.println(getThreadName() + "releases the top and sleeps in the super queue. (Can join context)");
                            superWaitQueue.release();
                            //we need to put the semaphore value back to 0 so that the super threads that really needs to wait can be suspended.
                            superWaitQueue.acquire();
                            
                            System.out.println(getThreadName() + " wakes up from super queue. (Can join context.)");
                            if (canStillFormTeams(totalCitizens, superCitizenCount, regularCitizenCount)) {
                                currTeam.addMember(this);
                            }
                        }
                        else{
                            System.out.println(getThreadName() + " waits in the super queue. (Can't join context)");
                            //this means that we cannot insert the citizen in the current team, so we make it wait until it can be recruited
                            //to ensure progress, signal mutex first
                            if (mutex.hasQueuedThreads() == true) {
                                mutex.release(); // we encontered a case where all threads now are waiting in their respective queues, but we can still form teams
                                // to resolve this, if mutex lock is already empty, then we release the other citizen type's queue because that's what the currTeam needs.
                            }
                            else{
                                System.out.println(getThreadName() + "Mutex is empty, releasing regular queue. (Can't join context)");
                                regularWaitQueue.release();
                            }
                            
                            superWaitQueue.acquire();
                            System.out.println(getThreadName() + " wakes up from super queue. (Can't join context)"); 
                            //if the thread waiting in this code wakes up, that means it can be inserted in the current team.
                            /*HOWEVER, there can be a scenario that the thread wakes up but the remaining citizens cannot form a team,
                             Then we put this if condition to make the thread simply exit.
                            */
                            if (canStillFormTeams(totalCitizens, superCitizenCount, regularCitizenCount)) {
                                currTeam.addMember(this);
                            }
                            else{
                                System.out.println(getThreadName() +"Cant join a team anymore (Can't join context) (Remaining citizens: " + totalCitizens +" | remaining supers: " + superCitizenCount + " | remaining reg: " + regularCitizenCount + ")");
                            }
                        }
                    }
                    else{
                        if(currTeam.canRecruitRegular()){
                            System.out.println(getThreadName() + "releases the top and sleeps in the regular queue. (Can join context)");
                            regularWaitQueue.release();
                            regularWaitQueue.acquire();
                            System.out.println(getThreadName() + " wakes up from regular queue. (Can join context)");
                            if (canStillFormTeams(totalCitizens, superCitizenCount, regularCitizenCount)) {
                                currTeam.addMember(this);
                            }
                        }
                        else{
                            System.out.println(getThreadName() + "waits in the regular queue. (Can't join context)");
                            //this means that we cannot insert the citizen in the current team, so we make it wait until it can be recruited
                            //to ensure progress, signal mutex first
                            if (mutex.hasQueuedThreads() == true) {
                                mutex.release(); // we encontered a case where all threads now are waiting in their respective queues, but we can still form teams
                                // to resolve this, if mutex lock is already empty, then we release the other citizen type's queue because that's what the currTeam needs.
                            }
                            else{
                                System.out.println(getThreadName() + "Mutex is empty, releasing super queue. (Can't join context)");
                                superWaitQueue.release();
                            }
                            regularWaitQueue.acquire();
                            System.out.println(getThreadName() + " wakes up from regular queue. (Can't join context)"); 
                            //if the thread waiting in this code wakes up, that means it can be inserted in the current team.
                            /*HOWEVER, there can be a scenario that the thread wakes up but the remaining citizens cannot form a team,
                                Then we put this if condition to make the thread simply exit.
                            */
                            if (canStillFormTeams(totalCitizens, superCitizenCount, regularCitizenCount)) {
                                currTeam.addMember(this);
                            }
                            else{
                                System.out.println(getThreadName() +"Cant join a team anymore (Can't join context) (Remaining citizens: " + totalCitizens +" | remaining supers: " + superCitizenCount + " | remaining reg: " + regularCitizenCount + ")");
                            }
                        }
                    }

                    if (currTeam.isFull()) {
                        currTeam.launchTeam();
                        launchedTeams++;
                        superCitizenCount -= currTeam.getSuperCount();
                        regularCitizenCount -= currTeam.getRegularCount();
                        totalCitizens -= 4;
                        currTeam = new Team(teamCount);
                        teamCount ++;
                        System.out.println(getThreadName() + "team " + currTeam.teamId + " launches");
                    } else {
                        // If it cannot form a team, the thread should exit
                        System.out.println(getThreadName() + "team " + currTeam.teamId + " can't launch yet (tm: " + currTeam.totalMembers +" | sc: " + currTeam.getSuperCount() + " | rc: " + currTeam.getRegularCount() + ")");
                    }
                }
                else{
                    System.out.println(getThreadName() +"Cant join a team anymore (Remaining citizens: " + totalCitizens +" | remaining supers: " + superCitizenCount + " | remaining reg: " + regularCitizenCount + ")");
                }

                mutexCount++; /* This is like if were about to exit, increment mutex count to check if the semaphore mutex lock IS ALREADY empty
                when were about release it, somewhat the waitQueues converts into mutex locks when this happens to keep mutual exclusion. Where the mutex.release 
                becomes irrelevant now in the remaining threads. In other words, we change the exit section of the queue.
                */
                System.out.println(getThreadName() + " Mutex value: " + mutexCount);
                if(!mutex.hasQueuedThreads()){
                    System.out.println(getThreadName() +"mutex queue is empty, mutex count is:" + mutexCount);
                    System.out.println(getThreadName() + "team " + currTeam.teamId + "requires" + currTeam.requires());
                    if(currTeam.requires().equals("Super")){
                        superWaitQueue.release();
                    }
                    else if(currTeam.requires().equals("Regular")){
                        regularWaitQueue.release();
                    }
                    else{
                        //if any, we decided to be super citizen biased
                        if(mutex.hasQueuedThreads()){
                            mutex.release();
                        }
                        else if(superWaitQueue.hasQueuedThreads()){
                            superWaitQueue.release();
                        }
                        else{
                            regularWaitQueue.release();
                        }
                    }
                }
                else if(!canStillFormTeams(totalCitizens, superCitizenCount, regularCitizenCount)){  
                    if(mutex.hasQueuedThreads()){
                        mutex.release();
                    }
                    else if(superWaitQueue.hasQueuedThreads()){
                        superWaitQueue.release();
                    }
                    else{
                        regularWaitQueue.release();
                    } 
                }
                else{
                    System.out.println(getThreadName() + "is about to release mutex lock");
                    mutex.release();  /*We decided to keep the mutex.release in an else statement so that 
                    the semaphore mutex value wont be modified anymore if it is empty. If the threads cant access this anymore,
                    that means we changed our exit section to releasing a thread in a queue.

                    To add more information. If we decided NOT to keep this in an else statement, and were in a scenario that the if statement above is true, current
                    thread is GUARANTEED to release a thread in a queueLock, making 2 threads in the critical section. HOWEVER, the current thread cannot instantly exit
                    the critical section because it has to run the mutex.release() command (reminder that this is the case where the mutex.release isnt in an else statement.)
                    This can potentially cause a race condition in the value of the semaphore mutex.
                     */
                }
                System.out.println(getThreadName() + "exits");

                
                

            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }


    
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the number of Regular Citizens (r): ");
        int regularCitizens = scanner.nextInt();

        System.out.print("Enter the number of Super Citizens (s): ");
        int superCitizens = scanner.nextInt();

        regularCitizenCount = regularCitizens;
        superCitizenCount = superCitizens;
        totalCitizens = regularCitizenCount + superCitizenCount;

        scanner.close();

        teamCount = 0;
        launchedTeams = 0;

        currTeam = new Team(teamCount);
        teamCount++;

        mutexCount = 1 - totalCitizens;
        /*Through various testing, we have to know what will be the maximum number of threads that will be sleeping in the mutex lock. This is
         * used to check if the mutex lock is empty. We subtracting this by one because the first thread that goes through the mutex lock has a free
         * pass to enter the critical section (note the initialization of semaphore lock mutex value is 1.)
         */
        // Creating threads for citizens
        Thread[] threads = new Thread[totalCitizens];
        for (int i = 0; i < superCitizenCount; i++) {
            threads[i] = new Citizen("Super", i); // Super Citizens
        }
        for (int i = superCitizenCount; i < superCitizenCount + regularCitizenCount; i++) {
            threads[i] = new Citizen("Regular", i); // Regular Citizens
        }

        for(Thread thread : threads){
            thread.start();

        }
        for(Thread thread : threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Total teams sent: " + launchedTeams);
        System.out.println("Remaining Regular Citizens: " + regularCitizenCount);
        System.out.println("Remaining Super Citizens: " + superCitizenCount);


    }
}
