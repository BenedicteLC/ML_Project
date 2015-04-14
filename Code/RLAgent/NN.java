package RLAgent;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

public class NN {
	double [][] weights_1;
	double [][] weights_2;
	double [] bias_1;
	double [] bias_2;
	double [] activations;
	double [] past_inputs;
	double [][] elig_1;
	double [][] elig_2;
	int output_size;
	int input_size;
	int hidden_units;
	String filename_out;
	int num_rounds = 0;
	Random rand = new Random();
	
	public NN(int in_size, int out_size, int hidden_u, String filename){
		output_size = out_size;
		input_size = in_size;
		filename_out = filename;
		hidden_units = hidden_u;
		init_weights();
	}
	
	public NN(int in_size, int out_size, int hidden_u, String filename, String filename_in){
		output_size = out_size;
		input_size = in_size;
		filename_out = filename;
		hidden_units = hidden_u;
		get_weights(filename_in);
	}
	
	public double[] matrix_mult(double[] A, double[][] B){
		double[] o = new double[B[0].length];
		for (int i=0; i<(B.length); i++){
			for (int j=0; j<(B[0].length); j++){
				o[j] += B[i][j] * A[i];
			}
		}
		return o;
	}
	
	public void init_weights(){
		weights_1 = new double[input_size][hidden_units];
		weights_2 = new double[hidden_units][output_size];
		bias_1 = new double[hidden_units];
		bias_2 = new double[output_size];
		double multiplier = 0.1;
		for (int i=0; i<input_size; i++){
			for (int j=0; j<hidden_units; j++){
				weights_1[i][j] = (rand.nextDouble() - 0.5)*multiplier;
			}
		}
		for (int i=0; i<hidden_units; i++){
			for (int j=0; j<output_size; j++){
				weights_2[i][j] = (rand.nextDouble() - 0.5)*multiplier;
			}
		}
		reset_traces();
	}
	
	public double[] get_output(double[] inputs, boolean t){
		double[] temp = matrix_mult(inputs, weights_1);
		for (int i=1; i<temp.length;i++) temp[i] += bias_1[i];
		double[] temp1 = new double[temp.length];
		for (int i=0; i<temp.length;i++){
			temp1[i] = tanh(temp[i]);
		}
		if (t){
			activations = temp1.clone();
			past_inputs = inputs.clone();
		}
		double[] outs = matrix_mult(temp1, weights_2);
		for (int i=0; i<outs.length;i++) outs[i] += bias_2[i];
		return outs;
	}
	
	
	public void get_weights(String filename){
		try {
			get_weights1(filename);
			get_biases1(filename);
		} catch (FileNotFoundException e) {
			init_weights();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void get_weights1(String filename_before) throws FileNotFoundException, IOException, ClassNotFoundException{
		for (int i=1;i<=2;i++){
			String filename = filename_before.substring(0, filename_before.length()-4) + i
					+ filename_before.substring(filename_before.length()-4);
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename));
			double[][] temp_weights = (double[][])inputStream.readObject();
			System.out.println(temp_weights.length+" "+temp_weights[0].length+" "+input_size+" "+output_size);
			if ((input_size == temp_weights.length) && (output_size == temp_weights[0].length)){
				if (i==1){
					weights_1 = new double[input_size][output_size];
					weights_1 = temp_weights.clone();
				}else{
					weights_2 = new double[input_size][output_size];
					weights_2 = temp_weights.clone();
				}
				reset_traces();
				System.out.println("Loaded weights!");
			}else{
				System.out.println("Weight dimensions didn't match. Initialized from scratch.");
				init_weights();
			}
			inputStream.close();
		}
	}
	
