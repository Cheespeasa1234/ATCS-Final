package conn;

import com.google.gson.annotations.SerializedName;

public class Utility {
    public static class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override public void uncaughtException(Thread t, Throwable e) {
            System.err.println("Unhandled exception in thread: " + t.getName());
            e.printStackTrace(System.err);
        }   
    }

    public static class Election {
        @SerializedName("candidates") public String[] candidates;
        @SerializedName("prompt") public String prompt;
        public static transient final String typeID = "CREATEVOTE";
        public Election(String[] candidates, String prompt) {
            this.candidates = candidates;
            this.prompt = prompt;
        }
    }
    
    public static enum GameState {
        WAITING_FOR_PLAYERS, WAITING, MAKING_PROMPTS, VOTING_PROMPTS, MAKING_PAINTINGS, VOTING_PAINTINGS, GAME_END
    }

    public static final int STATE_INTERVAL = 90 * 1000; // 90 seconds

}
