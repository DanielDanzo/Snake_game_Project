import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import za.ac.wits.snake.DevelopmentAgent;
import java.util.*;
import java.lang.Math;


//-9 zombie or rather wall
//-10 for apple
//0 for free space
//-i for snake where i = 0,...,numSnakes
//create function to make snake avoid tunnels ie.When there is a snake and an obstacle top and bottom, but check relative to after the head
// so when we add next time we are able top see that we should not go into that tunnel to ensure safety


public class MyAgent extends DevelopmentAgent {
	
	public int step;
	
	static class Coord{
        int Xcoord;
        int Ycoord;
        int Xparent;
        int Yparent;
        double Heuristic;
        
        public Coord(int Xcoord , int Ycoord, double Heuristic){
            this.Xcoord = Xcoord;
            this.Ycoord = Ycoord;
            this.Heuristic = Heuristic;
            this.Xparent = -1;
            this.Yparent = -1;
        }
        
        public void setParent(int Xparent, int Yparent){
            this.Xparent = Xparent;
            this.Yparent = Yparent;
        }
    }
    
    static class priorityQueue{
        ArrayList<Double> Queue;
        ArrayList<Coord> coord;
        ArrayList<Coord> original;
        
        public priorityQueue(){
            this.Queue = new ArrayList<>();
            this.coord = new ArrayList<>();
            this.original = new ArrayList<>();
        }
        
        public void add(double value, int nodeX, int nodeY){
            Queue.add(value);
            Collections.sort(Queue);
            int indexOfHigh = 0;
            for(int i =0; i<Queue.size(); i++){
                double val = Queue.get(i);
                if(val > value){
                    break;
                }
                else if(val == value){
                    indexOfHigh = i;
                }
            }
            this.original.add(indexOfHigh, new Coord(nodeX,nodeY, value));
            coord.add(indexOfHigh, new Coord(nodeX,nodeY,value));
        }
        
        public void add(double value, int nodeX, int nodeY, int parentX, int parentY){
            Queue.add(value);
            Collections.sort(Queue);
            int indexOfHigh = 0;
            for(int i =0; i<Queue.size(); i++){
                double val = Queue.get(i);
                if(val > value){
                    break;
                }
                else if(val == value){
                    indexOfHigh = i;
                }
            }
            Coord tmp = new Coord(nodeX,nodeY,value);
            tmp.setParent(parentX,parentY);
            coord.add(indexOfHigh, tmp);
            this.original.add(indexOfHigh, new Coord(nodeX,nodeY, value));
        }
        
        public int[] peek(){
            int[] value = new int[5];
            value[0] = coord.get(0).Xcoord;
            value[1] = coord.get(0).Ycoord;
            value[2] =  (int)coord.get(0).Heuristic;
            value[3] = coord.get(0).Xparent;
            value[4] = coord.get(0).Yparent;
            coord.remove(0);
            Queue.remove(0);
            return value;
        }
        
        public boolean empty(){
            if(coord.isEmpty()){
                return true;
            }else{
                return false;
            }
        }
    }

