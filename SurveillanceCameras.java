import java.io.*;
import java.util.*;

public class SurveillanceCameras {
	
	static int roomSize;
	static String[][] problemInstance;
	

	public static void main(String[] args) throws IOException{
		
		Scanner scan = new Scanner(System.in);
		int choice;
		SurveillanceCameras ob = new SurveillanceCameras();
		roomSize=4;
		problemInstance = new String[roomSize][roomSize];
		Node Nsolution;
		String[][] solution;
		LinkedList<int[]> path;
		int [] cameras;
		LinkedList<int[]> neighbors;
				
		readFile();
		
		while(true) {
			
			System.out.println("Choose a search strategy:\n1.Greedy Search\n2.A* Search\n3.Hill Climbing Search\n4.Simulated Annealing");
			choice = scan.nextInt();
			
			switch(choice) {
				case 1: 		Nsolution = ob.greedySearch();
							cameras = Nsolution.camerasPosition;
							solution = ob.visibleAreas(cameras);
				
							path = Nsolution.printPath();
							System.out.println("Solution Cost: "+ cameras.length/2);

							System.out.println("Solution Path: ");
							while(!path.isEmpty()) {
								printM(ob.visibleAreas(path.removeLast()),roomSize,roomSize);
								System.out.println("\n-----------------------------");
							}
							break;
				
				case 2:		Nsolution = ob.A();
							cameras = Nsolution.camerasPosition;
							solution = ob.visibleAreas(cameras);
				
							path = Nsolution.printPath();
				
							System.out.println("Solution Cost: "+ Nsolution.cost);
							System.out.println("Solution Path: ");
							while(!path.isEmpty()) {
								printM(ob.visibleAreas(path.removeLast()),roomSize,roomSize);
								System.out.println("\n-----------------------------");
							}
					
							break;
							
				case 3: neighbors = ob.hillClimbing(100);
						System.out.println("Neighbors: ");
					
						while(!neighbors.isEmpty()) {
							solution = ob.visibleAreas(neighbors.removeFirst());
							System.out.println("Value of objective function= "+ ob.objectiveFunction(solution));

							printM(solution,roomSize,roomSize);
							System.out.println("\n-----------------------------");
						}
						break;
						
				case 4: neighbors = ob.simulatedAnnealing(100);
						System.out.println("Best found solution : ");
						printM(ob.visibleAreas(neighbors.removeLast()),roomSize,roomSize);
						System.out.println("\n-----------------------------");


						while(!neighbors.isEmpty()) {
							solution = ob.visibleAreas(neighbors.removeFirst());
							System.out.println("Value of objective function= "+ ob.objectiveFunction(solution));

							printM(solution,roomSize,roomSize);
							System.out.println("\n-----------------------------");
						}
						break;
					
				default: System.exit(0);
			}
		}
		
	}
	
	public static void readFile() throws IOException{//Read from file and create problem instance
		
		Scanner sc = new Scanner(new BufferedReader(new FileReader("room")));
	      
	      while(sc.hasNextLine()) {
	          roomSize = Integer.parseInt(sc.nextLine());
	          problemInstance = new String[roomSize][roomSize];
	        		  
	         for (int i=0; i<roomSize; i++) {
	            String[] line = sc.nextLine().trim().split(" ");
	            for (int j=0; j<roomSize; j++) {
	               problemInstance[i][j] = line[j];
	            }
	         }
	      }   
	}
	
	
	
