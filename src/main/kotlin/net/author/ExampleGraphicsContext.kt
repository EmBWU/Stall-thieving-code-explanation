package net.author

import net.botwithus.rs3.imgui.ImGui
import net.botwithus.rs3.script.ScriptConsole
import net.botwithus.rs3.script.ScriptGraphicsContext

class ExampleGraphicsContext(
    private val script: ExampleScript,
    console: ScriptConsole
) : ScriptGraphicsContext (console) {


    override fun drawSettings() {
        // Call the parent drawSettings function
        super.drawSettings()

        // Begin a new ImGui window
        ImGui.Begin("Stall Thiever Code explanation", 0)
        ImGui.SetWindowSize(250f, -1f)

        // Button to start the bot
        if (ImGui.Button("Start")) {
            script.botState = ExampleScript.BotState.SKILLING;
        }

        // Button to stop the bot
        ImGui.SameLine()
        if (ImGui.Button("Stop")) {
            script.botState = ExampleScript.BotState.IDLE
        }

        // Combo box to select stalls
        ImGui.Combo("Stalls", script.stallPicked, *script.stallsToPick.toTypedArray())

        // Input field for bank preset
        script.bankPreset.set(ImGui.InputInt("Bank preset", script.bankPreset.get()))

        // End the ImGui window
        ImGui.End()
    }

    override fun drawOverlay() {
        super.drawOverlay()
    }

}