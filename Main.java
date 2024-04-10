import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;


public class Main{
    static class Citizen /*extends Thread*/{
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
    
        /*
        @Override
        public void run() {
            for(int i = 0; i < 5; i++){
                HelldiversSynchronization.totalCitizens++;
                System.out.println(HelldiversSynchronization.totalCitizens);
                
                 * Insert synchronization code here
                 
            }
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
    
            }
        }*/
    }
    
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

    public static int totalCitizens = 0;
    public static void main(String[] args) {
        /* 
        Citizen synchronization = new Citizen("asdasd", 0);
        Citizen synchronization2 = new Citizen("asdasdsad", 1);
        Citizen synchronization3 = new Citizen("asdasdsad", 3);*/

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the number of Regular Citizens (r): ");
        int regularCitizens = scanner.nextInt();

        System.out.print("Enter the number of Super Citizens (s): ");
        int superCitizens = scanner.nextInt();

        int regularCitizenCount = regularCitizens;
        int superCitizenCount = superCitizens;
        int totalCitizens = regularCitizenCount + superCitizenCount;
        
        ArrayList<Citizen> citizens = new ArrayList<Citizen>();

        // Add Regular Citizens to the citizens list
        for (int i = 1; i <= regularCitizenCount; i++) {
            citizens.add(new Citizen("Regular", i));
        }

        // Add Super Citizens to the citizens list
        for (int i = 1; i <= superCitizenCount; i++) {
            citizens.add(new Citizen("Super", i));
        }
 
        // Shuffle the citizens list to randomize the order
        Collections.shuffle(citizens);

        Queue<Citizen> citizenQueue = new LinkedList<>();
        Queue<Citizen> regularWaitQueue = new LinkedList<>();
        Queue<Citizen> superWaitQueue = new LinkedList<>();
        // Add shuffled citizens to the citizenQueue
        
        for (Citizen citizen : citizens) {
            citizenQueue.add(citizen);
        }

        int teamCount = 0;
        int launchedTeams = 0;
        Team currTeam = new Team(teamCount);

        while(!citizenQueue.isEmpty() || !regularWaitQueue.isEmpty() || !superWaitQueue.isEmpty()){
            Citizen incomingCitizen = citizenQueue.peek();

            if (canStillFormTeams(totalCitizens, superCitizenCount, regularCitizenCount)) {
                incomingCitizen.signUp();
                if (currTeam.getSuperCount() != 2 && incomingCitizen.type.equals("Super")) {
            
                    if (!superWaitQueue.isEmpty()){
                        currTeam.addMember(superWaitQueue.poll()); 
                    }
                    else{
                        currTeam.addMember(citizenQueue.poll());
                    }
                } 
                else if (incomingCitizen.type.equals("Regular") && currTeam.getSuperCount() == 2 & currTeam.getRegularCount() < 2) {
                    if (!regularWaitQueue.isEmpty()){
                        currTeam.addMember(regularWaitQueue.poll()); 
                    }
                    else{
                        currTeam.addMember(citizenQueue.poll());
                    }
                } 
                else if (incomingCitizen.type.equals("Regular") && currTeam.getSuperCount() < 2 & currTeam.getRegularCount() < 3) {
                    if (!regularWaitQueue.isEmpty()){
                        currTeam.addMember(regularWaitQueue.poll()); 
                    }
                    else{
                        currTeam.addMember(citizenQueue.poll());
                    }
                }
                else{
                    if(incomingCitizen.type.equals("Super")){
                        regularWaitQueue.add(citizenQueue.poll());
                    }
                    else{
                        superWaitQueue.add(citizenQueue.poll());
                    }
                }
            }
            else{
                citizenQueue.poll();
            }

            if (currTeam.isFull()) {
                currTeam.launchTeam();
                launchedTeams++;
                teamCount ++;
                superCitizenCount -= currTeam.getSuperCount();
                regularCitizenCount -= currTeam.getRegularCount();
                totalCitizens -= 4;
                currTeam = new Team(teamCount);
            }
        }
        System.out.println("Total teams sent: " + launchedTeams);
        System.out.println("Remaining Regular Citizens: " + regularCitizenCount);
        System.out.println("Remaining Super Citizens: " + superCitizenCount);

        scanner.close();
        
    }
    /* 
        public class HelldiversSynchronization {
    }
    */ 
}
