# RePack
An efficient new texture pack format that ""compiles"" to a normal minecraft texture pack.

## Note
This project is currently in its early stage and thus, cannot *fully* generate a texture pack (as the pack.mcmeta file currently does not automatically generate). The optifine CIT files however, work fine. Please understand that the project needs time to fully function and be usable.

## Main features and improvements
- Any file/folder structure works, the ""compiler"" sees all of the repack (.rep) files as one big texture pack
- (TODO) Templates, useful for not having to copypaste everything every time
- Variables, to easily refactor things in the future
- Less copypaste in general, as wildcards (\* and \*?) are supported in item name matches

## Example
RePack fully supports matching custom nbt to textures, such as custom display names. The following example would give any armor the emerald armor texture,  if the given item name contains "emerald armor" (case insensitive) [of course, only if the given texture files actually exist]
```rs
let filename = "textures/emerald_armor"
let name = "emerald armor"

match {
    display.Name = "ipattern:*$(name)*"
} for {
    items {
        *_helmet     = "$(filename)_helmet"
        *_chestplate = "$(filename)_chestplate"
        *_leggings   = "$(filename)_leggings"
        *_boots      = "$(filename)_boots"
    }
    armor {
        *_helmet
        *_chestplate
        *_leggings
        *_boots
    } = "$(filename)"
}
```
Note that the vanilla optifine pack equivalent would have to match for every single item separately, for both the item and armor; Here it's as simple as using something like \*\_helmet.

## Getting started
### Requirements
To use RePack, you need:
- Java 17
- A working computer
- A keyboard
- A terminal
- At least a few MB of free memory on your disk
- The newest release of this project on that same functioning computer.

Download and unzip the project from the [releases](https://github.com/crxyne/RePack/releases/tag/b0.0.1) into any directory on your computer.

### Compiling the example pack
Open up your terminal in the same directory as the unzipped release you hopefully just downloaded. To see all commands:
```sh
java -jar RePack.jar help
```
Yeah, this is in it's early stages. It'll get easier to use in the future. To compile a workspace, use the 'compile' argument:
```sh
java -jar RePack.jar compile "path-in" "path-out"
```
Applying this to the example pack of this repo:
```sh
java -jar RePack.jar compile "test-workspace" "test-out"
```
And there you have it, a "test-out" folder should be generated, containing the texture pack.

## Creating your own pack
At the given moment, there is no wiki. As soon as I get around to making one, there will be all the info about how this works and how to use it.

## Goal
The goal of RePack is to fully support everything that the optifine pack format supports, while making texture packs easily comprehensible, refactorable and expandable. RePack also shows you warnings and errors when ""compiling"" your pack, which might be annoying at times, but better than having to dig through minecraft logs when your pack has errors (Optifine doesn't even tell you ingame; It just either blacks out textures or refuses to retexture anything).

## Current TODOs
- Custom item models
- Templates
- In the future, a GUI interface, to create RePack texture packs graphically (will probably be a separate github repository)
