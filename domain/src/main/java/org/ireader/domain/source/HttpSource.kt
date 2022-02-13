package org.ireader.domain.source

import android.webkit.WebView
import okhttp3.*
import org.ireader.core.utils.call
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.source.*
import org.ireader.domain.utils.asJsoup
import org.ireader.infinity.core.data.network.models.*
import org.jsoup.nodes.Document
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.URI
import java.net.URISyntaxException
import java.security.MessageDigest
import java.util.*

/**
 * A simple implementation for sources from a website.
 */
abstract class HttpSource : Source, KoinComponent {


    protected val network: NetworkHelper by inject<NetworkHelper>()


    /**
     * Base url of the website without the trailing slash, like: http://mysite.com
     */
    abstract override val baseUrl: String

    /**
     * Default network client for doing requests.
     */
    open val client: OkHttpClient
        get() = network.client

    val webView: WebView by inject<WebView>()


    /**
     * Headers used for requests.
     */
    override val headers: Headers by lazy { headersBuilder().build() }

    /**
     * Id of the source. By default it uses a generated id using the first 16 characters (64 bits)
     * of the MD5 of the string: source name/language/versionId
     * Note the generated id sets the sign bit to 0.
     */
    override val sourceId by lazy {
        val key = "${name.lowercase()}/$lang/$versionId"
        val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
        (0..7).map { bytes[it].toLong() and 0xff shl 8 * (7 - it) }
            .reduce(Long::or) and Long.MAX_VALUE
    }


    /**
     * Version id used to generate the source id. If the site completely changes and urls are
     * incompatible, you may increase this value and it'll be considered as a new source.
     */
    open val versionId = 1

    /**
     * Visible name of the source.
     */
    override fun toString() = "$name (${lang.uppercase()})"

    val pageFormat: String = "{page}"
    val searchQueryFormat: String = "{query}"
    /****************************************************************************************************/

    /**
     *return the end point for the fetch latest books updates feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     */
    abstract val fetchLatestEndpoint: String?

    /**
     *return the end point for the  fetch Popular books feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     */
    abstract val fetchPopularEndpoint: String?

    /**
     *return the end point for the fetch Search feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     * note: use "{query}" in the endpoint instead of query
     */
    abstract val fetchSearchEndpoint: String?

    /**
     *return the end point for the fetch Chapters books feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     */
    abstract val fetchChaptersEndpoint: String?

    /**
     *return the end point for the fetch Content  books feature,
     * if there is not endpoint just return null
     */
    abstract val fetchContentEndpoint: String?

    /****************************************************************************************************/
    /**
     * Returns a page with a list of book. Normally it's not needed to
     * override this method.
     * @param page the page number to retrieve.
     */
    override suspend fun fetchPopular(page: Int): BooksPage {
        val request = client.call(popularRequest(page))
        request.close()
        return popularParse(request, page = page)
    }

    /**
     * Returns a page with a list of latest Book updates.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun fetchLatest(page: Int): BooksPage {
        val request = client.call(latestRequest(page))
        request.close()
        return latestParse(request, page = page)
    }

    /**
     * Returns a book. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun fetchBook(book: Book): Book {
        val request = client.call(detailsRequest(book))
        request.close()
        return detailParse(request)
    }

    /**
     * Returns a list of chapter. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     * @param book the chapters to retrieve.
     */
    override suspend fun fetchChapters(book: Book, page: Int): ChaptersPage {
        val request = client.call(chaptersRequest(book, page))
        request.close()
        return chapterListParse(request)
    }

    /**
     * Returns a ChapterPage. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun fetchContent(chapter: Chapter): ContentPage {
        val request = client.call(contentRequest(chapter))
        request.close()
        return pageContentParse(request)
    }

    /**
     * Returns a BooksPage. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     * @param query the search query to retrieve.
     */
    override suspend fun fetchSearch(page: Int, query: String): BooksPage {
        val request = client.call(searchRequest(page, query))
        request.close()
        return searchBookParse(request, page)
    }


    /**
     * Returns the request for the popular books given the page.
     *
     * @param page the page number to retrieve.
     */
    abstract fun popularRequest(page: Int): Request

