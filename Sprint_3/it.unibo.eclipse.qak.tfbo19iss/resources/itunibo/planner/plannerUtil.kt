package itunibo.planner

import java.util.ArrayList
import aima.core.agent.Action
import aima.core.search.framework.SearchAgent
import aima.core.search.framework.problem.GoalTest
import aima.core.search.framework.problem.Problem
import aima.core.search.framework.qsearch.GraphSearch
import aima.core.search.uninformed.BreadthFirstSearch
import java.io.PrintWriter
import java.io.FileWriter
import java.io.ObjectOutputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.FileInputStream
import itunibo.planner.model.RobotState
import itunibo.planner.model.Functions
import itunibo.planner.model.RobotState.Direction
import itunibo.planner.model.RobotAction
import itunibo.planner.model.RoomMap
import itunibo.planner.model.Box

object plannerUtil { 
    private var initialState: RobotState? = null
	private var actions: List<Action>?    = null
	private var tempObstacles = ArrayList<Pair<Int,Int>>()
/*
 * ------------------------------------------------
 * PLANNING
 * ------------------------------------------------
 */
    private var search: BreadthFirstSearch? = null
    var goalTest: GoalTest = Functions()		//init
    private var timeStart: Long = 0
	private var stopped = false

    @Throws(Exception::class)
    fun initAI() {
        println("plannerUtil initAI")
        initialState = RobotState(0, 0, RobotState.Direction.DOWN)
        search       = BreadthFirstSearch(GraphSearch())
    }

	fun resetRobotPos(x: Int, y:Int, oldx: Int, oldy: Int, direction: String ){
        //println("plannerUtil resetRobotPos direction=$direction")
		RoomMap.getRoomMap().put(oldx,oldy, Box(false, false, false))	
		RoomMap.getRoomMap().put(x,y, Box(false, false, true) )	

		var dir     = RobotState.Direction.DOWN  //init
		when( direction ){
			"down"   -> dir = RobotState.Direction.DOWN
			"up"     -> dir = RobotState.Direction.UP
			"left"   -> dir = RobotState.Direction.LEFT
			"right"  -> dir = RobotState.Direction.RIGHT			
		}
        initialState = RobotState(x,y, dir)
        var canMove = RoomMap.getRoomMap().canMove( x,y, initialState!!.direction  );
        println("resetRobotPos $x,$y from: ${oldy},${oldy} direction=${getDirection()} canMove=$canMove")
	}
	
	var currentGoalApplicable = true;
	
    fun getActions() : List<Action>{
        return actions!!
    }
 
    @Throws(Exception::class)
    fun doPlan(): List<Action>? {
				
		if( ! currentGoalApplicable ){
			println("plannerUtil doPlan cannot go into an obstacle")
			return null
		} 
		
        val searchAgent: SearchAgent
        //println("plannerUtil doPlan newProblem (A) $goalTest" );
		val problem = Problem(initialState, Functions(), Functions(), goalTest, Functions())
		
		
        //println("plannerUtil doPlan newProblem (A) search " );
        searchAgent = SearchAgent(problem, search!!)
        actions     = searchAgent.actions
		
		println("plannerUtil doPlan actions=$actions")
		
        if (actions == null || actions!!.isEmpty()) {
            println("plannerUtil doPlan NO MOVES !!!!!!!!!!!! $actions!!"   )
            if (!RoomMap.getRoomMap().isClean) RoomMap.getRoomMap().setObstacles()
            //actions = ArrayList()
            return null
        } else if (actions!![0].isNoOp) {
            println("plannerUtil doPlan NoOp")
            return null
        }
		
        //println("plannerUtil doPlan actions=$actions")
        return actions
    }
	
    fun executeMoves( ) {
		if( actions == null ) return
        val iter = actions!!.iterator()
        while (iter.hasNext() && !stopped) {
            plannerUtil.doMove(iter.next().toString())
        }
    }

/*
* ------------------------------------------------
* MAP UPDATE
* ------------------------------------------------
*/
	
    fun getPosX() : Int{ return initialState!!.x }
    fun getPosY() : Int{ return initialState!!.y }
     
