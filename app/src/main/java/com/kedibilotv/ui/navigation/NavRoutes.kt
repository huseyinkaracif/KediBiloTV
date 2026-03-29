package com.kedibilotv.ui.navigation

object NavRoutes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val CATEGORY = "category/{type}"
    const val CONTENT = "content/{type}/{categoryId}"
    const val DETAIL = "detail/{type}/{streamId}?name={name}&posterUrl={posterUrl}"
    const val PLAYER = "player/{type}/{streamId}?episodeId={episodeId}"
    const val SETTINGS = "settings"

    fun category(type: String) = "category/$type"
    fun content(type: String, categoryId: String) = "content/$type/$categoryId"
    fun detail(type: String, streamId: Int, name: String = "", posterUrl: String = "") =
        "detail/$type/$streamId?name=${java.net.URLEncoder.encode(name, "UTF-8")}&posterUrl=${java.net.URLEncoder.encode(posterUrl, "UTF-8")}"
    fun player(type: String, streamId: Int, episodeId: Int? = null): String {
        val base = "player/$type/$streamId"
        return if (episodeId != null) "$base?episodeId=$episodeId" else base
    }
}
