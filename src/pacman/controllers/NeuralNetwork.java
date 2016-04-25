/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman.controllers;

import static java.lang.Math.exp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.Executor;

/**
 *
 * @author Kenny
 */
public class NeuralNetwork {
    
    //need to maintain seperate list of input/hidden/output neurons for random
    //mutation purposes
    private ArrayList<InputNeuron> inputNeurons;
    private ArrayList<Neuron> hiddenNeurons;
    private Neuron outputNeuron;
    private ArrayList<Connection> connections;
    private Random rnd;
    private double fitness = 0;
    private int networkUid;

    
    //initialize a 4 input, 1 output network
    public NeuralNetwork(){
        rnd = new Random(System.currentTimeMillis());
        hiddenNeurons = new ArrayList<>();
        inputNeurons = new ArrayList<>();
        connections = new ArrayList<>();
        outputNeuron = new OutputNeuron();
        networkUid = Executor.getNextNetworkUid();
        for(int i = 0;i < 4; i++){
            InputNeuron inputNeuron = new InputNeuron(0.0);
            Connection connection = new Connection(inputNeuron, outputNeuron);
            outputNeuron.connect(connection);
            connections.add(connection);
            inputNeurons.add(inputNeuron);
        }
    }
    
    //copy constructor
    public NeuralNetwork(NeuralNetwork original){
        hiddenNeurons = new ArrayList<>();
        inputNeurons = new ArrayList<>();
        connections = new ArrayList<>();
        Map<Integer,Neuron> nodeUidToNode = new HashMap<>();
        rnd = new Random(System.currentTimeMillis());
        networkUid = Executor.getNextNetworkUid();
        for (Connection connection: original.connections){
//            System.out.println("copying connection : " + connection.getInputNeuron().getUid() + " <-> " + connection.getOutputNeuron().getUid());
            if(nodeUidToNode.get(connection.getInputNeuron().getUid()) == null){
//                System.out.println("getting input node: " + connection.getInputNeuron().getUid() + ", the type is " + connection.getInputNeuron().getType());
                if(connection.getInputNeuron().getType() == 0){
                    InputNeuron in = new InputNeuron(connection.getInputNeuron());
                    inputNeurons.add(in);
                    nodeUidToNode.put(in.getUid(), in);
//                    System.out.println("adding node uid (in) " + connection.getInputNeuron().getUid());
                }
                if(connection.getInputNeuron().getType() == 1){
                    Neuron n = new Neuron(connection.getInputNeuron());
                    hiddenNeurons.add(n);
                    nodeUidToNode.put(n.getUid(), n);
//                    System.out.println("adding node uid (n)" + connection.getInputNeuron().getUid());
                }

            }
            else{
//                System.out.println("we've already created node " + connection.getInputNeuron().getUid());
            }
            if(nodeUidToNode.get(connection.getOutputNeuron().getUid()) == null){
//                System.out.println("getting output node: " + connection.getInputNeuron().getUid() + ", the type is " + connection.getInputNeuron().getType());
                if(connection.getOutputNeuron().getType() == 2){
                    OutputNeuron on = new OutputNeuron(connection.getOutputNeuron());
                    outputNeuron = on;
                    nodeUidToNode.put(on.getUid(), on);
//                    System.out.println("adding node uid (on)" + connection.getOutputNeuron().getUid());
                }
                if(connection.getOutputNeuron().getType() == 1){
                    Neuron n = new Neuron(connection.getOutputNeuron());
                    hiddenNeurons.add(n);
                    nodeUidToNode.put(n.getUid(), n);
//                    System.out.println("adding node uid (n2)" + connection.getOutputNeuron().getUid());
                }
            }
            for(Integer n :nodeUidToNode.keySet()){
//                System.out.println("created copy of node uid:" + n);
            }
            
            Neuron input = nodeUidToNode.get(connection.getInputNeuron().getUid());
            Neuron output = nodeUidToNode.get(connection.getOutputNeuron().getUid());
            double weight = connection.getWeight();
            int connectionUid = connection.getUid();
//            System.out.println("creating connection : " + input.getUid() + " <-> " + output.getUid());
            connections.add(new Connection(input, output, weight, connectionUid));
        }
    }
    
    
    
    //TODO: implement this constructor to mate 2 neural networks
    public NeuralNetwork(NeuralNetwork parent1, NeuralNetwork parent2){
        for(Connection c1:parent1.getConnections()){
            
        }
    }
    
