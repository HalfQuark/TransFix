# TransFix
1.12 Spigot Plugin which mimics 1.10 tnt translocation behaviour
2.https://www.spigotmc.org/resources/transfix.90823/

# Pros:
+ Consistent behaviour: Translocation action is synchronous
+ Optimised for better synchronous performance: TNT entities are stored by chunk asynchronously for fast synchronous queries
  Time complexity per piston event: O(bt) = O(t)
    b - Number of blocks moved
    t - Number of TNT entities in a 3x3 Chunk area around the piston
+ Almost all 1.10 guns/redstone mechanisms using translocation should work on TransFix
  It uses the blobk movement to finish the translocation motion instead of teloporting the tnt 2 blocks. This way it does not require timed asynchronous tasks and will remain      consistent with 1.10 behaviour
+ Simple code easily editable to adjust behaviour
# Cons:
+ Doesn't mimic 1.10 behaviour for other entities
+ Doesn't mimic 1.10 behaviour exactly leading to some corner cases; however these are very specific setups and will not affect the majority of guns/redstone mechanisms
  For simplicity and time efficiency no block colision/hitbox checks are performed
  If the tnt isn't aligned in every axis except the movement axis it won't translocate it as if there was a block adjacent to the translocation piston blocking the path
  This does not affect the majority of guns and prevents "spread translocation"
  
# Known bugs:
- Sticky piston when retracting facing an immovable block will not call a BlockPistonRetractEvent and thus will not translocate
- TNT on blocks without hitbox (for example opened fence gate) will not translocate properly as it uses the block's movement to finish the translocation to keep it consistent with 1.10 behaviour
