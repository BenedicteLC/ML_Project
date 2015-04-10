package RLAgent;

import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite; // Monsters, etc.
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene; // Obstacles.
import java.lang.Math;

public class State {
	
	// Distance Mario must move between frames to be considered moving.
	public final int MIN_MOVE_DISTANCE = 1;
	// Consider Mario stuck when he doesn't move for this amount of frames.
	public final int NUM_STUCK_FRAMES = 20;
	
	// Variables contained within a state.
	private int isMarioBig, isMarioFire;
	private int isGrounded, canJump, isStuck;
	private int marioStatus;
	private int stepKillsByFire, stepKillsByStomp, totalKillsByFire, totalKillsByStomp, 
	previousDistance, stuckCounter, deltaDistance;
	private int[] groundMonsters, flyingMonsters, plantMonsters, obstacles; // flattened binary grids.
	private float currentMarioXPosition, previousMarioXPosition;	
	
	private Environment environment;
	private byte[][] scene; // Contains monsters and obstacles.
	
	private int[] stateArray; // What we will return.

	public State() {
		this.groundMonsters = new int[19*13];
		this.flyingMonsters = new int[19*13];
		this.plantMonsters = new int[7*7]; 
		this.obstacles = new int[11*11];
		this.stateArray = new int[5 + this.groundMonsters.length + this.flyingMonsters.length
		                          + this.plantMonsters.length + this.obstacles.length];
		this.currentMarioXPosition = 0;
		this.totalKillsByFire = 0;
		this.totalKillsByStomp = 0;
		this.previousDistance = 0;
		this.stuckCounter = 0;
		this.deltaDistance = 0;
	}
		
	// Update the state variables. 
	public void update(Environment environment) {
		
		// Get the simulator's latest environment.
		this.environment = environment;
		this.scene = environment.getMergedObservationZZ(1,1); //TODO Not confident about that 1,1. Need to check.	
		
		///////////////////
		// Update variables.
		///////////////////
		
		// Update Mario's float position.
		this.previousMarioXPosition = Math.max(this.currentMarioXPosition, 0);
		this.currentMarioXPosition = environment.getMarioFloatPos()[0];		
		// Get Marios's current status (running, dead, etc).
		this.marioStatus = this.environment.getMarioStatus();
		
		// Check if Mario is stuck.
	    int currentDistance = environment.getEvaluationInfo().distancePassedPhys;
	    deltaDistance = currentDistance - this.previousDistance;
	    // If the distance traversed between frames is smaller than the move threshold,
	    // consider that Mario didn't move.
	    if (Math.abs(deltaDistance) <= MIN_MOVE_DISTANCE) {
	    	deltaDistance = 0;
	    }
	    this.previousDistance = currentDistance;
	    
	    if(deltaDistance == 0){
	    	this.stuckCounter++;
	    }
	    //TODO: I want to try this instead.
	    else{
	    	this.stuckCounter--;
	    }
	    /*else{
	    	this.stuckCounter = 0; // This is what the other students did.
	    }*/
	    // If we haven't moved for several frames, consider ourself stuck.
	    if(this.stuckCounter >= NUM_STUCK_FRAMES){
	    	this.isStuck = 1;
	    }
	    else{
	    	this.isStuck = 0;
	    }
	    
		
		// Get the amount of kills in this tick.
		// Here we get the max in case the level restarts without resetting the state.
		this.stepKillsByFire = Math.max(this.environment.getKillsByFire() - this.totalKillsByFire, 0);
		this.stepKillsByStomp = Math.max(this.environment.getKillsByStomp() - this.stepKillsByStomp, 0);
		this.totalKillsByFire = this.environment.getKillsByFire();
		this.totalKillsByStomp = this.environment.getKillsByStomp();
		
		// Get Mario's mode (small/big, fire).
		// Mario can be both big and fire at the same time.
		int marioMode = environment.getMarioMode();
		this.isMarioBig = marioMode == 0 ? 0 : 1;
		this.isMarioFire = marioMode == 2 ? 1 : 0;		
		
		// 1 if Mario is grounded, 0 otherwise.
		this.isGrounded = environment.isMarioOnGround() ? 1 : 0;
		// 1 if Mario can jump, 0 otherwise.
		this.canJump = environment.isMarioAbleToJump() || !environment.isMarioOnGround() ? 1 : 0;			
		
		// Fill the monster array.
		int currentTile = 0;
		for(int i = 3; i < scene.length - 3; i++){
			for (int j = 0; j < scene[0].length; j++){
				// For now we're not discriminating between monster types
				// to reduce the state space.
				if(this.containsMonster(scene[i][j])){
					this.groundMonsters[currentTile] = 1;
				}
				else{
					this.groundMonsters[currentTile] = 0;
				}
				currentTile++;
			}
		}
		
		// Fill the flying monster array.
		currentTile = 0;
		for(int i = 3; i < scene.length - 3; i++){
			for (int j = 0; j < scene[0].length; j++){
				// For now we're not discriminating between monster types
				// to reduce the state space.
				if(this.containsFlyingMonster(scene[i][j])){
					this.flyingMonsters[currentTile] = 1;
				}
				else{
					this.flyingMonsters[currentTile] = 0;
				}
				currentTile++;
			}
		}
		
		int marioX = 9;
		int marioY = 9;		
		currentTile = 0;

		// Fill the plant monster array.
		for(int i = marioY - 3; i <= marioY + 3; i++){
			for (int j = marioX - 3; j <= marioX + 3; j++){
				// For now we're not discriminating between monster types
				// to reduce the state space. Piranha plants will get us killed by stomp!
				if(scene[i][j] == Sprite.KIND_ENEMY_FLOWER){
					this.plantMonsters[currentTile] = 1;
				}
				else{
					this.plantMonsters[currentTile] = 0;
				}
				currentTile++;
			}
		}
		
		// Fill the obstacle array.
		currentTile = 0;		
		for(int i = marioY - 5; i <= marioY + 5; i++){
			for (int j = marioX - 5; j <= marioX + 5; j++){
				if(this.containsObstacle(scene[i][j], i)){
					this.obstacles[currentTile] = 1;
				}
				else{
					this.obstacles[currentTile] = 0;
				}
				currentTile++;
			}
		}
	}
	
