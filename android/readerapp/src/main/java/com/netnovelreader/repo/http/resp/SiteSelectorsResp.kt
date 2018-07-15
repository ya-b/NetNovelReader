package com.netnovelreader.repo.http.resp

import com.netnovelreader.repo.db.SiteSelectorEntity

data class SiteSelectorsResp (var ret: Int, var rules: List<SiteSelectorEntity>)