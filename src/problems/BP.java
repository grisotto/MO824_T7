package problems;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.LinkedList;
import solutions.Solution;

public class BP implements Evaluator<LinkedList<Integer>> {

	/**
	 * @param args
	 */

	public final Integer size;
	
	public Integer binSize;
	
	public final ArrayList<Integer> weights;
	
	public BP(String filename) throws IOException  {
		weights = new ArrayList<Integer>();
		size = readInput(filename);
	}
	
	protected Integer readInput(String filename) throws IOException {
		Reader fileInst = new BufferedReader(new FileReader(filename));
		StreamTokenizer stok = new StreamTokenizer(fileInst);

		stok.nextToken();
		Integer _size = (int) stok.nval;
		stok.nextToken();
		Integer _binSize = (int) stok.nval;

		for (int i = 0; i < _size; i++) {
			stok.nextToken();
			weights.add((int) stok.nval);
		}
		
		this.binSize = _binSize;
		return _size;
	}
	
	public ArrayList<Integer> getWeights(){
		return this.weights;
	}
	
	@Override
	public Integer getDomainSize() {
		return size;
	}
	

	@Override
	public Double evaluate(Solution<LinkedList<Integer>> sol) {
		return (double) sol.size();
	}

	@Override
	public Double evaluateInsertionCost(LinkedList<Integer> elem,
			Solution<LinkedList<Integer>> sol) {
		return null;
	}
	
	private double evaluateCurrent(Solution<LinkedList<Integer>> sol, int newSize) {
		int sum = 0;
		for (LinkedList<Integer> l : sol) {
			int sumBin = 0;
			for (Integer i : l){
				sumBin += weights.get(i);
			}
			sum += (binSize - sumBin) * (binSize - sumBin);
		}
		return sum/newSize;
	}
	
	public double evaluateShift(int item, int binIn, int binOut, Solution<LinkedList<Integer>> sol) {
		int newSize = sol.size();
		if (sol.get(binOut).size() == 1){
			newSize = sol.size() - 1;
		}
		int sumIn = 0;
		for (Integer i : sol.get(binIn)) {
			sumIn += weights.get(i);
		}
		sumIn += weights.get(item);
		int sumOut = 0;
		for (Integer i : sol.get(binOut)) {
			sumOut += weights.get(i);
		}
		sumOut -= weights.get(item);
		Double objVal = evaluateCurrent(sol, newSize);
		return objVal + ((sumIn*sumIn)/newSize)
				      - ((sumOut*sumOut)/newSize);
	}
	
	public double evaluateSwitch(int i1, int i2, int b1, int b2, Solution<LinkedList<Integer>> sol){
		Double objVal = evaluateCurrent(sol, sol.size());
		return objVal + evaluateShift(i1,b1,b2,sol) + evaluateShift(i2,b2,b1,sol);
	}

	@Override
	public Double evaluateRemovalCost(LinkedList<Integer> elem,
			Solution<LinkedList<Integer>> sol) {
		return null;
	}

	@Override
	public Double evaluateExchangeCost(LinkedList<Integer> elemIn,
			LinkedList<Integer> elemOut, Solution<LinkedList<Integer>> sol) {
		return null;
	}

}
