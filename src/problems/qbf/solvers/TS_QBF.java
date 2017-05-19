package problems.qbf.solvers;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

import metaheuristics.tabusearch.AbstractTS;
import problems.qbf.QBF_Inverse;
import solutions.Solution;



/**
 * Metaheuristic TS (Tabu Search) for obtaining an optimal solution to a QBF
 * (Quadractive Binary Function -- {@link #QuadracticBinaryFunction}).
 * Since by default this TS considers minimization problems, an inverse QBF
 *  function is adopted.
 *
 * @author ccavellucci, fusberti
 */
public class TS_QBF extends AbstractTS<Integer> {
  private String searchType;

  /**
   * A fake value to insert in the Tabu List
   */
	private final Integer fake = new Integer(-1);

  /**
   * Represents the number of iterations until intensify
   */
  private int intensifyCeil;

  /**
   * Represents the current counter until intensifyCounter
   */
  private int intensifyCounter;

  private long maxTime;

  private boolean isIntensifyActive;

  public long intensifyTimeCounter;
  public long firstTimeCounter;
  public long bestTimeCounter;
  public boolean intensifyFirst;

  public static double target;

	/**
	 * Constructor for the TS_QBF class. An inverse QBF objective function is
	 * passed as argument for the superclass constructor.
	 *
	 * @param tenure
	 *            The Tabu tenure parameter.
	 * @param iterations
	 *            The number of iterations which the TS will be executed.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             necessary for I/O operations.
	 */
	public TS_QBF(Integer tenure, Integer iterations, String filename) throws IOException {
	    super(new QBF_Inverse(filename), tenure, iterations);
	    this.searchType = "best";
	}
	public TS_QBF(double tenure, Integer iterations, String searchType, String filename, long maxTime, int intensifyCeil, boolean isIntensifyActive, boolean intensifyFirst) throws IOException {
	    super(new QBF_Inverse(filename), 0, iterations);
	    this.tenure = (int)(ObjFunction.getDomainSize() * tenure);
	    this.searchType = searchType;
	    this.maxTime = maxTime;
	    this.intensifyCeil = intensifyCeil;
	    this.isIntensifyActive = isIntensifyActive;
	    this.intensifyFirst = intensifyFirst;
	
	    intensifyTimeCounter = 0;
	    firstTimeCounter = 0;
	    bestTimeCounter = 0;
	}

	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#makeCL()
	 */
	@Override
	public ArrayList<Integer> makeCL() {

		ArrayList<Integer> _CL = new ArrayList<Integer>();
		for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
			Integer cand = new Integer(i);
			_CL.add(cand);
		}

