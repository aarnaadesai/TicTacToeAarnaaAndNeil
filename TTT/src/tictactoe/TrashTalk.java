package tictactoe;

import java.util.Random;

public class TrashTalk {
    private static final Random rng = new Random();

    private static final String[] EASY = {
        "Even my grandma plays better than this.",
        "Are you sure you know the rules?",
        "Bold move. Questionable, but bold.",
        "I've seen rocks make better moves.",
        "Did you mean to do that?",
        "Interesting strategy... very interesting.",
        "My eyes! What is this?!"
    };

    private static final String[] MEDIUM = {
        "Not bad. Not good either.",
        "I see what you're doing. It won't work.",
        "Classic move. Classic mistake.",
        "Hmm. I'll allow it.",
        "You're gonna regret that.",
        "Bold. Let's see how this plays out.",
        "Oh you think you're slick?"
    };

    private static final String[] HARD = {
        "I calculated 847 moves ahead. You're doomed.",
        "Resistance is futile.",
        "Did you really just do that against ME?",
        "My neural pathways pity you.",
        "I've already won. You just don't know it yet.",
        "Processing your defeat... done.",
        "You cannot beat perfection."
    };

    private static final String[] PLAYER_TAUNT = {
        "You call that a move?",
        "My grandmother plays better in her sleep.",
        "Are you nervous? You look nervous.",
        "Classic blunder.",
        "Oh that's spicy. Spicy and wrong.",
        "Bold strategy, let's see if it pays off.",
        "Are you even trying?"
    };

    public static String getForAI(int difficulty) {
        String[] pool = difficulty == 0 ? EASY : difficulty == 1 ? MEDIUM : HARD;
        return pool[rng.nextInt(pool.length)];
    }

    public static String getForPlayer() {
        return PLAYER_TAUNT[rng.nextInt(PLAYER_TAUNT.length)];
    }
}