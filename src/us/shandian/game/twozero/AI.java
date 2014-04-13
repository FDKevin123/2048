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
    
    static final float WEIGHT_SMOOTH = 0.1f, WEIGHT_MONO = 1.0f,
                       WEIGHT_EMPTY = 2.7f, WEIGHT_MAX = 1.0f;
    
    MainGame mGame;
    
    public AI(MainGame game) {
        mGame = game;
    }
    
    private float tryToMove(MainGame game, int depth) {
        
        if (depth == 0) {
            return evaluate(game);
        } else {
            float maxScore = 0;
            for (int i = 0; i <= 3; i++) {
                MainGame g = game.clone();
                g.addRandomTile();
                
                if (!g.move(i)) {
                    continue;
                }
                
                float score = tryToMove(game, depth - 1);
                
                if (score > maxScore) {
                    maxScore = score;
                }
            }
            return maxScore;
        }
    }
    
    public int getBestMove() {
        
        int bestMove = 0;
        float bestScore = 0;
        
        for (int i = 0; i <= 3; i++) {
            MainGame game = mGame.clone();
            
            if (!game.move(i)) {
                continue;
            }
            
            float score = tryToMove(game, SEARCH_DEPTH);
            
            if (score > bestScore) {
                bestMove = i;
                bestScore = score;
            }
        }
        
        return bestMove;
    }
    
    // Evaluate how is it if we take the step
    private float evaluate(MainGame game) {
        int smooth = getSmoothness(game);
        int mono = getMonotonticity(game);
        int empty = game.grid.getAvailableCells().size();
        int max = getMaxValue(game);
        
        return (float) (smooth * WEIGHT_SMOOTH
                    + mono * WEIGHT_MONO
                    + Math.log(empty) * WEIGHT_EMPTY
                    + max * WEIGHT_MAX);
    }
    
    // How smooth the grid is
    private int getSmoothness(MainGame game) {
        int smoothness = 0;
        for (int x = 0; x < game.numSquaresX; x++) {
            for (int y = 0; y < game.numSquaresY; y++) {
                Tile t = game.grid.field[x][y];
                if (t != null) {
                    int value = (int) (Math.log(t.getValue()) / Math.log(2));
                    for (int direction = 1; direction <= 2; direction++) {
                        Cell vector = game.getVector(direction);
                        Cell targetCell = game.findFarthestPosition(new Cell(x, y), vector)[1];
                        
                        if (game.grid.isCellOccupied(targetCell)) {
                            Tile target = game.grid.getCellContent(targetCell);
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
    private int getMonotonticity(MainGame game) {
        int[] totals = {0, 0, 0, 0};
        
        // Vertical
        for (int x = 0; x < game.numSquaresX; x++) {
            int current = 0;
            while (current < game.numSquaresY && game.grid.field[x][current] == null) current++;
            for (int next = current + 1; next < game.numSquaresY; next = current + 1) {
                while (next < game.numSquaresY && game.grid.field[x][next] == null) {
                    next++;
                }
                if (next >= game.numSquaresY) break;
                int currentValue = (int) (game.grid.field[x][current] != null ? 
                                       Math.log(game.grid.field[x][current].getValue()) / Math.log(2) : 0);
                int nextValue = (int) (mGame.grid.field[x][next] != null ? 
                                       Math.log(game.grid.field[x][next].getValue()) / Math.log(2) : 0);
                if (currentValue > nextValue) {
                    totals[0] = currentValue - nextValue;
                } else if (currentValue < nextValue) {
                    totals[1] = nextValue - currentValue;
                }
                current = next;
            }
        }
        
        // Horizontal
        for (int y = 0; y < game.numSquaresY; y++) {
            int current = 0;
            while (current < game.numSquaresX && game.grid.field[current][y] == null) current++;
            for (int next = current + 1; next < game.numSquaresX; next = current + 1) {
                while (next < game.numSquaresX && mGame.grid.field[next][y] == null) {
                    next++;
                }
                if (next >= game.numSquaresX) break;
                int currentValue = (int) (game.grid.field[current][y] != null ? 
                                       Math.log(game.grid.field[current][y].getValue()) / Math.log(2) : 0);
                int nextValue = (int) (game.grid.field[next][y] != null ? 
                                       Math.log(game.grid.field[next][y].getValue()) / Math.log(2) : 0);
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
    
    private int getMaxValue(MainGame game) {
        int max = 0;
        for (int x = 0; x < game.numSquaresX; x++) {
            for (int y = 0; y < game.numSquaresY; y++) {
                Cell cell = new Cell(x, y);
                if (game.grid.isCellOccupied(cell)) {
                    Tile t = game.grid.getCellContent(cell);
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