    fun doMove(move: String) {
        val dir = initialState!!.direction
        val dimMapx = RoomMap.getRoomMap().dimX
        val dimMapy = RoomMap.getRoomMap().dimY
        val x = initialState!!.x 
        val y = initialState!!.y
       // println("plannerUtil: doMove move=$move  dir=$dir x=$x y=$y dimMapX=$dimMapx dimMapY=$dimMapy")
       try {
            when (move) {
                "w" -> {
                    RoomMap.getRoomMap().put(x, y, Box(false, false, false)) //clean the cell
                    initialState = Functions().result(initialState!!, RobotAction(RobotAction.FORWARD)) as RobotState
                    RoomMap.getRoomMap().put(initialState!!.x, initialState!!.y, Box(false, false, true))
                }
                "s" -> {
                    initialState = Functions().result(initialState!!, RobotAction(RobotAction.BACKWARD)) as RobotState
                    RoomMap.getRoomMap().put(initialState!!.x, initialState!!.y, Box(false, false, true))
                }
                "a"  -> {
                    initialState = Functions().result(initialState!!, RobotAction(RobotAction.TURNLEFT)) as RobotState
                    RoomMap.getRoomMap().put(initialState!!.x, initialState!!.y, Box(false, false, true))
                }
                "l" -> {
                    initialState = Functions().result(initialState!!, RobotAction(RobotAction.TURNLEFT)) as RobotState
                    RoomMap.getRoomMap().put(initialState!!.x, initialState!!.y, Box(false, false, true))
                }
                "d" -> {
                    initialState = Functions().result(initialState!!, RobotAction(RobotAction.TURNRIGHT)) as RobotState
                    RoomMap.getRoomMap().put(initialState!!.x, initialState!!.y, Box(false, false, true))
                }
                "r" -> {
                    initialState = Functions().result(initialState!!, RobotAction(RobotAction.TURNRIGHT)) as RobotState
                    RoomMap.getRoomMap().put(initialState!!.x, initialState!!.y, Box(false, false, true))
                }
                "c"    //forward and  clean
                -> {
                    RoomMap.getRoomMap().put(x, y, Box(false, false, false))
                    initialState = Functions().result(initialState!!, RobotAction(RobotAction.FORWARD)) as RobotState
                    RoomMap.getRoomMap().put(initialState!!.x, initialState!!.y, Box(false, false, true))
                }
				//Box(boolean isObstacle, boolean isDirty, boolean isRobot)
                "rightDir" -> RoomMap.getRoomMap().put(x + 1, y, Box(true, false, false)) 
                "leftDir"  -> RoomMap.getRoomMap().put(x - 1, y, Box(true, false, false))
                "upDir"    -> RoomMap.getRoomMap().put(x, y - 1, Box(true, false, false))
                "downDir"  -> RoomMap.getRoomMap().put(x, y + 1, Box(true, false, false))

		   }//switch
 			//RoomMap.getRoomMap().setObstacles()	
       } catch (e: Exception) {
            println("plannerUtil doMove: ERROR:" + e.message)
        }
    }
     
    fun showMap() {
        println(RoomMap.getRoomMap().toString())
    }
	
    fun saveMap(  fname : String) : Pair<Int,Int> {		
        println("saveMap in $fname")
		val pw = PrintWriter( FileWriter(fname+".txt") )
		pw.print( RoomMap.getRoomMap().toString() )
		pw.close()
		
		val os = ObjectOutputStream( FileOutputStream(fname+".bin") )
		os.writeObject(RoomMap.getRoomMap())
		os.flush()
		os.close()
		return getMapDims()
    }
	
	fun loadRoomMap( fname: String  ) : Pair<Int,Int> {

		try{
 			val inps = ObjectInputStream(FileInputStream("${fname}.bin"))
			val map  = inps.readObject() as RoomMap;
			println("loadRoomMap = $fname DONE")
			RoomMap.setRoomMap( map )
		}catch(e:Exception){			
			println("loadRoomMap = $fname FAILURE")
		}
		return getMapDims()//Pair(dimMapx,dimMapy)
	}
	
	fun getMapDims() : Pair<Int,Int> {
		if( RoomMap.getRoomMap() == null ){
			return Pair(0,0)
		}
	    val dimMapx = RoomMap.getRoomMap().getDimX()
	    val dimMapy = RoomMap.getRoomMap().getDimY()
	    //println("getMapDims dimMapx = $dimMapx, dimMapy=$dimMapy")
		return Pair(dimMapx,dimMapy)	
	}
			
	fun getMap() : String{
		return RoomMap.getRoomMap().toString() 
	}
	fun getMapOneLine() : String{ 
		return  "'"+RoomMap.getRoomMap().toString().replace("\n","@").replace("|","").replace(",","") +"'" 
	}
	
/*
 * ---------------------------------------------------------
 */
    fun setGoalInit() {
        goalTest = Functions()
    }

