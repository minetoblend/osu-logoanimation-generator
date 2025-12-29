import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import korlibs.datastructure.IntArray2
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.RGBA
import korlibs.io.file.VfsFile
import korlibs.io.file.std.cwdVfs
import kotlinx.coroutines.runBlocking

fun Bitmap.toIntArray2(transform: (RGBA) -> Int): IntArray2 {
    return IntArray2.withGen(width, height) { x, y -> transform(getRgba(x, y)) }
}

fun Bitmap.getAlpha(x: Int, y: Int) = getRgba(x, y).a

fun IntArray2.getOr(x: Int, y: Int, default: Int) = if (inside(x, y)) this[x, y] else default

fun RawOption.path(
    hasToExist: Boolean = false,
    canBeFile: Boolean = true,
    canBeDir: Boolean = true,
): NullableOption<VfsFile, VfsFile> {
    return convert { path ->
        val file = cwdVfs[path]

        runBlocking {
            if (hasToExist && !file.exists())
                fail("File not found: $path")

            if (!canBeFile && file.isFile())
                fail("""Cannot be a file: "$path"""")

            if (!canBeDir && file.isDirectory())
                fail("""Cannot be a directory: "$path"""")
        }

        file
    }
}


fun RawOption.file(hasToExist: Boolean = false): NullableOption<VfsFile, VfsFile> =
    path(hasToExist = hasToExist, canBeFile = true, canBeDir = false)

fun RawOption.directory(hasToExist: Boolean = false): NullableOption<VfsFile, VfsFile> =
    path(hasToExist = hasToExist, canBeFile = false, canBeDir = true)