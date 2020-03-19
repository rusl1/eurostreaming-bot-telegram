package scraper

import model.Episode
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class Scraper(siteUrl: String) {

    private val EPISODES = siteUrl + "aggiornamento-episodi/"
    private val SEARCH_SHOW = "$siteUrl?s="

    fun getTodayEpisodes(): List<Episode> {
        val document = Jsoup.connect(EPISODES).get()
        val list = mutableListOf<Element>()
        val elements = document.body().selectFirst("div.entry").children()
        var hasH4 = false
        for (e in elements) {
            if (e.`is`("h4")) {
                if (hasH4) break
                else hasH4 = true
            } else if (e.className() == "serieTitle") {
                list.add(e)
            }
        }
        return list.map { toEpisode(it) }
    }

    fun showExists(showName: String): Boolean {
        val document = Jsoup.connect("$SEARCH_SHOW$showName").get()
        val posts = document.body().selectFirst(".recent-posts").children()
        val shows = posts.map { it.selectFirst(".post-content h2 a").ownText().toLowerCase() }.toSet()
        return shows.contains(showName.toLowerCase())
    }

    private fun toEpisode(element: Element): Episode {
        val showName = sanitizeEpisodeTitle(element.ownText())
        val link = element.selectFirst("a")
        val url = link.attr("href")
        val episodeName = link.ownText()
        return Episode(showName, url, episodeName)
    }

    private fun sanitizeEpisodeTitle(title: String) =
        title.substring(0, title.length - 2)
}