package swarm

import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.ImageData
import scala.util.Random

case class Agent(position: (Float, Float), angle: Float)

@JSExport
object ParticlesApp {
  val CANVAS_HEIGHT = 300
  val CANVAS_WIDTH = 300
  val AGENT_COUNT = 300
  val SENSOR_OFFSET_DIST = 2
  val SENSOR_SIZE = 1
  val SIDE_ANGLE = 45
  val TURN_SPEED = 0.9F
  val MOVE_SPEED = 1F

  var iteration = 0
  @JSExport
  def main(canvas: html.Canvas): Unit = {
    val renderer = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    canvas.width = CANVAS_WIDTH
    canvas.height = CANVAS_HEIGHT

    val agents: Array[Agent] = {
      //val AGENT_COUNT = 10
      val agentsSeq: Seq[Agent] = for (_ <- 0 to AGENT_COUNT-1) yield {
        val x = Random.nextInt(canvas.width).toFloat
        val y = Random.nextInt(canvas.height).toFloat
        val angle = Random.nextFloat() * 2 * Math.PI.toFloat
        Agent((x, y), angle)
      }
      Array(agentsSeq:_*)
    }
    val trailMap: ImageData = renderer.getImageData(0, 0, canvas.width, canvas.height)

    def getPixelData(x: Int, y: Int, imageData: ImageData): (Int, Int, Int, Int) = {
      val red = y * (imageData.width * 4) + x * 4
      return (imageData.data(red), imageData.data(red+1), imageData.data(red+2), imageData.data(red+3))
    }

    def setPixelData(pixelData: (Int, Int, Int, Int), x: Int, y: Int, imageData: ImageData): Unit = {
      val red = y * (imageData.width * 4) + x * 4
      imageData.data(red) = pixelData._1
      imageData.data(red+1) = pixelData._2
      imageData.data(red+2) = pixelData._3
      imageData.data(red+3) = pixelData._4
    }

    def drawCfg(): Unit = {
      renderer.putImageData(trailMap, 0, 0)
    }


    def updateAgentWithSteering(a: Agent, deltaTime:Float=1F): Agent = {
      // returns agent
      def steerBasedOnTrail(agent: Agent, deltaTime:Float=1F): Agent = {
        def sense(agent: Agent, sensorAngleOffset: Float): Float = {
          val sensorAngle = agent.angle + sensorAngleOffset
          val sensorDir = (Math.cos(sensorAngle), Math.sin(sensorAngle))
          //val SENSOR_OFFSET_DIST = 2
          val sensorCentre = ((agent.position._1 + sensorDir._1 * SENSOR_OFFSET_DIST).toInt,
            (agent.position._2 + sensorDir._2 * SENSOR_OFFSET_DIST).toInt)
          var sum = 0F

          //val SENSOR_SIZE = 1
          for (
            offsetX <- -SENSOR_SIZE to SENSOR_SIZE;
            offsetY <- -SENSOR_SIZE to SENSOR_SIZE
          ) {

            val pos = (sensorCentre._1 + offsetX, sensorCentre._2 + offsetY)
            if (pos._1 >= 0 && pos._1 < trailMap.width && pos._2 >= 0 && pos._2 < trailMap.height) {
              sum += getPixelData(pos._1, pos._2, trailMap)._4
            }
          }
          sum
        }

//        val SIDE_ANGLE = 45
//        val TURN_SPEED = 0.9F
        val weightForward = sense(agent, 0)
        val weightLeft = sense(agent, SIDE_ANGLE)
        val weightRight = sense(agent, -SIDE_ANGLE)
        val randomSteerStrength = Random.nextFloat()
        //same direction
        val res = if (weightForward > weightLeft && weightForward > weightRight) {
          agent.copy(angle = agent.angle + 0)
        }
        //turn randomly
        else if(weightForward < weightLeft && weightForward < weightRight) {
          agent.copy(angle = agent.angle + (randomSteerStrength - 0.5F) * 2 * TURN_SPEED * deltaTime)
        }
        //turn right
        else if(weightRight > weightLeft) {
          agent.copy(angle = agent.angle - randomSteerStrength * TURN_SPEED * deltaTime)
        }
        //turn left
        else if(weightLeft > weightRight) {
          agent.copy(angle = agent.angle + randomSteerStrength * TURN_SPEED * deltaTime)
        }
        else {
          agent.copy()
        }
        res
      }

      val agent = steerBasedOnTrail(a, deltaTime)
      val direction = (Math.cos(agent.angle).toFloat, Math.sin(agent.angle).toFloat)

      //val deltaTime = 1F
      val multiplier = MOVE_SPEED * deltaTime
      val newPos: (Float, Float) = (agent.position._1 + direction._1 * multiplier, agent.position._2 + direction._2 * multiplier)

      val newAgent = if (newPos._1 < 0 || newPos._1 >= canvas.width || newPos._2 < 0 || newPos._2 >= canvas.height) {
        val newX = Math.min(canvas.width-0.01, Math.max(0, newPos._1)).toFloat
        val newY = Math.min(canvas.height-0.01, Math.max(0, newPos._2)).toFloat
        val newAngle = Random.nextFloat() * 2 * Math.PI.toFloat
        agent.copy(position = (newX, newY), angle = newAngle)
      } else {
        agent.copy(position = newPos)
      }
      setPixelData((0, 0, 0, 255), agent.position._1.toInt, agent.position._2.toInt, trailMap)
      newAgent
    }

    def updateCfg() = {
      def updateAgents(deltaTime:Float=1F): Unit = {
        for (i <- 0 to agents.length-1) {
          //agents(i) = updateAgent(agents(i), deltaTime)
          agents(i) = updateAgentWithSteering(agents(i), deltaTime)
        }
      }

      def diffuseTrailMap(deltaTime:Float=1F): Unit = {
        def lerp(v0: Float, v1: Float, t: Float) = v0 + t * (v1 - v0)

        for (i <- 0 to canvas.width-1;
             j <- 0 to canvas.height-1) {
          val originalPixelData = getPixelData(i, j, trailMap)

          val originalValue = originalPixelData._4

          var sum = 0F
          for {
            offsetX <-  -1 to 1
            offsetY <- -1 to 1
          } {
            val sampleX = i + offsetX
            val sampleY = j + offsetY
            if (sampleX >= 0 && sampleX < trailMap.width && sampleY >= 0 && sampleY < trailMap.height ) {
              sum += getPixelData(sampleX, sampleY, trailMap)._4
            }
          }
          val blurResult = sum / 9
          // if this value > 2 we have chaotic behaviour
          val DIFFUSE_SPEED = 0.1F
          val diffuseValue = lerp(originalValue, blurResult, DIFFUSE_SPEED * deltaTime)
          val EVAPORATION_SPEED = 0.03F
          val evaporated = Math.max(0, diffuseValue - EVAPORATION_SPEED * deltaTime).toInt
          setPixelData(originalPixelData.copy(_4 = evaporated), i, j, trailMap)
        }
      }

      updateAgents()
      diffuseTrailMap()
    }

    def run() = {
      updateCfg()
      drawCfg()
      iteration = iteration + 1
    }

    dom.window.setInterval(run _, 20)
  }
}



