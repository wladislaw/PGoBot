/**
 * Pokemon Go Bot  Copyright (C) 2016  PokemonGoBot-authors (see authors.md for more information)
 * This program comes with ABSOLUTELY NO WARRANTY;
 * This is free software, and you are welcome to redistribute it under certain conditions.
 *
 * For more information, refer to the LICENSE file in this repositories root directory
 */

package ink.abb.pogo.scraper.tasks

import Log
import POGOProtos.Networking.Responses.ReleasePokemonResponseOuterClass
import com.pokegoapi.api.pokemon.Pokemon
import ink.abb.pogo.scraper.Bot
import ink.abb.pogo.scraper.Context
import ink.abb.pogo.scraper.Settings
import ink.abb.pogo.scraper.Task
import ink.abb.pogo.scraper.util.pokemon.*

class ReleasePokemon : Task {
    override fun run(bot: Bot, ctx: Context, settings: Settings) {
        if (!settings.shouldAutoTransfer) {
            return
        }
        val groupedPokemon = ctx.api.inventories.pokebank.pokemons.groupBy { it.pokemonId }

        groupedPokemon.forEach {
            val sorted = it.value.sortedByDescending { it.cp }
            for ((index, pokemon) in sorted.withIndex()) {
                // never transfer highest rated Pokemon
                // never transfer > maxCP, unless set in obligatoryTransfer
                // stop releasing when pokemon is set in ignoredPokemon
                if (transferCondition(index, pokemon, settings)) {
                    val candies = CandiesToEvolve.valueOf(pokemon.pokemonId.name)
                    val candiesInJar: Int = try {ctx.api.inventories.candyjar.getCandies(pokemon.pokemonFamily)} catch ( e: NullPointerException) {0}
                    if (settings.tryToEvolve && settings.pokemonToEvolve.contains(pokemon.pokemonId.name)
                            && (candies.candies / candiesInJar) * 100 < settings.minCandiesToEvolve) {
                        Log.yellow("Going to evolve ${pokemon.pokemonId.name} by using ${candies.candies} from $candiesInJar")
                        val evolutionResult = pokemon.evolve()
                        if (evolutionResult.isSuccessful) {
                            Log.green("${pokemon.pokemonId.name} has been evolved to ${evolutionResult.evolvedPokemon.pokemonId.name}. Got ${evolutionResult.candyAwarded} as rewards")
                            val candiesLeftInJar = ctx.api.inventories.candyjar.getCandies(pokemon.pokemonFamily)
                            Log.green("$candiesLeftInJar left in jar")
                        }
                    } else {
                        ctx.pokemonStats.second.andIncrement
                        Log.yellow("Going to transfer ${pokemon.pokemonId.name} with CP ${pokemon.cp} and IV ${pokemon.getIv()}%")
                        val transferResult = pokemon.transferPokemon()
                        val candiesInJar: Int = try {ctx.api.inventories.candyjar.getCandies(pokemon.pokemonFamily)} catch ( e: NullPointerException) {0}
                        Log.yellow("${pokemon.pokemonId.name} has been transferred ${transferResult.name}. Now we have $candiesInJar candies")
                    }
                }
            }
        }
    }

    private fun transferCondition(index: Int, pokemon: Pokemon, settings: Settings) = index > 0
            && (( pokemon.getIvPercentage() < settings.transferIVthreshold && pokemon.cp < settings.maxTransferCP && !pokemon.favorite)
            || settings.obligatoryTransfer.contains(pokemon.pokemonId.name)) &&
            (!settings.ignoredPokemon.contains(pokemon.pokemonId.name))
}
