import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

class Citizen {
    String type;
    int id;

    public Citizen(String type, int id) {
        this.type = type;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return type + " " + id;
    }
}

class Team {
    int teamId;
    Citizen[] citizens;

    public Team(int teamId, Citizen[] citizens) {
        this.teamId = teamId;
        this.citizens = citizens;
    }

    public void launchTeam() {
        int superCount = 0;
        int regularCount = 0;
        for (Citizen citizen : citizens) {
            if (citizen.getType().equals("Super Citizen")) {
                superCount++;
            } else {
                regularCount++;
            }
        }
        System.out.println("Team " + teamId + " is ready and now launching to battle (sc: " + superCount + " | rc: " + regularCount + ")");
    }
}

public class HelldiversSynchronization {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the number of Regular Citizens (r): ");
        int regularCitizens = scanner.nextInt();

        System.out.print("Enter the number of Super Citizens (s): ");
        int superCitizens = scanner.nextInt();

        Queue<Citizen> regularQueue = new LinkedList<>();
        Queue<Citizen> superQueue = new LinkedList<>();
        int teamId = 1;

        for (int i = 1; i <= regularCitizens; i++) {
            regularQueue.add(new Citizen("Regular Citizen", i));
        }

        for (int i = 1; i <= superCitizens; i++) {
            superQueue.add(new Citizen("Super Citizen", i));
        }

        while (!regularQueue.isEmpty() || !superQueue.isEmpty()) {
            Citizen[] teamMembers = new Citizen[4];
            int superCount = 0;
            int regularCount = 0;

            // Form a team
            for (int i = 0; i < 4; i++) {
                if (superCount < 2 && !superQueue.isEmpty()) {
                    teamMembers[i] = superQueue.poll();
                    superCount++;
                } else if (!regularQueue.isEmpty()) {
                    teamMembers[i] = regularQueue.poll();
                    regularCount++;
                }
            }

            // Check if team is properly composed
            if (superCount >= 1 && regularCount >= 1) {
                Team team = new Team(teamId++, teamMembers);
                team.launchTeam();
            } else {
                // Not enough citizens to form a team
                break;
            }
        }

        int remainingRegular = regularQueue.size();
        int remainingSuper = superQueue.size();
        int totalTeams = teamId - 1;

        System.out.println("Total teams sent: " + totalTeams);
        System.out.println("Remaining Regular Citizens: " + remainingRegular);
        System.out.println("Remaining Super Citizens: " + remainingSuper);

        scanner.close();
    }
}
