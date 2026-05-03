package tictactoe;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persistent stats across sessions. Stored as key=value lines in
 * ~/.tictactoe-stats. Buckets: vs2p (PvP), vsai0/1/2 (Easy/Med/Hard).
 */
public class Stats {
    public static class Bucket {
        public int wins, losses, draws;
        public int currentStreak;   // consecutive wins (resets on loss/draw)
        public int bestStreak;
        public int gamesPlayed() { return wins + losses + draws; }
    }

    private static final File FILE =
        new File(System.getProperty("user.home"), ".tictactoe-stats");

    // 4 buckets: 0=vs2p (X-wins counted as 'wins', O-wins as 'losses'),
    // 1=vsAI Easy, 2=vsAI Medium, 3=vsAI Hard
    private static final Bucket[] buckets = {
        new Bucket(), new Bucket(), new Bucket(), new Bucket()
    };

    // Achievements unlocked
    private static final java.util.Set<String> achievements = new java.util.LinkedHashSet<>();

    static { load(); }

    public static Bucket pvp()  { return buckets[0]; }
    public static Bucket ai(int difficulty) { return buckets[1 + Math.max(0, Math.min(2, difficulty))]; }

    public static boolean hasAchievement(String id) { return achievements.contains(id); }
    /** Returns true if achievement was just unlocked (i.e. wasn't already there). */
    public static boolean unlock(String id) {
        boolean fresh = achievements.add(id);
        if (fresh) save();
        return fresh;
    }

    /** Record a PvP result. Winner is 'X', 'O', or 'D' for draw. */
    public static void recordPvP(char winner) {
        Bucket b = buckets[0];
        if (winner == 'X')      { b.wins++;   b.currentStreak++; b.bestStreak = Math.max(b.bestStreak, b.currentStreak); }
        else if (winner == 'O') { b.losses++; b.currentStreak = 0; }
        else                    { b.draws++;  b.currentStreak = 0; }
        save();
    }

    /** Record an AI result. result: 'W' (human won), 'L' (AI won), 'D' draw. */
    public static void recordAI(int difficulty, char result) {
        Bucket b = ai(difficulty);
        if (result == 'W')      { b.wins++;   b.currentStreak++; b.bestStreak = Math.max(b.bestStreak, b.currentStreak); }
        else if (result == 'L') { b.losses++; b.currentStreak = 0; }
        else                    { b.draws++;  b.currentStreak = 0; }
        save();
    }

    public static synchronized void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            String[] keys = {"pvp", "ai0", "ai1", "ai2"};
            for (int i = 0; i < 4; i++) {
                Bucket b = buckets[i];
                pw.println(keys[i] + ".wins="          + b.wins);
                pw.println(keys[i] + ".losses="        + b.losses);
                pw.println(keys[i] + ".draws="         + b.draws);
                pw.println(keys[i] + ".currentStreak=" + b.currentStreak);
                pw.println(keys[i] + ".bestStreak="    + b.bestStreak);
            }
            for (String a : achievements) pw.println("achievement=" + a);
        } catch (IOException ignored) {}
    }

    public static synchronized void load() {
        if (!FILE.exists()) return;
        Map<String,String> kv = new LinkedHashMap<>();
        try (BufferedReader r = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String k = line.substring(0, eq);
                String v = line.substring(eq + 1);
                if (k.equals("achievement")) achievements.add(v);
                else kv.put(k, v);
            }
        } catch (IOException ignored) { return; }

        String[] keys = {"pvp", "ai0", "ai1", "ai2"};
        for (int i = 0; i < 4; i++) {
            Bucket b = buckets[i];
            b.wins          = parseInt(kv.get(keys[i] + ".wins"));
            b.losses        = parseInt(kv.get(keys[i] + ".losses"));
            b.draws         = parseInt(kv.get(keys[i] + ".draws"));
            b.currentStreak = parseInt(kv.get(keys[i] + ".currentStreak"));
            b.bestStreak    = parseInt(kv.get(keys[i] + ".bestStreak"));
        }
    }

    private static int parseInt(String s) {
        if (s == null) return 0;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
    }

    /** Reset all stats — for a "clear stats" button if added later. */
    public static synchronized void resetAll() {
        for (int i = 0; i < 4; i++) {
            buckets[i].wins = buckets[i].losses = buckets[i].draws = 0;
            buckets[i].currentStreak = buckets[i].bestStreak = 0;
        }
        achievements.clear();
        save();
    }
}
