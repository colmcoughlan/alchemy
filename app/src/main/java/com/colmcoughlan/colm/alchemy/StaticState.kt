package com.colmcoughlan.colm.alchemy

import com.colmcoughlan.colm.alchemy.model.Charity

object StaticState {
    @JvmStatic
    var category = "All"
        private set
    private var charityList: List<Charity> = ArrayList()
    private var lastRefresh: Long = 0
    private const val minRefreshIntervalMillis = (60 * 60 * 1000 // one hour
            ).toLong()

    @JvmStatic
    fun setCurrentCategory(newCategory: String) {
        category = newCategory
    }

    @JvmStatic
    var charities: List<Charity>
        get() = ArrayList(charityList)
        set(newCharities) {
            lastRefresh = System.currentTimeMillis()
            charityList = newCharities
        }

    @JvmStatic
    fun shouldRefreshCharities(): Boolean {
        return (charityList.isEmpty()
                || System.currentTimeMillis() - lastRefresh > minRefreshIntervalMillis)
    }
}