	//------------------------------------------Best Fisrt Search-----------------------------------------------//

	
	/*
	 * Input: solution (room) represented as 2D matrix  
	 * Output: number of v(s) in the room
	 * This method will calculate the number of visible cells and it will add to it number of obstecls and cameras so if the output was = roomSize*roomSize it means that it the gool
	 */
	public int evaluationFunction(String[][] solution) {
		
		int obstacles=0;
		int sum=0;
		int cameras=0;
		
		for(int i=0; i<roomSize ; i++)
			for(int j=0 ; j<roomSize ; j++) {
				if(solution[i][j].equals("o"))
					obstacles++;
				else if(solution[i][j].equals("c"))
					cameras++;
				else if(solution[i][j].equals("v"))
					sum++;
			}
		return sum+obstacles+cameras;//check
	}
	
	
	//------------------------------------------Greedy Search-----------------------------------------------//
	
	
	/*
	 * this method will perfome greedySearch algorithm. 
	 * Output : node that has the location of the cameras
	 */
	public Node greedySearch() {
		
		int maxCovered=0;
		int[] loc= new int[2];
		
		for(int i=0;i<roomSize;i++)
			for(int j=0 ; j<roomSize;j++)
				if(!problemInstance[i][j].equals("o")) {
					loc[0]=i;
					loc[1]=j;
					if(evaluationFunction(visibleAreas(loc))>maxCovered)
						maxCovered = evaluationFunction(visibleAreas(loc));
				}
		
		/*
		 * the priority of the elements in the priority list is based on H(n) , the heuristic function 
		 */
		Comparator<Node> heuristicFunctionComparator = new Comparator<Node>() { 
			@Override
	         public int compare(Node s1, Node s2) {
				if(s1.heuristic - s2.heuristic < 0) return -1;
				else if(s1.heuristic - s2.heuristic == 0) return 0;
				else return 1;
	         }
	    };
	        
        LinkedList<Node> explored = new LinkedList<Node>();//to add the explore camera positions
        PriorityQueue<Node> queue = new PriorityQueue<Node>(heuristicFunctionComparator);
        int goalValue = roomSize*roomSize;
        int[] sourceCameras= new int[0];
        Node source = new Node(sourceCameras, greedyHeuristic(sourceCameras , maxCovered) , null);
        Node current = source;//no cameras
        
        queue.add(source);
        boolean found = false;
        
        while((!queue.isEmpty())&&(!found)) {
        	
        		current = queue.poll();
        		explored.add(current);
        		
        		if(evaluationFunction(visibleAreas(current.getCameras()))==goalValue) 
        			found = true;
        		
        		//check every child of current node , the child is costructed by adding one new camera in a valid position
        		for(int i=0; i<roomSize ; i++)
        			for(int j=0; j<roomSize ; j++) {
        				if(validLocation(i,j)&&notCamera(current.getCameras(),i,j)) {
        					int[] childCameras = new int[current.camerasPosition.length+2];
        					copyParent(childCameras,current.camerasPosition);
        					childCameras[current.getCameras().length]= i;
        					childCameras[current.getCameras().length+1]= j;
        					
        					Node child = new Node(childCameras, greedyHeuristic(childCameras,maxCovered),current);
        					if(!explored(explored,child)) {
        						if(existInQueue(queue,child)){
                                    queue.remove(child);
                            }
                            queue.add(child);
        					}
        				}
        			}
        }
        
        return current;
		
	}
	
