package us.shandian.game.twozero;

/*
 *
 * This is a simple AI for the 2048 game
 * Based on Monte Carlo method
 *
 */

public class AI
{
    static final int SEARCH_DEPTH = 4;
    
    static final float WEIGHT_SMOOTH = 0.2f, WEIGHT_MONO = 2.5f,
                       WEIGHT_EMPTY = 2.7f, WEIGHT_MAX = 1.0f,
                       WEIGHT_SCORE = 0.9f;
    
    MainGame mGame;
    
    public AI(MainGame game) {
        mGame = game;
    }
    
    private float tryToMove(int move, int depth) {
        long nowScore = mGame.score;
        
        boolean moved = mGame.move(move);
        
        Tile[][] savedField = mGame.grid.lastField;
        
        // Evaluate
        float score = moved ? evaluate() : -1f;
        if (depth <= SEARCH_DEPTH && moved) {
            int direction = getBestMove(depth + 1);
            mGame.move(direction);
            score += evaluate();
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
        
        int bestMove = 0;
        float lastScore = 0;
        
        for (int i = 0; i <= 3; i++) {
            float score = tryToMove(i, depth);
            
            if (score > lastScore) {
                bestMove = i;
            }
            
            lastScore = score;
        }
        
        if (depth == 1) mGame.emulating = false;
        
        return bestMove;
    }
    
    // Evaluate how is it if we take the step
    private float evaluate() {
        int smooth = getSmoothness();
        int mono = getMonotonticity();
        int empty = mGame.grid.getAvailableCells().size();
        int max = getMaxValue();
        long score = mGame.score;
        
        return (float) (smooth * WEIGHT_SMOOTH
                    + mono * WEIGHT_MONO
                    + Math.log(empty) * WEIGHT_EMPTY
                    + max * WEIGHT_MAX
                    + score * WEIGHT_SCORE);
    }
    
    // How smooth the grid is
    private int getSmoothness() {
        int smoothness = 0;
        for (int x = 0; x < mGame.numSquaresX; x++) {
            for (int y = 0; y < mGame.numSquaresY; y++) {
                Tile t = mGame.grid.field[x][y];
                if (t != null) {
                    int value = (int) (Math.log(t.getValue()) / Math.log(2));
                    for (int direction = 1; direction <= 2; direction++) {
                        Cell vector = mGame.getVector(direction);
                        Cell targetCell = mGame.findFarthestPosition(new Cell(x, y), vector)[1];
                        
                        if (mGame.grid.isCellOccupied(targetCell)) {
                            Tile target = mGame.grid.getCellContent(targetCell);
                            int targetValue = (int) (Math.log(target.getValue()) / Math.log(2));
                            
                            smoothness -= Math.abs(value - targetValue);
                        }
                    }
                    
                }
            }
        }
        
        return smoothness;
    }
    
    // How monotonic the grid is
    private int getMonotonticity() {
        int[] totals = {0, 0, 0, 0};
        
        // Vertical
        for (int x = 0; x < mGame.numSquaresX; x++) {
            int current = 0;
            while (current < mGame.numSquaresY && mGame.grid.field[x][current] == null) current++;
            for (int next = current + 1; next < mGame.numSquaresY; next = current + 1) {
                while (next < mGame.numSquaresY && mGame.grid.field[x][next] == null) {
                    next++;
                }
                if (next >= mGame.numSquaresY) break;
                int currentValue = (int) (mGame.grid.field[x][current] != null ? 
                                       Math.log(mGame.grid.field[x][current].getValue()) / Math.log(2) : 0);
                int nextValue = (int) (mGame.grid.field[x][next] != null ? 
                                       Math.log(mGame.grid.field[x][next].getValue()) / Math.log(2) : 0);
                if (currentValue > nextValue) {
                    totals[0] = currentValue - nextValue;
                } else if (currentValue < nextValue) {
                    totals[1] = nextValue - currentValue;
                }
                current = next;
            }
        }
        
        // Horizontal
        for (int y = 0; y < mGame.numSquaresY; y++) {
            int current = 0;
            while (current < mGame.numSquaresX && mGame.grid.field[current][y] == null) current++;
            for (int next = current + 1; next < mGame.numSquaresX; next = current + 1) {
                while (next < mGame.numSquaresX && mGame.grid.field[next][y] == null) {
                    next++;
                }
                if (next >= mGame.numSquaresX) break;
                int currentValue = (int) (mGame.grid.field[current][y] != null ? 
                                       Math.log(mGame.grid.field[current][y].getValue()) / Math.log(2) : 0);
                int nextValue = (int) (mGame.grid.field[next][y] != null ? 
                                       Math.log(mGame.grid.field[next][y].getValue()) / Math.log(2) : 0);
                if (currentValue > nextValue) {
                    totals[2] = currentValue - nextValue;
                } else if (currentValue < nextValue) {
                    totals[3] = nextValue - currentValue;
                }
                current = next;
            }
        }
        
        return Math.max(totals[0], totals[1]) + Math.max(totals[2], totals[3]);
    }
    
    private int getMaxValue() {
        int max = 0;
        for (int x = 0; x < mGame.numSquaresX; x++) {
            for (int y = 0; y < mGame.numSquaresY; y++) {
                Cell cell = new Cell(x, y);
                if (mGame.grid.isCellOccupied(cell)) {
                    Tile t = mGame.grid.getCellContent(cell);
                    int value = t.getValue();
                    if (value > max) {
                        max = value;
                    }
                }
            }
        }
        return max;
    }
}