    public ArrayList<Connection> getConnections(){
        return connections;
    }
    
    public void setFitness(double fitness){
        this.fitness = fitness;
    }
    
    public double getFitness(){
        return this.fitness;
    }
    
    private Connection getRandomConnection(){
        return connections.get(rnd.nextInt(connections.size()));
    }
    
    public int getUid(){
        return networkUid;
    }
    
    //NeuralNet comparator to compare neuralnet fitnesses.
    public static class NeuralNetworkComparator implements Comparator<NeuralNetwork>
    {
        @Override
        public int compare(NeuralNetwork o1, NeuralNetwork o2) {
            if(o1.fitness < o2.fitness){
                return -1;
            }
            if(o1.fitness > o2.fitness){
                return 1;
            }
            return 1;
//            return 0;
        }
    }
    
    
    //This method will add a connection between 2 neurons
    public void mutateAddConnection(Neuron input, Neuron output){
//        System.out.println("creating a new connection between nodes: " + input.getUid() + "<-> " + output.getUid());
        for(Connection c:this.connections){
            if(c.getInputNeuron() == input && c.getOutputNeuron() == output){
                //we are trying to add a connection that already exists.
                return;
            }
        }
        Connection connection = new Connection(input, output);
        connections.add(connection);
    }

    //add node on a connection
    public void mutateAddNode(Connection connection){
        connection.disable();
        Neuron neuron = new Neuron();
        //As per neat algo suggestion, the first connection has a weight of 1
        //This limits it's impact on the network until the weight evolves
        Connection newConnection1 = new Connection(connection.getInputNeuron(), neuron, 1);
        Connection newConnection2 = new Connection(neuron, connection.getOutputNeuron(), connection.getWeight());
//        System.out.println("created a new node of type: " + neuron.getType());
        hiddenNeurons.add(neuron);
        connections.add(newConnection1);
        connections.add(newConnection2);
    }

    //perform random mutations given several probablilities of each mutation ocurring
    public void randomMutation(double nodeProb, double connectionProb, double weightProb){
        if(rnd.nextDouble() < nodeProb){
            mutateAddNode(getRandomConnection());
        }
        //TODO: prevent looping (node 1 > node 2 > node 3 > node 1)
        if(rnd.nextDouble() < connectionProb){
            Neuron input = getRandomInput(-1);
            Neuron output = getRandomOutput(input.getUid()); //this makes sure we don't connect a neuron to itself;
            mutateAddConnection(input, output);
        }
        if(rnd.nextDouble() < weightProb){
            getRandomConnection().setWeight(rnd.nextDouble());
        }
    }
    
    //returns a random input or hidden neuron
    //exceptUid is the uid of a neuron we don't want
    private Neuron getRandomInput(int exceptUid){
        int options = hiddenNeurons.size() + inputNeurons.size();
        Neuron randomInputNeuron = new Neuron();
        do{
            int index = rnd.nextInt(options);
//            System.out.println("getting random input neuron " + index);
            randomInputNeuron = (index < hiddenNeurons.size()? hiddenNeurons.get(index):inputNeurons.get(index - hiddenNeurons.size()));
        }while(randomInputNeuron.getUid() == exceptUid);
        return randomInputNeuron;
    }

    //returns a random output or hidden neuron
    //exceptUid is the uid of a neuron we don't want
    private Neuron getRandomOutput(int exceptUid){
        int options = hiddenNeurons.size() + 1;
        Neuron randomOutputNeuron = new Neuron();
        do{
            int index = rnd.nextInt(options);
            randomOutputNeuron = (index < hiddenNeurons.size()? hiddenNeurons.get(index) : outputNeuron);
//            System.out.println("getting random output neuron " + index + " uid: " + randomOutputNeuron.getUid() + ", we cannot return uid: " + exceptUid);
        }while(randomOutputNeuron.getUid() == exceptUid);
        return randomOutputNeuron;
    }