		return _CL;
	}

	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#makeRCL()
	 */
	@Override
	public ArrayList<Integer> makeRCL() {

		ArrayList<Integer> _RCL = new ArrayList<Integer>();

		return _RCL;

	}

	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#makeTL()
	 */
	@Override
	public ArrayDeque<Integer> makeTL() {

		ArrayDeque<Integer> _TS = new ArrayDeque<Integer>(2*tenure);
		for (int i=0; i<2*tenure; i++) {
			_TS.add(fake);
		}

		return _TS;
	}

	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#updateCL()
	 */
	@Override
	public void updateCL() {
		for (Integer elem : incumbentSol) {
      if (elem == ObjFunction.getDomainSize()-1) {
        CL.remove(new Integer(elem-1));
      } else {
        if (elem == 0) {
          CL.remove(new Integer(elem+1));
        } else {
          CL.remove(new Integer(elem-1));
          CL.remove(new Integer(elem+1));
        }
      }
    }
	}

  /**
  * Generates a candidate list from a given solution
  */
  public ArrayList<Integer> createCLFrom(ArrayList<Integer> solution){
    ArrayList<Integer> candidateList = new ArrayList<Integer>();

    // Adds every possibility
    for(Integer i = 0; i < ObjFunction.getDomainSize(); i++)
      candidateList.add(i);

    // Removes elements from candidateList
    for (Integer elem : solution) {
      if (elem == ObjFunction.getDomainSize()-1) {
        candidateList.remove(new Integer(elem-1));
      } else {
        if (elem == 0) {
          candidateList.remove(new Integer(elem+1));
        } else {
          candidateList.remove(new Integer(elem-1));
          candidateList.remove(new Integer(elem+1));
        }
      }
    }

    return candidateList;
  }

	/**
	 * {@inheritDoc}
	 *
	 * This createEmptySol instantiates an empty solution and it attributes a
	 * zero cost, since it is known that a QBF solution with all variables set
	 * to zero has also zero cost.
	 */
	@Override
	public Solution<Integer> createEmptySol() {
		Solution<Integer> sol = new Solution<Integer>();
		sol.cost = 0.0;
		return sol;
	}

	/**
	 * {@inheritDoc}
	 *
	 * The local search operator developed for the QBF objective function is
	 * composed by the neighborhood moves Insertion, Removal and 2-Exchange.
	 */
	public Solution<Integer> neighborhoodMoveBest() {
    long startTime = System.currentTimeMillis();

		Double minDeltaCost;
		Integer bestCandIn = null, bestCandOut = null;

		minDeltaCost = Double.POSITIVE_INFINITY;
		updateCL();
		// Evaluate insertions
		for (Integer candIn : CL) {
			Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, incumbentSol);
			if (!TL.contains(candIn) || incumbentSol.cost+deltaCost < bestSol.cost) {
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = candIn;
					bestCandOut = null;
				}
			}
		}
		// Evaluate removals
		for (Integer candOut : incumbentSol) {
			Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, incumbentSol);
			if (!TL.contains(candOut) || incumbentSol.cost+deltaCost < bestSol.cost) {
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = null;
					bestCandOut = candOut;
				}
			}
		}
		// Evaluate exchanges
		for (Integer candIn : CL) {
			for (Integer candOut : incumbentSol) {
				Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, incumbentSol);
				if ((!TL.contains(candIn) && !TL.contains(candOut)) || incumbentSol.cost+deltaCost < bestSol.cost) {
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						bestCandOut = candOut;
					}
				}
			}
		}
		// Implement the best non-tabu move
    removeFromSolution(bestCandOut);
    addToSolution(bestCandIn);

		ObjFunction.evaluate(incumbentSol);

    bestTimeCounter += System.currentTimeMillis() - startTime;
		return null;
  }

  /**
   * Adds to incumbent solution the given element
   */
  private void addToSolution(Integer in){
    TL.poll();
		if (in != null && in != -1) {
			incumbentSol.add(in);
			CL.remove(in);
			TL.add(in);
		} else {
			TL.add(fake);
		}
  }

  /**
   * Removes an element from incumbent solution
   */
  private void removeFromSolution(Integer out){
    TL.poll();
		if (out != null && out != -1) {
			incumbentSol.remove(out);
			CL.add(out);
      boolean addprev = false, addnext = false;
      if(out > 0)
        addprev = !CL.contains(new Integer(out-1));
      if(out < ObjFunction.getDomainSize() - 1)
        addnext = !CL.contains(new Integer(out+1));

      if (out == ObjFunction.getDomainSize()-1 && addprev)
        CL.add(new Integer(out-1));
      else if (out == 0 && addnext)
        CL.add(out+1);
      else {
        if (addnext) CL.add(out+1);
        if (addprev) CL.add(out-1);
      }
			TL.add(out);
		} else {
			TL.add(fake);
		}
  }

  // create a list of all possible neighbor moves, shuffles it and look
  // for the first movement that causes an improvement, if none is found
  // performs the least worse move.
	public Solution<Integer> neighborhoodMoveFirst() {
    long startTime = System.currentTimeMillis();

    Double minDeltaCost;
		minDeltaCost = Double.POSITIVE_INFINITY;
    Integer candIn = null, candOut = null;

		updateCL();

    // Build list of all possible neighbor moves
    // possibility[0]: in; possibility [1]: out
    // - if [i] = -1: do nothing;
    ArrayList<ArrayList<Integer> > possibilities = new ArrayList<ArrayList<Integer> >();
    ArrayList<Integer> list;
		for (Integer cIn : CL) {
      list = new ArrayList<Integer>();
      list.add(cIn);
      list.add(new Integer(-1));
      possibilities.add(list);
      boolean first = true;
			for (Integer cOut : incumbentSol) {
        if (first) {
          first = false;
          list = new ArrayList<Integer>();
          list.add(new Integer(-1));
          list.add(cOut);
          possibilities.add(list);
        }
        list = new ArrayList<Integer>();
        list.add(cIn);
        list.add(cOut);
        possibilities.add(list);
      }
    }

    Collections.shuffle(possibilities);
    for (ArrayList<Integer> l : possibilities) {
      Integer in = l.get(0);
      Integer out = l.get(1);

      // possibility l is an exchange
      if (in != -1 && out != -1) {
				Double deltaCost = ObjFunction.evaluateExchangeCost(in, out, incumbentSol);
				if ((!TL.contains(in) && !TL.contains(out)) || incumbentSol.cost + deltaCost < bestSol.cost) {
          if (deltaCost < 0) {
            candIn = in;
            candOut = out;
            break;
          } else if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
            candIn = in;
            candOut = out;
					}
        }
      } else if (out == -1 && in != -1) { // possibility l is an insertion
        Double deltaCost = ObjFunction.evaluateInsertionCost(in, incumbentSol);
        if (!TL.contains(in) || incumbentSol.cost+deltaCost < bestSol.cost) {
          if (deltaCost < 0) {
            candIn = in;
            candOut = null;
            break;
          } else if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
            candIn = in;
            candOut = null;
          }
        }
      } else if (in == -1 && out != -1) { // possibility l is a removal
        Double deltaCost = ObjFunction.evaluateRemovalCost(out, incumbentSol);
        if (!TL.contains(out) || incumbentSol.cost+deltaCost < bestSol.cost) {
          if (deltaCost < 0) {
            candIn = null;
            candOut = out;
            break;
          } else if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
            candIn = null;
            candOut = out;
          }
        }
      }
    }

    // Implement first improving move
      removeFromSolution(candOut);
		  addToSolution(candIn);

		ObjFunction.evaluate(incumbentSol);


    firstTimeCounter += System.currentTimeMillis() - startTime;

		return null;
  }

  /**
   * Intensify the neighborhood of incumbentSol
   * Searchs in a larger neighborhood of the best known solution for a better cost
   * Operates over 3 candidates (each can be either inserted or removed)
   */
  public Solution<Integer> intensifyNeighborhood(){
    long startTime = System.currentTimeMillis();

    Double minDeltaCost;
    Integer bestCandIn1 = null, bestCandIn2 = null, bestCandIn3 = null;
    Integer bestCandOut1 = null, bestCandOut2 = null, bestCandOut3 = null;

    minDeltaCost = Double.POSITIVE_INFINITY;
    ArrayList<Integer> candidateList = createCLFrom(bestSol);

    // True if should check for an insert candidate
    // False if should check for a remove candidate
    ArrayList<Boolean> possibilities = new ArrayList<Boolean>();
    possibilities.add(true);
    possibilities.add(false);

    // Evaluate insertions
    // For every combination of insert/remove over any 3 elemnts
    intensification:
    for(Boolean iscandAdd1 : possibilities){
      for(Boolean iscandAdd2 : possibilities){
        for(Boolean iscandAdd3 : possibilities){

          // For every element using the given combination of insertions/removmals
          for (Integer cand1: (iscandAdd1? candidateList : bestSol)){
            for (Integer cand2 : (iscandAdd2? candidateList : bestSol)) {
              for (Integer cand3 : (iscandAdd3? candidateList : bestSol)){

                // Check for insertion of neighbor elements
                if((iscandAdd1 && iscandAdd2 && Math.abs(cand1 - cand2) <= 1)
                  || (iscandAdd1 && iscandAdd3 && Math.abs(cand1 - cand3) <= 1)
                  || (iscandAdd2 && iscandAdd3 && Math.abs(cand2 - cand3) <= 1))
                  continue;

                // Evaluate modifications
                Double deltaCost = 0d;
                if(iscandAdd1)
                  deltaCost += ObjFunction.evaluateInsertionCost(cand1, bestSol);
                else
                  deltaCost += ObjFunction.evaluateRemovalCost(cand1, bestSol);

                if(iscandAdd2)
                  deltaCost += ObjFunction.evaluateInsertionCost(cand2, bestSol);
                else
                  deltaCost += ObjFunction.evaluateRemovalCost(cand2, bestSol);

                if(iscandAdd3)
                  deltaCost += ObjFunction.evaluateInsertionCost(cand3, bestSol);
                else
                  deltaCost += ObjFunction.evaluateRemovalCost(cand3, bestSol);

                // Check if it is a better solution
                if (bestSol.cost + deltaCost < bestSol.cost) {
                  if (deltaCost < minDeltaCost) {
                    minDeltaCost = deltaCost;

                    if(iscandAdd1){
                      bestCandIn1 = cand1;
                      bestCandOut1 = null;
                    }
                    else{
                      bestCandIn1 = null;
                      bestCandOut1 = cand1;
                    }

                    if(iscandAdd2){
                      bestCandIn2 = cand2;
                      bestCandOut2 = null;
                    }
                    else{
                      bestCandIn2 = null;
                      bestCandOut2 = cand2;
                    }

                    if(iscandAdd3){
                      bestCandIn3 = cand3;
                      bestCandOut3 = null;
                    }
                    else{
                      bestCandIn3 = null;
                      bestCandOut3 = cand3;
                    }

                    if(intensifyFirst)
                      break intensification;
                  }
                }
              }
            }
          }
        }
      }
    }

    if(bestSol.cost + minDeltaCost < bestSol.cost){
      incumbentSol = new Solution<Integer>(bestSol);

      CL = candidateList;

      removeFromSolution(bestCandOut1);
      removeFromSolution(bestCandOut2);
      removeFromSolution(bestCandOut3);
      addToSolution(bestCandIn1);
      addToSolution(bestCandIn2);
      addToSolution(bestCandIn3);

      ObjFunction.evaluate(incumbentSol);
    }

    intensifyTimeCounter += System.currentTimeMillis() - startTime;

    return null;
  }

	@Override
	public Solution<Integer> neighborhoodMove() {
    // Check if should intensify the neighborhood
    if(isIntensifyActive)
      intensifyCounter++;

    if(intensifyCounter >= intensifyCeil && isIntensifyActive){
      intensifyCounter = 0;
      // System.out.println("Intensify");
      return intensifyNeighborhood();
    }
    // Performs a normal move
    else if (searchType.equals("first")) {
      return neighborhoodMoveFirst();
    } else {
      return neighborhoodMoveBest();
    }
	}

	/**
	 * The TS mainframe. It consists of a constructive heuristic followed by
	 * a loop, in which each iteration a neighborhood move is performed on
	 * the current solution. The best solution is returned as result.
	 *
	 * @return The best feasible solution obtained throughout all iterations.
	 */
	public Solution<Integer> solve() {

		bestSol = createEmptySol();
		constructiveHeuristic();
		TL = makeTL();
    long startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			neighborhoodMove();
			if (bestSol.cost > incumbentSol.cost) {
     //   if (bestSol.cost < -target)
       //   break;
				bestSol = new Solution<Integer>(incumbentSol);
				if (verbose)
					System.out.println("(Iter. " + i + ") BestSol = " + bestSol);
			}

      // Check if max time
      if(System.currentTimeMillis() - startTime >= maxTime * 60 * 1000)
        break;
		}

		return bestSol;
	}

	/**
	 * A main method used for testing the TS metaheuristic.
	 *
	 */
	public static void main(String[] args) throws IOException {
    /*String []instances = {"qbf020", "qbf040", "qbf060", "qbf080", "qbf100", "qbf200", "qbf400"};
    String []searchTypes = {"best", "first"};
    double []tenureFactors = {0.20d};
    Boolean []intensifies = {true, false};
    Boolean intensifyFirst = false;

    int maxTime = 30; //min
    int maxIterations = 100000;
    int intensifyCeil = (int)maxIterations / 10;

    for(String instance : instances){
      for(String searchType : searchTypes){
        for(Double tenureFactor : tenureFactors){
          for(Boolean intensifyActive : intensifies){
            System.out.println("Configuration: " +
              "Instance=" + instance +
              ", searchType=" + searchType +
              ", tenureFactor=" + tenureFactor +
              ", intensified=" + intensifyActive +
              ", maxTime=" + maxTime +
              ", maxIterations=" + maxIterations +
              ", intensifyCeil=" + intensifyCeil +
              ", intensification=first");

        		long startTime = System.currentTimeMillis();
        		TS_QBF tabusearch = new TS_QBF(tenureFactor, maxIterations,searchType,"instances/" + instance, maxTime, intensifyCeil, intensifyActive, intensifyFirst);
        		Solution<Integer> bestSol = tabusearch.solve();
        		System.out.println("maxVal = " + bestSol);
        		long endTime   = System.currentTimeMillis();
        		long totalTime = endTime - startTime;
        		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");
            System.out.println("first = " + tabusearch.firstTimeCounter + "ms, best = " + tabusearch.bestTimeCounter + "ms, intensification = " + tabusearch.intensifyTimeCounter + "ms");
            System.out.println(); // newline
          }
        }
    	}
    }*/
    /*verbose = true; 
    long startTime = System.currentTimeMillis();
    TS_QBF ts = new TS_QBF(0.20d,Integer.MAX_VALUE,"first","instances/qbf200",2,1000,true,false);
    Solution<Integer> bestSol = ts.solve();
    System.out.println("maxVal = " + bestSol);
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    System.out.println("Time = " + (double) totalTime / (double) 1000 + " seg");*/

    int nExecutions = 200;
    long startTime, endTime, totalTime;
    TS_QBF ts;
    Solution<Integer> bestSol;
    target = 3091.0d;
   // for (int i=0; i<nExecutions; i++) {
      startTime = System.currentTimeMillis();
      ts = new TS_QBF(0.10,100000,"first","instances/qbf400",15,934,false,false);
      bestSol = ts.solve();
      endTime = System.currentTimeMillis();
      System.out.println("maxVal = " + bestSol);
      totalTime = endTime - startTime;
      System.out.println((double) totalTime / (double) 1000);
    //}

  }
}
