package me.fang.kosh

import me.fang.kosh.exceptions.ExitCalledException
import me.fang.kosh.parser.commands
import me.fang.kosh.process.processPipeline
import me.fang.kosh.process.processSingleCommand

fun main() {
    while (true) {
        val input = readLine() ?: return

        if (input.isBlank()) continue

        val commands = commands.parse(input)

        if (commands == null) {
            System.err.println("Syntax error on '${input[0]}'")
            continue
        }

        if (commands.first != "") {
            System.err.println("Syntax error on '${commands.first[0]}'")
            continue
        }

        try {
            val stdout = if (commands.second.size == 1) {
                try {
                    processSingleCommand(commands.second[0])
                } catch (_: ExitCalledException) {
                    return
                }
            } else {
                processPipeline(commands.second)
            }

            println(stdout)
        } catch (e: Exception) {
            System.err.println(e.message)
        }
    }
}
