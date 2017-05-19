package problems.qbf.solvers;

import helpers.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import problems.Evaluator;
import problems.BP;

import solutions.Solution;
import metaheuristics.tabusearch.AbstractTS;

public class TS_BP{
	static Random rng = new Random();

	protected BP ObjFunction;

	protected Double bestCost;

	protected Double incumbentCost;

	protected Solution<LinkedList<Integer>> bestSol;

	protected Solution<LinkedList<Integer>> incumbentSol;

	protected Integer iterations;

	protected Integer tenure;

	protected ArrayList<LinkedList<Integer>> CL;

	protected ArrayList<LinkedList<Integer>> RCL;

	protected ArrayDeque<LinkedList<Integer>> TL;
	
	
	
	public TS_BP(BP objFunction, Integer tenure,
			     Integer iterations) {
		this.ObjFunction = objFunction;
		this.tenure = tenure;
		this.iterations = iterations;
		this.ObjFunction = new BP();
	}

	
	public ArrayList<Integer> makeCL() {
		Pair[] pairs = new Pair[ObjFunction.getDomainSize()];
		ArrayList<Integer> weights = ObjFunction.getWeights();
		for (int i = 0; i < ObjFunction.getDomainSize();i++){
			pairs[i] = new Pair(i,weights.get(i));
		}
	    Arrays.sort(pairs);
	    ArrayList<Integer> ret = new ArrayList<Integer>();
	    for (int i = 0; i < ObjFunction.getDomainSize();i++){
	    	ret.add(pairs[i].index);
	    }
	    return ret;
	}
	
	public ArrayDeque<Integer> makeTL() {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateCL() {
		// TODO Auto-generated method stub
		
	}

	public Solution<LinkedList<Integer>> createEmptySol() {
		// TODO Auto-generated method stub
		return null;
	}

	public Solution<LinkedList<Integer>> neighborhoodMove() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * The TS constructive heuristic, which is responsible for building a
	 * feasible solution by selecting in a greedy fashion, candidate
	 * elements to enter the solution.
	 *
	 * @return A feasible solution to the problem being minimized.
	 */
	public Solution<LinkedList<Integer>> constructiveHeuristic() {

		CL = makeCL();
		RCL = makeRCL();
		incumbentSol = createEmptySol();
		incumbentCost = Double.POSITIVE_INFINITY;

		/* Main loop, which repeats until the stopping criteria is reached. */
		while (!constructiveStopCriteria()) {

			Double maxCost = Double.NEGATIVE_INFINITY, minCost = Double.POSITIVE_INFINITY;
			incumbentCost = incumbentSol.cost;
			updateCL();

			/*
			 * Explore all candidate elements to enter the solution, saving the
			 * highest and lowest cost variation achieved by the candidates.
			 */
			for (Integer c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, incumbentSol);
				if (deltaCost < minCost)
					minCost = deltaCost;
				if (deltaCost > maxCost)
					maxCost = deltaCost;
			}

			/*
			 * Among all candidates, insert into the RCL those with the highest
			 * performance.
			 */
			for (Integer c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, incumbentSol);
				if (deltaCost <= minCost) {
					RCL.add(c);
				}
			}
			/* Choose a candidate randomly from the RCL */
			if(RCL.size() > 0){
				int rndIndex = rng.nextInt(RCL.size());
				Integer inCand = RCL.get(rndIndex);
				CL.remove(inCand);
				incumbentSol.add(Integer);
				ObjFunction.evaluate(incumbentSol);
				RCL.clear();
			}

		}
		
		/**
		 * The TS mainframe. It consists of a constructive heuristic followed by
		 * a loop, in which each iteration a neighborhood move is performed on
		 * the current solution. The best solution is returned as result.
		 *
		 * @return The best feasible solution obtained throughout all iterations.
		 */
		public Solution<E> solve() {

			bestSol = createEmptySol();
			constructiveHeuristic();
			TL = makeTL();
			for (int i = 0; i < iterations; i++) {
				neighborhoodMove();
				if (bestSol.cost > incumbentSol.cost) {
					bestSol = new Solution<E>(incumbentSol);
					if (verbose)
						System.out.println("(Iter. " + i + ") BestSol = " + bestSol);
				}
			}

			return bestSol;
		}

		/**
		 * A standard stopping criteria for the constructive heuristic is to repeat
		 * until the incumbent solution improves by inserting a new candidate
		 * element.
		 *
		 * @return true if the criteria is met.
		 */
		public Boolean constructiveStopCriteria() {
			return (incumbentCost > incumbentSol.cost) ? false : true;
		}

		return incumbentSol;
	}

	
	
	public static void main(String[] args) {
		

	}

	
}

