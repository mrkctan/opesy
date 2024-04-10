import java.util.concurrent.*;

class Shared {
    static int totalCitizens = 0;
    static int regularCitizensRemaining = 0;
    static int superCitizensRemaining = 0;
    static int teamCount = 0;
    static int teamId = 0;
    static Semaphore mutex = new Semaphore(1);
    static Semaphore superCitizenSem = new Semaphore(2);
    static Semaphore regularCitizenSem = new Semaphore(4);

    static void signUpRegular(int id) {
        try {
            regularCitizenSem.acquire();
            mutex.acquire();
            regularCitizensRemaining++;
            System.out.println("Regular Citizen " + id + " is signing up");
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void signUpSuper(int id) {
        try {
            superCitizenSem.acquire();
            mutex.acquire();
            superCitizensRemaining++;
            System.out.println("Super Citizen " + id + " is signing up");
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Boolean canStillFormTeams(int totalCitizens, int superCitizenCount, int regularCitizenCount){
        if(totalCitizens < 4){
            return false;
        }
        if(superCitizenCount < 2 && regularCitizenCount < 3){
            return false;
        }
        if(superCitizenCount == 0 || regularCitizenCount == 0){
            return false;
        }
        if(regularCitizenCount < 2){
            return false;
        }
        return true;
    }
    

    static void formTeam() {
        try {
            Team currTeam = new Team(teamCount);
            teamCount++;
            mutex.acquire();
            if (canStillFormTeams(totalCitizens, superCitizensRemaining, regularCitizensRemaining)) {
                
                teamId++;
                int teamSuperCount = 0;
                int teamRegularCount = 0;
                
                if (teamSuperCount < 2 && superCitizensRemaining > 0) {
                    superCitizensRemaining--;
                    teamSuperCount++;
                    System.out.println("Super Citizen has joined team " + teamId);
                    /**
                     * If teamCount == full
                     * launch team(){
                     *      print team teamCount has launched!
                     *      teamCount +1
                     *      superCount = 0
                     *      teamSuperCount = 0
                     *      regularSuperCount =0 
                     *      
                     *      
                     * }
                     */
                } else if (regularCitizensRemaining > 0) {
                    regularCitizensRemaining--;
                    teamRegularCount++;
                    System.out.println("Regular Citizen has joined team " + teamId);
                }
                
                if(currTeam.isFull()){
                    currTeam.launchTeam();
                    currTeam = new Team(teamCount);
                    teamCount++;
                }
            } else {
                System.out.println("Not enough citizens to form a team.");
            }
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            regularCitizenSem.release(4);
            superCitizenSem.release(2);
        }
    }
}

class Team {
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

    public int getSuperCount(){
        return superCitizenCount;
    }

    public int getRegularCount(){
        return regularCitizenCount;
    }
}


class Citizen extends Thread {
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
        if (type.equals("Super")) {
            Shared.signUpSuper(id);
        } else {
            Shared.signUpRegular(id);
        }
        Shared.formTeam();
    }
}

public class HelldiversSynchronization {
    public static void main(String[] args) {
        // Taking inputs for number of regular and super citizens
        int r = 5;
        int s = 4;

        // Creating threads for citizens
        Thread[] threads = new Thread[r + s];
        for (int i = 0; i < s; i++) {
            threads[i] = new Citizen(i + 1, true); // Super Citizens
        }
        for (int i = s; i < r + s; i++) {
            threads[i] = new Citizen(i + 1, false); // Regular Citizens
        }

        // Starting all threads
        for (Thread t : threads) {
            t.start();
        }

        // Joining all threads
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // After all threads finish, displaying summary
        System.out.println("\nTotal Teams Sent: " + Shared.teamCount);
        System.out.println("Remaining Regular Citizens: " + Shared.regularCitizens);
        System.out.println("Remaining Super Citizens: " + Shared.superCitizens);
    }
}
