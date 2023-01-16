# RePack
An efficient new texture pack format that ""compiles"" to a normal minecraft texture pack.

## Main features and improvements
- Any file/folder structure works, the ""compiler"" sees all of the repack (.rep) files as one big texture pack
- (TODO) Templates, useful for not having to copypaste everything every time
- Variables, to easily refactor things in the future
- Less copypaste in general, as wildcards (\* and \*?) are supported in item name matches

## Example
RePack fully supports matching custom nbt to textures, such as custom display names. The following example would texture any armor to emerald armor (impractical and a dumb idea, but works as an example i guess?) if the given item name contains "emerald armor" (case insensitive)
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
Note that the equivalent would have to match for every single item separately, for both the item and armor; Here it's as simple as using something like \*\_helmet.

## Getting started
WIP; There is no commandline interface yet, as the main goal at the moment is to make the basics functional.

## Goal
The goal of RePack is to fully support everything that the optifine pack format supports, while making texture packs easily comprehensible, refactorable and expandable. RePack also shows you warnings and errors when ""compiling"" your pack, which might be annoying at times, but better than having to dig through minecraft logs when optifine doesn't let you do stuff (It doesn't even tell you ingame! It just either blacks out textures or refuses to retexture anything).

## Current TODOs
- Custom item models
- Templates
- Commandline interface
- In the future, a GUI interface, to create RePack texture packs graphically (will probably be a separate github repository)
