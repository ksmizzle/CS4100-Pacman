/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman.controllers;

import static java.lang.Math.exp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 *
 * @author Kenny
 */
public class Kenny_NeuralNetwork_Controller extends Controller<Constants.MOVE> {
    NeuralNetwork nn;
    
    public Kenny_NeuralNetwork_Controller(NeuralNetwork nn){
        this.nn = nn;
    }

    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        
        return nn.getBestMove(game);
    }
    
    
    
    
    
    
    
    
}
