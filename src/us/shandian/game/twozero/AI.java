package us.shandian.game.twozero;

import java.util.ArrayList;

/*
 *
 * This is a simple AI for the 2048 game
 * Based on alpha-beta method
 *
 */

public class AI
{
    enum Player {
        DOCTOR,
        DALEKS
    }
    
    static final float WEIGHT_SMOOTH = 0.1f, WEIGHT_MONO = 1.0f,
                       WEIGHT_EMPTY = 2.7f, WEIGHT_MAX = 1.0f;
    
    MainGame mGame;
    
    public AI(MainGame game) {
        mGame = game;
    }
    
    public int getBestMove() {
        
        int bestMove = -1;
        int depth = 0;
        long start = System.nanoTime();
        
        do {
            int move = search(mGame.clone(), depth, Float.MIN_VALUE, Float.MAX_VALUE, Player.DOCTOR)[0];
            if (move == -1) {
                break;
            } else {
                bestMove = move;
                depth++;
            }
        } while (System.nanoTime() - start < MainView.BASE_ANIMATION_TIME * 2);
        
        return bestMove;
    }
    
    /*
     *
     * Search for the best move
     * Based on alpha-beta search method
     * Simulates two players' game
     * The Doctor V.S. The Daleks
     *
     */
    private Object[] search(MainGame game, int depth, float alpha, float beta, Player player) {
        int bestMove = -1;
        float bestScore = 0;
        
        if (player == Player.DOCTOR) {
            // The Doctoe's turn
            // Doctor wants to defeat the Daleks
            bestScore = alpha;
            
            for (int i = 0; i <= 3; i++) {
                MainGame g = game.clone();
                    
                if (!g.move(i)) {
                    continue;
                }
                
                // Just return if this is at the bottom
                if (depth == 0) {
                    return new Object[]{i, evaluate(game)};
                }
                
                // Pass the game to the Daleks
                float score = search(g, depth - 1, bestScore, beta, Player.DALEKS)[1];
                
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = i;
                }
                
                // We have found a much much better move
                // So, cutoff
                if (bestScore > beta) {
                    return new Object[]{bestMove, beta};
                }
            }
        } else if (player == Player.DALEKS) {
            // The Daleks' turn
            // "EXTETMINATE!"
            bestScore = beta;
            
            int minScore = Integer.MAX_VALUE;
            
            ArrayList<Object[]> worst = new ArrayList<Object[]>();
            
            ArrayList<Cell> cells = game.grid.getAvailableCells();
            
            // Pick out the worst ones for the Doctor
            // Try to insert 2
            for (Cell cell : cells) {
                Tile t = new Tile(cell, 2);
                game.grid.insertTile(t);
                int score = getSmoothness(game);
                if (score < minScore) {
                    minScore = score;
                    worst.clear();
                    worst.add(new Object[]{cell, 2});
                } else if (score == minScore) {
                    worst.add(new Object[]{cell, 2});
                }
                game.grid.removeTile(t);
            }
            
            // Try to insert 4
            for (Cell cell : cells) {
                Tile t = new Tile(cell, 4);
                game.grid.insertTile(t);
                int score = getSmoothness(game);
                if (score < minScore) {
                    minScore = score;
                    worst.clear();
                    worst.add(new Object[]{cell, 4});
                } else if (score == minScore) {
                    worst.add(new Object[]{cell, 4});
                }
                game.grid.removeTile(t);
            }
            
            // Play all the games with the Doctor
            for (Object[] obj : worst) {
                Cell cell = (Cell) obj[0];
                int value = (int) obj[1];
                MainGame g = game.clone();
                
                Tile t = new Tile(cell, value);
                g.grid.insertTile(t);
                
                // Pass the game to human
                float score = search(g, depth, alpha, bestScore, Player.DOCTOR)[1];
                
                if (score < bestScore) {
                    bestScore = score;
                }
                
                // Computer lose
                // Cutoff
                if (bestScore < alpha) {
                    return new Object[]{-1, alpha};
                }
            }
            //return new Object[]{bestMove, beta};
        }
        
        return new Object[]{bestMove, bestScore};
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
    
    private int countIslands(MainGame game) {
        int islands = 0;
        
        for (int x = 0; x < game.numSquaresX; x++) {
            for (int y = 0; y < game.numSquaresY; y++) {
                if (game.grid.isCellOccupied(new Cell(x, y))) {
                    game.grid.getCellContent(x, y).marked = false;
                }
            }
        }
        
        for (int x = 0; x < game.numSquaresX; x++) {
            for (int y = 0; y < game.numSquaresY; y++) {
                if (game.grid.isCellOccupied(new Cell(x, y))) {
                    Tile t = game.grid.getCellContent(x, y);
                    if (!t.marked) {
                        islands++;
                        mark(game, x, y, t.getValue());
                    }
                }
            }
        }
        
        return islands;
    }
    
    private void mark(MainGame game, int x, int y, int value) {
        if (game.grid.isCellWithinBounds(x, y) && game.grid.isCellOccupied(new Cell(x, y))) {
            Tile t = game.grid.getCellContent(x, y);
            if (!t.marked && t.getValue() == value) {
                t.marked = true;
                
                for (int i = 0; i <= 3; i++) {
                    Cell vector = game.getVector(i);
                    mark(game, x + vector.getX(), y + vector.getY(), value);
                }
            }
        }
    }
}