	public double greedyHeuristic( int[] cameraPosition , int max) {//the closer to the goal the smaller the number 
		
		double numOfUncovered;
		
		numOfUncovered = (roomSize*roomSize) - evaluationFunction(visibleAreas(cameraPosition));//this will givs us the uncovered cells
		return (numOfUncovered/max);
		
	}
	
	
	//------------------------------------------A*-----------------------------------------------//

	
	/*
	 * A* search method, performed using an addmisible heuristic to garantee optimality 
	 * Output : node that has the location of the cameras
	 */
	public Node A(){
		
		int maxCovered=0;
		int[] loc= new int[2];
		
		//to find the maximum nuumber of cell could be covered using one camera
		for(int i=0;i<roomSize;i++)
			for(int j=0 ; j<roomSize;j++)
				if(!problemInstance[i][j].equals("o")) {
					loc[0]=i;
					loc[1]=j;
					if(evaluationFunction(visibleAreas(loc))>maxCovered)
						maxCovered = evaluationFunction(visibleAreas(loc));
				}
		
		/*
		 * the priority of the elements in the priority list is based on F(n) = H(n) + C(n)
		 * where H(n) is the heuristic function, and c(n) is the cost function represented by the number of cameras 
		 */
		Comparator<Node> heuristicFunctionComparator = new Comparator<Node>() { 
			@Override
	         public int compare(Node s1, Node s2) {
				return (s1.getF()) - (s2.getF());
	         }
	    };
	        
        LinkedList<Node> explored = new LinkedList<Node>();//to add the explore camera positions
        PriorityQueue<Node> queue = new PriorityQueue<Node>(heuristicFunctionComparator);
        int goalValue = roomSize*roomSize;
        int[] sourceCameras= new int[0];
        Node source = new Node(sourceCameras, heuristic(sourceCameras , maxCovered) , null);
        Node current = source;//no cameras
        
        queue.add(source);
        boolean found = false;
        
        while((!queue.isEmpty())&&(!found)) {
        	
        		current = queue.poll();
        		explored.add(current);
        		
        		if(evaluationFunction(visibleAreas(current.getCameras()))==goalValue) //when all cell are covered
        			found = true;
        		
        		//check every child of current node , the child is costructed by adding one new camera in a valid position
        		for(int i=0; i<roomSize ; i++)
        			for(int j=0; j<roomSize ; j++) {
        				if(validLocation(i,j)&&notCamera(current.getCameras(),i,j)) {
        					int[] childCameras = new int[current.camerasPosition.length+2];
        					copyParent(childCameras,current.camerasPosition);
        					childCameras[current.getCameras().length]= i;
        					childCameras[current.getCameras().length+1]= j;
        					
        					Node child = new Node(childCameras,heuristic(childCameras,maxCovered),current);
        					if(!explored(explored,child)) {
        						if(existInQueue(queue,child)){
                                    queue.remove(child);
                            }
                            queue.add(child);
        					}
        				}
        			}
        }
        
        return current;
	}
	
	public int heuristic( int[] cameraPosition , int max) {//the closer to the goal the smaller the number 
		
		int numOfUncovered;
		
		numOfUncovered = (roomSize*roomSize) - evaluationFunction(visibleAreas(cameraPosition));//this will givs us the uncovered cells
		return (int) Math.ceil(numOfUncovered/max);
		
	}
	
	
	//------------------------------------------Method used by both Greedy Search and A*-----------------------------------------------//

	
	/*
	 * Input: linked list, node
	 * It will check if the node g had been explored before. 
	 * output: true->explored 
	 */
	public boolean explored(LinkedList<Node> list, Node g) {
		
		int i=0;
		int[] s;
		String[][] sState;
		String[][] gState = visibleAreas(g.getCameras());
		boolean flag = true; 
		
		
		while(i<list.size()) {
			
			s=list.get(i).getCameras();
			sState = visibleAreas(s);
					
			flag = true;
			
			for(int j=0 ; j<roomSize ; j++)
				for(int k=0 ; k<roomSize ; k++)
					if(sState[j][k]!=gState[j][k])
						flag = false; // flag will = false if there is at least one different index value, mean not the same camera position
			
			if(flag)
				break;
			
			i++;
		}
		
		if(flag)
			return true;
		return false;
	}
	
	/*
	 * Input: queue, node
	 * It will check if the node is in the frontier
	 * output: true->explored 
	 */
	public boolean existInQueue( PriorityQueue<Node> queue , Node g) {
		

		int i=0;
		int[] s;
		String[][] sState;
		String[][] gState = visibleAreas(g.getCameras());
		boolean flag = true; 
		PriorityQueue<Node> queue1 = new PriorityQueue<Node>(queue);
		
		while(!queue1.isEmpty()) {
			
			s=queue1.poll().getCameras();
			sState = visibleAreas(s);
					
			flag = true;
			
			for(int j=0 ; j<roomSize ; j++)
				for(int k=0 ; k<roomSize ; k++)
					if(sState[j][k]!=gState[j][k])
						flag = false; // flag will = false if there is at least one different index value, mean not the same camera position
			
			if(flag)
				break;
			
			i++;
		}
		
		if(flag)
			return true;
		return false;
	}

	
	//------------------------------------------Local Search-----------------------------------------------//

