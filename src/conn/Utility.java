package conn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utility {
    public static class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override public void uncaughtException(Thread t, Throwable e) {
            System.err.println("Unhandled exception in thread: " + t.getName());
            e.printStackTrace(System.err);
        }
    }

    public static enum GameState {
        WAITING_FOR_PLAYERS, WAITING, MAKING_PROMPTS, VOTING_PROMPTS, MAKING_PAINTINGS, VOTING_PAINTINGS, GAME_END
    }

    public static class Election<CANDIDATE_TYPE> {
        public List<CANDIDATE_TYPE> candidates;
        public Map<CANDIDATE_TYPE, Integer> votes = new HashMap<CANDIDATE_TYPE, Integer>();
        public String question;

        public Election(List<CANDIDATE_TYPE> candidates, String question) {
            this.candidates = candidates;
            this.question = question;
        }

        public void vote(int choice) {
            if (votes.containsKey(candidates.get(choice))) {
                votes.put(candidates.get(choice), votes.get(candidates.get(choice)) + 1);
            } else {
                votes.put(candidates.get(choice), 1);
            }
        }

        public CANDIDATE_TYPE getWinner() {
            int maxVotes = 0;
            int maxIndex = 0;
            for (int i = 0; i < candidates.size(); i++) {
                if (votes.containsKey(candidates.get(i)) && votes.get(candidates.get(i)) > maxVotes) {
                    maxVotes = votes.get(candidates.get(i));
                    maxIndex = i;
                }
            }
            return candidates.get(maxIndex);
        }
    }

    public static class Painting {

    }

    public static final int STATE_INTERVAL = 10 * 1000; // 90 seconds

}
