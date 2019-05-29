package io.gitlab.arturbosch.detekt.cli.baseline

import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.cli.baselineId
import io.gitlab.arturbosch.detekt.core.exists
import io.gitlab.arturbosch.detekt.core.isFile
import java.nio.file.Files
import java.nio.file.Path

/**
 * @author Artur Bosch
 */
class BaselineFacade(val baselineFile: Path) {

    private val listings: Pair<Whitelist, Blacklist>? =
            if (baselineExists()) {
                val format = BaselineFormat().read(baselineFile)
                format.whitelist to format.blacklist
            } else null

    fun filter(smells: List<Finding>) =
            if (listings != null) {
                val whiteFiltered = smells.filterNot { finding -> listings.first.ids.contains(finding.baselineId) }
                val blackFiltered = whiteFiltered.filterNot { finding -> listings.second.ids.contains(finding.baselineId) }
                blackFiltered
            } else smells

    fun create(smells: List<Finding>) {
        val blacklist = if (baselineExists()) {
            println("TOTOget old blacklist")
            BaselineFormat().read(baselineFile).blacklist
        } else {
            Blacklist(emptySet())
        }

        val whitelist = if (baselineExists()) {
            println("TOTOget old whitelist")
            BaselineFormat().read(baselineFile).whitelist
        } else {
            Whitelist(emptySet())
        }

        println("TOTOWHITE LIST : $whitelist")
        val ids = smells.map { it.baselineId }.toSortedSet()
        println("IDS :  $ids")

        val mergedWhitelist = ids.union(whitelist.ids)

        val smellBaseline = Baseline(blacklist, Whitelist(mergedWhitelist))
        baselineFile.parent?.let {
            println("It exist ? $baselineFile")
            if (!baselineExists()) {
                println("TOTOIt doesnt exist $baselineFile")
                Files.createDirectories(it)
            }
        }
        BaselineFormat().write(smellBaseline, baselineFile)
        println("TOTOSuccessfully wrote smell baseline to $baselineFile")
    }

    private fun baselineExists() = baselineFile.exists() && baselineFile.isFile()
}
