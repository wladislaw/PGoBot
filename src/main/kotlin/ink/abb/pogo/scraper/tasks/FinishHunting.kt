package ink.abb.pogo.scraper.tasks

import ink.abb.pogo.scraper.Bot
import ink.abb.pogo.scraper.Context
import ink.abb.pogo.scraper.Settings
import ink.abb.pogo.scraper.Task

/**
 * Created by Vlad on 23-Jul-16.
 */
class FinishHunting : Task {
    override fun run(bot: Bot, ctx: Context, settings: Settings) {
        if (ctx.api.inventories.pokebank.pokemons.size == ctx.profile.pokemonStorage && settings.exitNoSpace) {
            Log.red("There is no more space for pokemon in backpack :(. Going to finish walking...")
            System.exit(1)
        } else {
            Log.normal("We still have ${ctx.profile.pokemonStorage - ctx.api.inventories.pokebank.pokemons.size} spots for pokemon to catch")
        }
    }

}