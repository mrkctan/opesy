import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class HelldiverMissionOrganizer {
    private Semaphore superCitizens;
    private Semaphore regularCitizens;
    private int teamId = 0;
    private static final int TEAM_SIZE = 4;
    private static final int MAX_SUPER_CITIZENS = 2;

    public HelldiverMissionOrganizer(int r, int s) {
        this.regularCitizens = new Semaphore(r, true);
        this.superCitizens = new Semaphore(s, true);
    }

    public void startMission() {
        while (regularCitizens.availablePermits() + superCitizens.availablePermits() >= TEAM_SIZE) {
            int superCount = 0;
            int regularCount = 0;
            teamId++;

            // Try to include up to 2 Super Citizens in the team
            while (superCount < MAX_SUPER_CITIZENS && superCitizens.tryAcquire()) {
                System.out.println("Super Citizen has joined team " + teamId);
                superCount++;
            }

            // Fill the rest of the team with Regular Citizens
            while (regularCount < TEAM_SIZE - superCount && regularCitizens.tryAcquire()) {
                System.out.println("Regular Citizen has joined team " + teamId);
                regularCount++;
            }

            if (superCount + regularCount == TEAM_SIZE) {
                System.out.println("team " + teamId + " is ready and now launching to battle (sc: " + superCount + " | rc: " + regularCount + ")");
            } else {
                // Release the acquired permits if team cannot be formed
                superCitizens.release(superCount);
                regularCitizens.release(regularCount);
                System.out.println("Not enough citizens to form a full team.");
                break;
            }
        }

        int remainingSuper = superCitizens.availablePermits();
        int remainingRegular = regularCitizens.availablePermits();
        System.out.println("All possible teams have been launched.");
        System.out.println("Teams sent: " + teamId);
        System.out.println("Remaining Super Citizens: " + remainingSuper);
        System.out.println("Remaining Regular Citizens: " + remainingRegular);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter number of Regular Citizens (r): ");
        int r = scanner.nextInt();
        System.out.print("Enter number of Super Citizens (s): ");
        int s = scanner.nextInt();
        scanner.close();

        HelldiverMissionOrganizer organizer = new HelldiverMissionOrganizer(r, s);
        organizer.startMission();
    }
}