package conn;

import java.util.List;

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

    public static class Election {
        public List<String> candidates;
        public String question;
        public Election(List<String> candidates, String question) {
            this.candidates = candidates;
            this.question = question;
        }
    }

    public static class Painting {

    }

    public static final int STATE_INTERVAL = 90 * 1000; // 90 seconds

}