	fun setGoal( x: String, y: String) {
		setGoal( Integer.parseInt(x), Integer.parseInt(y))
	}	

	//Box(boolean isObstacle, boolean isDirty, boolean isRobot)
    fun setGoal( x: Int, y: Int) {
        try {
            println("setGoal $x,$y while robot in cell: ${getPosX()}, ${getPosY()} direction=${getDirection()}")	
            RoomMap.getRoomMap().put(x, y, Box(false, true, false))
			//initialState = RobotState(getPosX(), getPosY(), initialState!!.direction ) 
            goalTest = GoalTest { state  : Any ->
                val robotState = state as RobotState
				(robotState.x == x && robotState.y == y)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }	

	fun setStopped(stopped: Boolean) {
		this.stopped = stopped
	}
	
	fun getStopped() : Boolean {
		return stopped
	}
	
    fun startTimer() {
        timeStart = System.currentTimeMillis()
    }
	
    fun getDuration() : Int{
        val duration = (System.currentTimeMillis() - timeStart).toInt()
		println("DURATION = $duration")
		return duration
    }
	
	fun getDirection() : String{
		//val direction = initialState!!.direction.toString()
		val direction = initialState!!.direction 
		when( direction ){
			Direction.UP    -> return "upDir"
			Direction.RIGHT -> return "rightDir"
			Direction.LEFT  -> return "leftDir"
			Direction.DOWN  -> return "downDir"
			else            -> return "unknownDir"
 		}
  	}

/*
 * Direction
 */
    fun rotateDirection() {
        //println("before rotateDirection: " + initialState.getDirection() );
        initialState = Functions().result(initialState!!, RobotAction(RobotAction.TURNLEFT)) as RobotState
        initialState = Functions().result(initialState!!, RobotAction(RobotAction.TURNLEFT)) as RobotState
        //println("after  rotateDirection: " + initialState.getDirection() );
        //update the kb
        val x = initialState!!.x
        val y = initialState!!.y
        val newdir = initialState!!.direction.toString().toLowerCase() + "Dir"
    }	
 
    fun setObstacles( ){
		RoomMap.getRoomMap().setObstacles()
 	}
	
	fun addTempObstacle(posX: Int, posY: Int) {
		tempObstacles.add(Pair(posX, posY))
		//set box as obstacle
		RoomMap.getRoomMap().put(posX, posY, Box(true, false, false))
	}
	
	fun clearTempObstacles() {
		println("CLEAR TEMP OBSTACLES")
		for (obs in tempObstacles) {
			RoomMap.getRoomMap().put(obs.first, obs.second, Box(false, false, false))
		}
		tempObstacles.clear()
	}
	
	fun setObstacleWall(  dir: Direction, x:Int, y:Int){
		when( dir ){
			Direction.DOWN  -> RoomMap.getRoomMap().put(x, y + 1, Box(true, false, false))
			//Direction.UP    -> RoomMap.getRoomMap().put(x, y - 1, Box(true, false, false)) 
			//Direction.LEFT  -> RoomMap.getRoomMap().put(x - 1, y, Box(true, false, false)) 
			Direction.RIGHT -> RoomMap.getRoomMap().put(x + 1, y, Box(true, false, false)) 
 		}
	}
	
	fun wallFound(){
 		 val dimMapx = RoomMap.getRoomMap().getDimX()
		 val dimMapy = RoomMap.getRoomMap().getDimY()
		 val dir = initialState!!.getDirection()
		 val x   = initialState!!.getX()
		 val y   = initialState!!.getY()
		 setObstacleWall( dir,x,y )
 		 println("wallFound dir=$dir  x=$x  y=$y dimMapX=$dimMapx dimMapY=$dimMapy");
		 doMove( dir.toString() )  //set cell
 		 if( dir == Direction.UP)    setWallRight(dimMapx,dimMapy,x,y)
		 if( dir == Direction.RIGHT) setWallDown(dimMapx,dimMapy,x,y)  
	}
	
	fun setWallDown(dimMapx: Int,dimMapy: Int,x: Int,y: Int ){
		 var k   = 0
		 while( k < dimMapx ) {
			RoomMap.getRoomMap().put(k, y+1, Box(true, false, false))
			k++
		 }
		
	}
	
	fun setWallRight(dimMapx: Int,dimMapy: Int, x: Int,y: Int){
 		 var k   = 0
		 while( k < dimMapy ) {
			RoomMap.getRoomMap().put(x+1, k, Box(true, false, false))
			k++
		 }
		
	}
}