    /**
     * Returns the request for latest  Books given the page.
     *
     * @param page the page number to retrieve.
     */
    abstract fun latestRequest(page: Int): Request


    /**
     * Returns the request for the details of a Book. Override only if it's needed to change the
     * url, send different headers or request method like POST.
     *
     * @param book the Book to be updated.
     */
    open fun detailsRequest(book: Book): Request {
        return org.ireader.domain.utils.GET(baseUrl + getUrlWithoutDomain(book.link), headers)
    }

    /**
     * Returns the request for updating the chapter list. Override only if it's needed to override
     * the url, send different headers or request method like POST.
     *
     * @param book the Book to look for chapters.
     */
    open fun chaptersRequest(book: Book): Request {
        return org.ireader.domain.utils.GET(baseUrl + getUrlWithoutDomain(book.link), headers)
    }

    abstract fun chaptersRequest(book: Book, page: Int): Request


    /**
     * Returns the request for getting the page list. Override only if it's needed to override the
     * url, send different headers or request method like POST.
     *
     * @param chapter the chapter whose page list has to be fetched.
     */
    open fun contentRequest(chapter: Chapter): Request {
        return org.ireader.domain.utils.GET(baseUrl + getUrlWithoutDomain(chapter.link), headers)
    }

    /**
     * Returns the request for latest  Books given the page.
     *
     * @param page the page number to retrieve.
     */
    abstract fun searchRequest(page: Int, query: String): Request

    /****************************************************************************************************/


    /**
     * Parses the response from the site and returns a [BooksPage] object.
     *
     * @param response the response from the site.
     */
    private fun popularParse(
        response: Response,
        page: Int,
    ): BooksPage {
        return popularParse(response.asJsoup(), page)
    }

    abstract override fun popularParse(
        document: Document,
        page: Int,
    ): BooksPage


    /**
     * Parses the response from the site and returns a [BooksPage] object.
     *
     * @param response the response from the site.
     */
    fun latestParse(response: Response, page: Int): BooksPage {
        return latestParse(response.asJsoup(), page = page)
    }

    /**
     * Parses the document from the site and returns a [BooksPage] object.
     *
     * @param response the response from the site.
     */
    abstract override fun latestParse(
        document: Document,
        page: Int,
    ): BooksPage


    /**
     * Parses the response from the site and returns the details of a book.
     *
     * @param response the response from the site.
     */
    fun detailParse(response: Response): Book {
        return detailParse(response.asJsoup())
    }

    /**
     * Returns the details of the Book from the given [document].
     *
     * @param document the parsed document.
     */
    abstract override fun detailParse(document: Document): Book


    /**
     * Parses the response from the site and returns a list of pages.
     *
     * @param response the response from the site.
     */
    fun pageContentParse(response: Response): ContentPage {
        return contentFromElementParse(response.asJsoup())
    }

    abstract override fun contentFromElementParse(
        document: Document,
    ): ContentPage


    /**
     * Parses the response from the site and returns a list of chapters.
     *
     * @param response the response from the site.
     */
    abstract override fun chaptersParse(document: Document): ChaptersPage

    fun chapterListParse(response: Response): ChaptersPage {
        return chaptersParse(response.asJsoup())
    }

    /**
     * Parses the response from the site and returns a [BooksPage] object.
     *
     * @param response the response from the site.
     */
    fun searchBookParse(response: Response, page: Int): BooksPage {
        return searchParse(response.asJsoup(), page = page)
    }

    abstract override fun searchParse(
        document: Document,
        page: Int,
    ): BooksPage


    /**
     * Returns the url of the given string without the scheme and domain.
     *
     * @param orig the full url.
     */
    fun getUrlWithoutDomain(orig: String): String {
        return try {
            val uri = URI(orig.replace(" ", "%20"))
            var out = uri.path
            if (uri.query != null) {
                out += "?" + uri.query
            }
            if (uri.fragment != null) {
                out += "#" + uri.fragment
            }
            out
        } catch (e: URISyntaxException) {
            orig
        }
    }


    protected open fun headersBuilder() = Headers.Builder().apply {
        add("User-Agent", DEFAULT_USER_AGENT)
    }

    companion object {
        const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36 Edg/88.0.705.63"
    }
}
