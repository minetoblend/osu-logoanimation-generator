import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.animation.coroutines.animateInCoroutine
import com.github.ajalt.mordant.animation.progress.advance
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.*
import korlibs.datastructure.IntArray2
import korlibs.datastructure.each
import korlibs.datastructure.fill
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.RGBA
import korlibs.image.format.PNG
import korlibs.image.format.readBitmap
import korlibs.image.format.writeTo
import korlibs.io.async.launch
import korlibs.io.async.toChannel
import korlibs.io.file.std.cwdVfs
import korlibs.io.lang.format
import korlibs.math.geom.SizeInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlin.experimental.ExperimentalNativeApi
import kotlin.math.max
import kotlin.test.fail

fun main(args: Array<String>) {
    runBlocking {
        Application().main(args)
    }
}

class Application : SuspendingCliktCommand("logo-generator") {
    override val printHelpOnEmptyArgs = true

    init {
        context {
            helpFormatter = { MordantHelpFormatter(it, showDefaultValues = true) }
        }
    }


    override fun help(context: Context): String {
        val nl = "\u0085"

        return """
        Generates textures to be used by osu's LogoAnimation shader.
        The shader expects a texture with the red channel containing the progress along each path, and the green channel
         containing the alpha of the image. All other channels are ignored.
        
        Unless configured otherwise the tool expects the following file structure in the input directory:$nl
        - ####.png image sequence (0-255)$nl
        - alpha.png
        """.trimIndent()
    }

    val inputDir by option("-i", "--input", help = "Input directory")
        .directory(hasToExist = true)
        .required()

    val formatString by option("-f", "--format", help = "Format of the input filenames")
        .default("%04d.png")

    val alphaFile by option("-a", "--alpha", help = "Image to be used for the alpha channel")
        .file(hasToExist = true)
        .defaultLazy("<input-dir>/alpha.png") { inputDir["alpha.png"] }

    val outputFile by option("-o", "--output", help = "Output file")
        .file(hasToExist = false)
        .defaultLazy("out.png") { cwdVfs["out.png"] }

    val padding by option("-p", "--padding", help = "Amount of padding to apply to the progress map")
        .int()
        .default(10)

    private val terminal = Terminal(interactive = true)

    override suspend fun run() {
        if (!alphaFile.exists())
            fail("File ${alphaFile.path} not found")

        val alphaMap = alphaFile.readBitmap(PNG).toIntArray2 { color -> color.a }

        val progressMap = IntArray2(alphaMap.width, alphaMap.height, -1)

        for ((index, frame) in readImages().withIndex().toChannel()) {
            if (frame.size != SizeInt(alphaMap.width, alphaMap.height))
                fail("Frame ${index + 1} has different size than alpha map")

            frame.forEach { _, x, y ->
                if (frame.getAlpha(x, y) > 0 && progressMap[x, y] == -1)
                    progressMap[x, y] = index
            }
        }

        progressMap.applyPadding()

        progressMap.fill { if (it == -1) 255 else it }

        val output = Bitmap32(alphaMap.width, alphaMap.height) { x, y ->
            RGBA(progressMap[x, y], alphaMap[x, y], 0, 255)
        }

        output.writeTo(outputFile, PNG)

        terminal.println("Output written to ${outputFile.path}")
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalNativeApi::class)
    private suspend fun readImages(): Flow<Bitmap> {
        val progress = progressBarLayout {
            text("Processing images")
            progressBar(width = 20)
            completed(style = terminal.theme.success)
            timeRemaining()
        }.animateInCoroutine(terminal, total = 256)

        launch(currentCoroutineContext()) { progress.execute() }

        val files = (0..255)
            .map { formatString.format(it) }
            .map { inputDir[it] }
            .onEach {
                if (!it.exists()) fail("File ${it.path} not found")
            }

        return files
            .asFlow()
            .map { it.readBitmap(PNG) }
            .onEach { progress.advance() }
    }

    private fun IntArray2.applyPadding() {
        repeat(padding) {
            val cloned = clone()

            cloned.each { x, y, value ->
                if (value == -1) {
                    this[x, y] = max(
                        max(
                            cloned.getOr(x - 1, y, -1),
                            cloned.getOr(x + 1, y, -1)
                        ),
                        max(
                            cloned.getOr(x, y - 1, -1),
                            cloned.getOr(x, y + 1, -1)
                        ),
                    )
                }
            }
        }
    }
}