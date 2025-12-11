import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {
    // Maps statis ini akan menyimpan data selama aplikasi belum ditutup
    private static Map<String, Integer> globalScoreMap = new HashMap<>();
    private static Map<String, Integer> globalWinMap = new HashMap<>();

    public static void addScore(String playerName, int points) {
        globalScoreMap.put(playerName, globalScoreMap.getOrDefault(playerName, 0) + points);
    }

    public static void addWin(String playerName) {
        globalWinMap.put(playerName, globalWinMap.getOrDefault(playerName, 0) + 1);
        globalScoreMap.putIfAbsent(playerName, 0);
    }

    // [BARU] Mengambil skor pemain tertentu (untuk inisialisasi saat Replay)
    public static int getScore(String playerName) {
        return globalScoreMap.getOrDefault(playerName, 0);
    }

    // [MODIFIKASI] Mengambil Top 3 Skor Tertinggi
    public static List<Map.Entry<String, Integer>> getTop3Scores() {
        return globalScoreMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) // Urutkan Besar ke Kecil
                .limit(3) // Ambil 3 teratas
                .collect(Collectors.toList());
    }

    public static Map<String, Integer> getWins() { return globalWinMap; }
}