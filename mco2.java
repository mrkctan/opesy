import java.util.concurrent.Semaphore;

public class Helldivers {

    // Constants
    private static final int TEAM_SIZE = 4;
    private static final int MAX_SUPER_CITIZENS = 2;

    // Semaphores
    private static Semaphore regularSemaphore = new Semaphore(0);
    private static Semaphore superSemaphore = new Semaphore(0);
    private static Semaphore teamReadySemaphore = new Semaphore(0);
    private static Semaphore teamCountSemaphore = new Semaphore(1);

    // Variables
    private static int regularCount = 0;
    private static int superCount = 0;
    private static int teamId = 1;
    private static int remainingRegular = 0;
    private static int remainingSuper = 0;

    // Main method
    public static void main(String[] args) {
        int r = 10; // Example number of regular citizens
        int s = 6; // Example number of super citizens

        // Create threads for regular and super citizens
        Thread[] threads = new Thread[r + s];
        for (int i = 0; i < r; i++) {
            threads[i] = new Thread(() -> recruitRegularCitizen(i + 1));
            threads[i].start();
        }
        for (int i = 0; i < s; i++) {
            threads[r + i] = new Thread(() -> recruitSuperCitizen(i + 1));
            threads[r + i].start();
        }

        // Launch teams
        Thread launchThread = new Thread(Helldivers::launchTeams);
        launchThread.start();

        // Wait for all threads to finish
        try {
            for (Thread t : threads) {
                t.join();
            }
            launchThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Display final results
        System.out.println("\nFinal Results:");
        System.out.println("Total teams sent: " + (teamId - 1));
        System.out.println("Remaining Regular Citizens: " + remainingRegular);
        System.out.println("Remaining Super Citizens: " + remainingSuper);
    }

    // Regular citizen signing up
    private static void recruitRegularCitizen(int rcId) {
        System.out.println("Regular Citizen " + rcId + " is signing up");
        regularSemaphore.release();
        regularCount++;
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Super citizen signing up
    private static void recruitSuperCitizen(int scId) {
        System.out.println("Super Citizen " + scId + " is signing up");
        superSemaphore.release();
        superCount++;
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Form a team
    private static void formTeam(int teamId) {
        regularCount -= 2;
        superCount -= 2;
        System.out.println("Team " + teamId + " is ready and now launching to battle (sc: 2 | rc: 2)");
        teamReadySemaphore.release();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Process regular citizens
    private static void processRegularCitizen() {
        try {
            regularSemaphore.acquire();
            if (regularCount >= 2 && superCount >= 2) {
                formTeam(teamId);
            } else {
                remainingRegular++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Process super citizens
    private static void processSuperCitizen() {
        try {
            superSemaphore.acquire();
            if (regularCount >= 2 && superCount >= 2) {
                formTeam(teamId);
            } else {
                remainingSuper++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Launch teams
    private static void launchTeams() {
        while (true) {
            try {
                teamReadySemaphore.acquire();
                teamCountSemaphore.acquire();
                if (remainingRegular < 2 || remainingSuper < 2) {
                    break;
                }
                remainingRegular -= 2;
                remainingSuper -= 2;
                teamId++;
                teamCountSemaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
