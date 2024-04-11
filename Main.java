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
            System.out.println(citizen.type + " " + citizen.id + " has joined team " + teamId);
            if(citizen.getType().equals("Super")){
                superCitizenCount ++;
            }
            else{
                regularCitizenCount++;
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

        
        @Override
        public void run() {
            try{
                Thread.sleep(1000);
                mutexCount--;
                mutex.acquire();
                
                if (canStillFormTeams(totalCitizens, superCitizenCount, regularCitizenCount)) {
                    signUp();
                    if(type.equals("Super")){
                        if(currTeam.canRecruitSuper()){
                            //if the value of the superQueue is 0 (its initial value), then the current thread will just pass through the release and acquire here.
                            //we do a release (signal) here to apply the fcfs principle but more specifically in a citizen type.
                            //This is saying that if there is a sleeping thread in the super queue, I should wake that up and I should wait at the queue.
                            superWaitQueue.release();
                            //we need to put the semaphore value back to 0 so that the super threads that really needs to wait can be suspended.
                            superWaitQueue.acquire();
                            currTeam.addMember(this);
                        }
                        else{
                            //this means that we cannot insert the citizen in the current team, so we make it wait until it can be recruited
                            //to ensure progress, signal mutex first
                            mutex.release();
                            superWaitQueue.acquire();
                            //if the thread waiting in this code wakes up, that means it can be inserted in the current team.
                            /*HOWEVER, there can be a scenario that the thread wakes up but the remaining citizens cannot form a team,
                             Then we put this if condition to make the thread simply exit.
                            */
                            if (canStillFormTeams(totalCitizens, superCitizenCount, regularCitizenCount)) {
                                currTeam.addMember(this);
                            }
                        }
                    }
                    else{
                        if(currTeam.canRecruitRegular()){
                            regularWaitQueue.release();
                            regularWaitQueue.acquire();
                            currTeam.addMember(this);
                        }
                        else{
                            //this means that we cannot insert the citizen in the current team, so we make it wait until it can be recruited
                            //to ensure progress, signal mutex first
                            mutex.release();
                            regularWaitQueue.acquire();
                            //if the thread waiting in this code wakes up, that means it can be inserted in the current team.
                            /*HOWEVER, there can be a scenario that the thread wakes up but the remaining citizens cannot form a team,
                                Then we put this if condition to make the thread simply exit.
                            */
                            if (canStillFormTeams(totalCitizens, superCitizenCount, regularCitizenCount)) {
                                currTeam.addMember(this);
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
                    } else {
                        // If it cannot form a team, the thread should exit
                        System.out.println("Thread " + id + " cannot form a team and exits.");
                    }
                }
                mutexCount++;
                mutex.release();
                if(mutexCount >= 0){
                    if(currTeam.requires().equals("Super")){
                        superWaitQueue.release();
                    }
                    else if(currTeam.requires().equals("Regular")){
                        regularWaitQueue.release();
                    }
                    else{
                        //if any, we decided to be super citizen biased
                        if(superCitizenCount != 0){
                            superWaitQueue.release();
                        }
                        else{
                            regularWaitQueue.release();
                        }
                    }
                }
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

        currTeam = new Team(teamCount);
        teamCount++;

        // Creating threads for citizens
        Thread[] threads = new Thread[totalCitizens];
        for (int i = 0; i < superCitizenCount; i++) {
            threads[i] = new Citizen("Super", i); // Super Citizens
        }
        for (int i = superCitizenCount; i < superCitizenCount + regularCitizenCount; i++) {
            threads[i] = new Citizen("Regular", i - superCitizenCount); // Regular Citizens
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