	public void get_biases1(String filename_before) throws FileNotFoundException, IOException, ClassNotFoundException{
		for (int i=1;i<=2;i++){
			String filename = filename_before.substring(0, filename_before.length()-4) + i
					+"b"+ filename_before.substring(filename_before.length()-4);
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename));
			double[] temp_weights = (double[])inputStream.readObject();
			System.out.println(temp_weights.length+" "+output_size);
			if (output_size == temp_weights.length){
				if (i==1){
					bias_1 = new double[hidden_units];
					bias_1 = temp_weights.clone();
				}else{
					bias_2 = new double[output_size];
					bias_2 = temp_weights.clone();
				}
				reset_traces();
				System.out.println("Loaded biases!");
			}else{
				System.out.println("Weight dimensions didn't match. Initialized from scratch.");
				init_weights();
			}
			inputStream.close();
		}
	}
	
	public void save_weights1(String filename_before) throws FileNotFoundException, IOException, ClassNotFoundException{
		for (int i=1; i<3; i++){
			String filename = filename_before.substring(0, filename_before.length()-4) + i
					+ filename_before.substring(filename_before.length()-4);
			ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename));
			if (i == 1){
				outputStream.writeObject(weights_1);
			} else {
				outputStream.writeObject(weights_2);
			}
			outputStream.close();
		}
	}
	
	public void save_biases1(String filename_before) throws FileNotFoundException, IOException, ClassNotFoundException{
		for (int i=1; i<3; i++){
			String filename = filename_before.substring(0, filename_before.length()-4) + i
					+"b"+ filename_before.substring(filename_before.length()-4);
			ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename));
			if (i == 1){
				outputStream.writeObject(bias_1);
			} else {
				outputStream.writeObject(bias_2);
			}
			outputStream.close();
		}
	}
	
	public void save_weights(String filename){
		System.out.println("weights saved!");
	try {
		save_weights1(filename);
		save_biases1(filename);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
	
	
	public void reset_traces(){
		elig_1 = new double[input_size][hidden_units];
		elig_2 = new double[hidden_units][output_size];
		if (num_rounds > 0 && num_rounds%30 == 0) save_weights(filename_out);
		num_rounds ++;
	}
	
	public void print_sum(){
		double s1 = 0.0;
		double s2 = 0.0;
		for (int i=0; i<weights_1.length; i++){
			for (int j=0; j<weights_1[0].length; j++){
				//System.out.print(weights_1[i][j]);
				s1 += Math.abs(weights_1[i][j]);
			}
			//System.out.println();
		}
		for (int i=0; i<weights_2.length; i++){
			for (int j=0; j<weights_2[0].length; j++){
				s2 += Math.abs(weights_2[i][j]);
			}
		}
		System.out.println(s1 + " "+ s2);
	}
	
	public void print_biases(){
		for (double i : bias_2) 
			System.out.print(((int) i)+" ");
		System.out.println();
	}
	
	public double tanh(double x){
		//System.out.println("->"+x+" "+Math.tanh(x));
		return Math.tanh(x);
		//return x;
	}
	
	public void add_elig(double[] inp, int action){
		;
	}
	
	public void reset_traces_only(){
		elig_1 = new double[input_size][hidden_units];
		elig_2 = new double[hidden_units][output_size];
	}
	
	public void train(int action, double alpha, double errors, double lambda, double dec, double reg, boolean reset_trace){
		double[] hidden_errors = new double[activations.length];
		double alpha_1 = alpha/10000;
		double ss = 0;
		double ss1 = 0;
		for (int i=0; i<weights_2.length; i++){
			hidden_errors[i] = errors*weights_2[i][action]*(1.0-(activations[i]*activations[i]));
			//System.out.println(activations[i]+" "+hidden_errors[i]);
			weights_2[i][action] += activations[i]*alpha*errors - (alpha*reg*weights_2[i][action]);
			//ss += Math.abs(activations[i]*alpha*errors);
			//ss1 += Math.abs(alpha*reg*weights_2[i][action]);
		}
		//System.out.println(ss+" "+ss1);
		for (int i=0; i<weights_1.length; i++){
			for (int j=0; j<weights_1[0].length; j++){
				//System.out.println(weights_1[i][j]);
				weights_1[i][j] += past_inputs[i]*alpha_1*hidden_errors[j] - (alpha*reg*weights_1[i][j]);
			}
		}

		//for (int i=0;i<output_size;i++) bias_2[i] -= (alpha*reg*bias_2[i]);
		bias_2[action] += alpha*errors;
		//System.out.println(alpha*errors);
		//print_biases();
		for (int i=0;i<hidden_units;i++){
			bias_1[i] += hidden_errors[i]*alpha_1 /*- bias_1[i]*alpha*reg*/;
		}
	}
}
