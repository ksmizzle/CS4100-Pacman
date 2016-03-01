/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman.controllers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import static pacman.controllers.BFS_Controller.ghosts;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

/**
 *
 * @author Kenny
 */
public class Kenny_AStar_Controller extends Controller<Constants.MOVE> {

    // An A* node used to record game state, the previous gamestate,
    // total cost  f, cost to get here, and heuristic to get to goal, h.
    private class PacManNode {
        Game gameState;
        PacManNode parent;
        //total cost, cost to get here and heuristic cost
        int f, g, h;
        public PacManNode(Game game) {
            this.gameState = game;
        }
    }
    
    //pacman node comparator to compare pacmannode f costs.
    //This is needed because I am using a priority queue to store my pacman nodes.
    private class PacManNodeCostComparator implements Comparator<PacManNode>
    {
        @Override
        public int compare(PacManNode o1, PacManNode o2) {
            if(o1.f < o2.f){
                return -1;
            }
            if(o1.f > o2.f){
                return 1;
            }
            return 0;
        }
    }

    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        
        
        //create a node containing the start data
        Game gameCopy = game.copy();
        PacManNode start = new PacManNode(gameCopy);
        start.g = 0; //cost to get here
        start.h = getHeuristic(start.gameState);
        start.f = start.g + start.h;
        start.parent = null;
        PacManNode q = start;
        
        Comparator<PacManNode> comparator = new PacManNodeCostComparator();
        
        //initialize an open list to store unvisited gamestates
        PriorityQueue<PacManNode> openList = new PriorityQueue<PacManNode>(10, comparator);
        
        //initialize a closedList to store visited gamestates
        PriorityQueue<PacManNode> closedList = new PriorityQueue<PacManNode>(10, comparator);

        //add the start node to the unvisited queue
        openList.add(start);
        
        //While we have unvisited nodes...
        visitorLoop:
        while (!openList.isEmpty()) {

            //make sure we have time left, or break 
            if (timeDue - System.currentTimeMillis() < 5) {
                // System.out.println("ran out of time");
                break;
            }

            //find the node with the smallest total cost f on the open list, call it "q"
            q = openList.remove();
            
            //for each successor of the current node
            successorLoop:
            for (MOVE m : q.gameState.getPossibleMoves(q.gameState.getPacmanCurrentNodeIndex())) {
                gameCopy = q.gameState.copy();
                gameCopy.advanceGame(m, ghosts.getMove(gameCopy, 0));
                
                //don't add the node if it results in us getting eaten
                if(gameCopy.wasPacManEaten()){
                    // GameView.addPoints(game, Color.RED, q.gameState.getPacmanCurrentNodeIndex());
                    break;
                }
                
                //create a node from the successor
                PacManNode child = new PacManNode(gameCopy);
                child.g = q.g + 1; //increment cost to get here since we moved
                child.h = getHeuristic(gameCopy);
                child.f = child.g + child.h;
                child.parent = q;
                

                //if heuristic is 0, goal is achieved
                if (child.h == 0) {
                    openList.add(child);
//                    System.out.println("we found a path to eat last pellet");
                    break visitorLoop;
                }
//                GameView.addLines(game, 
//                            Color.yellow, 
//                            q.gameState.getPacmanCurrentNodeIndex(), 
//                            gameCopy.getPacmanCurrentNodeIndex());
                
                // Check if the current pacmanlocation has already been visited
                // at a cheaper cost than the current child.
                // Note: this is a slight modification from traditional A*
                // since I am not checking the entire gamespace (i.e. ghosts could
                // be in different locations and I consider the node to be a duplicate)
                int pacmanlocation = gameCopy.getPacmanCurrentNodeIndex();
                if (!betterPacManNodeExists(child, openList) && !betterPacManNodeExists(child, closedList)){
                    openList.add(child);
                }
//                else{
//                    GameView.addPoints(game, Color.BLUE, child.gameState.getPacmanCurrentNodeIndex());
//                }
            }
            
            // Add the current node to the list of visited nodes so we won't
            // visit it again unless we find a cheaper way to get to it.
            closedList.add(q);
        }
        
        // Traverse the best cost path to get the move to make from the root game state
        // (the root game state is the game that was initially passed to getMove)
        PacManNode tempnode = q;
        Game tempGame = q.gameState;
        Constants.MOVE move = tempGame.getPacmanLastMoveMade();
        while (tempnode.parent != null){
            move = tempnode.gameState.getPacmanLastMoveMade();
            tempnode = tempnode.parent;
            
            //I occasionally get an index out of range here, seems to be a bug with the pacman code
//            GameView.addPoints(game, Color.GREEN, tempnode.gameState.getPacmanCurrentNodeIndex());
        }
        return move;
    }
    
    
    // Given a PacManNode pmn and a PriorityQueue of pacman nodes return true 
    // if there is a duplicate of pmn in the list with a lower cost F, otherwise false
    public boolean betterPacManNodeExists(PacManNode node, PriorityQueue<PacManNode> queue){
        int pacmanlocation = node.gameState.getPacmanCurrentNodeIndex();
        for(PacManNode pmn: queue){
            if(pmn.f < node.f && pmn.gameState.getPacmanCurrentNodeIndex() == pacmanlocation){
                return true;
            }
        }
        return false;
    }

    //return the expected cost to get to a state where all food is eaten
    //for now, I just use the number of pills left.
    public int getHeuristic(Game game) {
        //initially my heuristic was just the number of active pills
//        return game.getNumberOfActivePills();
        
        //I found multiplying by 10 worked better
//        return game.getNumberOfActivePills()*10;
        
        //and 100 worked even better
        return game.getNumberOfActivePills()*100;
        
        //attempt of a heuristic that is the distance to the closest food:
//        int currentNode = game.getPacmanCurrentNodeIndex(); 
//        int[] foods = game.getActivePillsIndices();
//        int closestFood = game.getClosestNodeIndexFromNodeIndex(currentNode, foods, DM.PATH);
//        int distanceToCloseFood = game.getShortestPathDistance(currentNode, closestFood);
//        return distanceToCloseFood;

            //attempt of a heuristic that combines number of food and distance to closest food:
//        int currentNode = game.getPacmanCurrentNodeIndex(); 
//        int[] foods = game.getActivePillsIndices();
//        int closestFood = game.getClosestNodeIndexFromNodeIndex(currentNode, foods, DM.PATH);
//        int distanceToCloseFood = game.getShortestPathDistance(currentNode, closestFood);
//        int[] pathToCloseFood = game.getShortestPath(currentNode, closestFood, game.getPacmanLastMoveMade());
//        
//        return distanceToCloseFood + (10*game.getNumberOfActivePills());
        
    }
}