    //return the genotype of the neural net
    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append("Network number: " + getUid() + "\r\n");
        for (Connection connection: connections){


            sb.append("gene number: " + connection.getUid() + "\r\n");
                sb.append(connection.getInputNeuron().getUid() + " -> " + connection.getOutputNeuron().getUid() + "\r\n");
                if(connection.isDisabled()){
                    sb.append("DIS" + "\r\n");
                }
                sb.append("\r\n");
        }
        return sb.toString();
    }
    
    //given a game and a direction, decide how good of a move it is.
    //This was developed using the paper:
    //Reinforcement Learning to Train Ms. Pac-Man Using Higher-order Action-relative Inputs
    //as a reference for what types of inputs to use.
    //I am using 4 of the 7 inputs that they make use of.
    private double evaluateNetwork(Game game, Constants.MOVE direction){
        int pacmanIndex = game.getPacmanCurrentNodeIndex();

        //This first input which tracks how close to level completion we are
        double pillsEatenInput = (game.getNumberOfPills()-game.getNumberOfActivePills())/game.getNumberOfPills();

        //todo: this should be the ratio of time left on a power pill.
        double powerPillInput = 0;

        //
        int furthestNodeFromPacman = game.getFarthestNodeIndexFromNodeIndex(pacmanIndex, game.getAllIndices(), Constants.DM.PATH);
        double maxPathLength = game.getDistance(pacmanIndex, furthestNodeFromPacman, Constants.DM.PATH);
        int closestFoodIndexInDirection = game.getClosestNodeIndexFromNodeIndex(pacmanIndex, game.getActivePillsIndices(), direction, Constants.DM.PATH);
        double closestFoodInDirectionDistance = game.getDistance(pacmanIndex, closestFoodIndexInDirection, direction, Constants.DM.PATH);
        //This is a relative distance of how far we need to move in this direction to get to a pill
        double pillInput = (maxPathLength - closestFoodInDirectionDistance)/maxPathLength;

        
        //This is a relative metric of how much danger the ghosts pose in the current direction
        double ghostInput=0;
        int[]ghostIndices = getGhostsIndices(game);
        if(ghostIndices != null){
            int closestIntersectionInDirection = game.getClosestNodeIndexFromNodeIndex(pacmanIndex, game.getJunctionIndices(), direction, Constants.DM.PATH);
            int ghostNearCloseIntersection = game.getClosestNodeIndexFromNodeIndex(closestIntersectionInDirection, ghostIndices , Constants.DM.PATH);
            double ghostIntersectionDistance = game.getDistance(closestIntersectionInDirection, ghostNearCloseIntersection, Constants.DM.PATH);
            double pacmanIntersectionDistance = game.getDistance(pacmanIndex, closestIntersectionInDirection, direction, Constants.DM.PATH);
            double ghostspeed = 1.0;//todo: figure out ratio of ghost speed to pacman
            ghostInput = (maxPathLength + pacmanIntersectionDistance * ghostspeed - ghostIntersectionDistance)/maxPathLength;
        }
        else{
            ghostInput = 0;
        }

        //Todo: this will be a ratio of how much time left for ghosts to be edible
        double ghostAfraidInput=0;
        
        //Todo: this will be a ratio of safe routes available in a directions
        double entrapmentInput = 0;
        
        //This is a binary value on if we are going in the same direction as before
        //It can be used to tie break when 2 directions seem the same
        double holdTheCourseInput = (game.getPacmanLastMoveMade()==direction? 1.0: 0.0);

        inputNeurons.get(0).setValue(pillsEatenInput);
//            inputNeurons.get(1).setValue(powerPillInput);
        inputNeurons.get(1).setValue(pillInput);
        inputNeurons.get(2).setValue(ghostInput);
//            inputNeurons.get(4).setValue(ghostAfraidInput);
//            inputNeurons.get(5).setValue(entrapmentInput);
        inputNeurons.get(3).setValue(holdTheCourseInput);

//        System.out.println("about to fire network");
        return outputNeuron.fire();
    }
    
    //Run the neural network for each move available and return the best move
    //according to the network.
    public Constants.MOVE getBestMove(Game game){
        double bestMoveScore = 0;
        Constants.MOVE bestMove = Constants.MOVE.NEUTRAL;
        for(Constants.MOVE move: game.getPossibleMoves(game.getPacmanCurrentNodeIndex())){

            double moveScore = evaluateNetwork(game, move);
            if(moveScore > bestMoveScore){
                bestMove = move;
                bestMoveScore = moveScore;
            }                
        }
        return bestMove;
    }
    
   
    //returns an array of all ghost indices
    private int[] getGhostsIndices(Game game){
        ArrayList<Integer> ghostArrayList = new ArrayList<Integer>();
        for( Constants.GHOST ghost:Constants.GHOST.values()){
            //add the ghost to the arraylist if it is not edible and not in lair
            if((!game.isGhostEdible(ghost)) && (game.getGhostLairTime(ghost) == 0)){
                 ghostArrayList.add(game.getGhostCurrentNodeIndex(ghost));
            }
        }
        int[] ghostIndices = new int[ghostArrayList.size()];
        for(int i = 0; i < ghostIndices.length; i ++){
            ghostIndices[i] = ghostArrayList.get(i).intValue();
        }
        return ghostIndices;
    }
    

    //A neuron.  it will sum the inputs, pass through sigmoid and output them
    public class Neuron {
        private ArrayList<Connection> inputs;
        private int nodeUid;
        protected int type = 1; //0 = input, 1 = hidden, 2 = output
        public Neuron() {
            this.nodeUid = Executor.getNextNodeUid();
            inputs = new ArrayList<Connection>();
        }
        
        public Neuron(Neuron original){
            this.type = original.getType();
            this.nodeUid = original.getUid();
            inputs = new ArrayList<Connection>();
            
        }
        public int getUid(){
            return nodeUid;
        }
        public void connect (Connection ... cs) {
            for (Connection c : cs){
                this.inputs.add(c);
            }
        }
        double sigmoid(double x) {
            return 1/(1+exp(-x));
        }
        double sigmoidPrime(double x) {
              return x*(1-x);
        }
        public double fire(){
            double stimulus = 0;
            for(Connection connection : inputs){
                stimulus += connection.fire();
            }
            return sigmoid(stimulus);
        }
        public int getType(){
            return type;
        }
    }
    
    //This is just used to keep track of our output to make sure a random mutation
    //doesn't use it as input for something.    
    public class OutputNeuron extends Neuron{
        
        public OutputNeuron(){
            super();
            this.type = 2;
            
        }
        public OutputNeuron(Neuron original){
            super(original);
            type = 2;
        }
    }
    
    //input neuron can be assigned a hardcoded value that it returns on fire
    //This is also used to prevent random mutations from outputing to it.
    public class InputNeuron extends Neuron{
        private double value;
        public InputNeuron(double inputval){
            this.value = inputval;
            type = 0;
        }
        public InputNeuron(Neuron original){
            super(original);
            type = 0;
//            this.value = original.getValue();
            
        }
        public void setValue(double inputval){
            this.value = inputval;
        }
        public double getValue(){
            return this.value;
        }
        @Override
        public double fire(){
            return this.value;
        }
    }
    
    
    //The connection object keeps track of what nodes it's connected to
    //and handles multiplying by a weight.
    public class Connection{
        private Neuron input;
        private Neuron output;
        private double weight;
        private boolean disabled;
        private int uid;
        public Connection(Neuron input, Neuron output){
            this.input = input;
            this.output = output;
            output.connect(this);
            this.weight = rnd.nextDouble();
            this.disabled = false;
            this.uid = Executor.getNextConnectionUid();
        }
        public Connection(Neuron input, Neuron output, double weight){
            this.input = input;
            this.output = output;
            output.connect(this);
            this.weight = weight;
            this.disabled = false;
            this.uid = Executor.getNextConnectionUid();
        }
        public Connection(Neuron input, Neuron output, double weight, int uid){
//            System.out.println("adding connection: " + input.getUid() + "<->" + output.getUid());
            this.input = input;
            this.output = output;
            output.connect(this);
            this.weight = weight;
            this.disabled = false;
            this.uid = uid;
        }
        public int getUid(){
            return uid;
        }
        public Neuron getInputNeuron(){
            return input;
        }
        public void enable(){
            disabled = false;
        }
        public void disable(){
            disabled = true;
        }
        public boolean isDisabled(){
            return disabled;
        }
        public Neuron getOutputNeuron(){
            return output;
        }
        public void setInputNeuron(Neuron input){
            this.input = input;
        }
        public void setOutputNeuron(Neuron output){
            this.output = output;
        }
        public double fire(){
            if(input == output){
                System.out.println("THERE IS A CONNECTION FROM A NODE TO ITSELF!!! THIS SHOULD NOT HAPPEN!!!");
                return 0;
            }
            return ((disabled==false)?input.fire() * weight :  0.0 );
        }
        public double getWeight(){
            return this.weight;
        }
        public void setWeight(double weight){
            this.weight = weight;
        }
    }
    
    
}