	/*
	 * Input: number of cameras to be added
	 * Output: the position of each camera in a 2D array with the size roomSize*roomSize
	 * This method will generate a random location for each camera and assign the camera to it if possible (no obstical and valid position)
	 */
	public static int[] creatInitialSolution(int numOfCameras) {//create an initial random solution

		
		boolean flag =true;
		int i=0;
		int[] cameraPosition = new int[numOfCameras*2];
		Random randomGenerator = new Random();
		
		
		while(i<numOfCameras*2) {
			
			int row = randomGenerator.nextInt(roomSize);
			int col = randomGenerator.nextInt(roomSize);
			
			if(!problemInstance[row][col].equals("o")) {
				cameraPosition[i] = row;
				cameraPosition[i+1] = col;
				for(int j=0;j<i;j+=2) {
					if((cameraPosition[j]==cameraPosition[i])&&(cameraPosition[j+1]==cameraPosition[i+1]))
						flag=false;
				}
				
			if(flag) {	
				i+=2;
			}
			
			flag=true;
			}
			
		}		
		return cameraPosition;
	}
	
	/*
	 * Input: solution (room) represented as 2D matrix  
	 * Output: number of cameras and uncovered cells in the room
	 * This method will calculate the number of uncovered cells and it will add to it number of cameras
	 */
	public int objectiveFunction(String[][] solution) {//we want to minimize it 
		
		int sum=0;
		int cameras=0;
		
		for(int i=0; i<roomSize ; i++)
			for(int j=0 ; j<roomSize ; j++) {
				if(solution[i][j].equals("c"))
					cameras++;
				else if(solution[i][j].equals("-"))
					sum++;
			}
		return sum+cameras;//check
	}

	
	//------------------------------------------Hill Climbing-----------------------------------------------//

	/*
	 * Input: stopping criteria (maximum number of iterations) and the number of cameras in the solution 
	 * Output: List of neighbors used to reach the best found solution (usually local optimum)
	 * This method will perform hill climbing local seach metaheuristic 
	 */
	public LinkedList<int[]> hillClimbing(int maxIteration) {

				
		int[] currentSolution = creatInitialSolution(1);//the initial solution will be randomly generated by assigning one camera to a random position
		int currentSolutionFitness = objectiveFunction(visibleAreas(currentSolution));//to be minimized
		int[] candidateSolution ;
		int candidateSolutionFitness;
		LinkedList<int[]> list = new LinkedList<int[]>();

		list.add(currentSolution);
		int i = 0;
		while(i<maxIteration) {
			
			candidateSolution = getBestFirstNeighbor(currentSolution);
			
       		candidateSolutionFitness= objectiveFunction(visibleAreas(candidateSolution));
			System.out.println("candidateSolutionFitness :"+candidateSolutionFitness);

			
			if(candidateSolutionFitness<currentSolutionFitness) {//mimization problem 
					currentSolution = candidateSolution;
					currentSolutionFitness = candidateSolutionFitness;
					list.add(currentSolution);
			}else {
				break;
			}
				i++;		
		}
		
		return list;
	}
	
