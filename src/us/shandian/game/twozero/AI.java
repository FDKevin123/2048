package us.shandian.game.twozero;

import java.util.Random;

/*
 *
 * This is a simple AI for the 2048 game
 * Based on Monte Carlo method
 *
 */

public class AI
{
    static final int SEARCH_DEPTH = 2;
    
    MainGame mGame;
    
    public AI(MainGame game) {
        mGame = game;
    }
    
    private long tryToMove(int move, int depth) {
        long nowScore = mGame.score;
        long newScore = 0;
        
        mGame.move(move);
        
        Tile[][] savedField = mGame.grid.lastField;
            
        newScore = mGame.score;
        
        if (mGame.won) newScore = Long.MAX_VALUE;
        else if (mGame.lose) newScore = Long.MIN_VALUE;
        
        // Evaluate
        long score = newScore - nowScore;
        
        if (depth <= SEARCH_DEPTH && !mGame.won && !mGame.lose) {
            int dir = getBestMove(depth + 1);
            mGame.move(dir);
            score += mGame.score - newScore;
            mGame.revertState();
        }
        
        // Reverse
        mGame.grid.lastField = savedField;
        mGame.lastScore = nowScore;
        mGame.revertState();
        
        return score;
    }
    
    public int getBestMove(int depth) {
        if (depth == 1) mGame.startEmulation();
        
        int bestMove = Math.abs(new Random().nextInt()) % 4;
        int lastScore = 0;
        
        for (int i = 0; i <= 3; i++) {
            long score = tryToMove(i, depth);
            
            if (score > lastScore) {
                bestMove = i;
            }
        }
        
        if (depth == 1) mGame.emulating = false;
        
        return bestMove;
    }
}
