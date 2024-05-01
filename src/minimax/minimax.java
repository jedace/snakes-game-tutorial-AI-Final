package minimax;
import snakes.Bot;
import snakes.Snake;
import snakes.Coordinate;
import snakes.Direction;
import java.util.*;
/**
 * MinimaxBot implements a Minimax algorithm for deciding the next move in a snake game.
 */
public class minimax implements Bot {
    private static final Direction[] ALL_DIRECTIONS = new Direction[]{Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
    private Snake mySnake;
    private Snake opponentSnake;
    private Coordinate boardSize;
    private Coordinate appleLocation;
    private Map<List<Object>, List<Object>> transitions = new HashMap<>();
    private Map<List<Object>, List<Object>> optimalNextStates = new HashMap<>();
    private Map<List<Object>, Direction> bestMoves = new HashMap<>();
    private Map<List<Object>, Integer> stateScores = new HashMap<>();
    private int searchDepth = 8;
    @Override
    public Direction chooseDirection(Snake mySnake, Snake opponentSnake, Coordinate boardSize, Coordinate appleLocation) {
        setupGame(mySnake.clone(), opponentSnake.clone(), boardSize, appleLocation);
        return performMinimax();
    }
    private void setupGame(Snake mySnake, Snake opponentSnake, Coordinate boardSize, Coordinate appleLocation) {
        this.mySnake = mySnake;
        this.opponentSnake = opponentSnake;
        this.boardSize = boardSize;
        this.appleLocation = appleLocation;
        // Clear previous game state
        transitions.clear();
        optimalNextStates.clear();
        stateScores.clear();
        bestMoves.clear();
    }
    private Direction performMinimax() {
        List<Object> initialState = Arrays.asList(mySnake.clone(), opponentSnake.clone(), false, false);
        runMinimax(initialState, searchDepth, true);
        Direction chosenMove = bestMoves.get(initialState);
        if (chosenMove != null) {
            System.out.println("Decision: Move " + chosenMove + ", Score: " + stateScores.get(initialState));
            return chosenMove;
        } else {
            System.out.println("No valid move found, defaulting to UP.");
            return Direction.UP;
        }
    }
    
    private int runMinimax(List<Object> state, int depth, boolean isMaximizingPlayer) {
        if (depth == 0 || isTerminal(state)) {
            return evaluateState(state, isMaximizingPlayer);
        }
        
        int bestScore = isMaximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Direction move : ALL_DIRECTIONS) {
            List<Object> newState = simulateMove(state, move, isMaximizingPlayer);
            if (newState != null) {
                int score = runMinimax(newState, depth - 1, !isMaximizingPlayer);
                if (isMaximizingPlayer && score > bestScore || !isMaximizingPlayer && score < bestScore) {
                    transitions.put(state, newState);
                    bestMoves.put(state, move);
                    bestScore = score;
                }
            }
        }
        stateScores.put(state, bestScore);
        return bestScore;
    }
    
    private List<Object> simulateMove(List<Object> state, Direction move, boolean isMaximizingPlayer) {
        Snake snake = ((Snake) state.get(0)).clone();
        Snake opponent = ((Snake) state.get(1)).clone();
        boolean snakeAteApple = (Boolean) state.get(2);
        boolean opponentAteApple = (Boolean) state.get(3);
        Coordinate newHead = Coordinate.add(snake.getHead(), move.v);
        Coordinate opponentHead = opponent.getHead();
        
        if (!newHead.inBounds(boardSize) || snake.body.contains(newHead) || opponent.body.contains(newHead) || newHead.equals(opponentHead)) {
            return null; // Invalid move
        }
        Snake updatedSnake = simulateSnakeMove(snake, newHead, appleLocation.equals(newHead));
        return Arrays.asList(updatedSnake, opponent, snakeAteApple, opponentAteApple);
    }
    
    private Snake simulateSnakeMove(Snake snake, Coordinate newHead, boolean ateApple) {
        Snake newSnake = snake.clone();
        
        if (!ateApple) {
            newSnake.body.removeLast();
        }
        newSnake.body.addFirst(newHead);
        return newSnake;
    }
    
    private int evaluateState(List<Object> state, boolean isMaximizingPlayer) {
        Snake snake = (Snake) state.get(0);
        Snake opponent = (Snake) state.get(1);
        Coordinate head = snake.getHead();
        int score = 0;
        
        if (head.equals(appleLocation)) {
            score += 100000; // Score for eating an apple
        } else {
        	double appledist = calculateDistance(head, appleLocation);
            score -=  100 * appledist; // Penalty for distance to apple
            score += 25 * snake.body.size(); // Reward for increasing body length
        }
        if (snake.body.size() < opponent.body.size()) { // If the opponent is larger and head collides, the opponent wins
        	score -= 50 * calculateDistance(head, opponent.getHead()); // Penalty for head collision when smaller
        } 
        return isMaximizingPlayer ? score : -score;
    }
    
    private double calculateDistance(Coordinate from, Coordinate to) {
        return Math.sqrt(Math.abs(from.x - to.x) + Math.abs(from.y - to.y));
    }
    
    private boolean isTerminal(List<Object> state) {
        Snake snake = (Snake) state.get(0);
        Snake opponent = (Snake) state.get(1);
        
        if (opponent.body.contains(snake.getHead()) || !snake.getHead().inBounds(boardSize)) {
        	return true;
        }
    	
    	return false;
    }
}