	/*
	 * Input: the current solution used in hill climbing method, and the camera
	 * Output: the new current solution
	 * This method will generate the neighbos of the current solution, until it finds the neighbor with a higher fitness ( higher number of v cells )
	 * Move: we will add or remove on camera from the current solution
	 */
	public int[] getBestFirstNeighbor(int[] cameraPosition){
		
		int currentFitness = objectiveFunction(visibleAreas(cameraPosition));
		int i = cameraPosition.length/2;
		int[] neighbor2 = new int[cameraPosition.length+2];

		int neighborFitness;
		int counter=0;
		int k=0;
		
		if(cameraPosition.length>2) {//we will remove a camera when we have more than one 
			
			int[] neighbor1 = new int[cameraPosition.length-2];

		//case1 : removing one camera
			while(k<cameraPosition.length) {
				for(int j=0; j<cameraPosition.length ; j+=2) {
					if(k==j)
						continue;
						neighbor1[counter++]=cameraPosition[j];
						neighbor1[counter++]=cameraPosition[j+1];
				}
			
				neighborFitness = objectiveFunction(visibleAreas(neighbor1));
				if(neighborFitness<currentFitness)//minimization
					return neighbor1;
				
				k=k+2;
				counter=0;
			}
		}
		
		//case2: adding one camera
		i=0;
		
		String[][] room = visibleAreas(cameraPosition);
		
		for(int l=0; l<cameraPosition.length ; l++)//copy the location of cameras in current solution
			neighbor2[l] = cameraPosition[l];
		
		for(int j=0; j<roomSize; j++)
			for(int t=0; t<roomSize ; t++) {
				if((!room[j][t].equals("c"))&&(!room[j][t].equals("o"))) {
					neighbor2[cameraPosition.length]=j;//row
					neighbor2[cameraPosition.length+1]=t;//col
				neighborFitness = objectiveFunction(visibleAreas(neighbor2));
				if(neighborFitness<currentFitness)//minimization
					return neighbor2;
				}
			}
		
		return cameraPosition;
						
		
	}
	
	
	//------------------------------------------Simulated Annealing-----------------------------------------------//

	
	/*
	 * Input : maximum number of iteration (one of the stopping criterias)
	 * The method will perform the simulated annealing algorithm and it will keep track of the best found solution
	 * Output: All of the neighbors used to reach the final solution, in addition to the best found solution  
	 */
	public LinkedList<int[]> simulatedAnnealing(int maxIteration) {
		
		double temp= roomSize*roomSize; //starting temperature
		int[] currentSolution = creatInitialSolution(1);//generate initial solution by randomly selecting a location of one camera
		String[][] solution = visibleAreas(currentSolution);
	    int i = 0;
	    int[] candidateSolution = currentSolution;
	    int[] bestSolutionFound = currentSolution;
	    int currentFitness = objectiveFunction(solution); 
	    int BestFitness = currentFitness;
	    int candidateFitness = currentFitness;
	    int innerI = roomSize;//from where should we get these numbers?
	    int j=0;
	    int deltaE = 0;
	    LinkedList<int[]> list = new LinkedList<int[]>();
	    list .add(currentSolution);
	    
		while((temp>0)&&(i<maxIteration)) {

			while (j<innerI) {

			     candidateSolution = randomChange(currentSolution);

			     candidateFitness = objectiveFunction(visibleAreas(candidateSolution));//to be minimized 

			    	    deltaE = candidateFitness-objectiveFunction(visibleAreas(currentSolution));
			    	    if(deltaE<=0) { 
			    	    		currentSolution = candidateSolution;
			    	    		list.add(candidateSolution);
			    	    } else if(probabilityFunction(deltaE , temp)) {
			    	    		currentSolution = candidateSolution;
			    	    		list.add(candidateSolution);
			    	    }
			j++;
			}//end equilibrium state 
			//ref: metaheuristic book
			
			temp = 0.90*temp;
			currentFitness = objectiveFunction(visibleAreas(currentSolution));
			if(currentFitness<BestFitness) {
				bestSolutionFound = currentSolution;
				BestFitness = currentFitness;
			}
			j=0;
			i++;
		}//stopping criteria satisfied 
		
		list.add(bestSolutionFound);
		return list;
	}
	
