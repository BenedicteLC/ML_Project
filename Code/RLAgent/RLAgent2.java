package RLAgent;

import java.util.Random;

import ch.idsia.agents.Agent;
import ch.idsia.agents.AgentsPool;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;


public class RLAgent2 extends BasicMarioAIAgent implements Agent{
	
	int trueJumpCounter = 0;
	int trueSpeedCounter = 0;
	int round = 0;
	float last_intermediate;
	float reward;
	Random rand = new Random();
	boolean[][] all_actions = new boolean[13][6];
	double epsilon = 0.25;//0.0001;//
	double epsilon_min = 0.0001;
	double linear_ep = 1500*15000; // linear decay of epsilon over # of moves
	double alpha = 0.001; //0; learning rate
	double dec = 0.995; // Reward decay
	double lambda = 0.0; // eligibility traces
	double epsilon_decay = epsilon - epsilon_min;
	double num_moves = 0;
	int number_of_features;
	LinearReg LR;
	boolean FIRST_PART = true;
	boolean isLevelFinished;
	double q_A;
	double the_error;
	int the_action;
	State currentState;
	
	public RLAgent2()
	{
	    super("RLAgent2");
	    reset();
	    // First txt is write, second is read.
	    LR = new LinearReg(number_of_features, 13, "isStuck.txt");
	}
	
	public void reset()
	{		
		all_actions[0][1] = true; // 0  Mario.KEY_RIGHT & Mario.KEY_JUMP
		all_actions[0][3] = true;
		all_actions[1][1] = true; // 1  Mario.KEY_RIGHT
		all_actions[2][0] = true; // 2  Mario.KEY_LEFT
		all_actions[3][2] = true; // 3  Mario.KEY_DOWN
		all_actions[4][0] = true; // 4  Mario.KEY_LEFT & Mario.KEY_JUMP
		all_actions[4][3] = true;
		all_actions[5][1] = true; // 5  Mario.KEY_RIGHT & Mario.KEY_SPEED
		all_actions[5][4] = true;
		all_actions[6][0] = true; // 6  Mario.KEY_LEFT & Mario.KEY_SPEED
		all_actions[6][4] = true;
		all_actions[7][1] = true; // 7  Mario.KEY_RIGHT & Mario.KEY_SPEED & Mario.KEY_JUMP
		all_actions[7][3] = true;
		all_actions[7][4] = true;
		all_actions[8][0] = true; // 8  Mario.KEY_LEFT & Mario.KEY_SPEED & Mario.KEY_JUMP
		all_actions[8][3] = true;
		all_actions[8][4] = true;
		all_actions[10][3] = true;// 10 Mario.KEY_JUMP
		all_actions[11][4] = true;// 11 Mario.KEY_SPEED
		all_actions[12][3] = true;// 12 Mario.KEY_SPEED & Mario.KEY_JUMP
		all_actions[12][4] = true;
	    action = new boolean[Environment.numberOfKeys];	   
	    trueJumpCounter = 0;
	    trueSpeedCounter = 0;
	    currentState = new State();
	    number_of_features = currentState.integrate().length;
	}
	
	public int getMoveFromOutputs(double[] outputs){
		int maxIndex = 0;
		outputs[0]++;
		
		for (int i = 1; i < outputs.length; i++){
			if ((outputs[i] > outputs[maxIndex])){
				maxIndex = i;
			}
		  }
		return maxIndex;	  
	}
	
	public void giveIntermediateReward(float intermediateReward){
		//reward = last_intermediate - intermediateReward;
		
		reward = 3*(this.currentState.getDeltaDistance());
		
		/*if(Math.abs(this.currentState.getCurrentMarioXPosition() 
				- this.currentState.getPreviousMarioXPosition()) > 0.009){
			;//reward += 5*this.currentState.getMarioMode();
		}*/
		
		// Give a negative reward when Mario is stuck.
		if(this.currentState.isStuck() == 1)
		{
			reward -= 10;
		}
		
		if(this.currentState.getMarioStatus() == Mario.STATUS_DEAD){
			reward = -100;
		}
		if(this.currentState.getMarioStatus() == Mario.STATUS_WIN){
			reward += 100;
		}
		reward += this.currentState.getKillsByFire() + this.currentState.getKillsByStomp();
		
		//last_intermediate = intermediateReward;
	}	
	
	public double[] get_inputs(){
		int[] stateInfo = currentState.integrate();
		double[] castedState = new double[stateInfo.length];
		for (int i=0;i<stateInfo.length;i++) castedState[i] = (double) stateInfo[i];
		return castedState;
	}
	
	public void integrateObservation(Environment environment){
		super.integrateObservation(environment);
		currentState.update(environment);
		this.isLevelFinished = environment.isLevelFinished();
	}

	
	public boolean[] getAction()
	{
	    //if (DangerOfAny() && getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 1) != 1)  // a coin
	    // THIS IS AN ONLINE LEARNING AGENT
		if (FIRST_PART){
			//double[] inputs = {1, 0, 0, 1, 1, 1};
			double[] inputs = get_inputs();
			//System.out.println("-> "+ inputs.length);
			// GET INPUT FEATURES
	
			double current_epsilon = epsilon_min + (((linear_ep - num_moves)/linear_ep) * epsilon_decay); // calculate epsilon
			current_epsilon = Math.max(epsilon_min, current_epsilon);
			num_moves++;
			
			double[] q_values = LR.get_output(inputs);
			
			int action_num = 0;
			if (rand.nextDouble() < current_epsilon){
		    	action_num = rand.nextInt(all_actions.length);
		    } else {
		    	action_num = getMoveFromOutputs(q_values);
		    }
			
			q_A = q_values[action_num];
			LR.add_elig(inputs, action_num);
			the_error = (-1) * q_A;
		
			action = all_actions[action_num].clone();
			the_action = action_num;
			FIRST_PART = false;
		    return action;
		} else {
			double[] inputs = get_inputs();
			double[] q_values_prime = LR.get_output(inputs);
			int action_num = getMoveFromOutputs(q_values_prime);
			double q_A_prime = q_values_prime[action_num];
			//System.out.println();
			//for (double i : q_values_prime) System.out.printf("%.2f ", i);
			//System.out.println();
			//System.out.print(reward +" + " + (dec*q_A_prime) +  " = " + the_error*(-1) +"-> ");
			if (isLevelFinished){
				LR.reset_traces();
				the_error += reward - 0.01;
			} else {
				the_error += reward + (dec*q_A_prime) - 0.01;
				//System.out.println(the_error);
			}
			
			LR.train(the_action, alpha, the_error, lambda, dec);
			
			FIRST_PART = true;
			return action;
		}
	}
	
	public boolean[] getAction(int action_num){
		return all_actions[action_num].clone();
	}		
}

