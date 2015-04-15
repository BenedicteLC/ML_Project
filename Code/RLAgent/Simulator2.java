package RLAgent;

import java.io.IOException;
import java.util.Random;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.*;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.benchmark.tasks.RLBasicTask;
//import ch.idsia.benchmark.tasks.RLBasicTask;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.MarioAIOptions;
import competition.gic2010.learning.sergeykarakovskiy.*;

public class Simulator2 {
	
	public static void main(String[] args) throws CloneNotSupportedException
	{		
		final int ITERATIONS = 1500;
		int winCount = 0;
		int gameCount = 0;
		Random rand = new Random();
		
		//final String argsString = "-vis off"; // -vis: show display
	    final MarioAIOptions marioAIOptions = new MarioAIOptions(args);
	    final RLAgent2 agent = new RLAgent2();
	    final RLBasicTask basicTask = new RLBasicTask(marioAIOptions);
	    
	    int seed = 0;
	    int difficulty;
	    
	    for (int i = 0; i <= ITERATIONS; ++i)
	    {	
	    	difficulty = rand.nextInt(1);        	        

            marioAIOptions.setLevelDifficulty(difficulty);
            marioAIOptions.setLevelRandSeed(seed++);
            marioAIOptions.setAgent(agent);
            basicTask.setOptionsAndReset(marioAIOptions);
            
            // Run the stage. This is where the agent gets the state
            // and performs an action.
            basicTask.runSingleEpisode(1);
            Environment environment = basicTask.getEnvironment();
                        
            System.out.println("Iteration: " + i + " difficulty: " + difficulty);
            System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
            
            // Log the current model results every 100 iterations.
            if(i % 100 == 0 && i != 0){            	
            	//testModel(marioAIOptions, basicTask, agent, i);   
            }
            
            
            // Count wins in the last 1000 iters.
            if(ITERATIONS - i <= 1000){ 
	            gameCount++;
	            if(basicTask.getEnvironment().getEvaluationInfo().marioStatus == Mario.STATUS_WIN)
	            	winCount++;
            }
	    }
	    System.out.println("\n" + winCount + "/" + gameCount + " wins");
	    
	    Runtime rt = Runtime.getRuntime();
	    try
	    {
	    	//Process proc = rt.exec("/usr/local/bin/mate " + marioTraceFileName);
	        Process proc = rt.exec("python hello.py");
	    } catch (IOException e)
	    {
	        e.printStackTrace();
	    }
	    System.exit(0);

	}	
	
	static void testModel(MarioAIOptions marioAIOptions, RLBasicTask basicTask, RLAgent2 agent, int currentIter){	 
	    
	    int difficulty;
	    int numberTestIters = 100;
	    Random rand = new Random();
		int seed = rand.nextInt();
	    
	    final Logger logger = new Logger(currentIter);
	    
	    // This is ugly, but creating a clone of agent was a mess.
	    double backupNumMoves = agent.num_moves;
	    double backupEpsilonMin = agent.epsilon_min;
	
	    // Prevent learning and pick greedy policy.
	    agent.num_moves = agent.linear_ep;
    	agent.epsilon_min = 0;    	
    	agent.testRun = true;
    	
    	System.out.println("Testing. Please wait...");

    	for (int i = 0; i < numberTestIters; ++i)
 	    {	
 	    	 difficulty = rand.nextInt(1);        	        
 	        
             marioAIOptions.setLevelDifficulty(difficulty);
             marioAIOptions.setLevelRandSeed(seed++);
             marioAIOptions.setAgent(agent);
             basicTask.setOptionsAndReset(marioAIOptions);
             
             // Run the stage. This is where the agent gets the state
             // and performs an action.
             basicTask.runSingleEpisode(1);
             
             Environment environment = basicTask.getEnvironment();
             logger.log(environment);    
    
 	    }
    	
    	logger.writeToFile();
    	
    	// Restore the parameters.
    	agent.num_moves = backupNumMoves;
    	agent.epsilon_min = backupEpsilonMin; 
    	agent.testRun = false;
	}
}
