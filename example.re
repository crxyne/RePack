let filename = "black_maid"
let name = "black maid"

match {
    display.Name = "ipattern:*$(name)*"
} for {
    items {
        *_helmet     = "$(filename)_helmet"
        *_chestplate = "$(filename)_chestplate"
        *_leggings   = "$(filename)_leggings"
        *_boots      = "$(filename)_boots"
    }
    armor = "$(filename)_model"

}