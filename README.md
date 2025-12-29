# osu logo-animation generator

Small cli tool to generate textures to be used by osu's LogoAnimation shader.

## Usage
```
Usage: ./logo-generator [<options>]

  Generates textures to be used by osu's LogoAnimation shader. The shader
  expects a texture with the red channel containing the progress along each
  path, and the green channel containing the alpha of the image. All other
  channels are ignored.

  Unless configured otherwise the tool expects the following file structure in
  the input directory:
  - ####.png image sequence (0-255)
  - alpha.png

Options:
  -i, --input=<value>   Input directory
  -f, --format=<text>   Format of the input filenames (default: %04d.png)
  -a, --alpha=<value>   Image to be used for the alpha channel (default:
                        <input-dir>/alpha.png)
  -o, --output=<value>  Output file (default: out.png)
  -p, --padding=<int>   Amount of padding to apply to the progress map
                        (default: 10)
  -h, --help            Show this message and exit

```
