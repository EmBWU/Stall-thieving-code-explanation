package net.author


import net.botwithus.api.game.hud.inventories.Backpack
import net.botwithus.api.game.hud.inventories.Bank
import net.botwithus.internal.scripts.ScriptDefinition
import net.botwithus.rs3.game.Client
import net.botwithus.rs3.game.Coordinate
import net.botwithus.rs3.game.movement.Movement
import net.botwithus.rs3.game.movement.NavPath
import net.botwithus.rs3.game.movement.TraverseEvent
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery
import net.botwithus.rs3.game.scene.entities.characters.player.Player
import net.botwithus.rs3.game.scene.entities.`object`.SceneObject
import net.botwithus.rs3.imgui.NativeBoolean
import net.botwithus.rs3.imgui.NativeInteger
import net.botwithus.rs3.script.Execution
import net.botwithus.rs3.script.LoopingScript
import net.botwithus.rs3.script.config.ScriptConfig
import java.util.*

class ExampleScript(
    name: String,
    scriptConfig: ScriptConfig,
    scriptDefinition: ScriptDefinition
) : LoopingScript(name, scriptConfig, scriptDefinition) {


    //Variable Declaration
    val random: Random = Random()
    var botState: BotState = BotState.IDLE
    var bankPreset: NativeInteger = NativeInteger(1)
    var shouldBank: NativeBoolean = NativeBoolean(false)
    // List of stalls to pick from
    var stallsToPick = listOf("Vegetable Stalls", "Bakery Stall")

    // Variable to store the picked stall
    var stallPicked: NativeInteger = NativeInteger(1)

    // Coordinate of the stall position
    var stallPlayerPosition = Coordinate(3210, 3265, 0)

    // Enum class to define the bot states
    enum class BotState {
        IDLE,
        SKILLING,
        BANKING,
    }

    override fun initialize(): Boolean {
        super.initialize()
        // Set the script graphics context to our custom one
        this.sgc = ExampleGraphicsContext(this, console)
        println("Stall Thiever code explanation loaded!")
        return true;
    }


    //This is where the magic happens
    override fun onLoop() {
        // Get the current player
        val player = Client.getLocalPlayer()

        // Check if the game state is logged in and player is not null and bot state is not idle
        if (Client.getGameState() != Client.GameState.LOGGED_IN || player == null || botState == BotState.IDLE) {
            // Delay execution for a random time between 2500ms and 5500ms
            Execution.delay(random.nextLong(2500, 5500))
            return
        }

        // Perform actions based on the current bot state
        when (botState) {
            BotState.SKILLING -> {
                // Delay execution based on the result of handling skilling
                Execution.delay(handleSkilling(player))
                return
            }

            BotState.BANKING -> {
                // Delay execution based on the result of handling banking
                Execution.delay(handleBanking(player))
                return
            }

            else -> {
                // Print a message for unexpected bot state
                println("Unexpected bot state, report to author!")
            }
        }

        // Delay execution for a random time between 2000ms and 4000ms
        Execution.delay(random.nextLong(2000, 4000))
        return
    }

    private fun handleBanking(player: Player): Long {
        // Check if the player is moving or has an animation
        if (player.isMoving || player.animationId != -1) {
            return random.nextLong(1000, 2000)
        }

        // Check if the bank is open
        if (Bank.isOpen()) {
            // Load the bank preset based on the stored value from the interface
            Bank.loadPreset(bankPreset.get())
            Execution.delay(random.nextLong(200, 300))
            botState = BotState.SKILLING
            // Return immediately after loading the preset to avoid opening the bank again
            return random.nextLong(1000, 3000)
        } else {
            // Find the nearest bank chest and interact with it
            val sceneObject: SceneObject? =
                SceneObjectQuery.newQuery().name("Bank chest").option("Use").results().nearest()
            sceneObject?.interact("Use")
            botState = BotState.SKILLING
        }
        return random.nextLong(1000, 3000)
    }
    // Function to handle the skilling activity for the player
    private fun handleSkilling(player: Player): Long {
        // Check if the player is moving or has an animation, if so, return a random delay
        if (player.isMoving || player.animationId != -1)
            return random.nextLong(1000, 2000)

        // Check if the player's backpack is full
        if (Backpack.isFull()) {
            botState = BotState.BANKING
        } else {
            // Check if the player is not at the designated stall position
            if (player.coordinate != stallPlayerPosition) {
                // Resolve the coordinate to the stall position
                val coordinate = NavPath.resolve(stallPlayerPosition)
                val result = Movement.traverse(coordinate)
                // Handle different results of traversing to the stall
                when (result) {
                    TraverseEvent.State.NO_PATH -> println("No path to stall")
                    TraverseEvent.State.FINISHED -> println("Arrived at stall")
                    else -> {
                    };
                }
            } else {
                // Pick the stall based on the stored value
                val stallPicked = stallsToPick[stallPicked.get()]
                val stall = SceneObjectQuery.newQuery().name(stallPicked).option("Steal from").results().nearest()
                // Check if a stall is found
                if (stall != null) {
                    // Interact with the stall, steal from it, and add a delay
                    stall.interact("Steal from")
                    println("Stole from stall")
                    Execution.delay(random.nextLong(500, 1000))
                } else {
                    println("No stall found")
                }
            }
        }
        // Return a random delay
        return random.nextLong(1000, 3000)
    }
}