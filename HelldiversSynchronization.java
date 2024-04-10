import java.util.concurrent.*;

class Shared {
    static int regularCitizens;
    static int superCitizens;
    static int teamCount;
    static int teamId;
    static Semaphore mutex = new Semaphore(1);
    static Semaphore superCitizenSem = new Semaphore(2);
    static Semaphore regularCitizenSem = new Semaphore(4);

    static void signUpRegular(int id) {
        try {
            regularCitizenSem.acquire();
            mutex.acquire();
            regularCitizens++;
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
            superCitizens++;
            System.out.println("Super Citizen " + id + " is signing up");
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void formTeam() {
        try {
            mutex.acquire();
            if (superCitizens >= 1 && regularCitizens >= 3) {
                teamId++;
                int superCount = 0;
                int regularCount = 0;
                for (int i = 0; i < 4; i++) {
                    if (superCount < 2 && superCitizens > 0) {
                        superCitizens--;
                        superCount++;
                        System.out.println("Super Citizen has joined team " + teamId);
                    } else if (regularCitizens > 0) {
                        regularCitizens--;
                        regularCount++;
                        System.out.println("Regular Citizen has joined team " + teamId);
                    }
                }
                teamCount++;
                System.out.println("Team " + teamId + " is ready and now launching to battle (sc: " + superCount + " | rc: " + regularCount + ")");
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

class Citizen extends Thread {
    private int id;
    private boolean isSuper;

    Citizen(int id, boolean isSuper) {
        this.id = id;
        this.isSuper = isSuper;
    }

    @Override
    public void run() {
        if (isSuper) {
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