	/*
	 * Input: the current location of cameras
	 * Output: a new location for the cameras 
	 * This method will change the location of one or more camera (random number within the range [1,numOfCameras]) to a new location 
	 */
	public int[] randomChange(int[] cameraPosition) {
		
		Random randomGenerator = new Random();
		boolean flag=true;
		int randomChange = randomGenerator.nextInt(2);
		
		
			if(randomChange==0) {
			
				if(cameraPosition.length>2) {//we will remove a camera when we have more than one 
					
					int[] neighbor1 = new int[cameraPosition.length-2];

					int k=1 ;
						while(k%2!=0) {
							k = randomGenerator.nextInt(cameraPosition.length);
						}
						int counter=0;
				//case1 : removing one camera
						for(int j=0; j<cameraPosition.length ; j+=2) {
							if(k==j)
								continue;
								neighbor1[counter++]=cameraPosition[j];
								neighbor1[counter++]=cameraPosition[j+1];
						
						}
							return neighbor1;
				}
				
			}//end randomChange ==0 
			
			//case2: adding one camera
			
			int[] neighbor2 = new int[cameraPosition.length+2];
			String[][] room = visibleAreas(cameraPosition);
			
			for(int l=0; l<cameraPosition.length ; l++)//copy the location of cameras in current solution
				neighbor2[l] = cameraPosition[l];
			
			for(int j=0; j<roomSize; j++)
				for(int t=0; t<roomSize ; t++) {
					if((!room[j][t].equals("c"))&&(!room[j][t].equals("o"))) {
						neighbor2[cameraPosition.length]=j;//row
						neighbor2[cameraPosition.length+1]=t;//col
						return neighbor2;
					}
				}

//		int numOfCameras = cameraPosition.length/2;
//		int numOfCamerasToBeChanged = (int) Math.random()*numOfCameras + 1; //number of cameras we are going to change there location 
//		int counter =0;
//			
//		while(counter<numOfCamerasToBeChanged) {
//			
//			int row = (int) Math.random()*numOfCameras;
//			int col = (int) Math.random()*numOfCameras;
//			
//			if(validLocation(row,col)) {
//				cameraPosition[counter] = row;
//				cameraPosition[counter+1] = col;
//				counter+=2;
//			}
//		}
		
		return cameraPosition;

	}
	
	/*
	 * Input: delta e , current temperature 
	 * Output: boolean value indicates if we should accept the non-improving solution
	 */
	public static boolean probabilityFunction(int e, double t) {
		double random = Math.random();
		double probability = Math.exp((-e)/t);
		if(random <= probability)
			return true;
		return false;
	}
	
	
	//------------------------------------------Methods used by all the methods-----------------------------------------------//

	
	/*
	 * Input: the current location of cameras
	 * Output: 2D matrix representing the room.
	 * This method will labled each cell as "v" or "-" of "o" based on the location of the cameras and the obstacles
	 */
	public String[][] visibleAreas(int[] cameraPosition) {
		
		String[][] solution = copyM(problemInstance, roomSize, roomSize);
		
		int[] camera = new int[2];
		int[] cell = new int[2];
		
		for(int n=0 ; n<cameraPosition.length ; n+=2) {
			
			camera[0] = cameraPosition[n];//row
			camera[1] = cameraPosition[n+1];//col
			
			for(int j=0 ; j< roomSize ;j++) { //horizontal view
				if(j==camera[1])
					continue;
				if(!problemInstance[cameraPosition[n]][j].equals("o")) {
					cell[0]=cameraPosition[n];//row
					cell[1]=j;//col
					
					if(noObstacle(camera,cell,2))
						solution[cameraPosition[n]][j] = "v";
				}

			}
			
			for(int j=0 ; j< roomSize ;j++) { //vertical view
				if(j==camera[0])
					continue;
				if(!problemInstance[j][cameraPosition[n+1]].equals("o")) {
					cell[0]=j;
					cell[1]=cameraPosition[n+1];
					
					if(noObstacle(camera,cell,1))
						solution[j][cameraPosition[n+1]] = "v";
				}
			}
			
			//Diagonal
			
			int col=camera[1]-1;
			for(int j=camera[0]+1;j< roomSize;j++){// South and West(L&D)  
			  if((col<0)||(col>=roomSize)) break; 
		     	if(!problemInstance[j][col].equals("o")) {
		     		if(!solution[j][col].equals("c"))
		     			solution[j][col] = "v";
		     	}else break;
		     	col--;
		     	
			
			}

			col=camera[1]+1;
			for(int j=camera[0]-1;j>=0;j--){   // North and East(U&R)   
			 if((col<0)||(col>=roomSize)) break; 
		     	if(!problemInstance[j][col].equals("o")) {
		      	if(!solution[j][col].equals("c"))
		     			solution[j][col] = "v";
		     	}else break;
		     	col++;
		    }
			
			     col=camera[1]+1;
				for(int j=camera[0]+1;j<roomSize;j++){ // South and East(D&R)
				 if((col<0)||(col>=roomSize)) break; 

					if(!problemInstance[j][col].equals("o")) {
						if(!solution[j][col].equals("c"))
							solution[j][col] = "v";
		     	}else break;
		     	col++;
		     	}
				

				col=camera[1]-1;
				for(int j=camera[0]-1;j>=0;j--){ // North and West(U&L)
					if((col<0)||(col>=roomSize)) break; 

					if(!problemInstance[j][col].equals("o")) {
						if(!solution[j][col].equals("c"))
							solution[j][col] = "v";
					}else break;
					col--;
		     	}
			
		}
		
		for(int i=0 ; i<cameraPosition.length; i+=2)
			solution[cameraPosition[i]][cameraPosition[i+1]]="c";
		
		return solution;	
	}
	
