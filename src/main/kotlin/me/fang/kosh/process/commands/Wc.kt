package me.fang.kosh.process.commands

import me.fang.kosh.process.KoshProcess
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.readText

class Wc(override val args: List<String>) : KoshProcess {
    private var help = false
    private var defaultOutput = true
    private var wc = false
    private var lc = false
    private var cc = false
    private var bc = false
    private var dw = false
    private var fromFile: String? = null
    private var stdinRead = false

    override fun run(stdin: String): String {
        var filenames = args.drop(1).filter { it == "-" || !it.startsWith('-') }

        args.drop(1).filter { it.startsWith('-') && it != "-" }.forEach { arg ->
            run {
                if (arg.startsWith("--files0-from=")) {
                    fromFile = arg.split('=', limit = 1)[1]
                } else if (!arg.startsWith("--")) {
                    defaultOutput = false
                    arg.drop(1).forEach { c ->
                        when (c) {
                            'c' -> bc = true
                            'm' -> cc = true
                            'l' -> lc = true
                            'L' -> dw = true
                            'w' -> wc = true
                            else -> {
                                System.err.println("invalid option: '$c'")
                                return ""
                            }
                        }
                    }
                }

                when (arg) {
                    "--help" -> { help = true; return@forEach }
                    "--bytes" -> { bc = true; defaultOutput = false }
                    "--chars" -> { cc = true; defaultOutput = false }
                    "--lines" -> { lc = true; defaultOutput = false }
                    "--max-line-length" -> { dw = true; defaultOutput = false }
                    "--words" -> { wc = true; defaultOutput = false }
                }
            }
        }

        if (defaultOutput) {
            lc = true
            wc = true
            bc = true
        }

        if (help) {
            val file = this::class.java.getResource("/messages/commands-help/wc.txt")
            return if (file == null) {
                System.err.println("Internal error")
                ""
            } else {
                file.readText()
            }
        }

        if (fromFile != null) {
            filenames = if (fromFile == "-") {
                stdinRead = true
                stdin.split(0.toChar())
            } else {
                Path(fromFile!!).readText().split(0.toChar())
            }
        }

        return filenames.fold("") { acc, name ->
            run {
                val text = if (name == "-") {
                    if (stdinRead) "" else {
                        stdinRead = true
                        stdin
                    }
                } else {
                    val f = Path(name)
                    if (f.isDirectory()) {
                        System.err.println("$name is directory")
                    }
                    f.readText()
                }

                "${acc}\n" +
                    (if (lc) "${getLinesCount(text)} " else "") +
                    (if (wc) "${getWordsCount(text)} " else "") +
                    (if (cc) "${getCharsCount(text)} " else "") +
                    (if (bc) "${getBytesCount(text)} " else "") +
                    (if (dw) "${getDisplayWidth(text)} " else "") +
                    name
            }
        }.drop(1)
    }

    private fun getWordsCount(str: String): Int = str.split(' ').filter { it.isNotEmpty() }.size

    private fun getLinesCount(str: String): Int = str.split('\n').size

    private fun getCharsCount(str: String): Int = str.chars().reduce(0) { acc, _ -> acc + 1 }

    private fun getBytesCount(str: String): Int = str.toByteArray().size

    private fun getDisplayWidth(str: String): Int = str.split('\n').fold(0) { m, s ->
        kotlin.math.max(s.length, m)
    }
}