    public static void main(String args[]) {
        MyAgent agent = new MyAgent();
        MyAgent.start(agent, args);
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String initString = br.readLine();
            String[] temp = initString.split(" ");
            int nSnakes = Integer.parseInt(temp[0]);
            int numObstacles=3;
            step = 0;
            String movement;

            while (true) {
                String line = br.readLine();
                if (line.contains("Game Over")) {
                    break;
                }
                
                int[][] play = new int[50][50];
                int[][] visited = new int[50][50];
                int[][] stepArray = new int[50][50];
                //this is the value of the snakes head in our play array which will be seen later on
                ArrayList<Integer> SnakeHeads = new ArrayList<>();
                //SnakeHeads.add(-100);
                SnakeHeads.add(-99);
                SnakeHeads.add(-98);
                SnakeHeads.add(-97);
                SnakeHeads.add(-100);
                
         		for (int i = 0; i < 50; i++) {
         			for (int j = 0; j < 50; j++) {
         				play[i][j] = 0;
         				visited[i][j]=0;
         			}
         		}

                int move = 10;
                String apple1 = line;
                //do stuff with apples
                String [] apple = apple1.split(" ");
                int appleX = Integer.parseInt(apple[0]);
                int appleY = Integer.parseInt(apple[1]);
               
         		int myX = 0;
         		int myY = 0;
         		int myBody1x = 0;
         		int myBody1y = 0;

         		//Allocate a number for each obstacle to avoid collision
                for (int j=0; j<numObstacles; j++) {
                	String obsLine = br.readLine();
                	String[] obsBody = obsLine.split(" ");
                	for(String obs : obsBody) {
                		String[] obsCoord = obs.split(",");
                		int obsX = Integer.parseInt(obsCoord[0]);
                        int obsY = Integer.parseInt(obsCoord[1]);
                        play[obsY][obsX] = -10;
                	}
                }
                
                
                int mySnakeNum = Integer.parseInt(br.readLine());
                for (int i = 0; i < nSnakes; i++) {
                    String snakeLine = br.readLine();
                    
                    
                    if (i == mySnakeNum) {
                        //! That's me :)
                    	String[] mySnake = snakeLine.split(" ");
                    	
                    	//see where the head of the body is
                    	String[] myHead = mySnake[3].split(",");
                    	myX = Integer.parseInt(myHead[0]);
                    	myY = Integer.parseInt(myHead[1]);
                    	
                    	//see where our head is looking
                    	String[] myBody = mySnake[4].split(",");
                    	myBody1x = Integer.parseInt(myBody[0]);
                    	myBody1y = Integer.parseInt(myBody[1]);
                    	play = drawSnake(snakeLine, (-1)*(i+1), play);
                    	
                    	String[] extract = snakeLine.split(" ");
                    	if(extract[0].equals("dead")) {
                        	continue;
                        }
                        String[] head = extract[3].split(",");
                        play[Integer.parseInt(head[1])][Integer.parseInt(head[0])] = -100+i;
                        
                    	continue;

                    }
                    //do stuff with other snakes
                    
                    play = drawSnake(snakeLine, (-1)*(i+1), play);
                    
                    String[] extract = snakeLine.split(" ");
                    if(extract[0].equals("dead")) {
                    	continue;
                    }
                    String[] head = extract[3].split(",");
                    play[Integer.parseInt(head[1])][Integer.parseInt(head[0])] = -100 + i;
                }
                
                
                
                // see where the snake is looking
                if(myY == myBody1y && myX > myBody1x) {
                	movement = "Right";
                }else if(myY < myBody1y && myX == myBody1x) {
                	movement = "Up";
                }else if(myY == myBody1y && myX < myBody1x) {
                	movement = "Left";
                }else {
                	movement = "Down";
                }
                
                
                //Variables that tell me whether or not is is safe to move
                //in a specific direction in relation to the head
                //myX is x-coord of the snake and  myY is the y-coord
                boolean isSafeUp = false;
                boolean isSafeDown = false;
                boolean isSafeLeft = false;
                boolean isSafeRight = false;
                if(myY-1>=0 && play[myY-1][myX] == 0) {
                	isSafeUp = true;
                }
                if(myY+1<50 && play[myY+1][myX] == 0) {
                	isSafeDown = true;
                }
                if(myX-1>=0 && play[myY][myX-1] == 0) {
                	isSafeLeft = true;
                }
                if(myX+1<50 && play[myY][myX+1] == 0) {
                	isSafeRight = true;
                }
                
                
                //Now we check the diagonals for any of the snakes head approaching our head
                boolean isSafeUpLeft = true;
                boolean isSafeDownLeft = true;
                boolean isSafeDownRight = true;
                boolean isSafeUpRight = true;
                boolean isSafeDoubleUp = true;
                boolean isSafeDoubleDown = true;
                boolean isSafeDoubleLeft = true;
                boolean isSafeDoubleRight = true;
                
                //checking two blocks ahead of our snake to see whether it is safe or not
                if(myY-2>=0 ) {
                	if(SnakeHeads.contains(play[myY-2][myX])) {
                		isSafeDoubleUp = false;
                	}
                }
                if(myY+2<50) {
                	if(SnakeHeads.contains(play[myY+2][myX])) {
                		isSafeDoubleDown = false;
                	}	
                }
                if(myX+2<50) {
                	if(SnakeHeads.contains(play[myY][myX+2])) {
                		isSafeDoubleRight = false;
                	}
                }
                if(myX-2>=0) {
                	if(SnakeHeads.contains(play[myY][myX-2])) {
                		isSafeDoubleLeft = false;
                	}
                }
                
                
                //checking for diagonals if its safe to move in a specific direction
                if(myY-1>=0 && myX +1<50) {
                	if(SnakeHeads.contains(play[myY-1][myX+1])) {
                		isSafeUpRight =false;
                	}
                }
                if(myY+1<50 && myX -1>=0) {
                	if(SnakeHeads.contains(play[myY+1][myX-1])) {
                		isSafeDownLeft = false;
                	}
                }
                if(myY+1<50 && myX+1<50) {
                	if(SnakeHeads.contains(play[myY+1][myX+1])) {
                		isSafeDownRight = false;
                	}
                }
                if(myY-1>=0 && myX-1>=0) {
                	if(SnakeHeads.contains(play[myY-1][myX-1])) {
                		isSafeUpLeft = false;
                	}
                }
            	
                
                //Astar Algorithm
                //TODO Astar Algorithm
                int x = myX;
                int y = myY;
                priorityQueue dum = new priorityQueue();
                double heuristic = 0;

				//again up with the same process
				if(y-2>=-1 && x-1>=-1 && x+1<=50 && y-1>=0) {
					if((y-2 == -1|| y-2==-2 || !SnakeHeads.contains(play[y-2][x])) && (x+1 == 50 ||!SnakeHeads.contains(play[y-1][x+1])) && (x-1==-1||!SnakeHeads.contains(play[y-1][x-1]))) {
						heuristic = Math.sqrt( ((x-appleX)*(x-appleX)) +(((y-1)-appleY)*((y-1)-appleY)) );
						if(play[y-1][x] == 0 ) {
							dum.add(heuristic, x, y-1, x, y);
							visited[y-1][x] = 1;
							stepArray[y-1][x] = stepArray[y][x]+1;
						}
					}
				}

				//again down with the same process
				if(y+2<=50 && x-1>=-1 && x+1<=50 && y+1<50) {
					if((y+2 == 50 || y+2 == 51 ||!SnakeHeads.contains(play[y+2][x])) && (x-1 == -1 ||!SnakeHeads.contains(play[y+1][x-1])) && (x+1==50 ||!SnakeHeads.contains(play[y+1][x+1]))) {
						heuristic = Math.sqrt( ((x-appleX)*(x-appleX)) +(((y+1)-appleY)*((y+1)-appleY)) );
						if(play[y+1][x] == 0 ) {
							dum.add(heuristic, x, y+1, x, y);
							visited[y+1][x] = 1;
							stepArray[y+1][x] = stepArray[y][x]+1;
						}
					}
				}

				//check left and if the surounding spaces are safe else move on
				if(x-2 >=-1 && y+1<=50 && y-1>=0 && x-1>=0) {
					if((x-2==-1 || y-2==-2 ||!SnakeHeads.contains(play[y][x-2])) && (y+1==50||!SnakeHeads.contains(play[y+1][x-1])) && (y-1==-1||!SnakeHeads.contains(play[y-1][x-1]))) {
						heuristic = Math.sqrt( (((x-1)-appleX)*((x-1)-appleX)) +((y-appleY)*(y-appleY)) );
						if(play[y][x-1] == 0) {
							dum.add(heuristic, x-1, y, x, y);
							visited[y][x-1] = 1;
							stepArray[y][x-1] = stepArray[y][x]+1;
						}
					}
				}

				//again right with the same process
				if(x+2<=50 && y-1>=-1 && y+1<=50 && x+1<50) {
					if((x+2 == 50 || x+2 ==51|| !SnakeHeads.contains(play[y][x+2])) && (y+1==50||!SnakeHeads.contains(play[y+1][x+1])) && (y-1 == -1 ||!SnakeHeads.contains(play[y-1][x+1]))) {
						heuristic = Math.sqrt( (((x+1)-appleX)*((x+1)-appleX)) +((y-appleY)*(y-appleY)) );
						if(play[y][x+1] == 0 ) {
							dum.add(heuristic, x+1, y, x, y);
							visited[y][x+1] = 1;
							stepArray[y][x+1] = stepArray[y][x]+1;
						}
					}
				}


                step = step+1;;
                
                //TODO: Path
                while(!dum.empty())  {
                	int[] currentArray = dum.peek();
                	int lowestStep;
                	x = currentArray[0];
                	y = currentArray[1];
                	if(appleX == x && appleY == y) {
                		//move = 5;
                		break;
                	}
                	//check its neighbours
                	//check Up
                	if(y-1>=0) {
                		if(play[y-1][x] == 0 && !(visited[y-1][x] == 1)  && (x+1==50  || (!play[y-1][x+1]<50 || play[y-1][x+1]!=(-1)*num)) && (x-1==-1  || (!play[y-1][x-1]<50 || play[y-1][x-1]!=(-1)*num)) ) {
                			heuristic = Math.sqrt( ((x-appleX)*(x-appleX)) +(((y-1)-appleY)*((y-1)-appleY)) );
                			dum.add(heuristic, x, y-1, x, y);
                			visited[y-1][x] = 1;
                			
                			lowestStep = stepArray[y][x]+1;
                			//Right of next append
                			if(x+1<50) {
                				if(stepArray[y-1][x+1] < lowestStep-1 && stepArray[y-1][x+1]!= 0) {
                					lowestStep = stepArray[y][x+1]+1;
                				}
                			}
                			//Below next append
                			if(y+1<50) {
                				if(stepArray[y][x] < lowestStep-1 && stepArray[y][x]!= 0) {
                					lowestStep = stepArray[y][x]+1;
                				}
                			}
                			//Left of next append
                			if(x-1>=0) {
                				if(stepArray[y-1][x-1] < lowestStep && stepArray[y-1][x-1]!= 0) {
                					lowestStep = stepArray[y-1][x-1]+1;
                				}
                			}
                			//Above next append
                			if(y-2>=0) {
                				if(stepArray[y-2][x] < lowestStep && stepArray[y-2][x]!= 0) {
                					lowestStep = stepArray[y-2][x]+1;
                				}
                			}
                			stepArray[y-1][x] = lowestStep;
                		}
                	}
                	//Check Down
                	if(y+1 <50) {
                		if(play[y+1][x] == 0 && !(visited[y+1][x] == 1) && (x+1==50  || (!play[y+1][x+1]<50 || play[y+1][x+1]!=(-1)*num)) && (x-1==-1  || (!play[y+1][x-1]<50 || play[y+1][x-1]!=(-1)*num)) ) {
                			heuristic = Math.sqrt( ((x-appleX)*(x-appleX)) +(((y+1)-appleY)*((y+1)-appleY)) );
                			dum.add(heuristic, x, y+1, x, y);
                			visited[y+1][x] = 1;
                			lowestStep = stepArray[y][x]+1;
                			
                			//Right of next append
                			if(x+1<50 ) {
                				if(stepArray[y+1][x+1] < lowestStep-1 && stepArray[y+1][x+1]!= 0) {
                					lowestStep = stepArray[y+1][x+1]+1;
                				}
                			}
                			//Below next append
                			if(y+2<50) {
                				if(stepArray[y+2][x] < lowestStep-1 && stepArray[y+2][x]!= 0) {
                					lowestStep = stepArray[y+2][x]+1;
                				}
                			}
                			//Left of next append
                			if(x-1>=0) {
                				if(stepArray[y+1][x-1] < lowestStep && stepArray[y+1][x-1]!= 0) {
                					lowestStep = stepArray[y+1][x-1]+1;
                				}
                			}
                			//Above of next append
                			if(y>=0 && x>=0) {
                				if(stepArray[y][x] < lowestStep && stepArray[y][x]!= 0) {
                					lowestStep = stepArray[y][x]+1;
                				}
                			}
                			stepArray[y+1][x] = lowestStep;
                		}
                		
                	}
                	//Check Left
                	if(x-1>=0) {
                		if(play[y][x-1] == 0 && !(visited[y][x-1] == 1)  && (y+1==50 || (!(play[y+1][x-1]<50) || play[y+1][x-1]!=(-1)*num)) && (y-1==-1 || (!(play[y-1][x-1]<50) || play[y-1][x-1]!=(-1)*num)) ) {
                			heuristic = Math.sqrt( (((x-1)-appleX)*((x-1)-appleX)) +((y-appleY)*(y-appleY)) );
                			dum.add(heuristic, x-1, y, x, y);
                			visited[y][x-1] = 1;
                			
                			lowestStep = stepArray[y][x]+1;
                			//Right of next append
                			if(x<50 && x>=0 && y>=0 && y<50) {
                				if(stepArray[y][x] < lowestStep-1 && stepArray[y][x]!= 0) {
                					lowestStep = stepArray[y][x]+1;
                				}
                			}
                			//Below next append
                			if(y+1<50) {
                				if(stepArray[y+1][x-1] < lowestStep-1 && stepArray[y+1][x-1]!= 0) {
                					lowestStep = stepArray[y+1][x]+1;
                				}
                			}
                			//Left of next append
                			if(x-2>=0) {
                				if(stepArray[y][x-2] < lowestStep && stepArray[y][x-2]!= 0) {
                					lowestStep = stepArray[y][x-2]+1;
                				}
                			}
                			//Above next append
                			if(y-1>=0) {
                				if(stepArray[y-1][x-1] < lowestStep && stepArray[y-1][x-1]!= 0) {
                					lowestStep = stepArray[y-1][x-1]+1;
                				}
                			}
                			stepArray[y][x-1] = lowestStep;
                		}
                	}
                	//Check Right
                	if(x+1<50) {
                		if(play[y][x+1] == 0 && !(visited[y][x+1] == 1)  && (y+1==50 || (!(play[y+1][x+1]<50) || play[y+1][x+1]!=(-1)*num)) && (y-1==-1 || (!(play[y-1][x+1]<50) || play[y-1][x+1]!=(-1)*num)) ) {
                			heuristic = Math.sqrt( (((x+1)-appleX)*((x+1)-appleX)) +((y-appleY)*(y-appleY)) );
                			dum.add(heuristic, x+1, y, x, y);
                			visited[y][x+1] = 1; 
                			
                			lowestStep = stepArray[y][x]+1;
                			//Right of next append
                			if(x+2<50 ) {
                				if(stepArray[y][x+2] < lowestStep-1 && stepArray[y][x+2]!= 0) {
                					lowestStep = stepArray[y][x+2]+1;
                				}
                			}
                			//Below next append
                			if(y+1<50) {
                				if(stepArray[y+1][x+1] < lowestStep-1 && stepArray[y+1][x+1]!= 0) {
                					lowestStep = stepArray[y+1][x+1]+1;
                				}
                			}
                			//Left of next append
                			if(x-1>=0) {
                				if(stepArray[y][x] < lowestStep && stepArray[y][x]!= 0) {
                					lowestStep = stepArray[y][x]+1;
                				}
                			}
                			//Above next append
                			if(y-1>=0) {
                				if(stepArray[y-1][x+1] < lowestStep && stepArray[y-1][x+1]!= 0) {
                					lowestStep = stepArray[y-1][x+1]+1;
                				}
                			}
                			stepArray[y][x+1] = lowestStep;
                		}
                	}
                	step++;
                	
                }
                
                //TODO: next best move
                
                
                if(dum.empty() ) {
                	move = safeMove(myX, myY, appleX, appleY, isSafeUp, isSafeDown, isSafeLeft, isSafeRight, isSafeDoubleUp, isSafeDoubleDown, isSafeDoubleLeft, isSafeDoubleRight, isSafeUpLeft, isSafeUpRight, isSafeDownLeft, isSafeDownRight, movement);
                }else {
                	//TODO: backtrack to find the shortest path
                	//using stepArray
                	int xStep = appleX;
                	int yStep = appleY;
					for(int i = step-1; i>0;i--) {
						//check surounding for the block with the previous step
						if(yStep<50 && xStep<50 && stepArray[yStep][xStep] == 1) {
							break;
						}
						//check right
						if(xStep+1<50 && (stepArray[yStep][xStep+1] == (stepArray[yStep][xStep]-1))) {
							xStep = xStep+1;
						}
						//Left
						else if(xStep-1>=0 && (stepArray[yStep][xStep-1] == (stepArray[yStep][xStep]-1))) {
							xStep = xStep-1;
						}
						//Down
						else if(yStep+1<50 && (stepArray[yStep+1][xStep] == (stepArray[yStep][xStep]-1))) {
							yStep = yStep+1;
						}
						//Up
						else {
							yStep = yStep-1;
						}
					}
					//move up if up
					if(xStep == myX && yStep <myY) {
						move = 0;
					}
					//move down if down
					else if(xStep == myX && yStep >myY) {
						move = 1;
					}
					//move Left if Left
					else if(yStep == myY && xStep<myX) {
						move = 2;
					}
					else {
						move =3;
					}
                }
                
                //finished reading, calculate move:
                //move = new Random().nextInt(4);
                System.out.println(move);
                //System.out.println(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static int[][] drawSnake(String snake, int snakenum, int[][] play) {
 		String[] snake2 = snake.split(" ");
 		for(int i = 4; i<snake2.length;i++) {
 			if(i+1 == snake.length()) {
 				break; 
			  }
 			drawLine(play, snake2[i-1], snake2[i], snakenum);
 		} 
		return play;
}
 

    public static int[][] drawLine(int[][] play, String str1, String str2, int snakenum) {
    	String[] inputStr1 = str1.split(",");
 		String[] inputStr2 = str2.split(",");
 		 
		int str1x =Integer.parseInt(inputStr1[0]);
        int str1y =Integer.parseInt(inputStr1[1]);
 		
 	   	int str2x =Integer.parseInt(inputStr2[0]);
 	   	int str2y =Integer.parseInt(inputStr2[1]);
 		
 		play[str1y][str1x]= snakenum;
 		
 		if(str1x == str2x) {
 			if(str1y<str2y) {
 				for (int i = 0; i < (str2y - str1y +1); i++) {
 					play[str1y+i][str1x] = snakenum;
 				}
 			}else {
 				for (int i = 0; i < (str1y - str2y +1); i++) {
 					play[str1y-i][str1x] = snakenum;
 				}
 			}
 			
 		}else {
 			if(str1x<str2x) {
 				for (int i = 0; i < (str2x - str1x +1); i++) {
 					play[str1y][str1x+i] = snakenum;
 				}
 			}else {
 				for (int i = 0; i <(str1x - str2x +1); i++) {
 					play[str1y][str1x-i] = snakenum;
 				}
 			}
 		}
 		return play;
 		
 		
}

    public boolean isTunnel(String direction,int x , int y,int[][] play) {
    	if(direction.equals("Up")) {
    		if(x-1>=0 && x+1<50 && y-1>=0) {
    			if(play[y][x-1]<0 && play[y][x+1]<0 && play[y-1][x-1]<0 && play[y-1][x+1]<0) {
    				return true;
    			}
    		}
    	}else if(direction.equals("Down")) {
    		if(x-1>=0 && x+1<50 && y+1<50) {
    			if(play[y][x-1]<0 && play[y][x+1]<0 && play[y+1][x-1]<0 && play[y+1][x+1]<0) {
    				return true;
    			}
    		}
    	}else if(direction.equals("Left")) {
    		if(x-1>=0 && y+1<50 && y-1>=0) {
    			if(play[y+1][x-1]<0 && play[y-1][x-1]<0 && play[y-1][x]<0 && play[y+1][x]<0) {
    				return true;
    			}
    		}
    	}else {
    		if(x+1<50 && y+1<50 && y-1>=0) {
    			if(play[y-1][x+1]<0 && play[y+1][x+1]<0 && play[y-1][x]<0 && play[y+1][x]<0) {
    				return true;
    			}
    		}
    	}
		return false;
	}
    
    public int safeMove(int myX,int myY,int appleX,int appleY, boolean isSafeUp,boolean isSafeDown,boolean isSafeLeft, boolean isSafeRight,boolean isSafeDoubleUp, boolean isSafeDoubleDown, boolean isSafeDoubleLeft, boolean isSafeDoubleRight, boolean isSafeUpLeft,boolean isSafeUpRight, boolean isSafeDownLeft, boolean isSafeDownRight, String movement) {
    	int move =0;
    	//Three cases arise when we want to see vertically when the apple is 
        //This is in relation to our snakes head
        //CASE 1: Apple is Below us
        if(myY < appleY) {
        	//apple is below snake
        	
        	if(myX < appleX) {
        		//apple is on the bottom right
        		if(movement.equals("Up")) {
        			if(isSafeRight && isSafeUpRight  && isSafeDoubleRight) {
        				move = 3;	
        			}else if(isSafeUp && isSafeDoubleUp && isSafeUpLeft) {
        				move = 5;	
        			}else {
        				move = 2;
        			}	
        		}else if(movement.equals("Down")) {
        			if(isSafeDown && isSafeDoubleDown && isSafeDownLeft) {
        				move = 5;
        			}else if(isSafeRight && isSafeDoubleRight && isSafeUpRight ) {
        				move = 3;
        			}else {
        				move = 2;
        			}
        		}else if(movement.equals("Left")) {
        			if(isSafeDown && isSafeDoubleDown && isSafeDownLeft) {
        				move = 1;
        			}else if(isSafeLeft && isSafeDoubleLeft && isSafeUpLeft) {
        				move = 5;
        			}else {
        				move = 0;
        			}	
        		}else {
        			if(isSafeDown && isSafeDownLeft && isSafeDoubleDown) {
        				move = 1;	
        			}else if(isSafeRight && isSafeUpRight && isSafeDoubleRight) {
        				move = 5;
        			}else {
        				move = 0;
        			}	
        		}
        		
        	}else if(myX == appleX) {
        		//apple is directly below us
        		if(movement.equals("Up")) {
        			if(isSafeLeft && isSafeDoubleLeft&& isSafeUpLeft) {
        				move = 2;
        			}else if(isSafeUp && isSafeDoubleUp && isSafeUpLeft && isSafeUpRight) {
        				move = 5;
        			}else {
        				move = 3;
        			}	
        		}else if(movement.equals("Down")) {
        			if(isSafeDown && isSafeDoubleDown && isSafeDownRight && isSafeDownLeft) {
        				move = 5;
        			}else if (isSafeLeft && isSafeDoubleLeft && isSafeUpLeft) {
        				move = 2;
        			}else {
        				move = 3;
        			}
        			
        		}else if(movement.equals("Left")) {
        			if(isSafeDown && isSafeDoubleDown && isSafeDownLeft && isSafeDownRight) {
        				move = 1;	
        			}else if(isSafeLeft && isSafeDoubleLeft && isSafeUpLeft) {
        				move = 5;	
        			}else {
        				move = 0;
        			}
        		}else {
        			if(isSafeDown && isSafeDownLeft && isSafeDownRight) {
        				move = 1;
        			}else if(isSafeRight && isSafeDoubleRight && isSafeUpRight) {
        				move = 5;
        			}else {
        				move = 0;
        			}
        			
        		}
        		
        	}else {
        		//apple is on the bottom left
        		if(movement.equals("Up")) {
        			if(isSafeLeft && isSafeDoubleLeft && isSafeUpLeft) {
        				move = 2;
        			}else if(isSafeUp && isSafeDoubleUp && isSafeUpRight) {
        				move = 5;	
        			}else {
        				move = 3;
        			}
        			
        		}else if(movement.equals("Down")) {
        			if(isSafeDown && isSafeDoubleDown && isSafeDownRight) {
        				move = 5;
        			}else if(isSafeLeft  && isSafeDoubleLeft && isSafeUpLeft) {
        				move = 2;
        			}else {
        				move = 3;
        			}
        			
        		}else if(movement.equals("Left")) {
        			if(isSafeDown && isSafeDownRight) {
        				move = 1;
        			}else if(isSafeLeft && isSafeDoubleLeft&& isSafeUpLeft) {
        				move = 5;
        			}else {
        				move = 0;
        			}
        			
        		}else {
        			if(isSafeDown && isSafeDownRight) {
        				move = 1;
        			}else if (isSafeRight && isSafeDoubleRight && isSafeUpRight) {
        				move = 5;
        			}else {
        				move = 0;
        			}
        		}
        		
        	}
        }
        
        //CASE 2: Apple is in the same Vertical row as our snake
        else if(myY == appleY){
        	if(myX < appleX) {
        		//Apple is at the Right of Snake
        		if(movement.equals("Up")) {
        			if(isSafeRight && isSafeDownRight & isSafeUpRight && isSafeDoubleRight) {
        				move = 3;
        			}else if(isSafeUp && isSafeDoubleUp && isSafeUpLeft) {
        				move = 5;
        			}else {
        				move = 2;
        			}
        		}else if(movement.equals("Down")) {
        			if(isSafeRight && isSafeDownRight && isSafeUpRight && isSafeDoubleRight) {
        				move = 3;
        			}else if(isSafeDown && isSafeDoubleDown && isSafeDownLeft) {
        				move = 5;
        			}else {
        				move = 2;
        			}
        			
        		}else if(movement.equals("Left")) {
        			if(isSafeDown && isSafeDownLeft && isSafeDownRight) {
        				move = 1;
        			}else if(isSafeLeft && isSafeDoubleLeft && isSafeUpLeft && isSafeDownLeft) {
        				move = 5;
        			}else {
        				move = 0;
        			}
        		}else {
        			if(isSafeRight && isSafeDoubleRight && isSafeUpRight && isSafeDownRight) {
        				move = 5;
        			}else if(isSafeDown && isSafeDownLeft && isSafeDoubleDown) {
        				move = 1;
        			}else {
        				move = 0;
        			}
        		}
        	}
        	else if(myX > appleX) {
        		//Apple is to the Left of snake
        		if(movement.equals("Up")) {
        			if(isSafeLeft && isSafeUpLeft && isSafeDownLeft && isSafeDoubleLeft) {
        				move = 2;
        			}else if(isSafeUp && isSafeDoubleUp && isSafeUpRight) {
        				move = 5;
        			}else {
        				move = 3;
        			}
        		}else if(movement.equals("Down")) {
        			if(isSafeLeft && isSafeDownLeft && isSafeUpLeft && isSafeDoubleLeft) {
        				move = 2;
        			}else if(isSafeDown && isSafeDoubleDown && isSafeDownRight) {
        				move = 5;
        			}else {
        				move = 3;
        			}	
        		}else if(movement.equals("Left")) {
        			if(isSafeLeft && isSafeDoubleLeft && isSafeUpLeft && isSafeDownLeft) {
        				move = 5;
        			}else if(isSafeDown && isSafeDownRight && isSafeDoubleDown) {
        				move = 1;
        			}else {
        				move = 0;
        			}
        		}else {
        			if(isSafeUp && isSafeDoubleUp &&  isSafeUpRight) {
        				move = 0;
        			}else if(isSafeRight && isSafeDoubleRight && isSafeUp && isSafeDownRight) {
        				move = 5;
        			}else {
        				move = 1;
        			}
        		}
        	}else {
        		move = 5;
        	}	
        }
        
        //CASE 3: Apple is above our snake
        else {
        	//apple is to the top Left
        	if(myX > appleX) {
        		if(movement.equals("Up")) {
        			if(isSafeUp && isSafeDoubleUp && isSafeUpRight) {
        				move = 5;
        			}else if(isSafeLeft && isSafeDownLeft) {
        				move = 2;
        			}else {
        				move = 3;
        			}	
        		}else if(movement.equals("Down")) {
        			if(isSafeLeft && isSafeDownLeft && isSafeUpLeft ) {
        				move = 2;
        			}else if(isSafeDown && isSafeDoubleDown && isSafeDownRight) {
        				move = 5;
        			}else {
        				move = 3;
        			}
        		}else if(movement.equals("Left")) {
        			if(isSafeUp && isSafeDoubleUp && isSafeUpRight) {
        				move = 0;
        			}else if(isSafeLeft && isSafeDoubleLeft && isSafeDownLeft) {
        				move = 5;
        			}else {
        				move = 1;
        			}
        		}else {
        			if(isSafeUp && isSafeUpRight && isSafeDoubleUp) {
        				move = 0;
        			}else if(isSafeRight && isSafeDoubleRight && isSafeDownRight ) {
        				move = 5;
        			}else {
        				move = 1;
        			}
        		}
        	}
        	//apple is directly above us 
        	else if(myX == appleX) {
        		if(movement.equals("Up")) {
        			if(isSafeUp && isSafeDoubleUp && isSafeUpLeft && isSafeUpRight) {
        				move = 5;
        			}else if(isSafeLeft && isSafeDownLeft && isSafeDoubleLeft) {
        				move = 2;
        			}else {
        				move = 3;
        			}
        			
        		}else if(movement.equals("Down")) {
        			if(isSafeLeft && isSafeDownLeft && isSafeUpLeft ) {
        				move = 2;
        			}else if(isSafeDown && isSafeDoubleDown && isSafeDownLeft && isSafeDownRight) {
        				move = 5;
        			}else {
        				move = 3;
        			}
        			
        		}else if(movement.equals("Left")) {
        			if(isSafeUp && isSafeUpLeft && isSafeUpRight && isSafeDoubleUp) {
        				move = 0;
        			}else if(isSafeLeft && isSafeDoubleLeft && isSafeDownLeft) {
        				move = 5;
        			}else {
        				move = 1;
        			}
        		}else {
        			if(isSafeUp && isSafeUpRight && isSafeUpLeft && isSafeDoubleUp) {
        				move = 0;
        			}else if(isSafeRight && isSafeDoubleRight && isSafeDownRight) {
        				move = 5;
        			}else {
        				move = 1;
        			}
        		}
        	}
        	//apple is to the top Right
        	else {
        		if(movement.equals("Up")) {
        			if(isSafeUp && isSafeDoubleUp && isSafeUpLeft) {
        				move = 5;
        			}else if(isSafeRight && isSafeDoubleRight && isSafeDownRight) {
        				move = 3;
        			}else {
        				move = 2;
        			}
        		}else if(movement.equals("Down")) {
        			if(isSafeRight && isSafeUpRight && isSafeDownRight ) {
        				move = 3;
        			}else if(isSafeDown && isSafeDoubleDown && isSafeDownLeft) {
        				move = 5;
        			}else {
        				move = 2;
        			}
        			
        		}else if(movement.equals("Left")) {
        			if(isSafeUp && isSafeUpLeft && isSafeDoubleUp ) {
        				move = 0;
        			}else if(isSafeLeft && isSafeDoubleLeft && isSafeDownLeft) {
        				move = 5;
        			}else {
        				move = 1;
        			}
        		}else {
        			if(isSafeUp && isSafeDoubleUp && isSafeUpLeft ) {
        				move = 0;
        			}else if(isSafeRight && isSafeDoubleRight && isSafeDownRight) {
        				move = 5;
        			}else {
        				move = 1;
        			}
        		}
        	}
        }
    	return move;
    }
}