	/*
	 * Input: location of the camera, location of the cell, and checkCase indicating if they are on the same row or column 
	 * Output: true if the cell is visible by the camera , false if not 
	 * This method will check if the cell in location loc2 can be seen using camera in location loc1
	 */
	public boolean noObstacle(int[] loc1, int[] loc2 , int checkCase) {// camera location is loc2, the cell we want to checkif it is visible is loc2		
		
		switch(checkCase) {
				//the same column
			case 1: int sRow = Math.min(loc1[0], loc2[0]);//am sure that loc1 and loc2 are different 
					int fRow = Math.max(loc1[0], loc2[0]);
				
				for(int i=sRow ; i<fRow ; i++ ) { 
					if(problemInstance[i][loc1[1]].equals("o"))//this will raturn false of there was an obstacle between the two locations, which means that loc2 is not visible
						return false;
				}
				return true;
				//the same row
			case 2: int sCol = Math.min(loc1[1], loc2[1]);//am sure that loc1 and loc2 are different 
					int fCol = Math.max(loc1[1], loc2[1]);
			
					for(int i=sCol+1; i<fCol ; i++ ) { 
						if(problemInstance[loc1[0]][i].equals("o"))//this will raturn false of there was an obstacle between the two locations, which means that loc2 is not visible
							return false;
					}
					return true;
		}
		return false;
	}
	
	/*
	 * This method will check is the cell indecated by row col is a feasible cell to put the camera in
	 */
	public boolean validLocation(int row, int col) {
		
		if((row<roomSize)&&(row>=0))
			if((col<roomSize)&&(col>=0))
				if(!problemInstance[row][col].equals("o"))
						return true;
		return false;
	}
	
	/*
	 * Input: location of cameras currently in the room, location of the camera we wanna add
	 * It will check if there is a camera in the new location 
	 * output: true->no camera in that location [i][j]
	 */
	public boolean notCamera(int[] s ,int i ,int j) {
	   
	   for(int k=0 ; k<s.length ; k+=2) 
		   if((s[k]==i)&&(s[k+1]==j))
			   return false;
	   return true;
	   
	}
   
    public void copyParent(int[] c , int[]p) {
	   for(int i=0 ; i<p.length ; i++)
		   c[i]=p[i];
    } 

    static void printM(String[][] m, int r, int c) {
    	
    		for (int i=0 ;i<r;i++) {
    			System.out.println("");
    			for(int j=0 ; j<c ;j++)
    				System.out.print(" "+ m[i][j]);
    		}
    }
    
    static String[][] copyM(String[][] m , int r, int c) {
    	
    		String[][] s = new String[r][c];
    		
    			for (int i=0 ;i<r;i++) {
    				for(int j=0 ; j<c ;j++)
    					s[i][j]=m[i][j];
    			}
		
    			return s;
    }
    
    static int[] copyM(int[] m) {
    	
    		int[] s = new int[m.length];
	    	
			for (int i=0 ;i<m.length;i++) {
					s[i]=m[i];
			}
		
		return s;
    }
}