	// Convert this state to an array of ints.
	public int[] integrate(){
		
		this.stateArray[0] = this.isMarioBig;
		this.stateArray[1] = this.isMarioFire;
		this.stateArray[2] = this.isGrounded;
		this.stateArray[3] = this.canJump;
		this.stateArray[4] = this.isStuck;
		
		for(int i = 0; i < this.groundMonsters.length; i++){
			this.stateArray[5+i] = this.groundMonsters[i];
		}
		
		for(int i = 0; i < this.flyingMonsters.length; i++){
			this.stateArray[5 + this.groundMonsters.length + i] = this.flyingMonsters[i];
		}
		
		for(int i = 0; i < this.plantMonsters.length; i++){
			this.stateArray[5 + this.groundMonsters.length + this.flyingMonsters.length + i] 
					= this.plantMonsters[i];
		}
		
		for(int i = 0; i < this.obstacles.length; i++){
			this.stateArray[5 + this.groundMonsters.length + this.flyingMonsters.length + this.plantMonsters.length + i] 
					= this.obstacles[i];
		}
		
		return stateArray;
	}
	
	// Takes a tileValue and check if it's a monster.
	private boolean containsMonster(byte tileValue){
		boolean containsMonster = false;
		
		if(tileValue == Sprite.KIND_GOOMBA 
			|| tileValue == Sprite.KIND_BULLET_BILL
			//|| tileValue == Sprite.KIND_ENEMY_FLOWER
			//|| tileValue == Sprite.KIND_GOOMBA_WINGED
			|| tileValue == Sprite.KIND_GREEN_KOOPA
			//|| tileValue == Sprite.KIND_GREEN_KOOPA_WINGED
			|| tileValue == Sprite.KIND_RED_KOOPA
			//|| tileValue == Sprite.KIND_RED_KOOPA_WINGED
			|| tileValue == Sprite.KIND_SPIKY
			//|| tileValue == Sprite.KIND_SPIKY_WINGED
			|| tileValue == Sprite.KIND_WAVE_GOOMBA){
			containsMonster = true;
		}
		
		return containsMonster;
	}
	
	// Takes a tileValue and check if it's a monster.
		private boolean containsFlyingMonster(byte tileValue){
			boolean containsMonster = false;
			
			if( tileValue == Sprite.KIND_GOOMBA_WINGED
				|| tileValue == Sprite.KIND_GREEN_KOOPA_WINGED
				|| tileValue == Sprite.KIND_RED_KOOPA_WINGED
				|| tileValue == Sprite.KIND_SPIKY_WINGED){
				containsMonster = true;
			}
			
			return containsMonster;
		}
	
	// Takes a tileValue and check if it's an obstacle.
	private boolean containsObstacle(byte tileValue, int y){
		boolean containsObstacle = false;
		
		if(tileValue == GeneralizerLevelScene.BREAKABLE_BRICK 
			|| tileValue == GeneralizerLevelScene.BRICK
			|| tileValue == GeneralizerLevelScene.FLOWER_POT_OR_CANNON
			|| tileValue == GeneralizerLevelScene.UNBREAKABLE_BRICK){
			containsObstacle = true;
		}
		// BORDER_CANNOT_PASS_THROUGH are only obstacles when they're not above Mario.
		if(tileValue == GeneralizerLevelScene.BORDER_CANNOT_PASS_THROUGH
		&& y >= 9){
			containsObstacle = true;
		}
		
		return containsObstacle;
	}
	
	public float getCurrentMarioXPosition(){
		return this.currentMarioXPosition;
	}
	
	public float getPreviousMarioXPosition(){
		return this.previousMarioXPosition;
	}
	
	public int getMarioStatus(){
		return this.marioStatus;
	}
	
	public int getMarioMode(){
		return this.isMarioFire + this.isMarioBig;
	}
	
	// Number of kills in this tick.
	public int getKillsByFire(){
		return this.stepKillsByFire;
	}
	
	// Number of kills in this tick.
	public int getKillsByStomp(){
		return this.stepKillsByStomp;
	}
	
	public int isStuck(){
		return this.isStuck;
	}
	
	public int getDeltaDistance(){
		return this.deltaDistance;
	}
}

/*
 * 
		for(int i =0; i < this.scene.length; i++){
			for(int j =0; j < this.scene[0].length; j++){
				System.out.print(this.scene[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();*/



