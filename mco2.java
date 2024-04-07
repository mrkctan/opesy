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

    public static void recruitRegularCitizen(int rcId) throws InterruptedException {
        System.out.println("Regular Citizen " + rcId + " is signing up");
        regularSemaphore.release();
        regularCount++;
        Thread.sleep(500);
    }

    public static void recruitSuperCitizen(int scId) throws InterruptedException {
        System.out.println("Super Citizen " + scId + " is signing up");
        superSemaphore.release();
        superCount++;
        Thread.sleep(500);
    }

    public static void formTeam(int teamId) throws InterruptedException {
        regularCount -= 2;
        superCount -= 2;
        System.out.println("Team " + teamId + " is ready and now launching to battle (sc: 2 | rc: 2)");
        teamReadySemaphore.release();
        Thread.sleep(500);
    }

    public static void processRegularCitizen() throws InterruptedException {
        remainingRegular++;
        regularSemaphore.acquire();
        if (regularCount >= 2 && superCount >= 2) {
            formTeam(teamId);
        } else {
            remainingRegular--;
        }
    }

    public static void processSuperCitizen() throws InterruptedException {
        remainingSuper++;
        superSemaphore.acquire();
        if (regularCount >= 2 && superCount >= 2) {
            formTeam(teamId);
        } else {
            remainingSuper--;
        }
    }

    public static void launchTeams() throws InterruptedException {
        while (true) {
            teamReadySemaphore.acquire();
            teamCountSemaphore.acquire();
            if (remainingRegular < 2 || remainingSuper < 2) {
                break;
            }
            remainingRegular -= 2;
            remainingSuper -= 2;
            teamId++;
            teamCountSemaphore.release();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int r = 0, s = 0;

        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            System.out.print("Enter the number of Regular Citizens: ");
            r = scanner.nextInt();
            System.out.print("Enter the number of Super Citizens: ");
            s = scanner.nextInt();
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter integers.");
            System.exit(1);
        }

        Thread[] threads = new Thread[r + s];
        for (int i = 0; i < r; i++) {
            final int rcId = i + 1;
            threads[i] = new Thread(() -> {
                try {
                    recruitRegularCitizen(rcId);
                    processRegularCitizen();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        for (int i = 0; i < s; i++) {
            final int scId = i + 1;
            threads[i + r] = new Thread(() -> {
                try {
                    recruitSuperCitizen(scId);
                    processSuperCitizen();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        // Create a thread to launch teams
        Thread launchThread = new Thread(() -> {
            try {
                launchTeams();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Start all citizen threads
        for (Thread t : threads) {
            t.start();
        }

        // Start the launch thread
        launchThread.start();

        // Wait for all citizen threads to complete
        for (Thread t : threads) {
            t.join();
        }

        // Wait for launch thread to complete
        launchThread.join();

        // Display remaining citizens
        System.out.println("\nFinal Results:");
        System.out.println("Total teams sent: " + (teamId - 1));
        System.out.println("Remaining Regular Citizens: " + remainingRegular);
        System.out.println("Remaining Super Citizens: " + remainingSuper);
    }
}
